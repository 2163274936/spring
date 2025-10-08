package top.villion.www;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    public enum Type {
        CHAT, JOIN, LEAVE
    }

    private Long id;
    private String content;
    private String sender;
    private Long senderId;
    private String recipient;
    private Long roomId;
    private Type type;
    private Date timestamp;
}
