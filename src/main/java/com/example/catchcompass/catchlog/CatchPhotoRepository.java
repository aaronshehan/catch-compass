package com.example.catchcompass.catchlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CatchPhotoRepository extends JpaRepository<CatchPhoto, Long> {

    /**
     * The table allows many photos per catch so that Phase 6 needs no migration,
     * but the current UI attaches and shows only the first.
     */
    Optional<CatchPhoto> findFirstByCatchRecordIdOrderByIdAsc(Long catchId);

    /**
     * Which of these catches have a photo, in one query.
     *
     * <p>Asking per catch while rendering the journal would be a textbook N+1:
     * fifty catches, fifty extra queries, all to decide whether to draw a
     * thumbnail.
     */
    @Query("select p.catchRecord.id from CatchPhoto p where p.catchRecord.id in :catchIds")
    List<Long> findCatchIdsWithPhotos(@Param("catchIds") Collection<Long> catchIds);
}
