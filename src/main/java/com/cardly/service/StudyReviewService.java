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

	private static final int MIN_TIMEZONE_OFFSET_MINUTES = -14 * 60;
	private static final int MAX_TIMEZONE_OFFSET_MINUTES = 14 * 60;

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
	public List<String> listStudyDays(Integer userId, int limit, Integer timezoneOffsetMinutes) {
		int safeLimit = Math.max(1, Math.min(limit, 365));
		ZoneOffset zoneOffset = resolveZoneOffset(timezoneOffsetMinutes);
		LinkedHashSet<String> uniqueDays = new LinkedHashSet<>();
		List<StudyReviewEvent> events = studyReviewEventRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
		for (StudyReviewEvent event : events) {
			String day = event.getCreatedAt().atOffset(zoneOffset).toLocalDate().toString();
			uniqueDays.add(day);
			if (uniqueDays.size() >= safeLimit) {
				break;
			}
		}
		return List.copyOf(uniqueDays);
	}

	private ZoneOffset resolveZoneOffset(Integer timezoneOffsetMinutes) {
		int safeOffsetMinutes = timezoneOffsetMinutes == null
				? 0
				: Math.max(MIN_TIMEZONE_OFFSET_MINUTES, Math.min(MAX_TIMEZONE_OFFSET_MINUTES, timezoneOffsetMinutes));
		// JS getTimezoneOffset uses opposite sign from UTC offsets: UTC-3 => +180.
		return ZoneOffset.ofTotalSeconds(-safeOffsetMinutes * 60);
	}
}
