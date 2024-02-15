package com.chillin.hearting.api.service.facade;

import com.chillin.hearting.api.data.HeartData;
import com.chillin.hearting.api.data.HeartDetailData;
import com.chillin.hearting.api.data.HeartListData;
import com.chillin.hearting.api.service.*;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.domain.UserHeart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeartFacadeTest extends AbstractTestData {

    @InjectMocks
    private HeartFacade heartFacade;
    @Mock
    private HeartService heartService;
    @Mock
    private UserHeartService userHeartService;
    @Mock
    private HeartCheckService heartCheckService;
    @Mock
    private UserService userService;

    @Test
    @DisplayName("로그인 유저의 하트 도감 조회")
    void findAllHeartsLogin() {
        // given
        String userId = "id";
        User user = createUser("id");
        Heart defaultHeart = createHeart(1L,"name","DEFAULT");
        Heart acquiredHeart = createHeart(2L,"name","SPECIAL");
        Heart acquirableHeart = createHeart(3L,"name","SPECIAL");
        Heart eventHeart = createHeart(4L,"name","EVENT");
        List<Heart> heartList = List.of(defaultHeart,acquiredHeart,acquirableHeart,eventHeart);
        doReturn(heartList).when(heartService).findAll();
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(userId),eq(2L));
        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(userId),eq(3L));
        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(userId),eq(4L));

        doReturn(true).when(heartCheckService).isUserAcquirableHeart(eq(userId),eq(2L));
        doReturn(true).when(heartCheckService).isUserAcquirableHeart(eq(userId),eq(3L));
        doReturn(false).when(heartCheckService).isUserAcquirableHeart(eq(userId),eq(4L));

        // when
        HeartListData data = (HeartListData) heartFacade.findAllHearts(user);

        // then
        assertThat(data.getHeartList()).filteredOn(heartData -> {
           if (heartData.getHeartId() == defaultHeart.getId()) {
               return heartData.getIsLocked() == false && heartData.getIsAcq() == null;
           } else if (heartData.getHeartId() == acquiredHeart.getId()) {
               return heartData.getIsLocked() == false && heartData.getIsAcq() == null;
           } else if (heartData.getHeartId() == acquirableHeart.getId()) {
               return heartData.getIsLocked() == true && heartData.getIsAcq() == true;
           } else if (heartData.getHeartId() == eventHeart.getId()) {
               return heartData.getIsLocked() == true && heartData.getIsAcq() == null;
           }
           return false;
        });
    }

    @Test
    @DisplayName("비로그인 유저의 하트 도감 조회")
    void findAllHeartsNoLogin() {
        // given
        doReturn(heartList).when(heartService).findAll();

        // when
        HeartListData data = (HeartListData) heartFacade.findAllHearts(null);

        // then
        for (HeartData heartData : data.getHeartList()) {
            if (HeartType.isDefault(heartData.getType())) {
                assertThat(heartData.getIsLocked()).isFalse();
            } else {
                assertThat(heartData.getIsLocked()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("메시지 전송용 하트 리스트 조회 - 로그인")
    void findMessageHeartsLogin() {
        // given
        User user = createUser("id");
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();
        List<UserHeart> userHeart = List.of(UserHeart.of(user, mincho), UserHeart.of(user,noir));
        doReturn(userHeart).when(userHeartService).findAllByUserIdOrderByHeartId(anyString());

        // when
        List<HeartData> result = heartFacade.findMessageHearts(user);

        // then
        assertThat(result.size()).isEqualTo(defaultHeartList.size() + userHeart.size());
        for (HeartData heartData : result) {
            assertThat(heartData.getIsLocked()).isFalse();
        }
    }

    @Test
    @DisplayName("메시지 전송용 하트 리스트 조회 - 비로그인")
    void findMessageHeartsNoLogin() {
        // given
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();

        // when
        List<HeartData> result = heartFacade.findMessageHearts(null);

        // then
        assertThat(result.size()).isEqualTo(defaultHeartList.size());
        for (HeartData heartData : result) {
            if (HeartInfo.isLockedToNoLogin(heartData.getHeartId())) {
                assertThat(heartData.getIsLocked()).isTrue();
            } else {
                assertThat(heartData.getIsLocked()).isFalse();

            }
        }
    }

    @Test
    @DisplayName("하트 상세 조회 - 로그인 유저")
    void findHeartDetailForLogin() {
        // given
        User user = createUser("id");

        doReturn(like).when(heartService).findById(eq(1L));
        doReturn(rainbow).when(heartService).findById(eq(7L));
        doReturn(mincho).when(heartService).findById(eq(8L));
        doReturn(sunny).when(heartService).findById(eq(9L));

        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(user.getId()),eq(7L));
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(user.getId()),eq(8L));
        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(user.getId()),eq(9L));

        doReturn(false).when(heartCheckService).isUserAcquirableHeart(eq(user.getId()),eq(7L));
        doReturn(true).when(heartCheckService).isUserAcquirableHeart(eq(user.getId()),eq(9L));

        doReturn(mock(List.class)).when(heartCheckService).getSpecialHeartAcqCondition(eq(user.getId()),eq(7L));
        doReturn(mock(List.class)).when(heartCheckService).getSpecialHeartAcqCondition(eq(user.getId()),eq(9L));

        // when
        HeartDetailData defaultDetail = (HeartDetailData) heartFacade.findHeartDetail(user,1L);
        HeartDetailData notAcqDetail = (HeartDetailData) heartFacade.findHeartDetail(user,7L);
        HeartDetailData acqDetail = (HeartDetailData) heartFacade.findHeartDetail(user,8L);
        HeartDetailData acquirableDetail = (HeartDetailData) heartFacade.findHeartDetail(user,9L);

        // then
        assertThat(defaultDetail.getHeartId()).isEqualTo(1L);
        assertThat(defaultDetail.getIsAcq()).isNull();
        assertThat(defaultDetail.getIsLocked()).isFalse();
        assertThat(defaultDetail.getConditions()).isNull();

        assertThat(notAcqDetail.getHeartId()).isEqualTo(7L);
        assertThat(notAcqDetail.getIsAcq()).isNull();
        assertThat(notAcqDetail.getIsLocked()).isTrue();
        assertThat(notAcqDetail.getConditions()).isNotNull();

        assertThat(acqDetail.getHeartId()).isEqualTo(8L);
        assertThat(acqDetail.getIsAcq()).isNull();
        assertThat(acqDetail.getIsLocked()).isFalse();
        assertThat(acqDetail.getConditions()).isNull();

        assertThat(acquirableDetail.getHeartId()).isEqualTo(9L);
        assertThat(acquirableDetail.getIsAcq()).isTrue();
        assertThat(acquirableDetail.getIsLocked()).isTrue();
        assertThat(acquirableDetail.getConditions()).isNotNull();
    }

    @Test
    @DisplayName("하트 상세 조회 - 비로그인 유저")
    void findHeartDetailForNoLogin() {
        // given
        User user = createUser("id");

        doReturn(like).when(heartService).findById(eq(1L));
        doReturn(rainbow).when(heartService).findById(eq(7L));

        // when
        HeartDetailData defaultDetail = (HeartDetailData) heartFacade.findHeartDetail(null,1L);
        HeartDetailData specialDetail = (HeartDetailData) heartFacade.findHeartDetail(null,7L);

        // then
        assertThat(defaultDetail.getHeartId()).isEqualTo(1L);
        assertThat(defaultDetail.getIsAcq()).isNull();
        assertThat(defaultDetail.getIsLocked()).isFalse();
        assertThat(defaultDetail.getConditions()).isNull();

        assertThat(specialDetail.getHeartId()).isEqualTo(7L);
        assertThat(specialDetail.getIsAcq()).isNull();
        assertThat(specialDetail.getIsLocked()).isTrue();
        assertThat(specialDetail.getConditions()).isNull();
    }

    @Test
    @DisplayName("스페셜 하트 획득")
    void saveUserHearts() {
        // given
        User user = createUser("id");
        doReturn(user).when(userService).findById(eq(user.getId()));
        doReturn(rainbow).when(heartService).findById(eq(7L));
        doReturn(mincho).when(heartService).findById(eq(8L));
        doReturn(Optional.empty()).when(userHeartService).findByHeartIdAndUserId(eq(7L),eq(user.getId()));
        doReturn(Optional.of(UserHeart.class)).when(userHeartService).findByHeartIdAndUserId(eq(8L),eq(user.getId()));
        doReturn(mock(UserHeart.class)).when(userHeartService).save(any(UserHeart.class));

        // when
        heartFacade.saveUserHearts(user.getId(),7L);
        heartFacade.saveUserHearts(user.getId(),8L);

        // then
        verify(userService, times(2)).findById(eq(user.getId()));
        verify(heartService, times(2)).findById(anyLong());
        verify(userHeartService, times(2)).findByHeartIdAndUserId(anyLong(),anyString());
        verify(userHeartService, times(1)).save(any(UserHeart.class));
    }
}