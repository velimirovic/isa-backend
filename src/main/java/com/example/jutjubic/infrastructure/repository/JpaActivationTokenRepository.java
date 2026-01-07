package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.ActivationTokenEntity;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaActivationTokenRepository extends JpaRepository<ActivationTokenEntity, Long> {

    Optional<ActivationTokenEntity> findByToken(String token);
    Optional<ActivationTokenEntity> findByUser(UserEntity user);
    void deleteByUser(UserEntity user);
}