package top.villion.www;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/follows")
//@CrossOrigin(origins = "*")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{targetId}")
    public String follow(@RequestParam Long userId, @PathVariable Long targetId) {
        followService.follow(userId, targetId);
        return "已关注用户 " + targetId;
    }

    @DeleteMapping("/{targetId}")
    public String unfollow(@RequestParam Long userId, @PathVariable Long targetId) {
        followService.unfollow(userId, targetId);
        return "已取消关注用户 " + targetId;
    }

    @GetMapping("/following")
    public List<User> getFollowing(@RequestParam Long userId) {
        return followService.getFollowing(userId);
    }

    @GetMapping("/followers")
    public List<User> getFollowers(@RequestParam Long userId) {
        return followService.getFollowers(userId);
    }
}
