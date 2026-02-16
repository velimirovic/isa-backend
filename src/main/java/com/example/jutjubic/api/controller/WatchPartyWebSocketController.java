package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.watchparty.WatchPartyPlayMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WatchPartyWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/watch-party/{roomCode}/play")
    public void playVideo(@DestinationVariable String roomCode, WatchPartyPlayMessage message) {
        messagingTemplate.convertAndSend("/topic/watch-party/" + roomCode, message);
    }
}
