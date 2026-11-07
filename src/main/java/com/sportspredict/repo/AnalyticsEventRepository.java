package com.sportspredict.repo;

import com.sportspredict.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    long countByType(AnalyticsEvent.Type type);

    List<AnalyticsEvent> findByTypeAndCreatedAtAfter(AnalyticsEvent.Type type, LocalDateTime after);

    @Query("select e.matchId, count(e) from AnalyticsEvent e " +
           "where e.type = :type and e.matchId is not null group by e.matchId")
    List<Object[]> countByMatchForType(@Param("type") AnalyticsEvent.Type type);
}
