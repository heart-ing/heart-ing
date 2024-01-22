package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.Emoji;
import com.chillin.hearting.db.repository.EmojiRepository;
import com.chillin.hearting.exception.EmojiNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmojiService {

    private final EmojiRepository emojiRepository;

    public Emoji findById(long emojiId) {
        return emojiRepository.findById(emojiId).orElseThrow(EmojiNotFoundException::new);
    }
}
