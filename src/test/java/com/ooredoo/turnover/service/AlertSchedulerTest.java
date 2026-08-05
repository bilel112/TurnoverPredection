package com.ooredoo.turnover.service;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.service.impl.AlertScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AlertSchedulerTest {

    @Test
    void shouldNotRunBeforeConfiguredIntervalHasElapsed() {
        AlertScheduler scheduler = new AlertScheduler(
                mock(EmployeeRepository.class),
                null,
                new DynamicScoringProperties(),
                mock(TaskScheduler.class));

        assertFalse(scheduler.shouldEvaluateNow(60_000L, 30_000L, 60_000L));
    }

    @Test
    void shouldRunWhenConfiguredIntervalHasElapsed() {
        AlertScheduler scheduler = new AlertScheduler(
                mock(EmployeeRepository.class),
                null,
                new DynamicScoringProperties(),
                mock(TaskScheduler.class));

        assertTrue(scheduler.shouldEvaluateNow(90_000L, 30_000L, 60_000L));
    }
}
