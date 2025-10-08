package top.villion.www;

import lombok.Data;

@Data
public class RoomMessage {
    private Long roomId;          // 房间ID
    private Long senderId;        // 发送者ID
    private String senderName;    // 发送者名称
    private String senderAvatar;  // 发送者头像
    private String content;       // 消息内容
    private String sendTime;      // 发送时间（前端生成）
}