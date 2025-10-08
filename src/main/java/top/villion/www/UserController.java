package top.villion.www;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {

    // 用户名和密码校验规则（提取为常量，便于维护）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,20}$");

    private final UserRepository userRepository;

    // 构造器注入（推荐方式，符合Spring最佳实践）
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 用户登录
     * 成功返回200+用户信息，失败返回400（参数错误）或401（认证失败）
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User loginUser) {
        // 1. 验证输入参数有效性
        if (!isValidLoginInput(loginUser)) {
            return ResponseEntity.badRequest().build(); // 400：参数格式错误
        }

        // 2. 验证用户名和密码
        return userRepository.findByUsername(loginUser.getUsername())
                .filter(user -> user.getPassword().equals(loginUser.getPassword()))
                .map(ResponseEntity::ok) // 200：登录成功
                .orElseGet(() -> ResponseEntity.status(401).build()); // 401：认证失败（区别于参数错误）
    }

    /**
     * 用户注册
     * 成功返回201+用户信息，失败返回400（参数错误或用户名已存在）
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User newUser) {
        // 1. 验证输入参数
        if (!isValidRegisterInput(newUser)) {
            return ResponseEntity.badRequest().build(); // 400：参数格式错误
        }

        // 2. 检查用户名唯一性
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build(); // 400：用户名已存在
        }

        // 3. 设置默认值并保存
        setDefaultValuesForNewUser(newUser);
        User savedUser = userRepository.save(newUser);

        // 4. 返回201 Created（符合REST规范，新建资源应返回201）
        return ResponseEntity.status(201).body(savedUser);
    }

    /**
     * 根据ID查询用户
     * 存在返回200+用户信息，不存在返回404
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 根据用户名查询用户
     * 存在返回200+用户信息，不存在返回404
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 查询所有用户
     * 始终返回200+用户列表（空列表或实际数据）
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * 更新用户信息
     * 成功返回200+更新后信息，失败返回404（用户不存在）或400（参数错误）
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User updateData) {

        // 1. 检查用户是否存在
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404：用户不存在
        }

        User dbUser = userOpt.get();

        // 2. 处理用户名更新（如需修改）
        if (needsUsernameUpdate(dbUser, updateData)) {
            String newUsername = updateData.getUsername().trim();
            // 校验新用户名有效性
            if (!isValidUsername(newUsername) || isUsernameTaken(newUsername, id)) {
                return ResponseEntity.badRequest().build(); // 400：用户名无效或已被占用
            }
            dbUser.setUsername(newUsername);
        }

        // 3. 更新其他字段（只更新非空且有效的字段）
        updateUserFields(dbUser, updateData);

        // 4. 保存并返回更新后的用户
        User updatedUser = userRepository.save(dbUser);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 删除用户
     * 成功返回204（无内容），失败返回404（用户不存在）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204：删除成功（无返回内容）
    }

    // ------------------------------
    // 私有辅助方法（提高代码可读性）
    // ------------------------------

    /**
     * 验证登录输入参数
     */
    private boolean isValidLoginInput(User loginUser) {
        return loginUser != null
                && isValidUsername(loginUser.getUsername())
                && isValidPassword(loginUser.getPassword());
    }

    /**
     * 验证注册输入参数
     */
    private boolean isValidRegisterInput(User newUser) {
        return newUser != null
                && isValidUsername(newUser.getUsername())
                && isValidPassword(newUser.getPassword());
    }

    /**
     * 为新用户设置默认值
     */
    private void setDefaultValuesForNewUser(User user) {
        if (user.getAvatarUrl() == null || user.getAvatarUrl().trim().isEmpty()) {
            user.setAvatarUrl("https://picsum.photos/200/200?random=" + System.currentTimeMillis() % 100);
        }
    }

    /**
     * 判断是否需要更新用户名
     */
    private boolean needsUsernameUpdate(User dbUser, User updateData) {
        return updateData.getUsername() != null
                && !updateData.getUsername().trim().isEmpty()
                && !updateData.getUsername().equals(dbUser.getUsername());
    }

    /**
     * 检查用户名是否已被占用（排除当前用户）
     */
    private boolean isUsernameTaken(String username, Long currentUserId) {
        return userRepository.findByUsername(username)
                .filter(user -> !user.getId().equals(currentUserId))
                .isPresent();
    }

    /**
     * 更新用户其他字段（只处理非空且有效的值）
     */
    private void updateUserFields(User dbUser, User updateData) {
        if (updateData.getAvatarUrl() != null && !updateData.getAvatarUrl().trim().isEmpty()) {
            dbUser.setAvatarUrl(updateData.getAvatarUrl());
        }
        if (updateData.getGender() != null && !updateData.getGender().trim().isEmpty()) {
            dbUser.setGender(updateData.getGender());
        }
        if (updateData.getAge() != null && updateData.getAge() > 0) {
            dbUser.setAge(updateData.getAge());
        }
        if (updateData.getRegion() != null && !updateData.getRegion().trim().isEmpty()) {
            dbUser.setRegion(updateData.getRegion());
        }
        if (updateData.getSignature() != null) {
            dbUser.setSignature(updateData.getSignature());
        }
    }

    // 基础校验方法
    private boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
