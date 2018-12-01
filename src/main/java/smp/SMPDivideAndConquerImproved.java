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

    private int[][] m_optimalToOtherIndex;
    private int[][] m_otherToOptimalIndex;

    //private HashMap<Integer, HashMap<Integer, Integer>> m_otherToOptimalPrefIndexHashMap;
    //private HashMap<Integer, HashMap<Integer, Integer>>  m_optimalToActualPrefIndexHashMap;

    //private HashMap<Integer, HashMap<Integer, Integer>> m_otherToOptimalPrefIndexHashMap;
    //private HashMap<Integer, HashMap<Integer, Integer>>  m_optimalToActualPrefIndexHashMap;

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

        //m_otherToOptimalPrefIndexHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();

        //m_optimalToActualPrefIndexHashMap = new HashMap<Integer, HashMap<Integer, Integer>>();

        /*
        for(int i = 0; i < m_size; ++i)
        {
            HashMap<Integer, Integer> optimalActualPrefHashMap = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> otherToOptimalPrefHashMap = new HashMap<Integer, Integer>();

            for(int j = 0; j < m_size; ++j)
            {
                optimalActualPrefHashMap.put(m_optimalPrefList[i][j], j + 1);
                otherToOptimalPrefHashMap.put(m_otherPrefList[i][j], j + 1);
            }

            //m_optimalToActualPrefIndexHashMap.put(i + 1, optimalActualPrefHashMap);
            //m_otherToOptimalPrefIndexHashMap.put(i + 1, otherToOptimalPrefHashMap);
        }
        */

        m_optimalToOtherIndex = new int[m_size][m_size];
        m_otherToOptimalIndex = new int[m_size][m_size];

        for(int i = 0; i < m_size; ++i)
        {
            for(int j = 0; j < m_size; ++j)
            {
                //m_optimalToOtherIndex[m_otherPrefList[i][j]-1] = j;
                //m_otherToOptimalIndex[m_optimalPrefList[i][j]-1] = j;

                m_otherToOptimalIndex[i][j] = m_otherPrefList[i][j]-1;
                m_optimalToOtherIndex[i][j] = m_optimalPrefList[i][j]-1;
            }
        }

    }

    public MatchingPairIndices[] getInitialMatching()
    {
        // Here, we just match every optimal gender with their first preference
        m_matchingPairList = new MatchingPairIndices[m_size];

        for(int i = 0; i < m_size; ++i)
        {
            MatchingPairIndices matchingPair = new MatchingPairIndices(i, m_optimalPrefList[i][0] - 1);
            m_matchingPairList[i] = matchingPair;
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

    public String FinalMatchingArrayToString(MatchingPairIndices[] arr)
    {
        // Ordered results.
        StringBuilder matches = new StringBuilder();
        for(int i = 0; i < arr.length; ++i)
        {

            matches.append("(" + (arr[i].m_optimalGenderIndex + 1));
            matches.append("," + (arr[i].m_otherGenderIndex + 1));
            matches.append(")\n");
        }

        return matches.toString();
    }

    class MergeTwoMatchingSets
    {

        private MatchingPairIndices[] m_leftMatching;
        private MatchingPairIndices[] m_rightMatching;
        private MatchingPairIndices[] m_finalMatching;

        public MergeTwoMatchingSets(MatchingPairIndices[] leftMatching, MatchingPairIndices[] rightMatching)
        {
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

            // TODO: is this needed
            m_finalMatching = Arrays.copyOf(concatenatedMatchingArray, concatenatedMatchingArray.length);

            // Key is other gender
            //HashMap<Integer, Integer> alreadyOtherGenderMatched = new HashMap<Integer, Integer>();

            //HashMap<Integer, Integer> alreadyOptimalMatched = new HashMap<Integer, Integer>();

            int[] alreadyMatchedToOptimal = new int[concatenatedMatchingArray.length];
            int[] alreadyMatchedToOther = new int[concatenatedMatchingArray.length];

            for(int matchingIndex = 0; matchingIndex < concatenatedMatchingArray.length; ++matchingIndex)
            {
                MatchingPairIndices currentMatchingPair = concatenatedMatchingArray[matchingIndex];

                if(0 != alreadyMatchedToOther[currentMatchingPair.m_otherGenderIndex])
                {
                    while(0 != alreadyMatchedToOther[currentMatchingPair.m_otherGenderIndex])
                    {
                        int otherGenderIndex = currentMatchingPair.m_otherGenderIndex;
                        int optimalMatchedIndex = alreadyMatchedToOther[currentMatchingPair.m_otherGenderIndex];
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

                            alreadyMatchedToOptimal[optimalMatchedIndex] = 0;

                            alreadyMatchedToOther[currentMatchingPair.m_otherGenderIndex] = currentMatchingPair.m_optimalGenderIndex;
                            alreadyMatchedToOptimal[currentMatchingPair.m_optimalGenderIndex] = currentMatchingPair.m_otherGenderIndex;
                            // Advance conflicting optimal gender to his next
                            // preferred other gender
                            int nextPreferredOtherGender = getNextPreference(optimalMatchedIndex, otherGenderIndex);

                            currentMatchingPair.m_optimalGenderIndex = optimalMatchedIndex;
                            currentMatchingPair.m_otherGenderIndex = nextPreferredOtherGender;
                        }
                    } // while
                } // if

                alreadyMatchedToOptimal[currentMatchingPair.m_optimalGenderIndex] = currentMatchingPair.m_otherGenderIndex;
                alreadyMatchedToOther[currentMatchingPair.m_otherGenderIndex] = currentMatchingPair.m_optimalGenderIndex;
            } // for

            for (int i = 0; i < alreadyMatchedToOptimal.length; ++i)
            {
                m_finalMatching[i].m_optimalGenderIndex = alreadyMatchedToOther[i];
                m_finalMatching[i].m_otherGenderIndex = alreadyMatchedToOptimal[i];
            }
        }

        int getMostPreferredOptimalGender(int otherGenderIndex, int matchedOptimalIndex, int proposingOptimalIndex)
        {
            int matchedOptimalPrefIndex = m_otherToOptimalIndex[otherGenderIndex][matchedOptimalIndex];
            int proposingOptimalPrefIndex = m_otherToOptimalIndex[otherGenderIndex][proposingOptimalIndex];

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
            // No need to increment this since everything is saved as 1-based
            int otherGenderIndex = m_optimalToOtherIndex[optimalIndexToAdvance][otherGenderIndexWhoRejected];
            otherGenderIndex++;
            int nextPref = m_optimalPrefList[optimalIndexToAdvance][otherGenderIndex];

            return nextPref;
        }

        public MatchingPairIndices[] calculateResult()
        {
            matchAndAdvanceConflictsIfFound();
            return m_finalMatching;
        }

    } // MergeTwoMatchingSets

    class CallableThread implements Callable<MatchingPairIndices[]>
    {
        private MergeTwoMatchingSets m_mergeTwoSet;
        private MatchingPairIndices[] m_result;

        public CallableThread(MatchingPairIndices[] leftMatching, MatchingPairIndices[] rightMatching)
        {
            m_mergeTwoSet = new MergeTwoMatchingSets(leftMatching, rightMatching);
        }

        @Override
        public MatchingPairIndices[] call()
        {
            m_result = m_mergeTwoSet.calculateResult();
            return m_result;
        }
    }

    class RunnableThread implements Runnable
    {
        private MergeTwoMatchingSets m_mergeTwoSet;
        private MatchingPairIndices[] m_result;
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
            m_threadSafeList.add(m_result);
        }
    }

    /*
    public String runThread()
    {
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

        while(1 != matchingResultSize)
        {
            final boolean kOddNumberOfChuncks = (0 != matchingResultSize % 2) && (1 != matchingResultSize);
            MatchingPairIndices[] leftOverChunck = new MatchingPairIndices[1];

            if(kOddNumberOfChuncks)
            {
                leftOverChunck =
                        Arrays.copyOf(
                                curretMatchingList.get(matchingResultSize - 1),
                                curretMatchingList.get(matchingResultSize - 1).length);
            }

            ExecutorService pool = Executors.newCachedThreadPool(); // moving this outside causes an error

            CopyOnWriteArrayList<MatchingPairIndices[]> threadSafeResultList = new CopyOnWriteArrayList<MatchingPairIndices[]>();

            // increment by 2 since we are processing two chuncks at a time
            for(int i = 0; i < (matchingResultSize - 1); i = (i+2))
            {
                pool.execute(new RunnableThread(curretMatchingList.get(i), curretMatchingList.get(i+1), threadSafeResultList));
            }


            try
            {
                pool.shutdown();

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

        return finalMatchingString;
    }

    */
    public String runCallable()
    {
        MatchingPairIndices[] initialMatchingList = getInitialMatching();

        MatchingPairIndices[][] arrayChuncks = SMPDivideAndConquerImproved.chunkArray(initialMatchingList);

        int matchingResultSize = initialMatchingList.length;

        String finalMatchingString = "";
        ExecutorService pool = Executors.newCachedThreadPool();

        while(1 != matchingResultSize)
        {
            List<Callable<MatchingPairIndices[]>> callables = new ArrayList<Callable<MatchingPairIndices[]>>();

            // increment by 2 since we are processing two chuncks at a time
            for(int i = 0; i < (matchingResultSize - 1); i = (i+2))
            {
                callables.add(new CallableThread(arrayChuncks[i], arrayChuncks[i+1]));
            }

            try
            {
                List<Future<MatchingPairIndices[]>> results = pool.invokeAll(callables);

                final boolean kOddNumberOfChuncks = (0 != matchingResultSize % 2) && (1 != matchingResultSize);
                MatchingPairIndices[] leftOverChunck = new MatchingPairIndices[1];

                if(kOddNumberOfChuncks)
                {
                    leftOverChunck =
                            Arrays.copyOf(arrayChuncks[matchingResultSize - 1], arrayChuncks[matchingResultSize - 1].length);
                }

                matchingResultSize = kOddNumberOfChuncks ? (results.size() + 1) : results.size();

                // TODO: Can this be removed
                arrayChuncks = new MatchingPairIndices[matchingResultSize][];

                int i = 0;
                for(Future<MatchingPairIndices[]> result: results)
                {
                    // copy result into arrayChuncks so we can iterate again
                    arrayChuncks[i] = Arrays.copyOf(result.get(), result.get().length);

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
        }

        pool.shutdown();

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

        final boolean kIsDebug = true;
        String kFileName;
        String kOptimality;

        if(kIsDebug)
        {
            kFileName = "target\\classes\\WorstCase_3.txt";
            kOptimality = "m";
        }
        else
        {
            kFileName = args[0];
            kOptimality = args[1];
        }

        SMPData data = SMPData.loadFromFile(kFileName);

        SMPDivideAndConquerImproved smpDivideAndConquerImproved =
                new SMPDivideAndConquerImproved(data.getPreferencesOne(), data.getPreferencesTwo(), data.getSize(), kOptimality);

        final String kFinalMatchingString = smpDivideAndConquerImproved.runCallable();
        //final String kFinalMatchingString = smpDivideAndConquerImproved.runThread(); // seems a little faster
        System.out.println(kFinalMatchingString);

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <SMPDivideAndConquerImproved> is "+ totalTime);
    }
}

