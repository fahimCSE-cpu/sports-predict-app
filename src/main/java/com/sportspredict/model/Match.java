package com.sportspredict.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** An upcoming match between two teams, published by the admin. */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sport / format is required")
    private String sport;

    @NotBlank(message = "Team A name is required")
    private String teamA;

    @NotBlank(message = "Team B name is required")
    private String teamB;

    @NotNull(message = "Match date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate matchDate;

    private String venue;

    @Min(0) private int teamAPlayed;
    @Min(0) private int teamAWins;
    @Min(0) private int teamBPlayed;
    @Min(0) private int teamBWins;

    @Min(0) private int supportersA;
    @Min(0) private int supportersB;

    @Column(length = 2000)
    private String details;

    private boolean published;

    private LocalDateTime createdAt = LocalDateTime.now();

    @AssertTrue(message = "Wins cannot be more than matches played")
    public boolean isRecordsValid() {
        return teamAWins <= teamAPlayed && teamBWins <= teamBPlayed;
    }

    public String getTitle() {
        return teamA + " vs " + teamB;
    }
}
