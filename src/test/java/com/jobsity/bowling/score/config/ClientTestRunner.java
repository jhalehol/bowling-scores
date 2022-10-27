package com.jobsity.bowling.score.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientTestRunner implements ApplicationRunner {

    public ClientTestRunner() {
        log.info("Started application for integration test!");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // For integration test purposes
    }
}
