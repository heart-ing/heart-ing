package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.domain.Report;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportUserServiceTest {

    @InjectMocks
    private ReportUserService reportUserService;

    @Mock
    private ReportRepository reportRepository;

    @Test
    @DisplayName("신고 저장")
    void save() {
        // given
        Report report = Report.builder()
                .message(mock(Message.class))
                .reporter(mock(User.class))
                .reportedUser(mock(User.class))
                .content("content")
                .id(1L)
                .build();
        doReturn(report).when(reportRepository).save(report);

        // when
        Report savedReport = reportUserService.save(report);

        // then
        assertThat(savedReport.getId()).isEqualTo(report.getId());
    }
}