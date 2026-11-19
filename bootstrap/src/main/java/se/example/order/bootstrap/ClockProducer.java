package se.example.order.bootstrap;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/**
 * Wiring of infrastructure the core needs but should not create itself.
 * Swap in a fixed clock in tests with @InjectMock or a test producer.
 */
@Dependent
public class ClockProducer {

    @Produces
    @Singleton
    Clock clock() {
        return Clock.systemUTC();
    }
}
