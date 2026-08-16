package com.example.catchcompass.catchlog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatchPhotoRepository extends JpaRepository<CatchPhoto, Long> {

    /**
     * The table allows many photos per catch so that Phase 6 needs no migration,
     * but the current UI attaches and shows only the first.
     */
    Optional<CatchPhoto> findFirstByCatchRecordIdOrderByIdAsc(Long catchId);
}
