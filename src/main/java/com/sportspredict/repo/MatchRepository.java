package com.sportspredict.repo;

import com.sportspredict.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPublishedTrueOrderByMatchDateAsc();
    List<Match> findAllByOrderByMatchDateAsc();

    @Modifying
    @Query("update Match m set m.supportersA = m.supportersA + 1 where m.id = :id")
    int addSupportA(@Param("id") Long id);

    @Modifying
    @Query("update Match m set m.supportersB = m.supportersB + 1 where m.id = :id")
    int addSupportB(@Param("id") Long id);
}
