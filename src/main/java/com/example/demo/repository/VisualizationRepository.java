package com.example.demo.repository;

import com.example.demo.model.User;
import com.example.demo.model.Visualization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VisualizationRepository extends MongoRepository<Visualization, String> {
    Page<Visualization> findAllByUser(User user, Pageable pageable);

    Optional<Visualization> findByUserAndId(User user, String id);

    void deleteByUserAndId(User user, String id);

    List<Visualization> findAllByUserAndShowOnDashboard(User user, boolean showOnDashboard);
}
