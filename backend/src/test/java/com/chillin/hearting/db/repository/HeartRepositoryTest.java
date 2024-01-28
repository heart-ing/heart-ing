package com.chillin.hearting.db.repository;

import com.chillin.hearting.db.domain.Heart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class HeartRepositoryTest {

    @Autowired
    private HeartRepository heartRepository;

    @Test
    @DisplayName("하트 저장")
    void 하트저장() {
        // given
        Heart heart = createDefaultHeart();

        // when
        Heart savedHeart = heartRepository.save(heart);

        // then
        assertThat(heart.getId()).isEqualTo(savedHeart.getId());
    }

    @Test
    @DisplayName("모든 하트 리스트 조회")
    void 모든하트조회() {
        // given
        List<Heart> heartList = createHeartList();

        // when
        List<Heart> findHeartSize = heartRepository.saveAll(heartList);

        // then
        assertThat(heartList.size()).isEqualTo(findHeartSize.size());
    }

    public Heart createDefaultHeart() {
        Heart heart = Heart.builder()
                .name("호감 하트")
                .imageUrl("test.com")
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type("DEFAULT")
                .build();

        return heart;
    }

    public Heart createDefaultHeart(String name, String imageUrl) {
        Heart heart = Heart.builder()
                .name(name)
                .imageUrl(imageUrl)
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type("DEFAULT")
                .build();

        return heart;
    }

    public List<Heart> createHeartList() {
        List<Heart> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            result.add(createDefaultHeart("name"+i,"url"+i));
        }

        return result;
    }
}
