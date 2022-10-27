package com.jobsity.bowling.score.api;

import com.jobsity.bowling.score.exception.NotFoundException;
import com.jobsity.bowling.score.exception.ProcessException;
import com.jobsity.bowling.score.service.ScoreService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class Commands {

    private final ScoreService scoreService;

    public Commands(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @ShellMethod(value = "Calculate and print scores of the bowling results provided", key = "score")
    public String calculateAndPrintScores(@ShellOption(help = "Path to scores file", defaultValue = "")
    String source) {
        try {
            return scoreService.calculateAndReportScores(source);
        } catch (Exception e) {
            return String.format("Unable to complete the operation <%s>", e.getMessage());
        }
    }
}
