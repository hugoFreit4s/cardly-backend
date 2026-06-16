package com.cardly.repository;

import com.cardly.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

	List<Notification> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId);

	long countByUser_IdAndDeletedAtIsNullAndReadAtIsNull(Integer userId);

	Optional<Notification> findByIdAndUser_IdAndDeletedAtIsNull(Integer id, Integer userId);

	List<Notification> findByIdInAndUser_IdAndDeletedAtIsNull(Collection<Integer> ids, Integer userId);

	boolean existsByUser_IdAndReferenceKeyAndDeletedAtIsNull(Integer userId, String referenceKey);

	@Modifying
	@Query("""
			UPDATE Notification n
			SET n.readAt = :readAt, n.updatedAt = :readAt
			WHERE n.user.id = :userId AND n.deletedAt IS NULL AND n.readAt IS NULL
			""")
	int markAllReadForUser(@Param("userId") Integer userId, @Param("readAt") Instant readAt);

	@Modifying
	@Query("""
			UPDATE Notification n
			SET n.readAt = :readAt, n.updatedAt = :readAt
			WHERE n.user.id = :userId AND n.id IN :ids AND n.deletedAt IS NULL AND n.readAt IS NULL
			""")
	int markReadByIds(@Param("userId") Integer userId, @Param("ids") Collection<Integer> ids, @Param("readAt") Instant readAt);

	@Modifying
	@Query("""
			UPDATE Notification n
			SET n.deletedAt = :deletedAt, n.updatedAt = :deletedAt
			WHERE n.user.id = :userId AND n.id IN :ids AND n.deletedAt IS NULL
			""")
	int softDeleteByIds(@Param("userId") Integer userId, @Param("ids") Collection<Integer> ids, @Param("deletedAt") Instant deletedAt);
}
