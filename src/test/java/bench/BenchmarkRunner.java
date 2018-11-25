package bench;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(BenchmarkWorst8.class.getSimpleName())
                .include(BenchmarkRandom10.class.getSimpleName())
                .include(BenchmarkRandom100.class.getSimpleName())
                .include(BenchmarkRandom200.class.getSimpleName())
                .include(BenchmarkRandom1000.class.getSimpleName())
                .timeout(TimeValue.minutes(1))
                .warmupIterations(3)
                .forks(1)
                .build();

        new Runner(options).run();
    }
}
