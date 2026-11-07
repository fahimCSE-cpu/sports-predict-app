package com.sportspredict.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** A question a visitor sends to the admin after viewing match details. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId;
    private String matchTitle;

    @NotBlank @Size(max = 80)
    private String name;

    @NotBlank @Email @Size(max = 120)
    private String email;

    @NotBlank @Size(max = 1000)
    @Column(length = 1000)
    private String message;

    @Column(length = 1000)
    private String reply;

    private boolean resolved;

    private LocalDateTime createdAt = LocalDateTime.now();
}
