package com.gym.service.repository;

import com.gym.service.entity.PersonalTrainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalTrainerRepository extends JpaRepository<PersonalTrainer, Long> {
    Optional<PersonalTrainer> findByUserId(Long userId);
    List<PersonalTrainer> findBySpecialty(String specialty);
}
