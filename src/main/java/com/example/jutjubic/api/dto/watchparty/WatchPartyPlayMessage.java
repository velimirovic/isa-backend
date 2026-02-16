package com.example.jutjubic.api.dto.watchparty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchPartyPlayMessage {

    private String videoDraftId;
    private String videoTitle;
    private String hostUsername;
}
