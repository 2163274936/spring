package top.villion.www;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // 用户名唯一且不为空
    private String username;

    @Column(nullable = false) // 密码不为空
    private String password;

    private String avatarUrl;
    private String gender;
    private Integer age;
    private String region;
    private String signature;

    // 记录用户创建时间（新增字段）
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // 多对多关系：用户加入的房间（新增）
    @ManyToMany(mappedBy = "members")
    private Set<Room> joinedRooms = new HashSet<>();

    // 关注列表（存储用户ID，非持久化到数据库）
    @Transient
    private List<Long> following = new ArrayList<>();

    // 自动设置创建时间（新增）
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
