package com.sportspredict;

import com.sportspredict.model.Match;
import com.sportspredict.service.Prediction;
import com.sportspredict.service.PredictionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PredictionServiceTest {

    private final PredictionService service = new PredictionService(4, 3);

    private Match match(int aPlayed, int aWins, int bPlayed, int bWins, int supA, int supB) {
        Match m = new Match();
        m.setTeamA("Australia"); m.setTeamB("India");
        m.setTeamAPlayed(aPlayed); m.setTeamAWins(aWins);
        m.setTeamBPlayed(bPlayed); m.setTeamBWins(bWins);
        m.setSupportersA(supA); m.setSupportersB(supB);
        return m;
    }

    @Test
    void reproducesBriefExample() {
        Prediction p = service.predict(match(6, 4, 6, 3, 45, 55));
        assertEquals(0.0073, p.likeA(), 0.0001);
        assertEquals(0.0078, p.likeB(), 0.0001);
        assertEquals(0.4337, p.probA(), 0.001);
        assertEquals(0.5663, p.probB(), 0.001);
        assertEquals("India", p.favoured());
    }

    @Test
    void probabilitiesSumToOne() {
        Prediction p = service.predict(match(10, 7, 12, 5, 30, 70));
        assertEquals(1.0, p.probA() + p.probB(), 1e-9);
    }

    @Test
    void noHistoryNoSupportIsFiftyFifty() {
        Prediction p = service.predict(match(0, 0, 0, 0, 0, 0));
        assertEquals(0.5, p.probA(), 1e-9);
        assertEquals("Too close to call", p.favoured());
    }

    @Test
    void perfectRecordFallsBackToSupportShare() {
        Prediction p = service.predict(match(5, 5, 5, 0, 60, 40));
        assertEquals(0.6, p.probA(), 1e-9);
    }
}
