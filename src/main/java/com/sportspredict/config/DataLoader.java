package com.sportspredict.config;

import com.sportspredict.model.AnalyticsEvent;
import com.sportspredict.model.AnalyticsEvent.Type;
import com.sportspredict.model.Match;
import com.sportspredict.model.Subscriber;
import com.sportspredict.model.Tip;
import com.sportspredict.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Seeds sample data on the first run so the app is not empty. */
@Component
public class DataLoader implements CommandLineRunner {

    private final MatchRepository matches;
    private final TipRepository tips;
    private final SubscriberRepository subscribers;
    private final AnalyticsEventRepository events;
    private final boolean seed;

    public DataLoader(MatchRepository matches, TipRepository tips, SubscriberRepository subscribers,
                      AnalyticsEventRepository events, @Value("${app.seed.enabled:true}") boolean seed) {
        this.matches = matches;
        this.tips = tips;
        this.subscribers = subscribers;
        this.events = events;
        this.seed = seed;
    }

    @Override
    public void run(String... args) {
        if (!seed || matches.count() > 0) return;

        // The worked example from the project brief: Australia (4/6, 45 supporters) vs India (3/6, 55).
        Match m1 = match("T20 Cricket", "Australia", "India", LocalDate.now().plusMonths(1),
                "Melbourne Cricket Ground", 6, 4, 6, 3, 45, 55,
                "Australia has won 4 of its last 6 matches and India 3 of 6. Of 100 supporters polled, "
                + "45 back Australia and 55 back India.");
        Match m2 = match("Football", "Argentina", "Brazil", LocalDate.now().plusDays(20),
                "Estadio Monumental", 10, 6, 10, 7, 120, 95,
                "A classic South American derby. Both teams are in strong form this season.");
        Match m3 = match("ODI Cricket", "Bangladesh", "Sri Lanka", LocalDate.now().plusDays(12),
                "Sher-e-Bangla National Stadium", 8, 5, 8, 4, 210, 130,
                "Bangladesh play at home with a strong run of recent wins.");
        matches.save(m1);
        matches.save(m2);
        matches.save(m3);

        tips.save(new Tip("How the prediction works",
                "We blend each team's past win record with what fans think (supporter share) using a simple Bayes rule.",
                Tip.Kind.TIP));
        tips.save(new Tip("Cast your support", "Tap 'Support' on a banner. One vote per match per visit - it updates the odds.",
                Tip.Kind.TIP));
        tips.save(new Tip("Match-day alerts", "Subscribe with your email at the bottom of the page and get new match details first.",
                Tip.Kind.OFFER));

        subscribers.save(new Subscriber("demo.fan@example.com"));

        // Demo analytics so the admin charts are not empty on day one.
        Random rnd = new Random(42);
        List<AnalyticsEvent> evs = new ArrayList<>();
        Long[] ids = {m1.getId(), m2.getId(), m3.getId()};
        for (int d = 6; d >= 0; d--) {
            int views = 8 + rnd.nextInt(18);
            for (int i = 0; i < views; i++) {
                LocalDateTime t = LocalDateTime.now().minusDays(d).withHour(8 + rnd.nextInt(12)).withMinute(rnd.nextInt(60));
                Long id = ids[rnd.nextInt(3)];
                evs.add(new AnalyticsEvent(Type.PAGE_VIEW, "/match/" + id, id, t));
                if (rnd.nextInt(3) == 0) evs.add(new AnalyticsEvent(Type.PREDICTION_CHECK, "/", id, t));
                if (rnd.nextInt(5) == 0) evs.add(new AnalyticsEvent(Type.VOTE, "/match/" + id, id, t));
            }
        }
        events.saveAll(evs);
    }

    private Match match(String sport, String a, String b, LocalDate date, String venue,
                        int aPlayed, int aWins, int bPlayed, int bWins, int supA, int supB, String details) {
        Match m = new Match();
        m.setSport(sport);
        m.setTeamA(a);
        m.setTeamB(b);
        m.setMatchDate(date);
        m.setVenue(venue);
        m.setTeamAPlayed(aPlayed);
        m.setTeamAWins(aWins);
        m.setTeamBPlayed(bPlayed);
        m.setTeamBWins(bWins);
        m.setSupportersA(supA);
        m.setSupportersB(supB);
        m.setDetails(details);
        m.setPublished(true);
        return m;
    }
}
