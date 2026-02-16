package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.watchparty.WatchPartyRoomDTO;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.WatchPartyRoomEntity;
import com.example.jutjubic.infrastructure.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.repository.JpaWatchPartyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/watch-party")
@RequiredArgsConstructor
@CrossOrigin
public class WatchPartyController {

    private final JpaWatchPartyRoomRepository watchPartyRoomRepository;
    private final JpaUserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Optional<UserEntity> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Korisnik nije pronadjen");
        }

        UserEntity host = userOpt.get();

        // Zatvori prethodnu aktivnu sobu ako postoji
        Optional<WatchPartyRoomEntity> existingRoom = watchPartyRoomRepository.findByHostAndActive(host, true);
        existingRoom.ifPresent(room -> {
            room.setActive(false);
            watchPartyRoomRepository.save(room);
        });

        // Kreiraj novu sobu sa 8-karakternim kodom
        WatchPartyRoomEntity room = new WatchPartyRoomEntity();
        room.setRoomCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        room.setHost(host);
        room.setActive(true);
        watchPartyRoomRepository.save(room);

        WatchPartyRoomDTO dto = new WatchPartyRoomDTO(
                room.getRoomCode(),
                host.getUsername(),
                host.getEmail(),
                room.isActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getRoom(@PathVariable String roomCode) {
        Optional<WatchPartyRoomEntity> roomOpt = watchPartyRoomRepository.findByRoomCode(roomCode);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Soba nije pronadjena");
        }

        WatchPartyRoomEntity room = roomOpt.get();
        WatchPartyRoomDTO dto = new WatchPartyRoomDTO(
                room.getRoomCode(),
                room.getHost().getUsername(),
                room.getHost().getEmail(),
                room.isActive()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{roomCode}/close")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> closeRoom(
            @PathVariable String roomCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<WatchPartyRoomEntity> roomOpt = watchPartyRoomRepository.findByRoomCode(roomCode);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Soba nije pronadjena");
        }

        WatchPartyRoomEntity room = roomOpt.get();
        String userEmail = userDetails.getUsername();

        // Samo host moze da zatvori sobu
        if (!room.getHost().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Samo host moze da zatvori sobu");
        }

        room.setActive(false);
        watchPartyRoomRepository.save(room);

        return ResponseEntity.ok("Soba je zatvorena");
    }
}
