package com.jobsity.bowling.score.service.implementation;

import com.jobsity.bowling.score.dto.FrameScoreDto;
import com.jobsity.bowling.score.dto.GameResultDto;
import com.jobsity.bowling.score.dto.PinfallDto;
import com.jobsity.bowling.score.dto.PlayerDto;
import com.jobsity.bowling.score.dto.PlayerScoreLineDto;
import com.jobsity.bowling.score.dto.PlayerScoresDto;
import com.jobsity.bowling.score.helper.ResourceHelper;
import com.jobsity.bowling.score.service.ReportService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.jobsity.bowling.score.integration.ShellIntegrationTest.POSITIVE_SCORES_FILE;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScoreServiceImplTest {

    private static final String PLAYER_NAME = "Peter";
    private static final String REPORT_RESULT = "Report";

    private static final int FOURTH_FRAME = 4;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    @Mock
    private ReportService reportService;
    private ResourceHelper resourceHelper;
    @InjectMocks
    private ScoreServiceImpl scoreService;

    @Before
    public void setup() {
        resourceHelper = new ResourceHelper();
    }

    @Test
    public void givenValidScoresWhenCalculateGameResultThenShouldCalculateSuccessfully() throws Exception {
        // Arrange
        final List<PlayerScoreLineDto> scores = buildValidScores();

        // Act
        final GameResultDto gameResult = scoreService.calculateGameResult(scores);

        // Assert
        final List<PlayerScoresDto> playersScores = gameResult.getGameScores();
        errorCollector.checkThat(playersScores.size(), equalTo(1));
        final Set<FrameScoreDto> playerFrameScores = playersScores.get(0).getFrameScores();
        errorCollector.checkThat(playerFrameScores.size(), equalTo(10));
        final FrameScoreDto frameData = playerFrameScores.stream()
                .filter(frame -> frame.getFrame() == 6)
                .findFirst().orElseGet(FrameScoreDto::new);
        errorCollector.checkThat(frameData.getScore(), equalTo(84));
        errorCollector.checkThat(frameData.getFramePoints(), equalTo(10));
        errorCollector.checkThat(frameData.getFirstPinfallPoints(), equalTo(8));
        errorCollector.checkThat(frameData.getSecondPinfallPoints(), equalTo(2));
    }

    @Test
    public void givenValidScoresWhenCalculateAndReportGameResultThenShouldCallReport() throws Exception {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(POSITIVE_SCORES_FILE);
        when(reportService.buildGameResultsReport(any(GameResultDto.class))).thenReturn(REPORT_RESULT);

        // Act
        final String gameResult = scoreService.calculateAndReportScores(scoresFile);

        // Assert
        errorCollector.checkThat(gameResult, equalTo(REPORT_RESULT));
        verify(reportService).buildGameResultsReport(any(GameResultDto.class));
    }

    private List<PlayerScoreLineDto> buildValidScores() throws Exception {
        final List<PlayerScoreLineDto> scores = new ArrayList<>();
        scores.add(buildScore("10"));
        scores.add(buildScore("7"));
        scores.add(buildScore("3"));
        scores.add(buildScore("9"));
        scores.add(buildScore("0"));
        scores.add(buildScore("10"));
        scores.add(buildScore("0"));
        scores.add(buildScore("8"));
        scores.add(buildScore("8"));
        scores.add(buildScore("2"));
        scores.add(buildScore("F"));
        scores.add(buildScore("6"));
        scores.add(buildScore("10"));
        scores.add(buildScore("10"));
        scores.add(buildScore("10"));
        scores.add(buildScore("8"));
        scores.add(buildScore("1"));
        return scores;
    }

    private PlayerScoreLineDto buildScore(final String score) throws Exception {
        return PlayerScoreLineDto.builder()
                .player(new PlayerDto(PLAYER_NAME))
                .pinfall(new PinfallDto(score))
                .build();
    }

}
