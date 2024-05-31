package ibm.qa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public List<Planet> planets() {
        if (randomFailureOccured()) {
            throw new RandomException();
        }
        return this.planets;
    }

    public List<Planet> getPlanet(String name) {
        return this.planets.stream().filter(p -> p.name().equalsIgnoreCase(name)).toList();
    }

    public List<Planet> getPlanetsOfSun() {
        return this.planetsOfSun;
    }

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
