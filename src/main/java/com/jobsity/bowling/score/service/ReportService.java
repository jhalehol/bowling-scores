package com.jobsity.bowling.score.service;

import com.jobsity.bowling.score.dto.GameResultDto;
import com.jobsity.bowling.score.exception.ProcessException;

public interface ReportService {

    /**
     * Builds a report of the game results in bowling scoring format
     * @param gameResult
     * @return
     * @throws ProcessException
     */
    String buildGameResultsReport(GameResultDto gameResult) throws ProcessException;
}
