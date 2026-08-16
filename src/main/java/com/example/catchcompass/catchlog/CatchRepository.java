package com.example.catchcompass.catchlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CatchRepository extends JpaRepository<Catch, Long> {

    @Query("""
            select c from Catch c
            join fetch c.species
            where c.userId = :userId
            order by c.caughtAt desc
            """)
    List<Catch> findJournal(@Param("userId") Long userId);

    @Query("""
            select c from Catch c
            join fetch c.species
            where c.id = :id and c.userId = :userId
            """)
    Optional<Catch> findOwned(@Param("id") Long id, @Param("userId") Long userId);
}
