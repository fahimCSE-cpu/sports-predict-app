package com.sportspredict.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** "Great Offers or Tips to Explore" shown on the home page. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tip {

    public enum Kind { TIP, OFFER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    @Column(length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    private Kind kind = Kind.TIP;

    private boolean active = true;

    public Tip(String title, String body, Kind kind) {
        this.title = title;
        this.body = body;
        this.kind = kind;
    }
}
