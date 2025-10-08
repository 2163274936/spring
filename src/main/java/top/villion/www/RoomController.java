package top.villion.www;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final UserRepository userRepository;

    public RoomController(RoomService roomService, UserRepository userRepository) {
        this.roomService = roomService;
        this.userRepository = userRepository;
    }

    // 其他方法保持不变...

    /**
     * 加入聊天室（修复参数接收问题）
     * POST /rooms/{roomId}/join
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<Room> joinRoom(
            @PathVariable Long roomId,
            @RequestBody JoinRoomRequest request) {  // 使用专用请求类接收参数
        try {
            // 1. 验证参数
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(null);  // 参数为空返回400
            }

            // 2. 验证用户是否存在
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();  // 用户不存在返回404
            }

            // 3. 加入房间
            Room updatedRoom = roomService.joinRoom(roomId, request.getUserId());
            return ResponseEntity.ok(updatedRoom);

        } catch (Exception e) {
            // 捕获所有异常，返回500错误
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 离开聊天室
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Room> leaveRoom(
            @PathVariable Long roomId,
            @RequestBody JoinRoomRequest request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(null);
            }

            Room updatedRoom = roomService.leaveRoom(roomId, request.getUserId());
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 专用请求类（确保参数名正确）
    public static class JoinRoomRequest {
        private Long userId;  // 与前端传递的参数名一致

        // 必须有getter和setter
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}
