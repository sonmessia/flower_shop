package vn.quahoa.flowershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;
import vn.quahoa.flowershop.model.ChatMessage;
import vn.quahoa.flowershop.service.ChatService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    // REST endpoint to get chat history
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getChatHistory(roomId));
    }

    // REST endpoint to get all active rooms for Admin Dashboard
    @GetMapping("/rooms")
    public ResponseEntity<?> getActiveRooms() {
        return ResponseEntity.ok(chatService.getActiveRooms());
    }

    // WebSocket endpoint to receive a message and broadcast it
    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        chatMessage.setRoomId(roomId);
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        // Save to DB
        ChatMessage savedMessage = chatService.saveMessage(chatMessage);

        log.info("Message received for room {}: {}", roomId, chatMessage.getContent());

        // Broadcast to the specific room
        messagingTemplate.convertAndSend("/topic/chat." + roomId, savedMessage);

        // Broadcast notification to ALL admins/users who subscribe to /topic/notifications
        // They will use it to show a badge/toast if they are NOT currently in the matching room
        messagingTemplate.convertAndSend("/topic/notifications", savedMessage);
    }

    // WebSocket endpoint to handle user joining a room
    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, @NonNull SimpMessageHeaderAccessor headerAccessor) {
        // Add roomId and username to websocket session
        var attributes = headerAccessor.getSessionAttributes();
        if (attributes != null) {
            attributes.put("roomId", roomId);
            attributes.put("username", chatMessage.getSender());
        }

        chatMessage.setRoomId(roomId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessage.MessageType.JOIN);

        log.info("User {} joined room {}", chatMessage.getSender(), roomId);

        messagingTemplate.convertAndSend("/topic/chat." + roomId, chatMessage);
    }
}
