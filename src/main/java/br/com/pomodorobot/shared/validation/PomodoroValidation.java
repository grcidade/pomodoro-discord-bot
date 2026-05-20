package br.com.pomodorobot.shared.validation;

import br.com.pomodorobot.core.exception.BusinessException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PomodoroValidation {

    private static final int MINUTES_MIN_VALUE = 1;
    private static final int MINUTES_MAX_VALUE = 240;
    private static final int CYCLES_MIN_VALUE = 1;
    private static final int CYCLES_MAX_VALUE = 12;

    public void validateMinutes(String fieldName, int value) {
        if (value < MINUTES_MIN_VALUE || value > MINUTES_MAX_VALUE) {
            throw new BusinessException(fieldName + " must be between 1 and 240 minutes.");
        }
    }

    public void validateCycles(int value) {
        if (value < CYCLES_MIN_VALUE || value > CYCLES_MAX_VALUE) {
            throw new BusinessException("cycles must be between 1 and 12.");
        }
    }
}
