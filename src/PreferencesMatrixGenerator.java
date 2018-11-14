import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

class PreferencesMatrixGenerator
{
    enum PreferenceGeneratorMode
    {
        kBestCase,
        kWorstCase,
        kRandom;
    }

    // Usage: PreferencesMatrixGenerator NumberOfPreferences Mode "OutputFile.txt"
    // Example: PreferencesMatrixGenerator 10 0 "test1.txt"
    public static void main(String args[])
    {
        final int kExpectedNumberOfArguments = 3;

        if (kExpectedNumberOfArguments == args.length)
        {
            final int kDataSetSize = Integer.parseInt(args[0]);
            final int kMode = Integer.parseInt(args[1]);
            final String kFileName = args[2];

            PreferenceGeneratorMode mode = PreferenceGeneratorMode.kBestCase;

            switch(kMode)
            {
                case 0:
                    mode = PreferenceGeneratorMode.kBestCase;
                    break;
                case 1:
                    mode = PreferenceGeneratorMode.kWorstCase;
                    break;
                case 2:
                    mode = PreferenceGeneratorMode.kRandom;
                    break;
                default:
                    System.out.println("Invalid Mode!");
                    System.out.println("Valid Modes: BestCase:0 WorstCase:1 Random:2");
                    System.exit(-100);
            }

            PreferencesMatrixGenerator preferencesMatrixGenerator = new PreferencesMatrixGenerator(kDataSetSize, mode);
            //preferencesMatrixGenerator.printItems();
            preferencesMatrixGenerator.writeToFile(kFileName);
        }
        else
        {
            System.out.println("Invalid Number of Arguments");
            System.out.println("Usage: PreferencesMatrixGenerator NumberOfPreferences Mode \"outputFile.txt\"");
            System.out.println("Example: PreferencesMatrixGenerator 100 0 \"test1.txt\"");
            System.out.println("Valid Modes: BestCase:0 WorstCase:1 Random:2");
            System.exit(-100);
        }
    }

    private int m_dataSize;
    private int[][] m_menPreferenceMatrix;
    private int[][] m_womenPreferenceMatrix;
    private PreferenceGeneratorMode m_mode;

    PreferencesMatrixGenerator(int dataSize, PreferenceGeneratorMode mode)
    {
        m_dataSize = dataSize;
        m_mode = mode;
        m_menPreferenceMatrix = new int[m_dataSize][m_dataSize];
        m_womenPreferenceMatrix = new int[m_dataSize][m_dataSize];

        populatePreferenceMatrices();
    }

    private void populatePreferenceMatrices()
    {
        switch (m_mode)
        {

            case kBestCase:
                // Best case is when men and women have the same preference lists
                for (int i = 0; i < m_dataSize; i++)
                {
                    for (int j = 0 ; j < m_dataSize; j++)
                    {
                        m_menPreferenceMatrix[i][j] = j + 1;
                        m_womenPreferenceMatrix[i][j] = j + 1;
                    }
                }
                break;

            case kWorstCase:
                // Worst case is when men and women have inverted preference lists
                for (int i = 0; i < m_dataSize; i++)
                {
                    int k = (m_dataSize - 1);
                    for (int j = 0; j < m_dataSize; j++)
                    {
                        m_menPreferenceMatrix[i][j] = j + 1;
                        m_womenPreferenceMatrix[i][j] = k + 1;
                        --k;
                    }
                }
                break;
            case kRandom:
                for (int i = 0; i < m_dataSize; i++)
                {
                    for (int j = 0 ; j < m_dataSize; j++)
                    {
                        m_menPreferenceMatrix[i][j] = j + 1;
                        m_womenPreferenceMatrix[i][j] = j + 1;
                    }
                }
                shufflePrefernceMatrices();

                break;
            default:
                System.out.println("Invalid Mode!");
                System.out.println("This should not happen!");
                System.exit(-100);
        }
    }

    public static void shuffleArray(int[] arrayToShuffle)
    {
        Random random = new Random();
        for (int i = arrayToShuffle.length - 1; i > 0; --i)
        {
            int index = random.nextInt(i + 1);
            // Simple swap
            int a = arrayToShuffle[index];
            arrayToShuffle[index] = arrayToShuffle[i];
            arrayToShuffle[i] = a;
        }
    }

    public void shufflePrefernceMatrices()
    {
        for (int i = 0; i < m_dataSize; ++i)
        {
            shuffleArray(m_menPreferenceMatrix[i]);
            shuffleArray(m_womenPreferenceMatrix[i]);
        }
    }

    public int[][] getMenPreferenceMatrix()
    {
        return m_menPreferenceMatrix;
    }

    public int[][] getWomenPreferenceMatrix()
    {
        return m_womenPreferenceMatrix;
    }

    public void printItems()
    {
        String outputStr = this.toString();
        System.out.println(outputStr);
    }

    public String toString()
    {
        String outputStr = new String();

        outputStr += Integer.toString(m_dataSize);
        outputStr += "\n";

        String menPreferencesStr = new String();
        String womenPreferencesStr = new String();

        for (int i = 0; i < m_dataSize; i++)
        {
            for (int j = 0; j < m_dataSize; j++)
            {
                menPreferencesStr += Integer.toString(m_menPreferenceMatrix[i][j]);
                womenPreferencesStr += Integer.toString(m_womenPreferenceMatrix[i][j]);
                if (j != (m_dataSize - 1))
                {
                    menPreferencesStr += " ";
                    womenPreferencesStr += " ";
                }
            }
            menPreferencesStr += "\n";
            womenPreferencesStr += "\n";
        }

        outputStr += menPreferencesStr;
        outputStr += womenPreferencesStr;

        return outputStr;
    }

    // TODO: maybe we should have a class that handles reading and writing to files to avoid code duplication
    public void writeToFile(String outputFileName)
    {
        String outputStr = this.toString();

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
