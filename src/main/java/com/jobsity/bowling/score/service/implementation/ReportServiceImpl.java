package com.jobsity.bowling.score.service.implementation;

import com.jobsity.bowling.score.dto.FrameScoreDto;
import com.jobsity.bowling.score.dto.GameResultDto;
import com.jobsity.bowling.score.dto.PinfallDto;
import com.jobsity.bowling.score.dto.PlayerScoresDto;
import com.jobsity.bowling.score.exception.ForbiddenException;
import com.jobsity.bowling.score.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private static final String REPORT_HEADER = "Frame\t\t1\t\t2\t\t3\t\t4\t\t5\t\t6\t\t7\t\t8\t\t9\t\t10\n";
    private static final String PINFALLS_TITLE = "Pinfalls\t";
    private static final String SCORE_TITLE = "Score\t";
    private static final String STRIKE_SCORE_TEMPLATE = "\tX\t";
    private static final String SPARE_SCORE_TEMPLATE = "%s\t/\t";
    private static final String NORMAL_SCORE_TEMPLATE = "%s\t%s\t";
    private static final String LAST_SCORE_TEMPLATE = "%s\t%s\t%s";
    private static final String SCORE_TEMPLATE = "\t%s\t";

    public ReportServiceImpl() {
    }

    @Override
    public String buildGameResultsReport(GameResultDto gameResult) {
        return buildScoresPrintReport(gameResult);
    }

    private String buildScoresPrintReport(final GameResultDto gameResult) {
        final StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append(REPORT_HEADER);
        gameResult.getGameScores().forEach(result -> {
            try {
                final String detailReport = buildDetailFrameReport(result);
                reportBuilder.append(String.format("%s\n", result.getPlayer().getName()));
                reportBuilder.append(detailReport);
            } catch (ForbiddenException e) {
                reportBuilder.append(String.format("Unable to build report for player %s",
                        result.getPlayer().getName()));
            }
        });

        return reportBuilder.toString();
    }

    private String buildDetailFrameReport(final PlayerScoresDto playerScores) throws ForbiddenException {
        final StringBuilder detailedReport = new StringBuilder();
        final StringBuilder scoreReport = new StringBuilder();
        detailedReport.append(PINFALLS_TITLE);
        scoreReport.append(SCORE_TITLE);

        for (FrameScoreDto frameScore : playerScores.getFrameScores()) {
            scoreReport.append(String.format(SCORE_TEMPLATE, frameScore.getScore()));

            if (frameScore.getFrame() == FrameScoreDto.MAX_FRAMES) {
                detailedReport.append(String.format(LAST_SCORE_TEMPLATE,
                        getFormattedValue(frameScore.getFirstPinfall()),
                        getFormattedValue(frameScore.getSecondPinfall()),
                        getFormattedValue(frameScore.getThirdPinfall())));
            } else {
                if (frameScore.isStrike()) {
                    detailedReport.append(STRIKE_SCORE_TEMPLATE);
                } else if (frameScore.isSpare()) {
                    detailedReport.append(String.format(SPARE_SCORE_TEMPLATE,
                            frameScore.getFirstPinfall().getValue()));
                } else {
                    detailedReport.append(String.format(NORMAL_SCORE_TEMPLATE,
                            frameScore.getFirstPinfall().getValue(),
                            frameScore.getSecondPinfall().getValue()));
                }
            }
        }

        return detailedReport
                .append("\n").append(scoreReport)
                .append("\n").toString();

    }

    private String getFormattedValue(final PinfallDto pinfall) {
        return pinfall.getPinfallScore() == PinfallDto.MAXIMUM_SCORE ? "X" : pinfall.getValue();
    }
}
