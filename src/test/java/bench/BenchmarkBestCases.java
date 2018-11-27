package bench;

import org.openjdk.jmh.annotations.*;
import smp.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkBestCases {

    private static SMPData data;

    @Param({
            "testCases/BestCase_10.txt",
            "testCases/BestCase_100.txt",
            "testCases/BestCase_200.txt",
            "testCases/BestCase_1000.txt"
    })
    public String fileName;

    @Setup
    public void loadData() {
        data = SMPData.loadFromFile(fileName);
    }

    /*
    @Benchmark
    public String serial() {
        return SMP.performSMPAlgorithm(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), "m");
    }

    @Benchmark
    public String producerConsumer() {
        SMPProducerConsumer smp = new SMPProducerConsumer(data.getPreferencesOne(), data.getPreferencesTwo(), "m");
        return smp.run();
    }
*/
    @Benchmark
    public String divideAndConquerRunnable() {
        FileInputOutputHelper fileIOHelper = new FileInputOutputHelper();
        String[] customArgs = new String[2];
        customArgs[0] = fileName;
        customArgs[1] = "m";
        FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.parseInputFile(customArgs);

        SMPDivideAndConquer smp = new SMPDivideAndConquer(parsedInfo);
        return smp.runThread();
    }

/*
    @Benchmark
    public String divideAndConquerCallable() {
        FileInputOutputHelper fileHelper = new FileInputOutputHelper();
        FileInputOutputHelper.FileParsedInfo parsedInfo =
                fileHelper.parseInputData(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), "m");

        SMPDivideAndConquer smp = new SMPDivideAndConquer(parsedInfo);
        return smp.runCallable();
    }
    */
}
