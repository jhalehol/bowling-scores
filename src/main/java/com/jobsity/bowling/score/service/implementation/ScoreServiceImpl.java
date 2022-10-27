package com.jobsity.bowling.score.service.implementation;

import com.jobsity.bowling.score.dto.FrameScoreDto;
import com.jobsity.bowling.score.dto.GameResultDto;
import com.jobsity.bowling.score.dto.PinfallDto;
import com.jobsity.bowling.score.dto.PlayerDto;
import com.jobsity.bowling.score.dto.PlayerScoresDto;
import com.jobsity.bowling.score.dto.PlayerScoreLineDto;
import com.jobsity.bowling.score.exception.ForbiddenException;
import com.jobsity.bowling.score.exception.InvalidDataException;
import com.jobsity.bowling.score.exception.NotFoundException;
import com.jobsity.bowling.score.exception.ProcessException;
import com.jobsity.bowling.score.exception.ProcessRuntimeException;
import com.jobsity.bowling.score.exception.ReadException;
import com.jobsity.bowling.score.service.ReportService;
import com.jobsity.bowling.score.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jobsity.bowling.score.dto.FrameScoreDto.MAX_FRAMES;
import static com.jobsity.bowling.score.dto.PinfallDto.MAXIMUM_SCORE;

@Slf4j
@Service
public class ScoreServiceImpl implements ScoreService {

    private final ReportService reportService;

    public ScoreServiceImpl(ReportService reportService) {
        this.reportService = reportService;
    }

    public GameResultDto calculateGameResult(List<PlayerScoreLineDto> scores) {
        final Map<PlayerDto, Set<FrameScoreDto>> playersScores = extractAndBuildPlayerScores(scores);
        final List<PlayerScoresDto> gameScores = new ArrayList<>();
        playersScores.forEach((player, frameScores) -> {
            calculateFramesScores(frameScores);
            gameScores.add(PlayerScoresDto.builder()
                    .player(player)
                    .frameScores(frameScores)
                    .build());
        });

        return GameResultDto.builder()
                .gameScores(gameScores)
                .build();
    }

    @Override
    public String calculateAndReportScores(String scoresFile) throws ProcessException, NotFoundException {
        try {
            final List<PlayerScoreLineDto> scoreLines = readScores(scoresFile);
            if (scoreLines.size() > 0) {
                final GameResultDto gameResult = calculateGameResult(scoreLines);
                return reportService.buildGameResultsReport(gameResult);
            } else {
                throw new NotFoundException("The source does not contain valid scores");
            }
        } catch (ReadException e) {
            log.debug("Unable to read the score source file", e);
            throw new ProcessException(
                    String.format("Something failed trying to read the source file %s", e.getMessage()));
        }
    }

    private void calculateFramesScores(final Set<FrameScoreDto> framesScores) {
        int accumulatedScore = 0;
        int totalFrameScores = framesScores.size();
        final List<FrameScoreDto> scoresList = new ArrayList<>(framesScores);
        for(int frameIndex = 0; frameIndex < scoresList.size(); frameIndex++) {
            final FrameScoreDto currentFrameScore = scoresList.get(frameIndex);
            final List<FrameScoreDto> nextFrameScores = scoresList
                    .subList(frameIndex + 1, totalFrameScores);
            final int frameScoreCalculated = calculateFramePoints(currentFrameScore, nextFrameScores);
            accumulatedScore += frameScoreCalculated;
            currentFrameScore.setScore(accumulatedScore);
        }
    }

    private int calculateFramePoints(final FrameScoreDto frameScore, final List<FrameScoreDto> nextScores) {
        int framePoints = frameScore.getFramePoints();
        int continuousStrikeCount = 0;
        for (final FrameScoreDto nextFrameScore : nextScores) {
            if (frameScore.isSpare()) {
                framePoints += nextFrameScore.getFirstPinfallPoints();
                break;
            } else if (frameScore.isStrike()) {
                if (nextFrameScore.isStrike()) {
                    framePoints += MAXIMUM_SCORE;
                    continuousStrikeCount++;
                    if (continuousStrikeCount < 2) {
                        if (frameScore.getFrame() == MAX_FRAMES - 1) {
                            framePoints += nextFrameScore.getSecondPinfallPoints() == MAXIMUM_SCORE ?
                                    MAXIMUM_SCORE : nextFrameScore.getSecondPinfallPoints();
                        }
                    } else {
                        break;
                    }
                } else {
                    framePoints += nextFrameScore.getFramePoints();
                    break;
                }
            } else {
                break;
            }
        }

        return framePoints;
    }

