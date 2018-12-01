package smp;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SMP
{

    public static void main(String[] args)
    {
        // Log Start Time
        final long startTime = System.nanoTime();

        int[][] OriginalMenDataSet2D = new int[1][1];
        int[][] OriginalWomenDataSet2D = new int[1][1];
        int dataSetSize = 0;
        String kPreference = "";
        String kOutputFile = "";

        //final int kExpectedNumberOfArguments = 3;

        if(2 == args.length || 3 == args.length)
        {
            // Assuming File In Same Directory
            final String kFileName = args[0];
            kPreference = args[1];
            kOutputFile = (3 == args.length) ? args[2] : "";
            try (Stream<String> stream = Files.lines(Paths.get(kFileName)))
            {
                // This remove data from stream object into fileLines
                final List<String> fileLines = stream.collect(Collectors.toList());
                dataSetSize = Integer.parseInt(fileLines.get(0));

                // Remove First Line After Reading It
                fileLines.remove(0);

                OriginalMenDataSet2D = new int[dataSetSize][dataSetSize];
                OriginalWomenDataSet2D = new int[dataSetSize][dataSetSize];
                //int lineIndex = 0;
                for (int lineIndex = 0; lineIndex < (2 * dataSetSize); ++lineIndex)
                {
                    final String[] numberInLineArray = fileLines.get(lineIndex).split(" ");

                    if(lineIndex < dataSetSize)
                    {
                        // populate men data set
                        for (int numberIndexInLine = 0; numberIndexInLine < dataSetSize; ++numberIndexInLine)
                        {
                            OriginalMenDataSet2D[lineIndex % dataSetSize][numberIndexInLine] =
                                    Integer.parseInt(numberInLineArray[numberIndexInLine]);
                        }
                    }
                    else
                    {
                        // populate women data set
                        for (int numberIndexInLine = 0; numberIndexInLine < dataSetSize; ++numberIndexInLine)
                        {
                            OriginalWomenDataSet2D[lineIndex % dataSetSize][numberIndexInLine] =
                                    Integer.parseInt(numberInLineArray[numberIndexInLine]);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                System.out.println("Exception While Reading File: " + e.toString());
                System.exit(-100);
            }
        }
        else if(0 == args.length)
        {
            // Set HW Sample Case for debugging
            dataSetSize = 4;
            OriginalMenDataSet2D = new int[][]{ {2, 3, 1, 4}, {3, 1, 4, 2}, {4, 2, 3, 1}, {1, 4, 2, 4}};
            OriginalWomenDataSet2D = new int[][]{ {1, 4, 2, 3}, {1, 4, 3, 2}, {3, 2, 4, 1}, {2, 1, 3, 4}};
            kPreference = "m";
            kOutputFile = "test1.txt";

            // Check generating matrices using PreferencesMatrixGenerator class
        }

        //printDataSet2DArray(OriginalMenDataSet2D, dataSetSize);
        //System.out.println();
        //printDataSet2DArray(OriginalWomenDataSet2D, dataSetSize);

        String finalMatching = "";
        if(0 == kPreference.compareToIgnoreCase("m"))
        {
            // man optimal matching
            finalMatching = performSMPAlgorithm(
                    OriginalMenDataSet2D,
                    OriginalWomenDataSet2D,
                    dataSetSize,
                    kPreference);
        }
        else if (0 == kPreference.compareToIgnoreCase("w"))
        {
            // woman optimal matching
            finalMatching = performSMPAlgorithm(
                    OriginalWomenDataSet2D,
                    OriginalMenDataSet2D,
                    dataSetSize,
                    kPreference);
        }
        else
        {
            System.out.println("Invalid Preference Argument: " + kPreference);
            System.exit(-100);
        }

        System.out.println(finalMatching);

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <smp.SMP> is "+ totalTime);
    }

    public static String performSMPAlgorithm(
            int[][] optimalGenderDataSet2DArray,
            int[][] otherGenderDataSet2DArray,
            int numberOfRowsAndColumns,
            String preference)
    {
        BipartiteGraph myBipartiteGraph = new BipartiteGraph(
                optimalGenderDataSet2DArray,
                otherGenderDataSet2DArray,
                numberOfRowsAndColumns,
                preference);

        // This is helpful for debugging.
        // BestCase: it should be 0
        // WorstCase: it should be (n - 1) * (n - 1) = n^2 - 2n + 1
        // Where n is the number of preferences per gender
        // Reference: http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.52.824&rep=rep1&type=pdf
        int numberOfRejections = 0;

        while(!myBipartiteGraph.isOptimalGenderQueueEmpty())
        {
            String currentOptimalGender = myBipartiteGraph.dequeueOptimalPerson();

            String currentOtherGender = myBipartiteGraph.getNextMostPreferredOtherGender(currentOptimalGender);

            if(myBipartiteGraph.isOtherGenderEngaged(currentOtherGender))
            {
                if(myBipartiteGraph.doesOtherGenderPreferCurrentFianceOverOptimalChoice(currentOtherGender, currentOptimalGender))
                {
                    // current optimal gender was rejected, put him back in the queue
                    myBipartiteGraph.enqueueOptimalGenderPerson(currentOptimalGender);
                }
                else
                {
                    // this means that other gender prefers current optimal gender more than the one it is currently
                    // engaged to
                    String fianceThatWasDumped = myBipartiteGraph.dumpCurrentFiance(currentOtherGender);
                    // put back in queue
                    myBipartiteGraph.enqueueOptimalGenderPerson(fianceThatWasDumped);

                    // engage current partners
                    myBipartiteGraph.engageOtherGenderToOptimalGender(currentOtherGender, currentOptimalGender);
                }

                ++numberOfRejections;
            }
            else
            {
                // since other gender is free, they have to get engaged
                myBipartiteGraph.engageOtherGenderToOptimalGender(currentOtherGender, currentOptimalGender);
            }
        }

        //final String finalMatchingGraph = myBipartiteGraph.getFinalMatchingGraphToString();
        //System.out.println(finalMatchingGraph);
        // System.out.println("Number of Rejections: " + numberOfRejections);
        // n^2 - 2n + 1
        final double kWorstCaseRejections = Math.round(
                Math.pow(numberOfRowsAndColumns, 2) - (2 * numberOfRowsAndColumns) + 1);
        if((int)kWorstCaseRejections == numberOfRejections)
        {
            // System.out.println("This is a Worst Case");
        }

        return myBipartiteGraph.getFinalMatchingGraphToString();
    }

    public static void printDataSet2DArray(int[][] dataSet2DArray, int numberOfRowsAndColumns)
    {
        for (int i = 0; i < numberOfRowsAndColumns; ++i)
        {
            String lineOutput = new String();
            for(int j = 0; j < numberOfRowsAndColumns; ++j)
            {
                final int data = dataSet2DArray[i][j];

                lineOutput+= (Integer.toString(data) + "   ");
            }

            System.out.println(lineOutput);
        }
    }
}