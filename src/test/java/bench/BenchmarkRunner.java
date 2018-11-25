package bench;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(BenchmarkRandom10.class.getSimpleName())
                .include(BenchmarkRandom100.class.getSimpleName())
                .include(BenchmarkRandom200.class.getSimpleName())
                .warmupIterations(3)
                .forks(1)
                .build();

        new Runner(options).run();
    }
}
