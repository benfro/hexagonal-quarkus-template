package se.example.order.domain.model;

/**
 * A business rule was violated.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
