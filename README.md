# StableMatchingParallel
This repository contains stable matching parallel algorithm implementation

# IDE Used
Java and IntelliJ IDE will be used when working this project

# Configuration Files
Please make sure not to submit IDE configuration files

# SMP
Compile
```
javac SMP.java
```


Usage
```
java smp.SMP <input_file> m or w <output_file>
java smp.ParallelGaleShapley <input_file> <m|w>
java smp.SMPDivideAndConquerImproved <input_file> <m|w>
java smp.JavaSMPMasterSlave <input_file>
java smp.SMPII <input_file> <m|w> <output_file>
```

Example
```
java SMP "inputFile1.txt" m "File1matching.txt"
java SMP "inputFile2.txt" w "File2matching.txt"
```

Valid Preferences
```
m: gives men optimal matching
w: gives women optimal matching
```

# PreferenceMatrixGenerator
Compile
```
javac PreferenceMatrixGenerator.java
```


Usage
```
java PreferencesMatrixGenerator NumberOfPreferences Mode "outputFile.txt"
```

Example
```
java PreferencesMatrixGenerator 5 0 "test1.txt"
java PreferencesMatrixGenerator 10 1 "test1.txt"
java PreferencesMatrixGenerator 20 2 "test1.txt"
```

Valid Preference Matrix Generation Modes
```
BestCase:0
WorstCase:1
Random:2
```

# Running Benchmarks
In order to run benchmarks, IntelliJ IDE must be used due to some dependencies required for the benchmarking library we are using. In IntelliJ IDE right, build bench component then right click on 'BenchmarkRunner.java' and Run it. Currently it takes ~50 minutes to run the all cases.
