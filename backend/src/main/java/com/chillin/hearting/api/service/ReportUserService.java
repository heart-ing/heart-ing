package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.Report;
import com.chillin.hearting.db.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportUserService {

    private final ReportRepository reportRepository;

    public Report save(Report report) {
        return reportRepository.save(report);
    }
}
