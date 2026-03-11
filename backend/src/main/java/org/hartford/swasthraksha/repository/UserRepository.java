package org.hartford.swasthraksha.repository;

import org.hartford.swasthraksha.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);

    Users findByEmail(String email);

    List<Users> findByRoleContaining(String role);
}
