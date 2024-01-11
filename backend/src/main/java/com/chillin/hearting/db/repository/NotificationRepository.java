package com.chillin.hearting.db.repository;

import com.chillin.hearting.db.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class NotificationRepository {

    private final NotificationJPARepository notificationJPARepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<Notification> findByUserIdAndIsActiveTrue(String userId, Sort sort) {
        return notificationJPARepository.findByUserIdAndIsActiveTrue(userId, sort);
    }

    public Notification save(Notification notification) {
        return notificationJPARepository.save(notification);
    }

    public boolean hasNotificationIn24Hour(String key) {
        return (redisTemplate.opsForValue().get(key) != null) ? true : false;
    }

    public void setNotificationFor24Hour(String key) {
        redisTemplate.opsForValue().set(key, "true", 24L, TimeUnit.HOURS);
    }

    public Optional<Notification> findById(Long notificationId) {
        return notificationJPARepository.findById(notificationId);
    }
}
