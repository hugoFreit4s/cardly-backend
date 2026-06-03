package com.cardly.repository;

import com.cardly.domain.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Integer>, JpaSpecificationExecutor<Deck> {

	List<Deck> findByUser_IdAndDeletedAtIsNull(Integer userId);

	List<Deck> findByUser_IdAndDeletedAtIsNullOrderByPositionAscIdAsc(Integer userId);

	Optional<Deck> findByIdAndUser_IdAndDeletedAtIsNull(Integer id, Integer userId);

	Optional<Deck> findByIdAndDeletedAtIsNullAndIsPublicTrue(Integer id);

	Optional<Deck> findByUser_IdAndSourceDeck_IdAndDeletedAtIsNull(Integer userId, Integer sourceDeckId);

	long countByUser_IdAndDeletedAtIsNull(Integer userId);

	@Query("SELECT COALESCE(MAX(d.position), 0) FROM Deck d WHERE d.user.id = :userId AND d.deletedAt IS NULL")
	Integer findMaxPositionByUserId(@Param("userId") Integer userId);
}
