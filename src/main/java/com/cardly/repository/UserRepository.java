package com.cardly.repository;

import com.cardly.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

	Optional<User> findByEmail(String email);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByEmailAndDeletedAtIsNull(String email);

	Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

	Optional<User> findByIdAndDeletedAtIsNull(Integer id);

	Optional<User> findByPublicIdAndDeletedAtIsNull(Integer publicId);
}
