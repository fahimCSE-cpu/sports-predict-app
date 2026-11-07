package com.sportspredict.service;

import com.sportspredict.model.Match;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Simple Bayes rule used to estimate each team's chance of winning the next match. */
@Service
public class PredictionService {

    private final int winExp;
    private final int lossExp;

    public PredictionService(@Value("${app.prediction.win-exponent:4}") int winExp,
                             @Value("${app.prediction.loss-exponent:3}") int lossExp) {
        this.winExp = winExp;
        this.lossExp = lossExp;
    }

    public Prediction predict(Match m) {
        long totalSupport = (long) m.getSupportersA() + m.getSupportersB();
        double shareA = totalSupport == 0 ? 0.5 : (double) m.getSupportersA() / totalSupport;
        double shareB = totalSupport == 0 ? 0.5 : (double) m.getSupportersB() / totalSupport;

        double rateA = rate(m.getTeamAWins(), m.getTeamAPlayed());
        double rateB = rate(m.getTeamBWins(), m.getTeamBPlayed());

        double likeA = likelihood(rateA);
        double likeB = likelihood(rateB);

        double jointA = shareA * likeA;
        double jointB = shareB * likeB;
        double total = jointA + jointB;

        double probA;
        double probB;
        if (total == 0) {
            // Degenerate case (e.g. a team with a 0% or 100% record): fall back to support share.
            probA = shareA;
            probB = shareB;
        } else {
            probA = jointA / total;
            probB = jointB / total;
        }
        return new Prediction(m.getTeamA(), m.getTeamB(), shareA, shareB, rateA, rateB,
                likeA, likeB, jointA, jointB, total, probA, probB, winExp, lossExp);
    }

    private static double rate(int wins, int played) {
        return played <= 0 ? 0.5 : (double) wins / played;
    }

    private double likelihood(double p) {
        return Math.pow(p, winExp) * Math.pow(1 - p, lossExp);
    }
}
