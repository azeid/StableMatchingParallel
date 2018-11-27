package smp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileInputOutputHelper
{
    public class PreferenceInfo
    {
        String m_name;
        int m_Index;
        HashMap<String, Integer> m_prefNameToIndexHashMap;
    }

    public class FileParsedInfo
    {
        int count;
        String optimality;
        String outputFile;
        int[][] men_pref;
        int[][] women_pref;

        HashMap<String, PreferenceInfo> men_pref_with_details_HashMap;
        HashMap<String, PreferenceInfo> women_pref_with_details_HashMap;

        // Default constructor will contain debug pref lists
        public FileParsedInfo()
        {
        }

        public FileParsedInfo getDefaultInfo()
        {
            FileParsedInfo defaultInfo = new FileParsedInfo();

            defaultInfo.count = 4;
            defaultInfo.optimality = "m";
            defaultInfo.outputFile = "default_outputFile.txt";
            //men_pref = new int[][]{ {2, 3, 1, 4}, {2, 3, 4, 1}, {3, 2, 4, 1}, {1, 4, 2, 3}};
            //women_pref = new int[][]{ {1, 4, 2, 3}, {1, 4, 3, 2}, {3, 2, 4, 1}, {2, 1, 3, 4}};

            //men_pref = new int[][]{ {1, 2, 3, 4}, {2, 1, 4, 3}, {4, 2, 1, 3}, {1, 2, 3, 4}};
            //women_pref = new int[][]{ {2, 3, 1, 4}, {3, 4, 1, 2}, {4, 2, 1, 3}, {4, 2, 3, 1}};

            defaultInfo.men_pref = new int[][]{{3, 2, 1, 4}, {3, 1, 2, 4}, {4, 3, 1, 2}, {2, 4, 3, 1}};
            defaultInfo.women_pref = new int[][]{{1, 3, 2, 4}, {4, 1, 3, 2}, {4, 3, 1, 2}, {2, 4, 3, 1}};

            defaultInfo.men_pref_with_details_HashMap = new HashMap<String, PreferenceInfo>();
            defaultInfo.women_pref_with_details_HashMap = new HashMap<String, PreferenceInfo>();

            for (int i = 0; i < defaultInfo.men_pref.length; ++i)
            {
                PreferenceInfo prefInfo = new PreferenceInfo();
                prefInfo.m_prefNameToIndexHashMap = new HashMap<String, Integer>();

                for (int j = 0; j < defaultInfo.men_pref[0].length; ++j)
                {
                    // Pref number is already 1-based
                    String womanName = "w" + defaultInfo.men_pref[i][j];
                    prefInfo.m_prefNameToIndexHashMap.put(womanName, j);
                }

                // Naming is 1-based and indexing is 0-based
                String manName = "m" + Integer.toString(i + 1);
                prefInfo.m_name = manName;
                prefInfo.m_Index = i;
                defaultInfo.men_pref_with_details_HashMap.put(manName, prefInfo);
            }

            for (int i = 0; i < defaultInfo.women_pref.length; ++i)
            {
                PreferenceInfo prefInfo = new PreferenceInfo();
                prefInfo.m_prefNameToIndexHashMap = new HashMap<String, Integer>();

                for (int j = 0; j < defaultInfo.women_pref[0].length; ++j)
                {
                    // Pref number is already 1-based
                    String manName = "m" + defaultInfo.women_pref[i][j];
                    prefInfo.m_prefNameToIndexHashMap.put(manName, j);
                }

                // Naming is 1-based and indexing is 0-based
                String womanName = "w" + Integer.toString(i + 1);
                prefInfo.m_name = womanName;
                prefInfo.m_Index = i;
                defaultInfo.women_pref_with_details_HashMap.put(womanName, prefInfo);
            }

            return defaultInfo;
        }
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

        if (0 != parsedInfo.optimality.compareToIgnoreCase("w") && 0 != parsedInfo.optimality.compareToIgnoreCase("m"))
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

            parsedInfo.men_pref_with_details_HashMap = new HashMap<String, PreferenceInfo>();
            parsedInfo.women_pref_with_details_HashMap = new HashMap<String, PreferenceInfo>();

            //int lineIndex = 0;
            for (int lineIndex = 0; lineIndex < (2 * parsedInfo.count); ++lineIndex)
            {
                final String[] numberInLineArray = fileLines.get(lineIndex).split(" ");

                if (lineIndex < parsedInfo.count)
                {
                    PreferenceInfo prefInfo = new PreferenceInfo();
                    prefInfo.m_prefNameToIndexHashMap = new HashMap<String, Integer>();
                    // populate men data set
                    for (int numberIndexInLine = 0; numberIndexInLine < parsedInfo.count; ++numberIndexInLine)
                    {
                        parsedInfo.men_pref[lineIndex % parsedInfo.count][numberIndexInLine] =
                                Integer.parseInt(numberInLineArray[numberIndexInLine]);

                        // Pref number is already 1-based
                        String womanName = "w" + numberInLineArray[numberIndexInLine];
                        prefInfo.m_prefNameToIndexHashMap.put(womanName, numberIndexInLine);

                    }

                    // Naming is 1-based and indexing is 0-based
                    String manName = "m" + Integer.toString((lineIndex % parsedInfo.count) + 1);
                    prefInfo.m_name = manName;
                    prefInfo.m_Index = (lineIndex % parsedInfo.count);
                    parsedInfo.men_pref_with_details_HashMap.put(manName, prefInfo);
                }
                else
                {
                    PreferenceInfo prefInfo = new PreferenceInfo();
                    prefInfo.m_prefNameToIndexHashMap = new HashMap<String, Integer>();
                    // populate women data set
                    for (int numberIndexInLine = 0; numberIndexInLine < parsedInfo.count; ++numberIndexInLine)
                    {
                        parsedInfo.women_pref[lineIndex % parsedInfo.count][numberIndexInLine] =
                                Integer.parseInt(numberInLineArray[numberIndexInLine]);

                        // Pref number is already 1-based
                        String manName = "m" + numberInLineArray[numberIndexInLine];
                        prefInfo.m_prefNameToIndexHashMap.put(manName, numberIndexInLine);
                    }

                    // Naming is 1-based and indexing is 0-based
                    String womanName = "w" + Integer.toString((lineIndex % parsedInfo.count) + 1);
                    prefInfo.m_name = womanName;
                    prefInfo.m_Index = (lineIndex % parsedInfo.count);
                    parsedInfo.women_pref_with_details_HashMap.put(womanName, prefInfo);
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

    public FileParsedInfo parseInputData(int[][] man_preferences, int[][] women_preferences, int size, String optimality)
    {
        FileParsedInfo parsedInfo = new FileParsedInfo();

        parsedInfo.count = size;
        parsedInfo.optimality = optimality;
        parsedInfo.outputFile = "";

        if (0 != parsedInfo.optimality.compareToIgnoreCase("w") && 0 != parsedInfo.optimality.compareToIgnoreCase("m"))
        {
            System.out.println("Invalid Preference Argument: " + parsedInfo.optimality);
            System.exit(-100);
        }

        parsedInfo.men_pref = new int[parsedInfo.count][parsedInfo.count];
        parsedInfo.women_pref = new int[parsedInfo.count][parsedInfo.count];

        parsedInfo.men_pref_with_details_HashMap = new HashMap<String, PreferenceInfo>();
        parsedInfo.women_pref_with_details_HashMap = new HashMap<String, PreferenceInfo>();

        for (int i = 0; i < parsedInfo.count; ++i)
        {
            PreferenceInfo prefInfo = new PreferenceInfo();
            prefInfo.m_prefNameToIndexHashMap = new HashMap<String, Integer>();
            // populate men data set
            for (int j = 0; j < parsedInfo.count; ++j)
            {
                parsedInfo.men_pref[i][j] = man_preferences[i][j];

                // Pref number is already 1-based
                String womanName = "w" + man_preferences[i][j];
                prefInfo.m_prefNameToIndexHashMap.put(womanName, j);
            }

            // Naming is 1-based and indexing is 0-based
            String manName = "m" + Integer.toString(i + 1);
            prefInfo.m_name = manName;
            prefInfo.m_Index = i;
            parsedInfo.men_pref_with_details_HashMap.put(manName, prefInfo);
        }

        for (int i = 0; i < parsedInfo.count; ++i)
        {
            PreferenceInfo prefInfo = new PreferenceInfo();
            prefInfo.m_prefNameToIndexHashMap = new HashMap<String, Integer>();
            // populate women data set
            for (int j = 0; j < parsedInfo.count; ++j)
            {
                parsedInfo.women_pref[i][j] = women_preferences[i][j];

                // Pref number is already 1-based
                String manName = "m" + j;
                prefInfo.m_prefNameToIndexHashMap.put(manName, j);
            }

            // Naming is 1-based and indexing is 0-based
            String womanName = "w" + Integer.toString(i + 1);
            prefInfo.m_name = womanName;
            prefInfo.m_Index = i;
            parsedInfo.women_pref_with_details_HashMap.put(womanName, prefInfo);
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
