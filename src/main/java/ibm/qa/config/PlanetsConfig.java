package ibm.qa.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "qa.planets")
public interface PlanetsConfig {

    double failureProbability();

    Find find();

    public interface Find {
        double timeoutThreshold();

        double failThreshold();

        long maxDurationMillis();

    }

}
