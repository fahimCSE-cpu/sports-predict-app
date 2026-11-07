package com.sportspredict.repo;

import com.sportspredict.model.Tip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipRepository extends JpaRepository<Tip, Long> {
    List<Tip> findByActiveTrueOrderByIdAsc();
}
