package top.villion.www;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) throw new IllegalArgumentException("不能关注自己");
        if (!userRepository.existsById(followeeId)) throw new RuntimeException("被关注用户不存在");
        if (!followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            Follow f = new Follow();
            f.setFollowerId(followerId);
            f.setFolloweeId(followeeId);
            followRepository.save(f);
        }
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public List<User> getFollowing(Long userId) {
        List<Long> ids = followRepository.findFolloweeIdsByFollowerId(userId);
        if (ids.isEmpty()) return Collections.emptyList();
        return userRepository.findAllById(ids);
    }

    public List<User> getFollowers(Long userId) {
        List<Long> ids = followRepository.findFollowerIdsByFolloweeId(userId);
        if (ids.isEmpty()) return Collections.emptyList();
        return userRepository.findAllById(ids);
    }
}
