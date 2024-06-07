package ibm.qa.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import com.github.javafaker.Faker;

import ibm.model.Planet;
import ibm.qa.config.PlanetsConfig;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PlanetService {

    @Inject
    PlanetsConfig config;

    private List<Planet> planets = new ArrayList<>();
    private List<Planet> planetsOfSun = List.of(new Planet("Mercury", 0.4), new Planet("Venus", 0.7),
            new Planet("Earth", 1.0), new Planet("Mars", 1.5),
            new Planet("Jupiter", 5.2), new Planet("Saturn", 9.6), new Planet("Uranus", 19.2),
            new Planet("Neptune", 30.0));

    @PostConstruct
    void initPlanets() {
        Faker faker = new Faker();
        for (int i = 0; i < 8; i++) {
            planets.add(new Planet(faker.dune().planet(), Double.valueOf(faker.random().nextDouble() * 100.0f)));
        }
    }

    @Fallback(fallbackMethod = "getPlanetsOfSun", applyOn = IllegalArgumentException.class)
    public List<Planet> planets() {
        // if (true)
        // throw new IllegalArgumentException("Why not");
        if (randomFailureOccured()) {
            throw new RandomException();
        }
        return this.planets;
    }

    public List<Planet> getPlanet(String name) {
        return this.planets.stream().filter(p -> p.name().equalsIgnoreCase(name)).toList();
    }

    public List<Planet> getPlanetsOfSun() {
        // if (true)
        // throw new IllegalArgumentException("Why not");
        return this.planetsOfSun;
    }

    @Timeout(10000)
    @Retry(retryOn = RandomException.class, maxRetries = 5, delay = 2, abortOn = AstrometryException.class)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.2, successThreshold = 2, delay = 5, delayUnit = ChronoUnit.SECONDS, failOn = RandomException.class)
    public List<Planet> findPlanet(Double distance) {
        List<Planet> result;
        long start = System.currentTimeMillis();
        Log.info("Searching planet in a maximum distance of " + distance + " AU");
        maybeFail(distance);
        result = this.planets.stream().filter(p -> p.distance() <= distance).toList();
        long end = System.currentTimeMillis();
        logDuration(start, end);
        return result;
    }

    private void logDuration(long start, long end) {
        Log.info("The operation required " + (end - start) + " ms to finish");
    }

    private void maybeFail(Double distance) {
        if (distance > this.config.find().timeoutThreshold()) {
            long sleep = new Random().nextLong(this.config.find().maxDurationMillis());
            Log.info("This calculation is going to last " + sleep + " miliseconds");
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // nothing to do here
                e.printStackTrace();
            }
        }
        if (distance > this.config.find().failThreshold()) {
            throw new AstrometryException(distance);
        }
        if (randomFailureOccured()) {
            throw new RandomException();
        }

    }

    private boolean randomFailureOccured() {
        final double random = Math.random() + 0.000000000000001f;
        return random < this.config.failureProbability();
    }

    public class AstrometryException extends RuntimeException {
        public AstrometryException(Double distance) {
            super(String.format("%s AU is just too much for this engine", distance));
        }
    }

    public class RandomException extends RuntimeException {
        public RandomException() {
            super("What a pitty... Sometimes S**T just happens...");
        }
    }

}
