package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.exception.NoHeartStrategyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HeartCheckStrategyFactoryTest {

    @InjectMocks
    HeartCheckStrategyFactory heartCheckStrategyFactory;

    @Mock
    private HeartService heartService;

    @Mock
    private MessageService messageService;

    @Test
    @DisplayName("전략 리턴")
    void createHeartCheckStrategy() {
        // when
        HeartCheckStrategy planetStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(6L);
        HeartCheckStrategy rainbowStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(7L);
        HeartCheckStrategy minchoStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(8L);
        HeartCheckStrategy sunnyStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(9L);
        HeartCheckStrategy readingGlassesStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(10L);
        HeartCheckStrategy iceCreamStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(11L);
        HeartCheckStrategy shamrockStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(12L);
        HeartCheckStrategy fourLeafStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(13L);
        HeartCheckStrategy noirStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(14L);
        HeartCheckStrategy carnationStrategy = heartCheckStrategyFactory.createHeartCheckStrategy(15L);

        // then
        assertThat(planetStrategy).isInstanceOf(PlanetHeartCheckStrategy.class);
        assertThat(rainbowStrategy).isInstanceOf(RainbowHeartCheckStrategy.class);
        assertThat(minchoStrategy).isInstanceOf(MinchoHeartCheckStrategy.class);
        assertThat(sunnyStrategy).isInstanceOf(SunnyHeartCheckStrategy.class);
        assertThat(readingGlassesStrategy).isInstanceOf(ReadingGlassesHeartCheckStrategy.class);
        assertThat(iceCreamStrategy).isInstanceOf(IceCreamHeartCheckStrategy.class);
        assertThat(shamrockStrategy).isInstanceOf(ShamrockHeartCheckStrategy.class);
        assertThat(fourLeafStrategy).isInstanceOf(FourLeafHeartCheckStrategy.class);
        assertThat(noirStrategy).isInstanceOf(NoirHeartCheckStrategy.class);
        assertThat(carnationStrategy).isInstanceOf(CarnationHeartCheckStrategy.class);
    }

    @Test
    @DisplayName("전략 실패 - 없는 하트")
    void createHeartCheckStrategyFail() {
        // given
        List<Long> heartIds = List.of(5L,16L);

        // when, then
        for (long heartId : heartIds) {
            assertThrows(NoHeartStrategyException.class, ()-> heartCheckStrategyFactory.createHeartCheckStrategy(heartId));
        }
    }
}