package smp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SMPPII
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

        final int kExpectedNumberOfArguments = 3;

        if(kExpectedNumberOfArguments == args.length)
        {
            // Assuming File In Same Directory
            final String kFileName = args[0];
            kPreference = args[1];
            kOutputFile = args[2];
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
            OriginalMenDataSet2D = new int[][]{ {4, 2, 3, 1}, {3, 1, 2, 4}, {2, 4, 1, 3}, {1, 4, 3, 2}};
            OriginalWomenDataSet2D = new int[][]{ {1, 4, 2, 3}, {1, 2, 3, 4}, {4, 2, 3, 1}, {3, 1, 4, 2}};
            kPreference = "m";
            kOutputFile = "test1.txt";

            // Check generating matrices using PreferencesMatrixGenerator class
        }

        //printDataSet2DArray(OriginalMenDataSet2D, dataSetSize);
        //System.out.println();
        //printDataSet2DArray(OriginalWomenDataSet2D, dataSetSize);

        if(0 == kPreference.compareToIgnoreCase("m"))
        {
            // man optimal matching
            performSMPPII(
                    OriginalMenDataSet2D,
                    OriginalWomenDataSet2D,
                    dataSetSize,
                    kPreference);
        }
        else if (0 == kPreference.compareToIgnoreCase("w"))
        {
            // woman optimal matching
            performSMPPII(
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

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <smp.SMPPII> is "+ totalTime);
    }

    public static String performSMPPII(
            int[][] optimalGenderDataSet2DArray,
            int[][] otherGenderDataSet2DArray,
            int numberOfRowsAndColumns,
            String preference)
    {
        RankingMatrix myRankingMatrix = new RankingMatrix(
                optimalGenderDataSet2DArray,
                otherGenderDataSet2DArray,
                numberOfRowsAndColumns,
                preference);

        myRankingMatrix.findStableMatching();

        return myRankingMatrix.getFinalMatchingGraphToString();
    }


}