    private Map<PlayerDto, Set<FrameScoreDto>> extractAndBuildPlayerScores(List<PlayerScoreLineDto> scores) {
        final Map<PlayerDto, Set<FrameScoreDto>> playerFrameScores = new HashMap<>();
        int frameCounter = 1;
        int frameScore = 0;
        int frameShots = 0;
        PlayerDto initialPlayer = null;
        for (PlayerScoreLineDto scoreLine : scores) {
            try {
                if (initialPlayer == null) {
                    initialPlayer = scoreLine.getPlayer();
                } else {
                    if (frameCounter < MAX_FRAMES
                            && (frameScore >= MAXIMUM_SCORE || frameShots >= 2)) {
                        frameScore = 0;
                        frameShots = 0;

                        if (initialPlayer.equals(scoreLine.getPlayer())) {
                            frameCounter++;
                        }
                    }
                }

                frameScore += scoreLine.getPinfall().getPinfallScore();
                frameShots++;
                final PlayerDto player = scoreLine.getPlayer();
                Set<FrameScoreDto> updatedPlayerFrames =
                        createOrUpdateScoreFrame(playerFrameScores.get(player), scoreLine, frameCounter);
                playerFrameScores.put(player, updatedPlayerFrames);
            } catch (InvalidDataException | ForbiddenException e) {
                log.debug(String.format("Unable to process score %s", scoreLine), e);
            }
        }

        return playerFrameScores;
    }

    private Set<FrameScoreDto> createOrUpdateScoreFrame(Set<FrameScoreDto> playerFrames,
            final PlayerScoreLineDto playerScoreLine, final int frameNumber)
            throws InvalidDataException, ForbiddenException {
        FrameScoreDto playerFrameScore;
        Set<FrameScoreDto> updatedScores;
        if (playerFrames == null) {
            updatedScores = new LinkedHashSet<>();
            playerFrameScore = buildNewFrameScore(playerScoreLine.getPlayer(), frameNumber);
        } else {
            updatedScores = playerFrames;
            playerFrameScore = updatedScores.stream()
                    .filter(frame -> frame.getFrame() == frameNumber)
                    .findFirst().orElse(buildNewFrameScore(playerScoreLine.getPlayer(), frameNumber));
        }

        playerFrameScore.addPinfall(playerScoreLine.getPinfall());
        updatedScores.add(playerFrameScore);
        return updatedScores;
    }

    private FrameScoreDto buildNewFrameScore(final PlayerDto player, final int frameNumber)
            throws InvalidDataException {
        final FrameScoreDto frameScore = new FrameScoreDto();
        frameScore.setPlayer(player);
        frameScore.setFrame(frameNumber);
        return frameScore;
    }

    private List<PlayerScoreLineDto> readScores(final String sourceFile) throws ReadException {
        final Path path = Paths.get(sourceFile);
        try (Stream<String> fileStream = Files.lines(path)) {
            return fileStream.map(line -> {
                        try {
                            return convertScoreLine(line);
                        } catch (InvalidDataException e) {
                            throw new ProcessRuntimeException(e.getMessage());
                        }
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ReadException("Unable to read scores file", e);
        }
    }

    private Optional<PlayerScoreLineDto> convertScoreLine(final String line) throws InvalidDataException {
        if (line != null) {
            final List<String> lineData = Arrays.asList(line.split("\\s+"));
            if (lineData.size() == 2) {
                final String name = lineData.get(0);
                if (StringUtils.isNotEmpty(name)) {
                    final PinfallDto pinfallData = new PinfallDto(lineData.get(1));
                    return Optional.of(PlayerScoreLineDto.builder()
                            .player(new PlayerDto(name))
                            .pinfall(pinfallData)
                            .build());
                } else {
                    throw new InvalidDataException("Player name not provided in the scores");
                }
            }
        }

        return Optional.empty();
    }
}
