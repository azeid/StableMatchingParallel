import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class FileInputOutputHelper
{
    public class preferenceInfo
    {
        String m_name;
        int m_prefListIndex;
    }

    public class FileParsedInfo
    {
        int count;
        String optimality;
        String outputFile;
        int[][] men_pref;
        int[][] women_pref;

        preferenceInfo[][] men_pref_with_details;
        preferenceInfo[][] women_pref_with_details;

        // Default constructor will contain debug pref lists
        public FileParsedInfo()
        {
            count = 4;
            optimality = "m";
            outputFile = "default_outputFile.txt";
            //men_pref = new int[][]{ {2, 3, 1, 4}, {2, 3, 4, 1}, {3, 2, 4, 1}, {1, 4, 2, 3}};
            //women_pref = new int[][]{ {1, 4, 2, 3}, {1, 4, 3, 2}, {3, 2, 4, 1}, {2, 1, 3, 4}};

            //men_pref = new int[][]{ {1, 2, 3, 4}, {2, 1, 4, 3}, {4, 2, 1, 3}, {1, 2, 3, 4}};
            //women_pref = new int[][]{ {2, 3, 1, 4}, {3, 4, 1, 2}, {4, 2, 1, 3}, {4, 2, 3, 1}};

            men_pref = new int[][]{ {3, 2, 1, 4}, {3, 1, 2, 4}, {4, 3, 1, 2}, {2, 4, 3, 1}};
            women_pref = new int[][]{ {1, 3, 2, 4}, {4, 1, 3, 2}, {4, 3, 1, 2}, {2, 4, 3, 1}};
        }
    };

    public FileParsedInfo getDefaultInfo()
    {
        FileParsedInfo defaultInfo = new FileParsedInfo();
        return defaultInfo;
    }

    public FileParsedInfo parseInputFile(String[] args)
    {
        if (2 != args.length && 3 != args.length)
        {
            System.out.println("Usage:\njava SMP <input_file> <m|w>");
            System.out.println("or Usage:\njava SMP <input_file> <m|w> <output_file>");
            System.exit(-100);
        }

        FileParsedInfo parsedInfo = new FileParsedInfo();

        final String kFileName = args[0];
        parsedInfo.optimality = args[1];
        parsedInfo.outputFile = (args.length == 3) ? args[2] : "";

        if (0 != parsedInfo.optimality.compareToIgnoreCase("w") && 0 != parsedInfo.optimality.compareToIgnoreCase("m") )
        {
            System.out.println("Invalid Preference Argument: " + parsedInfo.optimality);
            System.exit(-100);
        }

        try (Stream<String> stream = Files.lines(Paths.get(kFileName)))
        {
            // This remove data from stream object into fileLines
            final List<String> fileLines = stream.collect(Collectors.toList());
            parsedInfo.count = Integer.parseInt(fileLines.get(0));

            // Remove First Line After Reading It
            fileLines.remove(0);

            parsedInfo.men_pref = new int[parsedInfo.count][parsedInfo.count];
            parsedInfo.women_pref = new int[parsedInfo.count][parsedInfo.count];
            //int lineIndex = 0;
            for (int lineIndex = 0; lineIndex < (2 * parsedInfo.count); ++lineIndex)
            {
                final String[] numberInLineArray = fileLines.get(lineIndex).split(" ");

                if(lineIndex < parsedInfo.count)
                {
                    // populate men data set
                    for (int numberIndexInLine = 0; numberIndexInLine < parsedInfo.count; ++numberIndexInLine)
                    {
                        parsedInfo.men_pref[lineIndex % parsedInfo.count][numberIndexInLine] =
                                Integer.parseInt(numberInLineArray[numberIndexInLine]);
                    }
                }
                else
                {
                    // populate women data set
                    for (int numberIndexInLine = 0; numberIndexInLine < parsedInfo.count; ++numberIndexInLine)
                    {
                        parsedInfo.women_pref[lineIndex % parsedInfo.count][numberIndexInLine] =
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

        return parsedInfo;
    }

    public void writeStringToFile(String outputFileName, String outputStr)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write(outputStr);
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception While Writing To File: " + e.toString());
            System.exit(-100);
        }
    }
}
