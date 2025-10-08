package top.villion.www;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    @Query("select f.followeeId from Follow f where f.followerId = ?1")
    List<Long> findFolloweeIdsByFollowerId(Long followerId);

    @Query("select f.followerId from Follow f where f.followeeId = ?1")
    List<Long> findFollowerIdsByFolloweeId(Long followeeId);
}
