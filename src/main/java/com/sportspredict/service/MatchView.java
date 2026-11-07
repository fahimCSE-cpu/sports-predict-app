package com.sportspredict.service;

import com.sportspredict.model.Match;

/** A match plus its computed prediction, for templates. */
public record MatchView(Match match, Prediction prediction) {}
