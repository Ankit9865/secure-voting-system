package com.votingsystem.repository;

import com.votingsystem.entity.AppUser;
import com.votingsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    /**
     * Atomic "claim the vote": only one concurrent request can flip has_voted from false to true.
     * Returns 1 if this call claimed it, 0 if the user had already voted.
     */
    @Modifying
    @Query("update AppUser u set u.hasVoted = true where u.id = :id and u.hasVoted = false")
    int markVoted(@Param("id") Long id);
}
