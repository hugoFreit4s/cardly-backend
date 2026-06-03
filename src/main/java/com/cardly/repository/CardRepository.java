package com.cardly.repository;

import com.cardly.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Integer>, JpaSpecificationExecutor<Card> {

	List<Card> findByDeck_IdAndDeletedAtIsNull(Integer deckId);

	List<Card> findByDeck_IdAndDeletedAtIsNullOrderByIdAsc(Integer deckId);

	Optional<Card> findByIdAndDeck_IdAndDeletedAtIsNull(Integer cardId, Integer deckId);

	Optional<Card> findByIdAndDeletedAtIsNull(Integer cardId);

	int countByDeck_IdAndDeletedAtIsNull(Integer deckId);

	long countByDeck_User_IdAndDeletedAtIsNull(Integer userId);

	long countByDeck_User_IdAndDeletedAtIsNullAndDueAtLessThanEqual(Integer userId, Instant now);

	long countByDeck_User_IdAndDeletedAtIsNullAndDueAtGreaterThan(Integer userId, Instant now);

	long countByDeck_User_IdAndDeletedAtIsNullAndDueAtIsNull(Integer userId);

	long countByDeck_User_IdAndDeletedAtIsNullAndUpdatedAtGreaterThanEqualAndScheduledIntervalIsNotNull(Integer userId, Instant updatedAt);

	int countByDeck_IdAndDeletedAtIsNullAndDueAtIsNotNull(Integer deckId);

	int countByDeck_IdAndDeletedAtIsNullAndDueAtGreaterThan(Integer deckId, Instant now);

	int countByDeck_IdAndDeletedAtIsNullAndDueAtIsNotNullAndDueAtLessThanEqual(Integer deckId, Instant now);

	@Query("""
			SELECT DISTINCT c.deck.user.id
			FROM Card c
			WHERE c.deletedAt IS NULL
			  AND c.dueAt IS NOT NULL
			  AND c.dueAt <= :now
			""")
	List<Integer> findUserIdsWithDueCardsOnly(@Param("now") Instant now);
}
