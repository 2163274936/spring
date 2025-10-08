package top.villion.www;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 聊天控制器：处理实时消息和用户匹配，确保无依赖错误
 */
@Controller
public class ChatController {

    // 依赖注入（必须确保这些Bean在项目中已定义）
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final UserRepository userRepository;

    // 匹配队列（线程安全）
    private final ConcurrentLinkedQueue<Long> matchQueue = new ConcurrentLinkedQueue<>();

    // 构造器注入（Spring会自动装配）
    public ChatController(SimpMessagingTemplate messagingTemplate,
                          RoomService roomService,
                          UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
        this.userRepository = userRepository;
    }

    /**
     * 处理房间消息（群聊）
     * 前端通过 /app/roomMessage 发送消息
     * 所有订阅 /topic/rooms 的客户端会收到消息
     */
    @MessageMapping("/roomMessage")
    @SendTo("/topic/rooms")
    public RoomMessage handleRoomMessage(RoomMessage message) {
        // 简单校验（避免空消息）
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        return message;
    }

    /**
     * 处理随机匹配请求
     * 前端通过 /app/randomMatch 发送请求
     */
    @MessageMapping("/randomMatch")
    public void handleRandomMatch(MatchRequest request) {
        // 校验用户ID
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        Long currentUserId = request.getUserId();
        Long matchedUserId = matchQueue.poll(); // 从队列取一个等待匹配的用户

        if (matchedUserId != null && !matchedUserId.equals(currentUserId)) {
            // 匹配成功：创建临时房间
            Room tempRoom = createTempRoom(currentUserId);
            // 让双方加入房间
            roomService.joinRoom(tempRoom.getId(), currentUserId);
            roomService.joinRoom(tempRoom.getId(), matchedUserId);
            // 发送匹配结果给双方
            sendMatchResult(currentUserId, matchedUserId, tempRoom.getId());
            sendMatchResult(matchedUserId, currentUserId, tempRoom.getId());
        } else {
            // 没匹配到，加入等待队列
            if (!matchQueue.contains(currentUserId)) {
                matchQueue.add(currentUserId);
            }
        }
    }

    // 创建临时房间
    private Room createTempRoom(Long creatorId) {
        Room room = new Room();
        room.setName("临时聊天-" + System.currentTimeMillis());
        room.setCreatedBy(creatorId);
        room.setMaxCapacity(2); // 临时房间最多2人
        return roomService.createRoom(room, creatorId);
    }

    // 发送匹配结果给用户
    private void sendMatchResult(Long receiverId, Long matchedUserId, Long roomId) {
        // 查询匹配到的用户信息
        User matchedUser = userRepository.findById(matchedUserId).orElse(null);
        if (matchedUser == null) {
            return;
        }

        // 构造匹配结果
        MatchResult result = new MatchResult();
        result.setMatchedUserId(matchedUser.getId());
        result.setMatchedUsername(matchedUser.getUsername());
        result.setMatchedAvatarUrl(matchedUser.getAvatarUrl());
        result.setTempRoomId(roomId);

        // 发送到用户的专属通道
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/match",
                result
        );
    }
}
