package vn.quahoa.flowershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.quahoa.flowershop.entity.ChatMessageEntity;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(String roomId);

    @org.springframework.data.jpa.repository.Query("SELECT c.roomId, MAX(c.timestamp) FROM ChatMessageEntity c GROUP BY c.roomId ORDER BY MAX(c.timestamp) DESC")
    List<Object[]> findActiveRooms();
}
