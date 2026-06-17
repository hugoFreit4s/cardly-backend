package com.cardly.service;

import com.cardly.domain.StudyReviewEvent;
import com.cardly.repository.StudyReviewEventRepository;
import com.cardly.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyReviewServiceTest {

	@Mock
	private StudyReviewEventRepository studyReviewEventRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private StudyReviewService studyReviewService;

	@Test
	void listStudyDaysUsesClientTimezoneOffset() {
		StudyReviewEvent event = new StudyReviewEvent();
		event.setCreatedAt(Instant.parse("2026-06-18T01:30:00Z"));
		when(studyReviewEventRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(1))
				.thenReturn(List.of(event));

		List<String> days = studyReviewService.listStudyDays(1, 120, 180);

		assertThat(days).containsExactly("2026-06-17");
	}

	@Test
	void listStudyDaysDefaultsToUtcWhenOffsetMissing() {
		StudyReviewEvent event = new StudyReviewEvent();
		event.setCreatedAt(Instant.parse("2026-06-18T01:30:00Z"));
		when(studyReviewEventRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(1))
				.thenReturn(List.of(event));

		List<String> days = studyReviewService.listStudyDays(1, 120, null);

		assertThat(days).containsExactly("2026-06-18");
	}
}
