package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.Emoji;
import com.chillin.hearting.db.repository.EmojiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmojiServiceTest {

    @InjectMocks
    private EmojiService emojiService;

    @Mock
    private EmojiRepository emojiRepository;

    @Test
    @DisplayName("Id로 이모지 조회")
    void findById() {
        // given
        Long emojiId = 1L;
        Emoji emoji = Emoji.builder().id(emojiId).name("name").imageUrl("url").build();
        doReturn(Optional.of(emoji)).when(emojiRepository).findById(eq(emojiId));

        // when
        Emoji result = emojiService.findById(emojiId);

        // then
        assertThat(result.getId()).isEqualTo(emojiId);
    }
}