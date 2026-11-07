package com.sportspredict.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One tracked user action, used by the admin analytics panel. */
@Entity
@Table(indexes = {@Index(columnList = "type"), @Index(columnList = "createdAt")})
@Getter
@Setter
@NoArgsConstructor
public class AnalyticsEvent {

    public enum Type { PAGE_VIEW, PREDICTION_CHECK, VOTE, INQUIRY, SUBSCRIBE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String path;
    private Long matchId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public AnalyticsEvent(Type type, String path, Long matchId, LocalDateTime createdAt) {
        this.type = type;
        this.path = path;
        this.matchId = matchId;
        this.createdAt = createdAt;
    }
}
