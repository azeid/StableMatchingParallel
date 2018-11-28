/*
 * Inspired by
 * "A PARALLEL ALGORITHM TO SOLVE THE STABLE MARRIAGE PROBLEM"
 * S. S. TSENG and R. C. T. LEE - 1984
 */

package smp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class SMPDivideAndConquerImproved
{
    private int m_size;
    private int[][] m_optimalPrefList;
    private int[][] m_otherPrefList;
    private MatchingPairIndices[] m_matchingPairList;

    private HashMap<Integer, HashMap<Integer, Integer>> m_optimalToPrefIndexHashMap;
    private HashMap<Integer, HashMap<Integer, Integer>> m_otherToOptimalPrefIndexHashMap;

    //private int[][] m_optimalToPrefIndexArray;
    //private int[][] m_otherToOptimalPrefIndexArray;

    public class MatchingPairIndices
    {
        public int m_optimalGenderIndex;
        public int m_otherGenderIndex;

        public MatchingPairIndices(int optimalGenderIndex, int otherGenderIndex)
        {
            m_optimalGenderIndex = optimalGenderIndex;
            m_otherGenderIndex = otherGenderIndex;
        }
    }

    /*
    public class PrefInfo
    {
        int m_Index;
        HashMap<Integer, Integer> m_prefIndexHashMap;
    }
    */

    public SMPDivideAndConquerImproved(int[][] man_preferences, int[][] women_preferences, int size, String optimality)
    {
        //System.out.println("Optimality: " + parsedInfo.optimality);

        m_size = size;

        if(0 == optimality.compareToIgnoreCase("m"))
        {
            //System.out.println("Man Optimal");
            m_optimalPrefList = man_preferences;
            m_otherPrefList = women_preferences;
        }
        else
        {
            //System.out.println("Woman Optimal");
            m_optimalPrefList = women_preferences;
            m_otherPrefList = man_preferences;
        }

        m_optimalToPrefIndexHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();
        m_otherToOptimalPrefIndexHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();

        //m_optimalToPrefIndexArray = new int[m_size][m_size];
        //m_otherToOptimalPrefIndexArray = new int[m_size][m_size];


        for(int i = 0; i < m_size; ++i)
        {
            HashMap<Integer, Integer> optimalPrefHashMap = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> otherPrefHashMap = new HashMap<Integer, Integer>();

            for(int j = 0; j < m_size; ++j)
            {
                //m_optimalToPrefIndexArray[i][j] = m_otherPrefList[][];
                optimalPrefHashMap.put(m_optimalPrefList[i][j], j);
                otherPrefHashMap.put(m_otherPrefList[i][j], j);
            }

            m_optimalToPrefIndexHashMap.put(i, optimalPrefHashMap);
            m_otherToOptimalPrefIndexHashMap.put(i, otherPrefHashMap);
        }

    }

    public MatchingPairIndices[] getInitialMatching()
    {
        // Here, we just match every optimal gender with their first preference
        m_matchingPairList = new MatchingPairIndices[m_size];

        for(int i = 0; i < m_size; ++i)
        {
            m_matchingPairList[i].m_optimalGenderIndex = i;
            m_matchingPairList[i].m_otherGenderIndex = m_optimalPrefList[i][0];
        }

        //System.out.println("Initial Matching");
        //System.out.println(matchingArrayToString(m_matchingPairList));

        return m_matchingPairList;
    }

    public static MatchingPairIndices[][] chunkArray(MatchingPairIndices[] array)
    {
        MatchingPairIndices[][] output = new MatchingPairIndices[array.length][1];

        for(int i = 0; i < array.length; ++i)
        {
            output[i][0] = array[i];
        }

        return output;
    }

    public static MatchingPairIndices[] concatenate1DArray(MatchingPairIndices[] a, MatchingPairIndices[] b)
    {
        int aLen = a.length;
        int bLen = b.length;

        MatchingPairIndices[] c = new MatchingPairIndices[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static String FinalMatchingArrayToString(MatchingPairIndices[] arr)
    {
        /*
        // Order results. This should not be part of the benchmarking since it is just
        // how the matching is returned to user
        StringBuilder matches = new StringBuilder();
        for(Integer optimalGender : OrderedMatchingInteger.keySet())
        {

            matches.append("(" + optimalGender);
            matches.append("," + OrderedMatchingInteger.get(optimalGender));
            matches.append(")\n");
        }

        return matches.toString();
        */
        return new String();
    }

    public static int getNumberFromString(String str)
    {
        return Integer.parseInt(str.substring(1));
    }

    class MergeTwoMatchingSets
    {

        private MatchingPairIndices[] m_leftMatching;
        private MatchingPairIndices[] m_rightMatching;
        private MatchingPairIndices[] m_finalMatching;

        class Result
        {
            MatchingPairIndices[] m_finalMatching;
        }

        public MergeTwoMatchingSets(MatchingPairIndices[] leftMatching, MatchingPairIndices[] rightMatching)
        {
            // TODO: Do I need to clone them?
            m_leftMatching = leftMatching;
            m_rightMatching = rightMatching;
        }

        public void matchAndAdvanceConflictsIfFound()
        {
            // example without conflict
            //        (M3, W4)   (M4, W2)
            //              \    /
            //               \ /
            //       (M3, W4),(M4, W2)
            //
            // example with conflict
            //        (M1, W3)   (M2, W3)
            //              \    /
            //               \ /
            //       (M1, W3),(M2, W1) where w1 is next on M2 preference list

            MatchingPairIndices[] concatenatedMatchingArray = concatenate1DArray(m_leftMatching, m_rightMatching);
            m_finalMatching = Arrays.copyOf(concatenatedMatchingArray, concatenatedMatchingArray.length);

            // Key is other gender
            HashMap<Integer, Integer> alreadyOtherGenderMatched = new HashMap<Integer, Integer>();

            HashMap<Integer, Integer> alreadyOptimalMatched = new HashMap<Integer, Integer>();

            for(int matchingIndex = 0; matchingIndex < concatenatedMatchingArray.length; ++matchingIndex)
            {
                MatchingPairIndices currentMatchingPair = concatenatedMatchingArray[matchingIndex];

                if(alreadyOtherGenderMatched.containsKey(currentMatchingPair.m_otherGenderIndex))
                {
                    while(alreadyOtherGenderMatched.containsKey(currentMatchingPair.m_otherGenderIndex))
                    {
                        int otherGenderIndex = currentMatchingPair.m_otherGenderIndex;
                        int optimalMatchedIndex = alreadyOtherGenderMatched.get(currentMatchingPair.m_otherGenderIndex);
                        int optimalProposingIndex = currentMatchingPair.m_optimalGenderIndex;

                        int mostPreferredOptimalIndex = getMostPreferredOptimalGender(
                                otherGenderIndex,
                                optimalMatchedIndex,
                                optimalProposingIndex);

                        if(mostPreferredOptimalIndex == optimalMatchedIndex)
                        {
                            // Keep current matching, advance conflicting optimal gender to his next
                            // preferred other gender
                            int nextPreferredOtherGenderIndex = getNextPreference(optimalProposingIndex, otherGenderIndex);

                            currentMatchingPair.m_otherGenderIndex = nextPreferredOtherGenderIndex;
                        }
                        else
                        {
                            // This means that we need to reject already matched and match
                            // the more preferred
                            alreadyOtherGenderMatched.put(currentMatchingPair.m_otherGenderIndex, optimalProposingIndex);
                            alreadyOptimalMatched.put(optimalProposingIndex, currentMatchingPair.m_otherGenderIndex);
                            // Advance conflicting optimal gender to his next
                            // preferred other gender
                            int nextPreferredOtherGender = getNextPreference(optimalMatchedIndex, otherGenderIndex);

                            currentMatchingPair.m_optimalGenderIndex = optimalMatchedIndex;
                            currentMatchingPair.m_otherGenderIndex = nextPreferredOtherGender;
                        }
                    } // while
                } // if

                alreadyOtherGenderMatched.put(currentMatchingPair.m_otherGenderIndex, currentMatchingPair.m_optimalGenderIndex);
                alreadyOptimalMatched.put(currentMatchingPair.m_optimalGenderIndex, currentMatchingPair.m_otherGenderIndex);
            } // for

            /*
            // TODO: find better way! I need to sort them??? Does order really matter?
            int i = 0;
            for (String optimalGender: alreadyOptimalMatched.keySet())
            {
                m_finalMatching[i].m_optimalGenderName = optimalGender;
                m_finalMatching[i].m_otherGenderName = alreadyOptimalMatched.get(optimalGender);
                ++i;
            }
            */
        }

        int getMostPreferredOptimalGender(int otherGenderIndex, int matchedOptimalIndex, int proposingOptimalIndex)
        {
            // Get the optimal indices in the other gender list
            Integer matchedOptimalPrefIndex = m_otherToOptimalPrefIndexHashMap.get(otherGenderIndex).get(matchedOptimalIndex);
            Integer proposingOptimalPrefIndex = m_otherToOptimalPrefIndexHashMap.get(otherGenderIndex).get(proposingOptimalIndex);

            // Compare indices
            if(matchedOptimalPrefIndex == proposingOptimalPrefIndex)
            {
                System.out.println("Both Optimal Genders Have Same Index!");
                System.exit(-100);
            }
            if(matchedOptimalPrefIndex < proposingOptimalPrefIndex)
            {
                return matchedOptimalIndex;
            }
            else
            {
                return proposingOptimalIndex;
            }
        }

        int getNextPreference(int optimalIndexToAdvance, int otherGenderIndexWhoRejected)
        {
            int otherGenderIndex = m_optimalToPrefIndexHashMap.get(optimalIndexToAdvance).get(otherGenderIndexWhoRejected);
            otherGenderIndex++;

            return m_optimalPrefList[optimalIndexToAdvance][otherGenderIndex];
        }

        public Result calculateResult()
        {
            matchAndAdvanceConflictsIfFound();
            Result result = new Result();
            result.m_finalMatching = m_finalMatching;
            return result;
        }

    } // MergeTwoMatchingSets

    class CallableThread implements Callable<MergeTwoMatchingSets.Result>
    {
        private MergeTwoMatchingSets m_mergeTwoSet;
        private MergeTwoMatchingSets.Result m_result;

        public CallableThread(MatchingPairIndices[] leftMatching, MatchingPairIndices[] rightMatching)
        {
            m_mergeTwoSet = new MergeTwoMatchingSets(leftMatching, rightMatching);
        }

        @Override
        public MergeTwoMatchingSets.Result call()
        {
            m_result = m_mergeTwoSet.calculateResult();
            return m_result;
        }
    }

    class RunnableThread implements Runnable
    {
        private MergeTwoMatchingSets m_mergeTwoSet;
        private MergeTwoMatchingSets.Result m_result;
        private CopyOnWriteArrayList<MatchingPairIndices[]> m_threadSafeList;

        public RunnableThread(MatchingPairIndices[] leftMatching,
                              MatchingPairIndices[] rightMatching,
                              CopyOnWriteArrayList<MatchingPairIndices[]> threadSafeList)
        {
            m_mergeTwoSet = new MergeTwoMatchingSets(leftMatching, rightMatching);
            m_threadSafeList = threadSafeList;

        }

        @Override
        public void run()
        {
            m_result = m_mergeTwoSet.calculateResult();
            m_threadSafeList.add(m_result.m_finalMatching);
        }
    }

    public String runThread()
    {
        //final long startTime = System.nanoTime();

        MatchingPairIndices[] initialMatchingList = getInitialMatching();

        ArrayList<MatchingPairIndices[]> curretMatchingList = new ArrayList<MatchingPairIndices[]>();

        for(int i = 0; i < initialMatchingList.length; ++i)
        {
            MatchingPairIndices[] matchingPair = new MatchingPairIndices[1];
            matchingPair[0] = initialMatchingList[i];
            curretMatchingList.add(matchingPair);
        }

        int matchingResultSize = curretMatchingList.size();

        String finalMatchingString = "";

        /*
        final int kNumberOfThreads = 20;
        ExecutorService pool =
                new ThreadPoolExecutor(
                        kNumberOfThreads, // core size
                        kNumberOfThreads, // max size
                        60, // idle timeout
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(initialMatchingList.length)); // queue with a size
                        */

        //ExecutorService pool = Executors.newCachedThreadPool();

        while(1 != matchingResultSize)
        {
            //pool.

            final boolean kOddNumberOfChuncks = (0 != matchingResultSize % 2) && (1 != matchingResultSize);
            MatchingPairIndices[] leftOverChunck = new MatchingPairIndices[1];

            if(kOddNumberOfChuncks)
            {
                leftOverChunck =
                        Arrays.copyOf(
                                curretMatchingList.get(matchingResultSize - 1),
                                curretMatchingList.get(matchingResultSize - 1).length);
            }

            //final int kNumberOfThreadPools = (kOddNumberOfChuncks ? (matchingResultSize - 1) : matchingResultSize) / 2;

            ExecutorService pool = Executors.newCachedThreadPool(); // moving this outside causes an error

            //ExecutorService pool = Executors.newFixedThreadPool(kNumberOfThreadPools/2);

            CopyOnWriteArrayList<MatchingPairIndices[]> threadSafeResultList = new CopyOnWriteArrayList<MatchingPairIndices[]>();

            // increment by 2 since we are processing two chuncks at a time
            for(int i = 0; i < (matchingResultSize - 1); i = (i+2))
            {
                // TODO: remove threadsafe list since it isn ot required for left and right
                pool.execute(new RunnableThread(curretMatchingList.get(i), curretMatchingList.get(i+1), threadSafeResultList));
            }

            try
            {
                pool.shutdownNow();

                final int kTimeoutIsSeconds = 10;
                if (!pool.awaitTermination(kTimeoutIsSeconds, TimeUnit.SECONDS))
                {
                    System.out.println("Threads Did Not Terminate Successfully!");
                    System.exit(-100);
                }

                matchingResultSize = kOddNumberOfChuncks ? (threadSafeResultList.size() + 1) : threadSafeResultList.size();

                curretMatchingList = new ArrayList<MatchingPairIndices[]>(threadSafeResultList);

                threadSafeResultList.clear();

                if(kOddNumberOfChuncks)
                {
                    curretMatchingList.add(leftOverChunck);
                }

                if(1 == matchingResultSize)
                {
                    finalMatchingString = FinalMatchingArrayToString(curretMatchingList.get(0));
                }
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }

        if(finalMatchingString.isEmpty())
        {
            System.out.println("Final matching string is empty!");
            System.exit(-100);
        }

        //final long endTime = System.nanoTime();
        //final long totalTime = endTime - startTime;
        //System.out.println("Total time taken for <SMPDivideAndConquer> is "+ totalTime);

        return finalMatchingString;
    }

    public String runCallable()
    {
        MatchingPairIndices[] initialMatchingList = getInitialMatching();

        MatchingPairIndices[][] arrayChuncks = SMPDivideAndConquerImproved.chunkArray(initialMatchingList);

        int matchingResultSize = initialMatchingList.length;

        String finalMatchingString = "";

        //ExecutorService pool = Executors.newFixedThreadPool(matchingResultSize);
        //ExecutorService pool = Executors.newCachedThreadPool();

        while(1 != matchingResultSize)
        {
            ExecutorService pool = Executors.newCachedThreadPool(); // moving this outside causes an error

            List<Callable<MergeTwoMatchingSets.Result>> callables = new ArrayList<Callable<MergeTwoMatchingSets.Result>>();

            // increment by 2 since we are processing two chuncks at a time
            for(int i = 0; i < (matchingResultSize - 1); i = (i+2))
            {
                //pool.submit(new WorkerThread(arrayChuncks[i], arrayChuncks[i+1], i));
                callables.add(new CallableThread(arrayChuncks[i], arrayChuncks[i+1]));
            }

            try
            {
                // There is a hard requirement that results are in order
                List<Future<MergeTwoMatchingSets.Result>> results = pool.invokeAll(callables);

                final boolean kOddNumberOfChuncks = (0 != matchingResultSize % 2) && (1 != matchingResultSize);
                MatchingPairIndices[] leftOverChunck = new MatchingPairIndices[1];

                if(kOddNumberOfChuncks)
                {
                    leftOverChunck =
                            Arrays.copyOf(arrayChuncks[matchingResultSize - 1], arrayChuncks[matchingResultSize - 1].length);
                }

                matchingResultSize = kOddNumberOfChuncks ? (results.size() + 1) : results.size();

                arrayChuncks = new MatchingPairIndices[matchingResultSize][];

                // The results are in order.
                // TODO: Order does not matter the way it is now implemented. Try using threads
                int i = 0;
                for(Future<MergeTwoMatchingSets.Result> result: results)
                {
                    // copy result into arrayChuncks so we can iterate again
                    arrayChuncks[i] = Arrays.copyOf(result.get().m_finalMatching, result.get().m_finalMatching.length);

                    //System.out.println("Results #"+ i);
                    //System.out.println(SMPDivideAndConquer.matchingArrayToString(result.get().m_finalMatching));
                    ++i;
                }

                if(kOddNumberOfChuncks)
                {
                    arrayChuncks[i] = Arrays.copyOf(leftOverChunck, leftOverChunck.length);
                }

                if(1 == matchingResultSize)
                {
                    finalMatchingString = FinalMatchingArrayToString(arrayChuncks[0]);
                }
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
            catch (ExecutionException ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                pool.shutdownNow();
            }
        }

        if(finalMatchingString.isEmpty())
        {
            System.out.println("Final matching string is empty!");
            System.exit(-100);
        }

        return finalMatchingString;
    }

    public static void main(String[] args)
    {

        // Log Start Time
        final long startTime = System.nanoTime();

        /*
        FileInputOutputHelper fileIOHelper = new FileInputOutputHelper();

        final boolean kIsDebug = true;

        FileInputOutputHelper.FileParsedInfo parsedInfo;

        if(kIsDebug)
        {
            String[] customArgs = new String[2];
            customArgs[0] = "out\\production\\StableMatchingParallel\\Random_100.txt";
            customArgs[1] = "m";
            parsedInfo = fileIOHelper.parseInputFile(customArgs);
        }
        else
        {
            parsedInfo = fileIOHelper.parseInputFile(args);
        }
        */


        //FileInputOutputHelper fileIOHelper = new FileInputOutputHelper();
        //FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.getDefaultInfo();

        //String[] customArgs = new String[2];
        //customArgs[0] = "out\\production\\StableMatchingParallel\\WorstCase_8.txt";
        //customArgs[1] = "m";
        //FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.parseInputFile(customArgs);

        //SMPData data = SMPData.loadFromFile("out\\production\\StableMatchingParallel\\WorstCase_8.txt");

        //FileInputOutputHelper fileHelper = new FileInputOutputHelper();
        //FileInputOutputHelper.FileParsedInfo parsedInfo =
        //        fileHelper.parseInputData(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), "m");

        final String kFileName = args[0];
        final String kOptimality = args[1];

        SMPData data = SMPData.loadFromFile(kFileName);

        SMPDivideAndConquerImproved smpDivideAndConquerImproved =
                new SMPDivideAndConquerImproved(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), kOptimality);

        //final String kFinalMatchingString = smpDivideAndConquer.runCallable();
        final String kFinalMatchingString = smpDivideAndConquerImproved.runThread(); // seems a little faster
        System.out.println(kFinalMatchingString);

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <SMPDivideAndConquer> is "+ totalTime);
    }
}

