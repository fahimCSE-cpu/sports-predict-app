package com.sportspredict.service;

/**
 * Full working of the Bayes-style calculation so the UI can show the reasoning.
 *
 * share  = supporters of team / total supporters           (prior)
 * rate   = wins / matches played                           (past form)
 * like   = rate^winExp * (1 - rate)^lossExp                (win-or-loss likelihood)
 * joint  = share * like
 * prob   = joint / (jointA + jointB)                       (posterior)
 */
public record Prediction(
        String teamA, String teamB,
        double shareA, double shareB,
        double rateA, double rateB,
        double likeA, double likeB,
        double jointA, double jointB,
        double total,
        double probA, double probB,
        int winExp, int lossExp) {

    public String favoured() {
        if (Math.abs(probA - probB) < 1e-9) return "Too close to call";
        return probA > probB ? teamA : teamB;
    }

    public double favouredPercent() {
        return Math.max(probA, probB) * 100.0;
    }

    public double pctA() { return probA * 100.0; }
    public double pctB() { return probB * 100.0; }
}
