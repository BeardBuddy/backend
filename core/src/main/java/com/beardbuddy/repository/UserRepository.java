package com.beardbuddy.repository;

import com.beardbuddy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * Stamps the barber-import time only if the user is out of cooldown. One conditional UPDATE
     * is atomic, so two parallel clicks (or two API replicas) cannot both get through.
     *
     * @return 1 when the import may go ahead, 0 when the user is still cooling down
     */
    @Modifying
    @Query(value = """
            UPDATE app_user
               SET last_barber_import_at = :now
             WHERE id = :userId
               AND (last_barber_import_at IS NULL OR last_barber_import_at <= :cutoff)
            """, nativeQuery = true)
    int claimBarberImport(@Param("userId") String userId, @Param("now") Instant now, @Param("cutoff") Instant cutoff);

    @Query(value = "SELECT last_barber_import_at FROM app_user WHERE id = :userId", nativeQuery = true)
    Optional<Instant> findLastBarberImportAt(@Param("userId") String userId);
}
