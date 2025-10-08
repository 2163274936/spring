package top.villion.www;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "follows")
@Data
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long followerId;
    private Long followeeId;
}
