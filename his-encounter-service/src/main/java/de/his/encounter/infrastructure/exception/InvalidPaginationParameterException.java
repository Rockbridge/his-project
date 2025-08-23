package de.his.encounter.infrastructure.exception;

/**
 * Exception thrown when pagination parameters like page or size are invalid.
 */
public class InvalidPaginationParameterException extends RuntimeException {

    private final String parameter;
    private final int value;

    public InvalidPaginationParameterException(String parameter, int value) {
        super(String.format("Invalid value '%d' for parameter '%s'", value, parameter));
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public int getValue() {
        return value;
    }
}
