package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.ReviewResultENUM;
import com.cardly.domain.StudyReviewEvent;
import com.cardly.domain.User;
import com.cardly.repository.StudyReviewEventRepository;
import com.cardly.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class StudyReviewService {

	private final StudyReviewEventRepository studyReviewEventRepository;
	private final UserRepository userRepository;

	public StudyReviewService(StudyReviewEventRepository studyReviewEventRepository, UserRepository userRepository) {
		this.studyReviewEventRepository = studyReviewEventRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void registerReview(Card card, ReviewResultENUM result) {
		StudyReviewEvent event = new StudyReviewEvent();
		User owner = userRepository.getReferenceById(card.getDeck().getUser().getId());
		event.setUser(owner);
		event.setDeck(card.getDeck());
		event.setCard(card);
		event.setResult(result);
		studyReviewEventRepository.save(event);
	}

	@Transactional(readOnly = true)
	public List<String> listStudyDays(Integer userId, int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 365));
		LinkedHashSet<String> uniqueDays = new LinkedHashSet<>();
		List<StudyReviewEvent> events = studyReviewEventRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
		for (StudyReviewEvent event : events) {
			String day = event.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().toString();
			uniqueDays.add(day);
			if (uniqueDays.size() >= safeLimit) {
				break;
			}
		}
		return List.copyOf(uniqueDays);
	}
}
