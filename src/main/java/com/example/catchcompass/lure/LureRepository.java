package com.example.catchcompass.lure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LureRepository extends JpaRepository<Lure, Long> {

    List<Lure> findByUserIdAndActiveTrueOrderByLureTypeAscBrandAsc(Long userId);

    /**
     * Both arguments matter. Looking up by id alone would let one user attach
     * another user's lure to their catch by guessing a number.
     */
    Optional<Lure> findByIdAndUserId(Long id, Long userId);
}
