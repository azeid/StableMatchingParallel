package bench;

import org.openjdk.jmh.annotations.*;
import smp.SMP;
import smp.SMPData;
import smp.SMPProducerConsumer;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkWorst8 {

    private static SMPData data;

    static {
        data = SMPData.loadFromFile("testCases/WorstCase_8.txt");
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
