/*
 * Inspired by
 * "A PARALLEL ALGORITHM TO SOLVE THE STABLE MARRIAGE PROBLEM"
 * S. S. TSENG and R. C. T. LEE - 1984
 */

package smp;

import java.util.*;
import java.util.concurrent.*;

public class SMPDivideAndConquer
{
    private int[][] m_optimalPrefList;
    private int[][] m_otherPrefList;
    private MatchingPair[] m_matchingPairList;

    private String m_optimalGenderPrefix;
    private String m_otherGenderPrefix;

    HashMap<String, FileInputOutputHelper.PreferenceInfo> m_optimalPrefWithDetailsHashMap;
    HashMap<String, FileInputOutputHelper.PreferenceInfo> m_otherPrefWithDetailsHashMap;

    public SMPDivideAndConquer(FileInputOutputHelper.FileParsedInfo parsedInfo)
    {
        //System.out.println("Optimality: " + parsedInfo.optimality);

        if(0 == parsedInfo.optimality.compareToIgnoreCase("m"))
        {
            //System.out.println("Man Optimal");
            m_optimalPrefList = parsedInfo.men_pref;
            m_otherPrefList = parsedInfo.women_pref;

            m_optimalPrefWithDetailsHashMap = parsedInfo.men_pref_with_details_HashMap;
            m_otherPrefWithDetailsHashMap = parsedInfo.women_pref_with_details_HashMap;

            m_optimalGenderPrefix = "m";
            m_otherGenderPrefix = "w";
        }
        else
        {
            //System.out.println("Woman Optimal");
            m_optimalPrefList = parsedInfo.women_pref;
            m_otherPrefList = parsedInfo.men_pref;

            m_optimalPrefWithDetailsHashMap = parsedInfo.women_pref_with_details_HashMap;
            m_otherPrefWithDetailsHashMap = parsedInfo.men_pref_with_details_HashMap;

            m_optimalGenderPrefix = "w";
            m_otherGenderPrefix = "m";
        }
    }

    public class MatchingPair
    {
        public String m_optimalGenderName;
        public String m_otherGenderName;

        public MatchingPair(String optimalGendername, String otherGenderName)
        {
            m_optimalGenderName = optimalGendername;
            m_otherGenderName = otherGenderName;
        }

        // Default constructor
        public MatchingPair()
        {
            m_optimalGenderName = "";
            m_otherGenderName = "";
        }

        public String first()
        {
            return m_optimalGenderName;
        }

        public String second()
        {
            return m_otherGenderName;
        }
    }

    public MatchingPair[] getInitialMatching()
    {
        // Here, we just match every optimal gender with their first preference
        int[] initialMatching = new int[m_optimalPrefList.length];

        m_matchingPairList = new MatchingPair[m_optimalPrefList.length];

        for(int i = 0; i < initialMatching.length; ++i)
        {
            initialMatching[i] = m_optimalPrefList[i][0];
            String optimalGenderName = m_optimalGenderPrefix + (i + 1);
            String otherGenderName = m_otherGenderPrefix + m_optimalPrefList[i][0];

            m_matchingPairList[i] = new MatchingPair(optimalGenderName, otherGenderName);
        }

        //System.out.println("Initial Matching");
        //System.out.println(matchingArrayToString(m_matchingPairList));

        return m_matchingPairList;
    }

    public static MatchingPair[][] chunkArray(MatchingPair[] array)
    {
        MatchingPair[][] output = new MatchingPair[array.length][1];

        for(int i = 0; i < array.length; ++i)
        {
            output[i][0] = array[i];
        }

        return output;
    }

