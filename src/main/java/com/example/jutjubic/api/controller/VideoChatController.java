package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.videopost.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VideoChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/video/{videoId}/chat")
    public void sendChatMessage(@DestinationVariable String videoId, ChatMessage message) {
        message.setVideoId(videoId);
        messagingTemplate.convertAndSend("/topic/video/" + videoId + "/chat", message);
    }

    @MessageMapping("/video/{videoId}/join")
    public void userJoined(@DestinationVariable String videoId, ChatMessage joinMessage) {
        joinMessage.setMessage(joinMessage.getUsername() + " se pridružio/la chat-u");
        messagingTemplate.convertAndSend("/topic/video/" + videoId + "/chat", joinMessage);
    }

    @MessageMapping("/video/{videoId}/leave")
    public void userLeft(@DestinationVariable String videoId, ChatMessage leaveMessage) {
        leaveMessage.setMessage(leaveMessage.getUsername() + " je napustio/la chat");
        messagingTemplate.convertAndSend("/topic/video/" + videoId + "/chat", leaveMessage);
    }
}
