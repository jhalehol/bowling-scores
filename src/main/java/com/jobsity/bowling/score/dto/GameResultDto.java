package com.jobsity.bowling.score.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameResultDto {

    private List<PlayerScoresDto> gameScores;
}
