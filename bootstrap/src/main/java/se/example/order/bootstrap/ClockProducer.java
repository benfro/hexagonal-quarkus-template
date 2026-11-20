package se.example.order.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/**
 * Wiring of infrastructure the core needs but should not create itself.
 * Swap in a fixed clock in tests with @InjectMock or a test producer.
 */
@ApplicationScoped
public class ClockProducer {

    @Produces
    Clock clock() {
        return Clock.systemUTC();
    }
}
