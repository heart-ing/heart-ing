package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.AbstractTestData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HeartCheckStrategyTest extends AbstractTestData {

    @InjectMocks
    PlanetHeartCheckStrategy planetHeartCheckStrategy;

    @InjectMocks
    RainbowHeartCheckStrategy rainbowHeartCheckStrategy;

    @InjectMocks
    MinchoHeartCheckStrategy minchoHeartCheckStrategy;

    @InjectMocks
    SunnyHeartCheckStrategy sunnyHeartCheckStrategy;

    @InjectMocks
    ReadingGlassesHeartCheckStrategy readingGlassesHeartCheckStrategy;

    @InjectMocks
    IceCreamHeartCheckStrategy iceCreamHeartCheckStrategy;

    @InjectMocks
    ShamrockHeartCheckStrategy shamrockHeartCheckStrategy;

    @InjectMocks
    FourLeafHeartCheckStrategy fourLeafHeartCheckStrategy;

    @InjectMocks
    NoirHeartCheckStrategy noirHeartCheckStrategy;

    @InjectMocks
    CarnationHeartCheckStrategy carnationHeartCheckStrategy;

    @Mock
    private HeartService heartService;
    @Mock
    private MessageService messageService;

    @Test
    @DisplayName("행성 하트 획득 불가능")
    void isUserAcquirableHeartForPlanetFalse() {
        // given
        String userId = "userId";

        // when
        boolean result = planetHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("행성 하트 획득 조건 현황")
    void getAcqConditionForPlanet() {
        // given
        String userId = "userId";

        // when
        List<HeartConditionData> result = planetHeartCheckStrategy.getAcqCondition(userId);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("무지개 하트 획득 가능")
    void isUserAcquirableHeartForRainbowTrue() {
        // given
        String userId = "userId";
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(1L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(2L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(3L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(4L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(5L));

        // when
        boolean result = rainbowHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("무지개 하트 획득 불가")
    void isUserAcquirableHeartForRainbowFalse() {
        // given
        String userId = "userId";
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(1L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(2L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(3L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(4L));
        doReturn(0).when(messageService).getUserSentHeartCnt(eq(userId), eq(5L));

        // when
        boolean result = rainbowHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("무지개 하트 획득 조건 현황")
    void getAcqConditionForRainbow() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(1L,1);
        map.put(2L,1);
        map.put(3L,1);
        map.put(4L,1);
        map.put(5L,0);
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();
        doReturn(map.get(1L)).when(messageService).getUserSentHeartCnt(eq(userId), eq(1L));
        doReturn(map.get(2L)).when(messageService).getUserSentHeartCnt(eq(userId), eq(2L));
        doReturn(map.get(3L)).when(messageService).getUserSentHeartCnt(eq(userId), eq(3L));
        doReturn(map.get(4L)).when(messageService).getUserSentHeartCnt(eq(userId), eq(4L));
        doReturn(map.get(5L)).when(messageService).getUserSentHeartCnt(eq(userId), eq(5L));
        doReturn(like).when(heartService).findById(eq(1L));
        doReturn(cheer).when(heartService).findById(eq(2L));
        doReturn(friendship).when(heartService).findById(eq(3L));
        doReturn(flutter).when(heartService).findById(eq(4L));
        doReturn(love).when(heartService).findById(eq(5L));

        // when
        List<HeartConditionData> result = rainbowHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 1 &&
                    d.getHeartUrl().equals(rainbow.getImageUrl()) &&
                    d.getName().equals(rainbow.getName()));
        }
    }

    @Test
    @DisplayName("민초 하트 획득 가능")
    void isUserAcquirableHeartForMinchoTrue() {
        // given
        String userId = "userId";
        doReturn(5).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.BLUE.getId()));

        // when
        boolean result = minchoHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("민초 하트 획득 불가")
    void isUserAcquirableHeartForMinchoFalse() {
        // given
        String userId = "userId";
        doReturn(4).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.BLUE.getId()));

        // when
        boolean result = minchoHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("민초 하트 획득 조건 현황")
    void getAcqConditionForMincho() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(HeartInfo.BLUE.getId(), 4);
        doReturn(cheer).when(heartService).findById(eq(HeartInfo.BLUE.getId()));
        doReturn(map.get(HeartInfo.BLUE.getId())).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.BLUE.getId()));

        // when
        List<HeartConditionData> result = minchoHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 5 &&
                    d.getHeartUrl().equals(mincho.getImageUrl()) &&
                    d.getName().equals(mincho.getName()));
        }
    }

    @Test
    @DisplayName("햇살 하트 획득 가능")
    void isUserAcquirableHeartForSunnyTrue() {
        // given
        String userId = "userId";
        doReturn(5).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.YELLOW.getId()));

        // when
        boolean result = sunnyHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("햇살 하트 획득 불가")
    void isUserAcquirableHeartForSunnyFalse() {
        // given
        String userId = "userId";
        doReturn(4).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.YELLOW.getId()));

        // when
        boolean result = sunnyHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("햇살 하트 획득 조건 현황")
    void getAcqConditionForSunny() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(HeartInfo.YELLOW.getId(), 4);
        doReturn(like).when(heartService).findById(eq(HeartInfo.YELLOW.getId()));
        doReturn(map.get(HeartInfo.YELLOW.getId())).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.YELLOW.getId()));

        // when
        List<HeartConditionData> result = sunnyHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 5 &&
                    d.getHeartUrl().equals(sunny.getImageUrl()) &&
                    d.getName().equals(sunny.getName()));
        }
    }

    @Test
    @DisplayName("돋보기 하트 획득 가능")
    void isUserAcquirableHeartForReadingGlassesTrue() {
        // given
        String userId = "userId";
        doReturn(3).when(messageService).findMaxMessageCountToSameUser(eq(userId), eq(HeartInfo.PINK.getId()));

        // when
        boolean result = readingGlassesHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("돋보기 하트 획득 불가")
    void isUserAcquirableHeartForReadingGlassesFalse() {
        // given
        String userId = "userId";
        doReturn(null).when(messageService).findMaxMessageCountToSameUser(eq(userId), eq(HeartInfo.PINK.getId()));

        // when
        boolean result = readingGlassesHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("돋보기 하트 획득 조건 현황")
    void getAcqConditionForReadingGlasses() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(HeartInfo.PINK.getId(), 4);
        doReturn(flutter).when(heartService).findById(eq(HeartInfo.PINK.getId()));
        doReturn(map.get(HeartInfo.PINK.getId())).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.PINK.getId()));

        // when
        List<HeartConditionData> result = readingGlassesHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 3 &&
                    d.getHeartUrl().equals(readingGlasses.getImageUrl()) &&
                    d.getName().equals(readingGlasses.getName()));
        }
    }

    @Test
    @DisplayName("아이스크림 하트 획득 가능")
    void isUserAcquirableHeartForIceCreamTrue() {
        // given
        String userId = "userId";
        doReturn(3).when(messageService).getUserReceivedHeartCnt(eq(userId), eq(HeartInfo.SUNNY.getId()));

        // when
        boolean result = iceCreamHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("아이스크림 하트 획득 불가")
    void isUserAcquirableHeartForIceCreamFalse() {
        // given
        String userId = "userId";
        doReturn(2).when(messageService).getUserReceivedHeartCnt(eq(userId), eq(HeartInfo.SUNNY.getId()));

        // when
        boolean result = iceCreamHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("아이스크림 하트 획득 조건 현황")
    void getAcqConditionForIceCream() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(HeartInfo.SUNNY.getId(), 4);
        doReturn(sunny).when(heartService).findById(eq(HeartInfo.SUNNY.getId()));
        doReturn(map.get(HeartInfo.SUNNY.getId())).when(messageService).getUserReceivedHeartCnt(eq(userId), eq(HeartInfo.SUNNY.getId()));

        // when
        List<HeartConditionData> result = iceCreamHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 3 &&
                    d.getHeartUrl().equals(iceCream.getImageUrl()) &&
                    d.getName().equals(iceCream.getName()));
        }
    }

    @Test
    @DisplayName("세잎클로버 하트 획득 가능")
    void isUserAcquirableHeartForShamrockTrue() {
        // given
        String userId = "userId";
        doReturn(3).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.GREEN.getId()));

        // when
        boolean result = shamrockHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("세잎클로버 하트 획득 불가")
    void isUserAcquirableHeartForShamrockFalse() {
        // given
        String userId = "userId";
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.GREEN.getId()));

        // when
        boolean result = shamrockHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("세잎클로버 하트 획득 조건 현황")
    void getAcqConditionForShamrock() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(HeartInfo.GREEN.getId(), 4);
        doReturn(friendship).when(heartService).findById(eq(HeartInfo.GREEN.getId()));
        doReturn(map.get(HeartInfo.GREEN.getId())).when(messageService).getUserSentHeartCnt(eq(userId), eq(HeartInfo.GREEN.getId()));

        // when
        List<HeartConditionData> result = shamrockHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 3 &&
                    d.getHeartUrl().equals(shamrock.getImageUrl()) &&
                    d.getName().equals(shamrock.getName()));
        }
    }

    @Test
    @DisplayName("네잎클로버 하트 획득 가능")
    void isUserAcquirableHeartForFourLeafTrue() {
        // given
        String userId = "userId";
        doReturn(4).when(messageService).getUserReceivedHeartCnt(eq(userId), eq(HeartInfo.SHAMROCK.getId()));

        // when
        boolean result = fourLeafHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("네잎클로버 하트 획득 불가")
    void isUserAcquirableHeartForFourLeafFalse() {
        // given
        String userId = "userId";
        doReturn(3).when(messageService).getUserReceivedHeartCnt(eq(userId), eq(HeartInfo.SHAMROCK.getId()));

        // when
        boolean result = fourLeafHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("네잎클로버 하트 획득 조건 현황")
    void getAcqConditionForFourLeaf() {
        // given
        String userId = "userId";
        HashMap<Long,Integer> map = new HashMap<>();
        map.put(HeartInfo.SHAMROCK.getId(), 4);
        doReturn(shamrock).when(heartService).findById(eq(HeartInfo.SHAMROCK.getId()));
        doReturn(map.get(HeartInfo.SHAMROCK.getId())).when(messageService).getUserReceivedHeartCnt(eq(userId), eq(HeartInfo.SHAMROCK.getId()));

        // when
        List<HeartConditionData> result = fourLeafHeartCheckStrategy.getAcqCondition(userId);

        // then
        for (HeartConditionData data : result) {
            assertThat(data).extracting(d -> d.getCurrentValue() == map.get(d.getHeartId()) &&
                    d.getMaxValue() == 4 &&
                    d.getHeartUrl().equals(fourLeaf.getImageUrl()) &&
                    d.getName().equals(fourLeaf.getName()));
        }
    }

    @Test
    @DisplayName("누아르 하트 획득 가능")
    void isUserAcquirableHeartForNoirTrue() {
        // given
        String userId = "userId";
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(1L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(2L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(3L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(4L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(5L));
        // when
        boolean result = noirHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("누아르 하트 획득 불가")
    void isUserAcquirableHeartForNoirFalse() {
        // given
        String userId = "userId";
        doReturn(defaultHeartList).when(heartService).findDefaultTypeHearts();
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(1L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(2L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(3L));
        doReturn(2).when(messageService).getUserSentHeartCnt(eq(userId), eq(4L));
        doReturn(1).when(messageService).getUserSentHeartCnt(eq(userId), eq(5L));
        // when
        boolean result = noirHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("누아르 하트 획득 조건 현황")
    void getAcqConditionForNoir() {
        // given
        String userId = "userId";

        // when
        List<HeartConditionData> result = noirHeartCheckStrategy.getAcqCondition(userId);

        // then
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("카네이션 하트 획득 불가능")
    void isUserAcquirableHeartForCarnationFalse() {
        // given
        String userId = "userId";

        // when
        boolean result = carnationHeartCheckStrategy.isAcquirable(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("카네이션 하트 획득 조건 현황")
    void getAcqConditionForCarnation() {
        // given
        String userId = "userId";

        // when
        List<HeartConditionData> result = carnationHeartCheckStrategy.getAcqCondition(userId);

        // then
        assertThat(result).isNull();
    }
}