package com.jobsity.bowling.score.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerScoreLineDto {

    private PlayerDto player;
    private PinfallDto pinfall;

    @Override
    public String toString() {
        return String.format("[%s - %s]",
                player == null ? "Unknown player" : player.getName(),
                pinfall == null ? "-" : pinfall.getValue());
    }
}
