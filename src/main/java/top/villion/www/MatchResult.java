package top.villion.www;

import lombok.Data;

@Data
public class MatchResult {
    private Long matchedUserId;      // 匹配到的用户ID
    private String matchedUsername;  // 匹配到的用户名
    private String matchedAvatarUrl; // 匹配到的用户头像
    private Long tempRoomId;         // 临时房间ID
}