package com.jobsity.bowling.score.service.implementation;

import com.jobsity.bowling.score.dto.FrameScoreDto;
import com.jobsity.bowling.score.dto.GameResultDto;
import com.jobsity.bowling.score.dto.PinfallDto;
import com.jobsity.bowling.score.dto.PlayerDto;
import com.jobsity.bowling.score.dto.PlayerScoresDto;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceImplTest {

    @Rule
    public final ErrorCollector errorCollector = new ErrorCollector();

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    public void givenGameResultWhenBuildReportThenReturnReport() throws Exception {
        // Arrange
        final GameResultDto gameResult = buildGameResult();

        // Act
        final String report = reportService.buildGameResultsReport(gameResult);

        // Assert
        errorCollector.checkThat(report,
                equalTo("Frame\t\t1\t\t2\t\t3\t\t4\t\t5\t\t6\t\t7\t\t8\t\t9\t\t10\nPeter\nPinfalls\t\tX\t\tX\t\nScore\t\t0\t\t20\t\n"));
    }

    private GameResultDto buildGameResult() throws Exception {
        final PlayerDto player = PlayerDto.builder()
                .name("Peter")
                .build();
        final FrameScoreDto frameScoreDto = new FrameScoreDto();
        frameScoreDto.setFrame(1);
        frameScoreDto.setPlayer(player);
        frameScoreDto.setScore(20);
        final PinfallDto pinfall = new PinfallDto("10");
        frameScoreDto.setPinfalls(Collections.singletonList(pinfall));
        final Set<FrameScoreDto> frameScores = new HashSet<>();
        frameScores.add(frameScoreDto);
        final FrameScoreDto frameScoreDto2 = new FrameScoreDto();
        frameScoreDto2.setFrame(2);
        frameScoreDto2.setPlayer(player);
        final PinfallDto pinfall2 = new PinfallDto("10");
        frameScoreDto2.setPinfalls(Collections.singletonList(pinfall2));
        frameScores.add(frameScoreDto2);
        final List<PlayerScoresDto> playerScores = Collections.singletonList(
                PlayerScoresDto.builder()
                        .frameScores(frameScores)
                        .player(player)
                        .build());
        return GameResultDto.builder()
                .gameScores(playerScores)
                .build();
    }
}
