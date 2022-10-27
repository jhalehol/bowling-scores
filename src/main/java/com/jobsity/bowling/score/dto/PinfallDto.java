package com.jobsity.bowling.score.dto;

import com.jobsity.bowling.score.exception.InvalidDataException;
import lombok.Builder;
import lombok.Data;

@Data
public class PinfallDto {

    public static final String FOUL_SCORE = "F";
    public static final int MINIMUM_SCORE = 0;
    public static final int MAXIMUM_SCORE = 10;
    private String value;
    private int pinfallScore;

    public PinfallDto(String value) throws InvalidDataException {
        this.value = value;
        calculatePinfall();
    }

    public boolean isFault() {
        return FOUL_SCORE.equals(value);
    }

    private void calculatePinfall() throws InvalidDataException {
        if (!isFault()) {
            try {
                final int intValue = Integer.parseInt(value);
                if (intValue >= MINIMUM_SCORE && intValue <= MAXIMUM_SCORE) {
                    pinfallScore = intValue;
                } else {
                    throw new InvalidDataException(
                            String.format("Provided score %s is not in the expected range", value));
                }
            } catch (NumberFormatException e) {
                throw new InvalidDataException(String.format("Value %s is not a valid score", value));
            }
        }
    }
}
