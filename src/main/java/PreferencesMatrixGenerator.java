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
                populateBestCaseMatrices();
                break;

            case kWorstCase:
                populateWorstCaseMatrices();
                break;

            case kRandom:
                populateRandomCaseMatrices();
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

    public void shufflePreferenceMatrices()
    {
        for (int i = 0; i < m_dataSize; ++i)
        {
            shuffleArray(m_menPreferenceMatrix[i]);
            shuffleArray(m_womenPreferenceMatrix[i]);
        }
    }

    private void populateBestCaseMatrices()
    {
        // Best case is when men and women have the same preference lists
        int [] temp = new int[m_dataSize];

        for (int i = 0; i < m_dataSize; i++)
        {
            temp[i] = i + 1;
        }

        final int kNumberOfRotations = 1;
        for (int i = 0; i < m_dataSize; i++)
        {
            m_menPreferenceMatrix[i] = temp.clone();
            m_womenPreferenceMatrix[i] = temp.clone();

            leftRotateArray(temp, kNumberOfRotations);
        }
    }

    private void populateWorstCaseMatrices()
    {
        // Worst case is when men and women have inverted preference lists
        // TODO: https://ir.nctu.edu.tw/bitstream/11536/4865/1/A1984TX77500006.pdf
        // http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.52.824&rep=rep1&type=pdf

        for (int i = 0; i < m_dataSize; i++) {
            for (int j = m_dataSize - 1; j >= 0; j--) {
                m_menPreferenceMatrix[i][j] = j + 1;
            }

            reverseArray(m_menPreferenceMatrix[i], 0, m_dataSize - 2);
            rotateArrayRight(m_menPreferenceMatrix[i], i + 1, 0, m_dataSize - 2);
        }

        for (int i = 0; i < m_dataSize; i++) {
            for (int j = 0; j < m_dataSize; j++) {
                m_womenPreferenceMatrix[i][j] = m_dataSize - j;
            }

            rotateArrayRight(m_womenPreferenceMatrix[i], i, 0, m_dataSize - 1);
        }

        leftRotateArray(m_womenPreferenceMatrix[0], 1);
        rotateArrayRight(m_womenPreferenceMatrix[m_dataSize - 1], 1, 0, m_dataSize -1);
    }

    private void rotateArrayRight(int arr[], int offset, int start, int end) {
        offset = offset % (end + 1);
        if (offset == 0) return;

        reverseArray(arr, start, end);
        reverseArray(arr, start, offset - 1);
        reverseArray(arr, offset, end);
    }

    private void reverseArray(int arr[], int start, int end) {
        for (; start < end; start++, end--) {
            swap(arr, start, end);
        }
    }

    private void swap(int arr[], int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    private void populateRandomCaseMatrices()
    {
        // Random Case
        for (int i = 0; i < m_dataSize; i++)
        {
            for (int j = 0 ; j < m_dataSize; j++)
            {
                m_menPreferenceMatrix[i][j] = j + 1;
                m_womenPreferenceMatrix[i][j] = j + 1;
            }
        }
        shufflePreferenceMatrices();
    }

    private void leftRotateArray(int arr[], int numberOfRotations)
    {
        for (int i = 0; i < numberOfRotations; i++)
        {
            leftRotateArrayByOne(arr);
        }
    }

    private void leftRotateArrayByOne(int arr[])
    {
        int i, temp;
        temp = arr[0];
        for (i = 0; i < (arr.length - 1); i++)
        {
            arr[i] = arr[i + 1];
        }
        arr[i] = temp;
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
        StringBuilder outPut = new StringBuilder();

        outPut.append(m_dataSize);
        outPut.append('\n');

        StringBuilder menPreferencesStr = new StringBuilder();
        StringBuilder womenPreferencesStr = new StringBuilder();

        for (int i = 0; i < m_dataSize; i++)
        {
            for (int j = 0; j < m_dataSize; j++)
            {
                menPreferencesStr.append(m_menPreferenceMatrix[i][j]);
                womenPreferencesStr.append(m_womenPreferenceMatrix[i][j]);
                if (j != (m_dataSize - 1))
                {
                    menPreferencesStr.append(' ');
                    womenPreferencesStr.append(' ');
                }
            }
            menPreferencesStr.append('\n');
            womenPreferencesStr.append('\n');
        }

        outPut.append(menPreferencesStr);
        outPut.append(womenPreferencesStr);

        return outPut.toString();
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
