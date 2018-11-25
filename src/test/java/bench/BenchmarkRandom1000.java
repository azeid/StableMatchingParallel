package bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import smp.SMP;
import smp.SMPData;
import smp.SMPProducerConsumer;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkRandom1000 {

    private static SMPData data;

    static {
        data = SMPData.loadFromFile("testCases/Random_1000.txt");
    }

    @Benchmark
    public String serial() {
        return SMP.performSMPAlgorithm(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), "m");
    }

    @Benchmark
    public String producerConsumer() {
        SMPProducerConsumer smp = new SMPProducerConsumer(data.getPreferencesOne(), data.getPreferencesTwo(), "m");
        return smp.run();
    }
}
