package bench;

import org.openjdk.jmh.annotations.*;
import smp.SMP;
import smp.SMPData;
import smp.SMPProducerConsumer;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xms2G"})
public class BenchmarkRandom10 {

    private static SMPData data;

    static {
        data = SMPData.loadFromFile("testCases/Random_10.txt");
    }

    @Benchmark
    public void serial() {
        SMP.performSMPAlgorithm(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), "m");
    }

    @Benchmark
    public void producerConsumer() {
        SMPProducerConsumer smp = new SMPProducerConsumer(data.getPreferencesOne(), data.getPreferencesTwo(), "m");
        smp.run();
    }
}
