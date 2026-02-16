package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.WatchPartyRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaWatchPartyRoomRepository extends JpaRepository<WatchPartyRoomEntity, Long> {

    Optional<WatchPartyRoomEntity> findByRoomCode(String roomCode);

    Optional<WatchPartyRoomEntity> findByHostAndActive(UserEntity host, boolean active);
}
