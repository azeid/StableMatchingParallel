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

    //HashMap<Integer, Integer> m_optimalToPrefIndexHashMap;
    //HashMap<Integer, Integer> m_otherToOptimalPrefIndexHashMap;

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
/*
        m_optimalToPrefIndexHashMap = new HashMap<Integer, Integer>();
        m_otherToOptimalPrefIndexHashMap = new HashMap<Integer, Integer>();

        for(int i = 0; i < m_size; ++i)
        {
            PrefInfo manPrefInfo = new PrefInfo();
            PrefInfo womanPrefInfo = new PrefInfo();

            // This is the map for indices for women preferences
            //HashMap<Integer, Integer> manPrefIndexHashMap = new HashMap<Integer, Integer>();

            // This is the map for indices for men preferences
            //HashMap<Integer, Integer> womanPrefIndexHashMap = new HashMap<Integer, Integer>();

            for(int j = 0; j < m_size; ++j)
            {
                m_optimalToPrefIndexHashMap.put();


            }

            manPrefInfo.m_Index = i;
            womanPrefInfo.m_Index = i;

        }
        */
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
                        Integer otherGenderIndex = currentMatchingPair.m_otherGenderIndex;
                        Integer optimalMatchedIndex = alreadyOtherGenderMatched.get(currentMatchingPair.m_otherGenderIndex);
                        String optimalProposing = currentMatchingPair.m_optimalGenderIndex;

                        String mostPreferredOptimal = getMostPreferredOptimalGender(otherGender, optimalMatched, optimalProposing);

                        if(0 == mostPreferredOptimal.compareToIgnoreCase(optimalMatched))
                        {
                            // Keep current matching, advance conflicting optimal gender to his next
                            // preferred other gender
                            String nextPreferredOtherGender = getNextPreference(optimalProposing, otherGender);

                            currentMatchingPair.m_otherGenderName = nextPreferredOtherGender;
                        }
                        else
                        {
                            // This means that we need to reject already matched and match
                            // the more preferred
                            alreadyOtherGenderMatched.put(currentMatchingPair.second(), optimalProposing);
                            alreadyOptimalMatched.put(optimalProposing, currentMatchingPair.second());
                            // Advance conflicting optimal gender to his next
                            // preferred other gender
                            String nextPreferredOtherGender = getNextPreference(optimalMatched, otherGender);

                            currentMatchingPair.m_optimalGenderName = optimalMatched;
                            currentMatchingPair.m_otherGenderName = nextPreferredOtherGender;
                        }
                    } // while
                } // if

                alreadyOtherGenderMatched.put(currentMatchingPair.m_otherGenderIndex, currentMatchingPair.m_optimalGenderIndex);
                alreadyOptimalMatched.put(currentMatchingPair.m_optimalGenderIndex, currentMatchingPair.m_otherGenderIndex;
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

        String getMostPreferredOptimalGender(String otherGender, String matchedOptimalName, String proposingOptimalName)
        {
            FileInputOutputHelper.PreferenceInfo prefInfo = m_otherPrefWithDetailsHashMap.get(otherGender);
            int matchedOptimalPrefIndex = prefInfo.m_prefNameToIndexHashMap.get(matchedOptimalName);
            int proposingOptimalPrefIndex = prefInfo.m_prefNameToIndexHashMap.get(proposingOptimalName);

            if(matchedOptimalPrefIndex == proposingOptimalPrefIndex)
            {
                System.out.println("Both Optimal Genders Have Same Index!");
                System.exit(-100);
            }
            if(matchedOptimalPrefIndex < proposingOptimalPrefIndex)
            {
                return matchedOptimalName;
            }
            else
            {
                return proposingOptimalName;
            }
        }

        String getNextPreference(String optimalToAdvance, String otherGenderWhoRejected)
        {
            FileInputOutputHelper.PreferenceInfo prefInfo = m_optimalPrefWithDetailsHashMap.get(optimalToAdvance);

            int otherGenderIndex = prefInfo.m_prefNameToIndexHashMap.get(otherGenderWhoRejected);
            int otherNextPreferredGenderIndex = otherGenderIndex + 1;

            int optimalGenderIndex = prefInfo.m_Index;

            if(otherNextPreferredGenderIndex > (m_optimalPrefList[optimalGenderIndex].length - 1))
            {
                System.out.println("Index of next preference other gender is out of bounds!");
                System.exit(-100);
            }

            String nextOtherGenderPrefName =
                    m_otherGenderPrefix + m_optimalPrefList[optimalGenderIndex][otherNextPreferredGenderIndex];

            return nextOtherGenderPrefName;
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

        public CallableThread(MatchingPair[] leftMatching, MatchingPair[] rightMatching)
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
        private CopyOnWriteArrayList<MatchingPair[]> m_threadSafeList;

        public RunnableThread(MatchingPair[] leftMatching,
                              MatchingPair[] rightMatching,
                              CopyOnWriteArrayList<MatchingPair[]> threadSafeList)
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

        MatchingPair[] initialMatchingList = getInitialMatching();

        ArrayList<MatchingPair[]> curretMatchingList = new ArrayList<MatchingPair[]>();

        for(int i = 0; i < initialMatchingList.length; ++i)
        {
            MatchingPair[] matchingPair = new MatchingPair[1];
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
            MatchingPair[] leftOverChunck = new MatchingPair[1];

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

            CopyOnWriteArrayList<MatchingPair[]> threadSafeResultList = new CopyOnWriteArrayList<MatchingPair[]>();

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

                curretMatchingList = new ArrayList<MatchingPair[]>(threadSafeResultList);

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
        MatchingPair[] initialMatchingList = getInitialMatching();

        MatchingPair[][] arrayChuncks = SMPDivideAndConquerImproved.chunkArray(initialMatchingList);

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
                MatchingPair[] leftOverChunck = new MatchingPair[1];

                if(kOddNumberOfChuncks)
                {
                    leftOverChunck =
                            Arrays.copyOf(arrayChuncks[matchingResultSize - 1], arrayChuncks[matchingResultSize - 1].length);
                }

                matchingResultSize = kOddNumberOfChuncks ? (results.size() + 1) : results.size();

                arrayChuncks = new MatchingPair[matchingResultSize][];

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


        SMPDivideAndConquerImproved smpDivideAndConquerImproved = new SMPDivideAndConquerImproved(parsedInfo);

        //final String kFinalMatchingString = smpDivideAndConquer.runCallable();
        final String kFinalMatchingString = smpDivideAndConquerImproved.runThread(); // seems a little faster
        System.out.println(kFinalMatchingString);

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <SMPDivideAndConquer> is "+ totalTime);
    }
}

