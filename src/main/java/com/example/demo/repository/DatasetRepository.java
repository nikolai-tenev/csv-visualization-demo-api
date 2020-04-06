package com.example.demo.repository;

import com.example.demo.model.Dataset;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DatasetRepository extends MongoRepository<Dataset, String> {
    Page<Dataset> findAllByUser(User user, Pageable pageable);

    Optional<Dataset> findByUserAndId(User user, String id);

    void deleteByUserAndId(User user, String id);
}
