package com.chillin.hearting.api.service;

import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.repository.*;
import com.chillin.hearting.exception.HeartNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class HeartService {

    private final HeartRepository heartRepository;

    @Transactional(readOnly = true)
    public List<Heart> findDefaultTypeHearts() {
        return heartRepository.findAllByType(HeartType.DEFAULT.name());
    }

    @Transactional(readOnly = true)
    public List<Heart> findSpecialTypeHearts() {
        return heartRepository.findAllByType(HeartType.SPECIAL.name());
    }

    @Transactional(readOnly = true)
    public Heart findById(Long id) {
        return heartRepository.findById(id).orElseThrow(HeartNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Heart> findAll() {
        return heartRepository.findAll();
    }

}
