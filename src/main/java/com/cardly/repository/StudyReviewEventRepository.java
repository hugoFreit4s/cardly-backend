package com.cardly.repository;

import com.cardly.domain.StudyReviewEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StudyReviewEventRepository extends JpaRepository<StudyReviewEvent, Integer> {

	List<StudyReviewEvent> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId);

	long countByUser_IdAndDeletedAtIsNullAndCreatedAtGreaterThanEqual(Integer userId, Instant createdAt);
}
