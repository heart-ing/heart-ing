package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.*;
import com.chillin.hearting.api.service.heartcheck.HeartCheckStrategyFactory;
import com.chillin.hearting.api.service.heartcheck.HeartChecker;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Notification;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.domain.UserHeart;
import com.chillin.hearting.db.repository.*;
import com.chillin.hearting.exception.HeartNotFoundException;
import com.chillin.hearting.exception.RedisKeyNotFoundException;
import com.chillin.hearting.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class HeartService {

    private final HeartRepository heartRepository;
    private final UserHeartRepository userHeartRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final MigrationService migrationService;

    /**
     * 모든 도감 리스트를 반환합니다.
     * DEFAULT 타입의 도감은 잠금이 해제됩니다.
     * 로그인 사용자는 추가적으로 하트 획득 조건을 달성한 하트에 대해 잠금이 해제됩니다.
     *
     * @param user
     * @return 하트 DTO
     */
    public Data findAllHearts(User user) {
        log.info("도감 하트 리스트 조회 - DB의 모든 하트를 조회한다.");
        // 모든 하트를 반환하되, 기본 하트이거나 내가 획득한 하트는 잠금이 해제됩니다. 아직 잠긴 하트 중 내가 획득할 수 있는 하트인지 체크합니다.
        List<HeartData> result;
        if (user != null) result = findAllHeartDataWithUser(user);
        else result = findAllHeartDataWithDefault();

        return HeartListData.builder().heartList(result).build();
    }

    private List<HeartData> findAllHeartDataWithDefault() {
        List<HeartData> result = new ArrayList<>();
        for (Heart heart : findDefaultTypeHearts()) {
            result.add(HeartData.of(heart));
        }

        return result;
    }

    private List<HeartData> findAllHeartDataWithUser(User user) {
        List<HeartData> result = new ArrayList<>();
        for (Heart heart : findAllTypeHearts()) {
            HeartData heartData = HeartData.of(heart);
            result.add(heartData);
            if (HeartType.isDefault(heart.getType())) continue;

            if (!isUserAcquiredHeart(user.getId(), heart.getId())) {
                heartData.setLock();
            }

            if (isUserAcquirableHeart(user.getId(), heart.getId())) {
//                heartData.setAcq(true);
            }
        }

        return result;
    }

    /**
     * 메시지 전송용 하트 리스트를 조회합니다.
     * 기본 하트 - 모든 잠금이 해제되어있습니다. 비로그인 유저에 한해 두 개의 하트가 잠겨있습니다.
     * 스페셜 하트 - 로그인 유저 중 획득한 스페셜 하트가 제공됩니다.
     *
     * @param user
     * @return
     */
    public List<HeartData> findMessageHearts(User user) {
        log.info("메시지 전송용 하트 리스트 조회 - 기본 하트 + 내가 획득한 하트를 조회한다.");
        List<HeartData> result;
        if (user != null) {
            result = findMessageHeartsForUser(user);
        } else {
            result = findMessageHeartsForNoLogin();
        }

        return result;
    }

    private List<HeartData> findMessageHeartsForUser(User user) {
        List<HeartData> result = findAllHeartDataWithDefault();
        for (UserHeart myHeart : userHeartRepository.findAllByUserIdOrderByHeartId(user.getId())) {
            result.add(HeartData.of(myHeart.getHeart()));
        }

        return result;
    }

    private List<HeartData> findMessageHeartsForNoLogin() {
        ArrayList<HeartData> result = new ArrayList<>();
        for (Heart heart : findDefaultTypeHearts()) {
            HeartData heartData = HeartData.of(heart);
            if (HeartInfo.isLockedToNoLogin(heart.getId())) {
                heartData.setLock();
            }
            result.add(heartData);
        }

        return result;
    }

    /**
     * 도감 하트 상세보기
     * 특정 하트에 대해 하트 정보, 획득 조건에 대한 정보를 제공한다.
     * 기본 하트 - 잠금이 해제되어 있다.
     * 스페셜 하트 - 로그인 유저가 획득한 하트에 대해 잠금이 해제되어 있다. 획득하지 못한 하트에 대해서는 달성 현황 정보를 제공한다.
     *
     * @param user
     * @param heartId
     * @return
     */
    public Data findHeartDetail(User user, Long heartId) {
        Heart findHeart = heartRepository.findById(heartId).orElseThrow(HeartNotFoundException::new);
        HeartDetailData heartDetailData = HeartDetailData.of(findHeart);
        if (HeartType.isDefault(findHeart.getType())) {
            heartDetailData.unLock();
        } else {
            if (user != null) {
                setHeartDetailForUser(user.getId(), heartId, heartDetailData);
            }
        }

        return heartDetailData;
    }

    @Transactional
    private void setHeartDetailForUser(String userId, Long heartId, HeartDetailData heartDetailData) {
        if (isUserAcquiredHeart(userId,heartId)) {
            heartDetailData.unLock();
        } else {
            if (isUserAcquirableHeart(userId,heartId)) {
                heartDetailData.setAcqTrue();
            }
            heartDetailData.setConditions(getSpecialHeartAcqCondition(userId, heartId));
        }
    }

    /**
     * 유저가 획득 가능한 스페셜 하트가 있는지 체크합니다.
     *
     * @param userId
     * @return 알림이 필요한가 ? true : false
     */
    @Transactional
    public boolean hasAcquirableHeart(String userId) {
        boolean isAcq = false;
        // 스페셜 하트 달성 여부 체크
        for (Heart heart : findSpecialTypeHearts()) {
            Long heartId = heart.getId();
            if (!isUserAcquiredHeart(userId, heartId) && isUserAcquirableHeart(userId, heartId)) {
                String key = "user:" + userId + ":notifiedHeartId:" + heartId;
                if (!notificationRepository.hasNotificationIn24Hour(key)) {
                    User findUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
                    Heart findHeart = heartRepository.findById(heartId).orElseThrow(HeartNotFoundException::new);
                    notificationRepository.save(Notification.builder()
                            .user(findUser)
                            .content(findHeart.getName() + "하트를 획득할 수 있습니다!")
                            .heart(findHeart)
                            .type("H")
                            .build());
                    notificationRepository.setNotificationFor24Hour(key);
                    isAcq = true;
                    log.info("{}번째 하트 획득 가능!! 알림 저장", heartId);
                }
            }
        }

        return isAcq;
    }

    @Transactional(readOnly = true)
    private HeartChecker initHeartChecker(String userId, long heartId) {
        HeartChecker heartChecker = new HeartChecker(userId);
        HeartCheckStrategyFactory strategyFactory = new HeartCheckStrategyFactory(heartRepository, messageRepository);
        heartChecker.setHeartCheckStrategy(strategyFactory.createHeartCheckStrategy(heartId));

        return heartChecker;
    }

    @Transactional(readOnly = true)
    private boolean isUserAcquirableHeart(String userId, long heartId) {
        HeartChecker heartChecker = initHeartChecker(userId, heartId);

        return heartChecker.isAcquirable();
    }

    @Transactional(readOnly = true)
    private List<HeartConditionData> getSpecialHeartAcqCondition(String userId, Long heartId) {
        HeartChecker heartChecker = initHeartChecker(userId, heartId);

        return heartChecker.getAcqCondition();
    }

    /**
     * 유저가 보낸 메시지를 바탕으로 보낸 하트 개수를 업데이트합니다.
     *
     * @param userId
     * @param heartId
     */
    public void updateSentHeartCount(String userId, Long heartId) {
        log.info("Redis에 userSentHeart를 업데이트합니다. userId:{} heartId:{}", userId, heartId);
        try {
            heartRepository.updateUserSentHeartCnt(userId, heartId);
        } catch (RedisKeyNotFoundException e) {
            log.info(e.getMessage());
            migrationService.migrateUserSentHeart(userId);
        }
    }

    /**
     * 유저가 받은 메시지를 바탕으로 받은 하트 개수를 업데이트합니다.
     *
     * @param userId
     * @param heartId
     */
    public void updateReceivedHeartCount(String userId, Long heartId) {
        log.info("Redis에 userReceivedHeart를 업데이트합니다. userId:{} heartId:{}", userId, heartId);
        try {
            heartRepository.updateUserReceivedHeartCnt(userId, heartId);
        } catch (RedisKeyNotFoundException e) {
            log.info(e.getMessage());
            migrationService.migrateUserReceivedHeart(userId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isUserAcquiredHeart(String userId, long heartId) {
        Optional<UserHeart> result = userHeartRepository.findByHeartIdAndUserId(heartId, userId);
        return !result.isEmpty();
    }

    @Transactional(readOnly = true)
    public List<Heart> findAllTypeHearts() {
        return heartRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Heart> findDefaultTypeHearts() {
        return heartRepository.findAllByType(HeartType.DEFAULT.name());
    }

    @Transactional(readOnly = true)
    public List<Heart> findSpecialTypeHearts() {
        return heartRepository.findAllByType(HeartType.SPECIAL.name());
    }
}
