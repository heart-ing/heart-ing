package com.chillin.hearting.api.service.facade;

import com.chillin.hearting.api.data.Data;
import com.chillin.hearting.api.data.HeartData;
import com.chillin.hearting.api.data.HeartDetailData;
import com.chillin.hearting.api.data.HeartListData;
import com.chillin.hearting.api.service.HeartCheckService;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.UserHeartService;
import com.chillin.hearting.api.service.UserService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.domain.UserHeart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HeartFacade {

    private final HeartService heartService;
    private final UserHeartService userHeartService;
    private final HeartCheckService heartCheckService;
    private final UserService userService;

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
        for (Heart heart : heartService.findDefaultTypeHearts()) {
            result.add(HeartData.of(heart));
        }

        return result;
    }

    private List<HeartData> findAllHeartDataWithUser(User user) {
        List<HeartData> result = new ArrayList<>();
        for (Heart heart : heartService.findAll()) {
            HeartData heartData = HeartData.of(heart);
            result.add(heartData);
            if (HeartType.isDefault(heart.getType())) continue;

            if (!userHeartService.isUserAcquiredHeart(user.getId(), heart.getId())) {
                heartData.setLock();
            }

            if (heartCheckService.isUserAcquirableHeart(user.getId(), heart.getId())) {
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
        for (UserHeart myHeart : userHeartService.findAllByUserIdOrderByHeartId(user.getId())) {
            result.add(HeartData.of(myHeart.getHeart()));
        }

        return result;
    }

    private List<HeartData> findMessageHeartsForNoLogin() {
        ArrayList<HeartData> result = new ArrayList<>();
        for (Heart heart : heartService.findDefaultTypeHearts()) {
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
        Heart findHeart = heartService.findById(heartId);
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

    private void setHeartDetailForUser(String userId, Long heartId, HeartDetailData heartDetailData) {
        if (userHeartService.isUserAcquiredHeart(userId,heartId)) {
            heartDetailData.unLock();
        } else {
            if (heartCheckService.isUserAcquirableHeart(userId,heartId)) {
                heartDetailData.setAcqTrue();
            }
            heartDetailData.setConditions(heartCheckService.getSpecialHeartAcqCondition(userId, heartId));
        }
    }

    @Transactional
    public void saveUserHearts(String userId, Long heartId) {
        User findUser = userService.findById(userId);
        Heart findHeart = heartService.findById(heartId);
        UserHeart userHeart = UserHeart.of(findUser, findHeart);
        if (isNotAcquiredUserHeart(heartId,userId)) userHeartService.save(userHeart);
    }


    public boolean isNotAcquiredUserHeart(long heartId, String userId) {
        return userHeartService.findByHeartIdAndUserId(heartId, userId).isEmpty();
    }
}
