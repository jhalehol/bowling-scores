package com.jobsity.bowling.score.dto;

import com.jobsity.bowling.score.exception.ForbiddenException;
import com.jobsity.bowling.score.exception.InvalidDataException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.jobsity.bowling.score.dto.PinfallDto.MAXIMUM_SCORE;
import static com.jobsity.bowling.score.dto.PinfallDto.MINIMUM_SCORE;

@Data
@EqualsAndHashCode(exclude = {"pinfalls", "score"})
public class FrameScoreDto {

    public static final int MAX_FRAMES = 10;
    private static final int STRIKE_SHOTS = 1;
    private static final int SPARE_SHOTS = 2;

    private PlayerDto player;
    private int frame;
    private List<PinfallDto> pinfalls;
    private int score;

    public void setFrame(int frame) throws InvalidDataException {
        if (frame > MAX_FRAMES) {
            throw new InvalidDataException("Frame %s is not allowed");
        }

        this.frame = frame;
    }

    public void addPinfall(final PinfallDto pinfall) throws ForbiddenException {
        if (pinfalls == null) {
            pinfalls = new ArrayList<>();
        }

        if (frame < MAX_FRAMES) {
            int totalPoints = getFramePoints() + pinfall.getPinfallScore();
            if (totalPoints > MAXIMUM_SCORE) {
                throw new ForbiddenException(String.format("Total score %s is invalid in frame %s for player %s",
                        totalPoints, frame, player.getName()));
            }

            if (pinfalls.size() >= 2) {
                throw new ForbiddenException(String.format("Exceeded maximum tries in a frame for player %s",
                        player.getName()));
            }
        } else {
            if (pinfalls.size() >= 3) {
                throw new ForbiddenException(String.format("Exceeded maximum tries in last frame for player %s",
                        player.getName()));
            }
        }

        pinfalls.add(pinfall);
    }

    public boolean isSpare() {
        if (CollectionUtils.isEmpty(pinfalls)) {
            return false;
        }

        return pinfalls.size() == SPARE_SHOTS
                && getFramePoints() == MAXIMUM_SCORE;
    }

    public boolean isStrike() {
        if (CollectionUtils.isEmpty(pinfalls)) {
            return false;
        }

        return (pinfalls.size() == STRIKE_SHOTS && getFramePoints() == MAXIMUM_SCORE)
                || (frame == MAX_FRAMES && getFirstPinfallPoints() == MAXIMUM_SCORE) ;
    }

    public int getFramePoints() {
        if (CollectionUtils.isEmpty(pinfalls)) {
            return 0;
        }

        return pinfalls.stream()
                .mapToInt(PinfallDto::getPinfallScore)
                .sum();
    }

    public PinfallDto getFirstPinfall() throws ForbiddenException {
        return getPinfall(0);
    }

    public PinfallDto getSecondPinfall() throws ForbiddenException {
        return getPinfall(1);
    }

    public PinfallDto getThirdPinfall() throws ForbiddenException {
        return getPinfall(2);
    }

    public int getFirstPinfallPoints() {
        return getPinfallPoints(0);
    }

    public int getSecondPinfallPoints() {
        return getPinfallPoints(1);
    }

    private PinfallDto getPinfall(final int index) throws ForbiddenException {
        if (!CollectionUtils.isEmpty(pinfalls) && index < pinfalls.size()) {
            return pinfalls.get(index);
        }

        throw new ForbiddenException("Invalid pinfall requested");
    }

    private int getPinfallPoints(final int index) {
        if (!CollectionUtils.isEmpty(pinfalls) && index < pinfalls.size()) {
            return pinfalls.get(index).getPinfallScore();
        }

        return MINIMUM_SCORE;
    }
}
