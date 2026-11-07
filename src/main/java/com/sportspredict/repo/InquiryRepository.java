package com.sportspredict.repo;

import com.sportspredict.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    List<Inquiry> findTop5ByResolvedFalseOrderByCreatedAtDesc();
    long countByResolvedFalse();
}
