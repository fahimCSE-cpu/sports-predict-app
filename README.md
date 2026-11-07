# Sports Predictive Selection System

Spring Boot 3 + Thymeleaf web app. Shows upcoming two-team matches and the winning chance of each
team, calculated with a simple Bayes rule (past form x fan support).

## Run
Requires JDK 17+ and Maven (internet access to Maven Central).

    mvn spring-boot:run          # then open http://localhost:8080
    mvn test                     # unit + web smoke tests

Admin login: `admin` / `admin123` (change in `application.properties`).
Data is stored in `./data/sportsdb` (H2 file). Delete the folder to reset; sample matches are re-seeded.

## Features (from the project brief)
1. **Welcome frame** pops up shortly after load with usage tips (`fragments.html`, `app.js`).
2. **Top nav**: logo left; Login, Admin, FAQ right.
3. **Home**: horizontal banner per match (Team A VS Team B), "Check which team succeeds?" reveals the chance, Support buttons.
4. **Admin panel** (`/admin`): create/edit/hide/publish matches, which appear on the front page.
5. **Analytics** in admin: page views per day, prediction checks, votes, per-match engagement.
6. **Simple mail**: notify subscribers on publish, send updates, reply to inquiries. Off by default (logged only);
   enable with `app.mail.enabled=true` + `spring.mail.*`.
Extras from the use-case diagram: match detail page with step-by-step reasoning, "ask the admin" inquiry form,
tips/offers section (admin-managed), FAQ page.

## Prediction formula
    share = supporters / total supporters
    rate  = wins / played
    like  = rate^4 * (1 - rate)^3          (exponents configurable: app.prediction.*)
    prob  = share*like / (shareA*likeA + shareB*likeB)
Australia 4/6 + 45 fans vs India 3/6 + 55 fans -> India 56.62% (the brief shows 56.63% because it rounds intermediate values).
