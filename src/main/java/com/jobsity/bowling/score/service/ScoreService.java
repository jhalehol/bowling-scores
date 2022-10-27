package com.jobsity.bowling.score.service;

import com.jobsity.bowling.score.dto.GameResultDto;
import com.jobsity.bowling.score.dto.PlayerScoreLineDto;
import com.jobsity.bowling.score.exception.NotFoundException;
import com.jobsity.bowling.score.exception.ProcessException;

import java.util.List;

public interface ScoreService {

    /**
     * Builds a game result object with the provided scores of the bowling game
     * @param scores
     * @return
     */
    GameResultDto calculateGameResult(List<PlayerScoreLineDto> scores);

    /**
     * Calculates and report the scores of a bowling game provided in a formated file
     * @param scoresFile
     * @return
     * @throws ProcessException
     */
    String calculateAndReportScores(String scoresFile) throws ProcessException, NotFoundException;
}
