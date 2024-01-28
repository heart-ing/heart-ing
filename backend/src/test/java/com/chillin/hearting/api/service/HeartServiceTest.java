package com.chillin.hearting.api.service;

import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.repository.HeartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HeartServiceTest extends AbstractTestData {

    @InjectMocks
    private HeartService heartService;

    @Mock
    private HeartRepository heartRepository;

    @Test
    @DisplayName("모든 타입 하트 조회")
    void findAll() {
        // given
        doReturn(heartList).when(heartRepository).findAll();

        // when
        List<Heart> findHeartList = heartService.findAll();

        // then
        assertThat(findHeartList).filteredOn("type",HeartType.DEFAULT.name()).containsOnly(like,friendship,cheer,flutter,love);
        assertThat(findHeartList).filteredOn("type",HeartType.SPECIAL.name()).containsOnly(rainbow,mincho,sunny,readingGlasses,iceCream,shamrock,fourLeaf,noir);
        assertThat(findHeartList).filteredOn("type",HeartType.EVENT.name()).containsOnly(planet,carnation);
    }

    @Test
    @DisplayName("모든 기본 타입 하트 조회")
    void findDefaultTypeHearts() {
        // given
        doReturn(defaultHeartList).when(heartRepository).findAllByType(HeartType.DEFAULT.name());

        // when
        List<Heart> findHeartList = heartService.findDefaultTypeHearts();

        // then
        assertThat(findHeartList).containsOnly(like,friendship,cheer,flutter,love);
    }

    @Test
    @DisplayName("모든 스페셜 타입 하트 조회")
    void findSpecialTypeHearts() {
        // given
        doReturn(specialHeartList).when(heartRepository).findAllByType(HeartType.SPECIAL.name());

        // when
        List<Heart> findHeartList = heartService.findSpecialTypeHearts();

        // then
        assertThat(findHeartList).containsOnly(rainbow,mincho,sunny,readingGlasses,iceCream,shamrock,fourLeaf,noir);
    }

    @Test
    @DisplayName("하트 아이디로 조회")
    void findById() {
        // given
        doReturn(Optional.of(like)).when(heartRepository).findById(anyLong());

        // when
        Heart findHeart = heartService.findById(1L);

        // then
        assertThat(findHeart).isEqualTo(like);
    }
}