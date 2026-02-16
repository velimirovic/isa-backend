package com.example.jutjubic.api.dto.watchparty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchPartyRoomDTO {

    private String roomCode;
    private String hostUsername;
    private String hostEmail;
    private boolean active;
}
