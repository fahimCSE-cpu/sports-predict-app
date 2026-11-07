package com.sportspredict.service;

import com.sportspredict.model.AnalyticsEvent;
import com.sportspredict.model.AnalyticsEvent.Type;
import com.sportspredict.model.Match;
import com.sportspredict.repo.AnalyticsEventRepository;
import com.sportspredict.repo.MatchRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {

    public record DayCount(String label, long count, int pct) {}
    public record MatchStat(String title, long views, long checks, long votes) {}

    private final AnalyticsEventRepository events;
    private final MatchRepository matches;

    public AnalyticsService(AnalyticsEventRepository events, MatchRepository matches) {
        this.events = events;
        this.matches = matches;
    }

    public void record(Type type, String path, Long matchId) {
        try {
            events.save(new AnalyticsEvent(type, path, matchId, LocalDateTime.now()));
        } catch (Exception ignored) {
            // analytics must never break a user request
        }
    }

    public long total(Type type) {
        return events.countByType(type);
    }

    /** Page views for each of the last {@code days} days (oldest first). */
    public List<DayCount> viewsPerDay(int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);
        Map<LocalDate, Long> perDay = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) perDay.put(start.plusDays(i), 0L);
        for (AnalyticsEvent e : events.findByTypeAndCreatedAtAfter(Type.PAGE_VIEW, start.atStartOfDay())) {
            perDay.merge(e.getCreatedAt().toLocalDate(), 1L, Long::sum);
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEE d");
        List<DayCount> out = new ArrayList<>();
        long max = Math.max(1L, perDay.values().stream().mapToLong(Long::longValue).max().orElse(1L));
        perDay.forEach((d, c) -> out.add(new DayCount(d.format(f), c, (int) (c * 100 / max))));
        return out;
    }

    public List<MatchStat> perMatch() {
        Map<Long, Long> views = toMap(events.countByMatchForType(Type.PAGE_VIEW));
        Map<Long, Long> checks = toMap(events.countByMatchForType(Type.PREDICTION_CHECK));
        Map<Long, Long> votes = toMap(events.countByMatchForType(Type.VOTE));
        List<MatchStat> out = new ArrayList<>();
        for (Match m : matches.findAllByOrderByMatchDateAsc()) {
            out.add(new MatchStat(m.getTitle(),
                    views.getOrDefault(m.getId(), 0L),
                    checks.getOrDefault(m.getId(), 0L),
                    votes.getOrDefault(m.getId(), 0L)));
        }
        return out;
    }

    private static Map<Long, Long> toMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) map.put((Long) r[0], (Long) r[1]);
        return map;
    }
}
