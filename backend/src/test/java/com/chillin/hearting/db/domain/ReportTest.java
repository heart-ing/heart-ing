package com.chillin.hearting.db.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTest {

    @Test
    @DisplayName("Test PrePersist")
    void prePersist() {
        // given
        Report report = Report.builder().build();

        // when
        report.prePersist();

        // then
        assertThat(report.getCreatedDate()).isNotNull();
    }
}