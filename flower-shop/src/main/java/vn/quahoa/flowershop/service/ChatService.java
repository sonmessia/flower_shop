package vn.quahoa.flowershop.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.quahoa.flowershop.entity.ChatMessageEntity;
import vn.quahoa.flowershop.model.ChatMessage;
import vn.quahoa.flowershop.repository.ChatMessageRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        log.info("Saving chat message for room: {}", chatMessage.getRoomId());
        
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .content(chatMessage.getContent())
                .sender(chatMessage.getSender())
                .roomId(chatMessage.getRoomId())
                .type(chatMessage.getType())
                .timestamp(chatMessage.getTimestamp() != null ? chatMessage.getTimestamp() : LocalDateTime.now())
                .build();
                
        ChatMessageEntity savedEntity = chatMessageRepository.save(entity);
        
        return mapToDto(savedEntity);
    }

    public List<ChatMessage> getChatHistory(String roomId) {
        log.info("Fetching chat history for room: {}", roomId);
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    private ChatMessage mapToDto(ChatMessageEntity entity) {
        return ChatMessage.builder()
                .content(entity.getContent())
                .sender(entity.getSender())
                .roomId(entity.getRoomId())
                .type(entity.getType())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public List<Map<String, Object>> getActiveRooms() {
        List<Object[]> results = chatMessageRepository.findActiveRooms();
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", row[0]);
            map.put("lastActive", row[1]);
            map.put("customerName", row[2]);
            return map;
        }).collect(Collectors.toList());
    }
}
