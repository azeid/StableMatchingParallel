package bench;

import org.openjdk.jmh.annotations.*;
import smp.SMP;
import smp.SMPData;
import smp.SMPProducerConsumer;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkRandomCases {

    private static SMPData data;

    @Param({
            "testCases/Random_10.txt",
            "testCases/Random_100.txt",
            "testCases/Random_200.txt",
            "testCases/Random_1000.txt"
    })
    public String fileName;

    @Setup
    public void loadData() {
        data = SMPData.loadFromFile(fileName);
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
