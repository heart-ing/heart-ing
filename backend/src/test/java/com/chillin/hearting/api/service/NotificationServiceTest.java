package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.NotificationListData;
import com.chillin.hearting.db.domain.Notification;
import com.chillin.hearting.db.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest extends AbstractTestData {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private HeartService heartService;
    @Mock
    private UserService userService;

    @Test
    @DisplayName("모든 알람 조회")
    void getNotifications() {
        // given
        Notification notification1 = createNotification(sender,message,like,null);
        Notification notification2 = createNotification(sender,message,love,null);
        List<Notification> notificationList = List.of(notification1, notification2);

        NotificationService spyService = spy(notificationService);
        doNothing().when(spyService).processNotification(any(Notification.class), any());
        doReturn(notificationList).when(notificationRepository).findByUserIdAndIsActiveTrue(anyString(),any(Sort.class));

        // when
        NotificationListData result = spyService.getNotifications("user");

        // then
        assertThat(result).extracting(notificationListData -> notificationListData.notificationList.size() == notificationList.size());
    }

    @Test
    @DisplayName("유효/만료 알림 처리")
    void processNotification() {
        // given
        Notification validNotification1 = createNotification(sender,message,like, LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(10));
        Notification validNotification2 = createNotification(sender,null,null, LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(10));
        Notification expiredNotification1 = createNotification(sender,message,like,LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(10));
        Notification expiredNotification2 = createNotification(sender,null,null,LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(10));
        List<Notification> notificationList = List.of(validNotification1,validNotification2, expiredNotification1, expiredNotification2);
        NotificationListData notificationListData = NotificationListData.builder().notificationList(new ArrayList<>()).build();

        // when
        for (Notification n : notificationList) {
            notificationService.processNotification(n, notificationListData);
        }

        // then
        assertThat(notificationListData).extracting(data -> data.notificationList.size() == 2);
    }

    @Test
    @DisplayName("알림 읽음 처리")
    void readNotification() {
        // given
        Notification notification = createNotification(sender,null,null,null);
        doReturn(notification).when(notificationRepository).save(any(Notification.class));
        doReturn(Optional.of(notification)).when(notificationRepository).findById(anyLong());

        assertThat(notification).extracting(n -> n.isChecked() == false);

        // when
        notificationService.readNotification(1L);

        // then
        assertThat(notification).extracting(n -> n.isChecked() == true);
    }

    @Test
    @DisplayName("알림 저장")
    void save() {
        // given
        Notification notification = createNotification(sender,null,null,null);
        doReturn(notification).when(notificationRepository).save(any(Notification.class));

        // when
        Notification savedNotification = notificationService.save(notification);

        // then
        assertThat(savedNotification).isEqualTo(notification);
    }

    @Test
    @DisplayName("알림 저장(유저, 하트 포함)")
    void saveWithUserIdAndHeartId() {
        // given
        Notification notification = createNotification(sender,null,like,null);
        doReturn(sender).when(userService).findById(anyString());
        doReturn(like).when(heartService).findById(anyLong());
        doReturn(notification).when(notificationRepository).save(any(Notification.class));

        // when
        Notification savedNotification = notificationService.save(sender.getId(), like.getId());

        // then
        assertThat(savedNotification).extracting(n -> n.getUser().getId().equals(sender.getId()) && n.getHeart().getId() == notification.getHeart().getId());
    }

    @Test
    @DisplayName("24시간 내 존재하는 알림 확인")
    void hasNotificationIn24Hour() {
        // given
        final String EXIST_KEY = "existKey";
        final String NOT_EXIST_KEY = "notExistKey";
        ValueOperations mockRedisOperations = mock(ValueOperations.class);
        doReturn(mockRedisOperations).when(redisTemplate).opsForValue();
        doReturn(Object.class).when(mockRedisOperations).get(EXIST_KEY);
        doReturn(null).when(mockRedisOperations).get(NOT_EXIST_KEY);

        // when
        boolean exist = notificationService.hasNotificationIn24Hour(EXIST_KEY);
        boolean notExist = notificationService.hasNotificationIn24Hour(NOT_EXIST_KEY);

        // then
        assertThat(exist).isTrue();
        assertThat(notExist).isFalse();
    }

    @Test
    @DisplayName("24시간 유효한 알림 저장")
    void setNotificationFor24Hour() {
        // given
        String KEY = "key";
        ValueOperations mockRedisOperations = mock(ValueOperations.class);
        doReturn(mockRedisOperations).when(redisTemplate).opsForValue();
        doNothing().when(mockRedisOperations).set(anyString(),anyString(),anyLong(),any(TimeUnit.class));

        // when
        notificationService.setNotificationFor24Hour(KEY);

        // then
        verify(mockRedisOperations,times(1)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("알림 ID로 조회")
    void findById() {
        // given
        Notification notification = createNotification(sender,null,null,null);
        doReturn(Optional.of(notification)).when(notificationRepository).findById(anyLong());

        // when
        Notification findNotification = notificationService.findById(1L);

        // then
        assertThat(findNotification).extracting(n -> n.getUser().getId() == findNotification.getUser().getId());
    }
}