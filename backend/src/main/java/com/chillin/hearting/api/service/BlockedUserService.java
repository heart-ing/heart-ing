package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.BlockedUser;
import com.chillin.hearting.db.repository.BlockedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockedUserService {

    private final BlockedUserRepository blockedUserRepository;

    public BlockedUser save(BlockedUser blockedUser) {
        return blockedUserRepository.save(blockedUser);
    }
}
