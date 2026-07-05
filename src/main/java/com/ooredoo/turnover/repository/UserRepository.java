// UserRepository.java
package com.ooredoo.turnover.repository;

import com.ooredoo.turnover.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}