    public static MatchingPair[] concatenate1DArray(MatchingPair[] a, MatchingPair[] b)
    {
        int aLen = a.length;
        int bLen = b.length;

        MatchingPair[] c = new MatchingPair[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static String matchingArrayToString(MatchingPair[] arr)
    {
        String matches = "";

        // This is assuming data is arranged in order which they should be.
        for(int i = 0; i < arr.length; ++i)
        {
            matches += "(" + arr[i].m_optimalGenderName;
            matches += "," + arr[i].m_otherGenderName;
            matches += ")\n";
        }

        return matches;
    }

    public static String FinalMatchingArrayToString(MatchingPair[] arr)
    {
        String matches = "";

        // Order results. This should not be part of the benchmarking since it is just
        // how the matching is returned to user
        HashMap<Integer, Integer> OrderedMatchingInteger = new HashMap<Integer, Integer>();

        // This is assuming data is arranged in order which they should be.
        for(int i = 0; i < arr.length; ++i)
        {
            // use .substring(1) to remove first non-digit characters i.e. just leave the integers
            OrderedMatchingInteger.put(
                    SMPDivideAndConquer.getNumberFromString(arr[i].m_optimalGenderName),
                    SMPDivideAndConquer.getNumberFromString(arr[i].m_otherGenderName));
        }

        for(Integer optimalGender : OrderedMatchingInteger.keySet())
        {
            // use .substring(1) to remove first non-digit characters i.e. just leave the integers
            matches += "(" + optimalGender;
            matches += "," + OrderedMatchingInteger.get(optimalGender);
            matches += ")\n";
        }

        return matches;
    }

    public static int getNumberFromString(String str)
    {
        return Integer.parseInt(str.substring(1));
    }

    class MergeTwoMatchingSets
    {

        private MatchingPair[] m_leftMatching;
        private MatchingPair[] m_rightMatching;
        private MatchingPair[] m_finalMatching;

        class Result
        {
            MatchingPair[] m_finalMatching;
        }

        public MergeTwoMatchingSets(MatchingPair[] leftMatching, MatchingPair[] rightMatching)
        {
            m_leftMatching = leftMatching.clone();
            m_rightMatching = rightMatching.clone();
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

            MatchingPair[] concatenatedMatchingArray = concatenate1DArray(m_leftMatching, m_rightMatching);
            m_finalMatching = Arrays.copyOf(concatenatedMatchingArray, concatenatedMatchingArray.length);

            // Key is other gender
            HashMap<String, String> alreadyOtherGenderMatched = new HashMap<String, String>();

            HashMap<String, String> alreadyOptimalMatched = new HashMap<String, String>();

            for(int matchingIndex = 0; matchingIndex < concatenatedMatchingArray.length; ++matchingIndex)
            {
                MatchingPair currentMatchingPair = concatenatedMatchingArray[matchingIndex];

                if(alreadyOtherGenderMatched.containsKey(currentMatchingPair.second()))
                {
                    while(alreadyOtherGenderMatched.containsKey(currentMatchingPair.second()))
                    {
                        String otherGender = currentMatchingPair.second();
                        String optimalMatched = alreadyOtherGenderMatched.get(otherGender);
                        String optimalProposing = currentMatchingPair.first();

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

                alreadyOtherGenderMatched.put(currentMatchingPair.second(), currentMatchingPair.first());
                alreadyOptimalMatched.put(currentMatchingPair.first(), currentMatchingPair.second());
            } // for

            // TODO: find better way! I need to sort them??? Does order really matter?
            int i = 0;
            for (String optimalGender: alreadyOptimalMatched.keySet())
            {
                m_finalMatching[i].m_optimalGenderName = optimalGender;
                m_finalMatching[i].m_otherGenderName = alreadyOptimalMatched.get(optimalGender);
                ++i;
            }
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
            final boolean kOddNumberOfChuncks = (0 != matchingResultSize % 2) && (1 != matchingResultSize);
            MatchingPair[] leftOverChunck = new MatchingPair[1];

            if(kOddNumberOfChuncks)
            {
                leftOverChunck =
                        Arrays.copyOf(
                                curretMatchingList.get(matchingResultSize - 1),
                                curretMatchingList.get(matchingResultSize - 1).length);
            }

            final int kNumberOfThreadPools = (kOddNumberOfChuncks ? (matchingResultSize - 1) : matchingResultSize) / 2;

            ExecutorService pool = Executors.newCachedThreadPool();

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

        MatchingPair[][] arrayChuncks = SMPDivideAndConquer.chunkArray(initialMatchingList);

        int matchingResultSize = initialMatchingList.length;

        String finalMatchingString = "";

        while(1 != matchingResultSize)
        {
            //ExecutorService pool = Executors.newFixedThreadPool(matchingResultSize);
            ExecutorService pool = Executors.newCachedThreadPool();

            List<Callable<MergeTwoMatchingSets.Result>> callables = new ArrayList<Callable<MergeTwoMatchingSets.Result>>();

            // increment by 2 since we are processing two chuncks at a time
            for(int i = 0; i < (matchingResultSize - 1); i = (i+2))
            {
                // TODO: I want results to be added to queue in right order
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

        FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.parseInputFile(args);


        //FileInputOutputHelper fileIOHelper = new FileInputOutputHelper();
        //FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.getDefaultInfo();

        //String[] customArgs = new String[2];
        //customArgs[0] = "out\\production\\StableMatchingParallel\\Random_5.txt";
        //customArgs[1] = "w";
        //FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.parseInputFile(customArgs);

        SMPDivideAndConquer smpDivideAndConquer = new SMPDivideAndConquer(parsedInfo);

        //final String kFinalMatchingString = smpDivideAndConquer.runCallable();
        final String kFinalMatchingString = smpDivideAndConquer.runThread(); // seems a little faster
        System.out.println(kFinalMatchingString);

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <SMPDivideAndConquer> is "+ totalTime);
    }
}
