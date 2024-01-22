package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.NotificationData;
import com.chillin.hearting.api.data.NotificationListData;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.domain.Notification;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.NotificationRepository;
import com.chillin.hearting.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final HeartService heartService;
    private final UserService userService;

    @Transactional
    public NotificationListData getNotifications(String userId) {
        List<Notification> notificationList = notificationRepository.findByUserIdAndIsActiveTrue(userId, Sort.by(Sort.Direction.DESC, "createdDate"));
//        System.out.println(notificationList.get(0));
        NotificationListData notificationListData = NotificationListData.builder().notificationList(new ArrayList<>()).build();

        for (Notification n : notificationList) {
            processNotification(n, notificationListData);
        }

        log.info(userId + " 유저가 알림 리스트를 조회했습니다. 총 " + notificationListData.getNotificationList().size() + "개의 알림이 조회되었습니다.");

        return notificationListData;
    }

    public void processNotification(Notification n, NotificationListData notificationListData) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime expiredDate = n.getExpiredDate();

        Heart heart = n.getHeart();
        Message message = n.getMessage();
        String heartName = heart != null ? heart.getName() : null;
        String heartUrl = heart != null ? heart.getImageUrl() : null;
        Long messageId = message != null ? message.getId() : null;

        if (expiredDate.isAfter(now)) {
            // Not yet expired
            NotificationData notificationData = NotificationData.builder()
                    .notificationId(n.getId())
                    .heartName(heartName)
                    .heartUrl(heartUrl)
                    .messageId(messageId)
                    .type(n.getType())
                    .createdDate(n.getCreatedDate())
                    .isChecked(n.isChecked()).build();
            notificationListData.getNotificationList().add(notificationData);
        } else {
            // Expired, need to persist to DB
            n.deleteNotification();
            notificationRepository.save(n);
        }
    }

    @Transactional
    public Long readNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);
        notification.readNotification();
        notificationRepository.save(notification);

        log.info(notificationId + " 알림을 읽었습니다.");

        return notification.getId();
    }

    @Transactional(readOnly = true)
    public List<Notification> findByUserIdAndIsActiveTrue(String userId, Sort sort) {
        return notificationRepository.findByUserIdAndIsActiveTrue(userId, sort);
    }

    @Transactional(readOnly = true)
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Notification save(String userId, long heartId) {
        User findUser = userService.findById(userId);
        Heart findHeart = heartService.findById(heartId);

        return save(Notification.builder()
                .user(findUser)
                .content(findHeart.getName() + "하트를 획득할 수 있습니다!")
                .heart(findHeart)
                .type("H")
                .build());
    }

    @Transactional(readOnly = true)
    public boolean hasNotificationIn24Hour(String key) {
        return (redisTemplate.opsForValue().get(key) != null) ? true : false;
    }

    @Transactional(readOnly = true)
    public void setNotificationFor24Hour(String key) {
        redisTemplate.opsForValue().set(key, "true", 24L, TimeUnit.HOURS);
    }

    @Transactional(readOnly = true)
    public Optional<Notification> findById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }
}
