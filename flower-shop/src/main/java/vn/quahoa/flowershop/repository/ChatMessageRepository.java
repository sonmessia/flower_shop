package vn.quahoa.flowershop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.quahoa.flowershop.entity.ChatMessageEntity;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(String roomId);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT
                m.room_id,
                MAX(m.timestamp) AS last_active,
                COALESCE(
                    (
                        SELECT j.sender
                        FROM chat_messages j
                        WHERE j.room_id = m.room_id
                          AND j.type = 'JOIN'
                        ORDER BY j.timestamp ASC
                        LIMIT 1
                    ),
                    (
                        SELECT f.sender
                        FROM chat_messages f
                        WHERE f.room_id = m.room_id
                        ORDER BY f.timestamp ASC
                        LIMIT 1
                    )
                ) AS customer_name
            FROM chat_messages m
            GROUP BY m.room_id
            ORDER BY MAX(m.timestamp) DESC
            """, nativeQuery = true)
    List<Object[]> findActiveRooms();
}
