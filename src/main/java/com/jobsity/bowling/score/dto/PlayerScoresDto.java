package com.jobsity.bowling.score.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PlayerScoresDto {

    private PlayerDto player;
    private Set<FrameScoreDto> frameScores;
}
