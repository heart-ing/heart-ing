package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.*;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

    private static final String HEART_TYPE_DEFAULT = "DEFAULT";
    private static final String HEART_TYPE_SPECIAL = "SPECIAL";
    private static final String HEART_TYPE_EVENT = "EVENT";

    private static final int HEART_RAINBOW_MAX_VALUE = 1;
    private static final int HEART_MINCHO_MAX_VALUE = 5;
    private static final int HEART_SUNNY_MAX_VALUE = 5;
    private static final int HEART_READING_GLASSES_MAX_VALUE = 3;
    private static final int HEART_ICECREAM_MAX_VALUE = 3;
    private static final int HEART_SHAMROCK_MAX_VALUE = 3;
    private static final int HEART_FOUR_LEAF_MAX_VALUE = 4;
    private static final int HEART_NOIR_MAX_VALUE = 2;

    private static final long ID_YELLOW_HEART = 1;
    private static final long ID_BLUE_HEART = 2;
    private static final long ID_GREEN_HEART = 3;
    private static final long ID_PINK_HEART = 4;
    private static final long ID_SUNNY_HEART = 9;
    private static final long ID_SHAMROCK_HEART = 12;

    private static final HashSet<Long> lockedHeartSet = new HashSet<>(Arrays.asList(4L, 5L));

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
        List<Heart> allHearts = heartRepository.findAll();

        // 유저가 존재한다면, 획득한 하트를 가져옵니다.
        HashSet<Long> myHeartSet = new HashSet<>();
        if (user != null) {
            myHeartSet = findUserHeartIds(user.getId());
            log.info("들어온 유저 아이디 : {} 이미 획득한 스페셜 하트 개수 : {}", user.getId(), myHeartSet.size());
        }


        // 모든 하트를 반환하되, 기본 하트이거나 내가 획득한 하트는 잠금이 해제됩니다. 아직 잠긴 하트 중 내가 획득할 수 있는 하트인지 체크합니다.
        List<HeartData> resHearts = new ArrayList<>();
        for (Heart heart : allHearts) {
            HeartData heartData = HeartData.of(heart, (HEART_TYPE_DEFAULT.equals(heart.getType()) || myHeartSet.contains(heart.getId()) ? false : true));
            if (user != null && heartData.getIsLocked())
                heartData.setAcq(isAcquirableSpecialHeart(user.getId(), heart.getId()));
            resHearts.add(heartData);
        }
        return HeartListData.builder().heartList(resHearts).build();
    }

    /**
     * 유저의 획득 하트 아이디 Set을 반환합니다.
     *
     * @param userId
     * @return
     */
    private HashSet<Long> findUserHeartIds(String userId) {
        HashSet<Long> myHeartSet = new HashSet<>();

        if (userId == null) return myHeartSet;

        List<UserHeart> userHearts = userHeartRepository.findAllByUserId(userId);
        for (UserHeart myHeart : userHearts) {
            myHeartSet.add(myHeart.getHeart().getId());
        }
        return myHeartSet;
    }

    /**
     * 메시지 전송용 하트 리스트를 조회합니다.
     * 기본 하트 - 모든 잠금이 해제되어있습니다. 비로그인 유저에 한해 두 개의 하트가 잠겨있습니다.
     * 스페셜 하트 - 로그인 유저 중 획득한 스페셜 하트가 제공됩니다.
     *
     * @param user
     * @return
     */
    public List<HeartData> findUserMessageHearts(User user) {
        log.info("메시지 전송용 하트 리스트 조회 - 기본 하트 + 내가 획득한 하트를 조회한다.");
        List<HeartData> resHearts = new ArrayList<>();
        for (Heart heart : findDefaultHeartList()) {
            resHearts.add(HeartData.of(heart, false));
        }

        if (user != null) {
            String userId = user.getId();
            List<UserHeart> myHearts = userHeartRepository.findAllByUserIdOrderByHeartId(userId);
            log.info("들어온 유저 아이디 : {} 이미 획득한 하트 개수 : {}", userId, myHearts.size());
            for (UserHeart myHeart : myHearts) {
                resHearts.add(HeartData.of(myHeart.getHeart(), false));
            }
        } else {
            log.info("비로그인 유저입니다. 특정 하트에 대해 사용을 제한합니다.");
            for (HeartData heartData : resHearts) {
                if (lockedHeartSet.contains(heartData.getHeartId())) {
                    heartData.setLock();
                }
            }
        }
        return resHearts;
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

        if (HEART_TYPE_DEFAULT.equals(findHeart.getType())) {
            heartDetailData.unLock();
        } else if (HEART_TYPE_SPECIAL.equals(findHeart.getType()) || HEART_TYPE_EVENT.equals(findHeart.getType())) {
            if (user != null) {
                String userId = user.getId();
                log.info("{}님이 {}번 하트를 상세 조회합니다.", userId, heartId);
                if (hasUserHeart(userId,heartId)) {
                    heartDetailData.unLock();
                } else {
                    if (isAcquirableSpecialHeart(userId, heartId)) {
                        heartDetailData.setAcqTrue();
                    }
                    heartDetailData.setConditions(getSpecialHeartAcqCondition(userId, heartId));
                }
            } else {
                log.info("비로그인 유저가 {}번 하트를 상세 조회합니다.", heartId);
            }
        }

        return heartDetailData;
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
        HashSet<Long> mySpecialHeartIds = findUserHeartIds(userId);

        for (Heart heart : findSpecialHeartList()) {
            Long hId = heart.getId();
            if (!mySpecialHeartIds.contains(hId) && isAcquirableSpecialHeart(userId, hId)) {
                String key = "user:" + userId + ":notifiedHeartId:" + hId;
                if (!notificationRepository.hasNotificationIn24Hour(key)) {
                    User findUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
                    Heart findHeart = heartRepository.findById(hId).orElseThrow(HeartNotFoundException::new);
                    notificationRepository.save(Notification.builder()
                            .user(findUser)
                            .content(findHeart.getName() + "하트를 획득할 수 있습니다!")
                            .heart(findHeart)
                            .type("H")
                            .build());
                    notificationRepository.setNotificationFor24Hour(key);
                    isAcq = true;
                    log.info("{}번째 하트 획득 가능!! 알림 저장", hId);
                }
            }
        }

        return isAcq;
    }

    /**
     * 스페셜 하트 획득 조건을 충족했는지 확인합니다.
     *
     * @param userId
     * @param heartId
     * @return 유저가 해당 하트를 획득 가능한가 ? true : false
     */
    private boolean isAcquirableSpecialHeart(String userId, Long heartId) {
        log.info("{}번 스페셜 하트 획득 조건을 충족했는지 확인합니다.", heartId);
        boolean isAcquirable = false;
        switch (heartId.intValue()) {
            case 7:
                // 무지개 하트 - 모든 기본하트 1개 보내기
                isAcquirable = true;
                for (Heart heart : findDefaultHeartList()) {
                    int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, heart.getId());
                    if (sentHeartCnt < HEART_RAINBOW_MAX_VALUE) {
                        isAcquirable = false;
                        log.info("무지개 하트를 획득 불가 - {}번 하트 조건 미충족", heart.getId());
                    }
                }
                break;
            case 8:
                // 민초 하트 - 파란색 하트 5개 보내기
                isAcquirable = true;
                int blueHeartSentCnt = heartRepository.getUserSentHeartCnt(userId, ID_BLUE_HEART);
                if (blueHeartSentCnt < HEART_MINCHO_MAX_VALUE) {
                    isAcquirable = false;
                    log.info("민초 하트를 획득 불가 - {}번 하트 조건 미충족", ID_BLUE_HEART);
                }
                break;
            case 9:
                // 햇살 하트 - 노랑 하트 5개 보내기
                isAcquirable = true;
                int yellowHeartSentCnt = heartRepository.getUserSentHeartCnt(userId, ID_YELLOW_HEART);
                if (yellowHeartSentCnt < HEART_SUNNY_MAX_VALUE) {
                    isAcquirable = false;
                    log.info("햇살 하트를 획득 불가 - {}번 하트 조건 미충족", ID_YELLOW_HEART);
                }
                break;
            case 10:
                // 돋보기 하트 - 특정인에게 핑크 하트 3개 보내기
                isAcquirable = true;
                Integer result = messageRepository.findMaxMessageCountToSameUser(userId, ID_PINK_HEART);
                int msgCnt = result == null ? 0 : result;
                if (msgCnt < HEART_READING_GLASSES_MAX_VALUE) {
                    isAcquirable = false;
                    log.info("돋보기 하트를 획득 불가 - {}번 하트 조건 미충족", ID_PINK_HEART);
                }
                break;
            case 11:
                // 아이스크림 하트  - 햇살 하트 3개 받기
                isAcquirable = true;
                int receivedHeartCnt = heartRepository.getUserReceivedHeartCnt(userId, ID_SUNNY_HEART);
                if (receivedHeartCnt < HEART_ICECREAM_MAX_VALUE) {
                    isAcquirable = false;
                    log.info("아이스크림 하트를 획득 불가 - {}번 하트 조건 미충족", ID_SUNNY_HEART);
                }
                break;
            case 12:
                // 세잎클로버 하트 - 초록 하트 3개 보내기
                isAcquirable = true;
                int greenHeartSentCnt = heartRepository.getUserSentHeartCnt(userId, ID_GREEN_HEART);
                if (greenHeartSentCnt < HEART_SHAMROCK_MAX_VALUE) {
                    isAcquirable = false;
                    log.info("세잎클로버 하트를 획득 불가 - {}번 하트 조건 미충족", ID_GREEN_HEART);
                }
                break;
            case 13:
                // 네잎클로버 하트 - 세잎클로버 하트 4개 받기
                isAcquirable = true;
                int shamrockHeartReceivedCnt = heartRepository.getUserReceivedHeartCnt(userId, ID_SHAMROCK_HEART);
                if (shamrockHeartReceivedCnt < HEART_FOUR_LEAF_MAX_VALUE) {
                    isAcquirable = false;
                    log.info("네잎클로버 하트를 획득 불가 - {}번 하트 조건 미충족", ID_SHAMROCK_HEART);
                }
                break;
            case 14:
                // 질투의 누아르 하트 - 모든 기본하트 2개 보내기
                isAcquirable = true;
                for (Heart heart : findDefaultHeartList()) {
                    int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, heart.getId());
                    if (sentHeartCnt < HEART_NOIR_MAX_VALUE) {
                        isAcquirable = false;
                        log.info("누아르 하트를 획득 불가 - {}번 하트 조건 미충족", heart.getId());
                    }
                }
                break;
        }
        return isAcquirable;
    }

    /**
     * 특정 하트 획득 상세 조건을 리턴한다.
     *
     * @param userId
     * @param heartId
     *
     */
    private ArrayList<HeartConditionData> getSpecialHeartAcqCondition(String userId, long heartId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();
        if (heartId == 7) {
            for (Heart defaultHeart : findDefaultHeartList()) {
                int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, defaultHeart.getId());
                Heart heart = heartRepository.findById(defaultHeart.getId()).orElseThrow(HeartNotFoundException::new);
                result.add(
                        HeartConditionData.of(heart, sentHeartCnt, HEART_RAINBOW_MAX_VALUE)
                );
            }

        } else if (heartId == 8) {
            Heart heart = heartRepository.findById(ID_BLUE_HEART).orElseThrow(HeartNotFoundException::new);
            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, ID_BLUE_HEART);
            result.add(
                    HeartConditionData.of(heart,sentHeartCnt,HEART_MINCHO_MAX_VALUE)
            );

        } else if (heartId == 9) {
            Heart heart = heartRepository.findById(ID_YELLOW_HEART).orElseThrow(HeartNotFoundException::new);
            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, ID_YELLOW_HEART);
            result.add(
                    HeartConditionData.of(heart,sentHeartCnt,HEART_SUNNY_MAX_VALUE)
            );

        } else if (heartId == 10) {
            Heart heart = heartRepository.findById(ID_PINK_HEART).orElseThrow(HeartNotFoundException::new);
            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, ID_PINK_HEART);
            result.add(
                    HeartConditionData.of(heart,sentHeartCnt,HEART_READING_GLASSES_MAX_VALUE)
            );

        } else if (heartId == 11) {
            Heart heart = heartRepository.findById(ID_SUNNY_HEART).orElseThrow(HeartNotFoundException::new);
            int receivedHeartCnt = heartRepository.getUserReceivedHeartCnt(userId, ID_SUNNY_HEART);
            result.add(
                    HeartConditionData.of(heart,receivedHeartCnt,HEART_ICECREAM_MAX_VALUE)
            );

        } else if (heartId == 12) {
            Heart heart = heartRepository.findById(ID_GREEN_HEART).orElseThrow(HeartNotFoundException::new);
            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, ID_GREEN_HEART);
            result.add(
                    HeartConditionData.of(heart,sentHeartCnt,HEART_SHAMROCK_MAX_VALUE)
            );

        }  else if (heartId == 13) {
            Heart heart = heartRepository.findById(ID_SHAMROCK_HEART).orElseThrow(HeartNotFoundException::new);
            int receivedHeartCnt = heartRepository.getUserReceivedHeartCnt(userId, ID_SHAMROCK_HEART);
            result.add(
                    HeartConditionData.of(heart,receivedHeartCnt,HEART_FOUR_LEAF_MAX_VALUE)
            );

        } else if (heartId == 14) {
            for (Heart heart : findDefaultHeartList()) {
                int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, heart.getId());
                result.add(
                        HeartConditionData.of(heart,sentHeartCnt,HEART_NOIR_MAX_VALUE)
                );
            }
        }

        return result;

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

    public boolean hasUserHeart(String userId, long heartId) {
        UserHeart findUserHeart = userHeartRepository.findByHeartIdAndUserId(heartId, userId).orElseGet(null);
        return findUserHeart != null;
    }

    @Transactional
    public List<Heart> findDefaultHeartList() {
        return heartRepository.findAllByType(HEART_TYPE_DEFAULT);
    }

    @Transactional
    public List<Heart> findSpecialHeartList() {
        return heartRepository.findAllByType(HEART_TYPE_SPECIAL);
    }
}
