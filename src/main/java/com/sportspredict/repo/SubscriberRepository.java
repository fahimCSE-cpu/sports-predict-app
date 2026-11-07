package com.sportspredict.repo;

import com.sportspredict.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
