/*
 * Inspired by
 * "A PARALLEL ALGORITHM TO SOLVE THE STABLE MARRIAGE PROBLEM"
 * S. S. TSENG and R. C. T. LEE - 1984
 */

import javafx.util.Pair;

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

    //private ConcurrentLinkedQueue<int[]> m_resultsConcurrentQueue;

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

    /*
    public class SplitMatching
    {
        int[][] m_leftMatching;
        int[][] m_rightMatching;
        int[] m_optimalGenderOffsetIndex;
    }
    */

    // TODO: do I need to override lessthan for hashmap to order these properly?
    public class MatchingPair
    {
        public String m_optimalGenderName;
        public String m_otherGenderName;

        //public int m_optimalGenderIndex;
        //public int m_otherGenderIndex;

        /*
        public MatchingPair(String optimalGendername, int optimalGenderIndex, String otherGenderName, int otherGenderIndex)
        {
            m_optimalGenderName   = optimalGendername;
            m_otherGenderName = otherGenderName;
            m_optimalGenderIndex = optimalGenderIndex;
            m_otherGenderIndex = otherGenderIndex;
        }
        */

        public MatchingPair(String optimalGendername, String otherGenderName)
        {
            m_optimalGenderName   = optimalGendername;
            m_otherGenderName = otherGenderName;
            //m_optimalGenderIndex = optimalGenderIndex;
            //m_otherGenderIndex = otherGenderIndex;
        }

        // Default constructor
        public MatchingPair()
        {
            m_optimalGenderName = "";
            m_otherGenderName = "";
            //m_optimalGenderIndex = optimalGenderIndex;
            //m_otherGenderIndex = otherGenderIndex;
        }

        public String first()
        {
            return m_optimalGenderName;
        }

        public String second()
        {
            return m_otherGenderName;
        }

        /*
        public String first()
        {
            return getOptimalGenderName();
        }

        public String second()
        {
            return getOtherGenderName();
        }


        public String getOptimalGenderName()
        {
            return m_optimalGenderName;
        }

        public void setOptimalGenderName(String m_optimalGenderName)
        {
            this.m_optimalGenderName = m_optimalGenderName;
        }

        public String getOtherGenderName()
        {
            return m_otherGenderName;
        }

        public void setOtherGenderName(String m_otherGenderName)
        {
            this.m_otherGenderName = m_otherGenderName;
        }
        */
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
            int optimalGenderIndex = i;
            int otherGenderIndex = 0;
            /*
            m_matchingPairList[i] = new MatchingPair(
                    optimalGenderName,
                    optimalGenderIndex,
                    otherGenderName,
                    otherGenderIndex);
                    */

            m_matchingPairList[i] = new MatchingPair(optimalGenderName, otherGenderName);
        }

        //System.out.println("Initial Matching");
        //System.out.println(matchingArrayToString(m_matchingPairList));

        return m_matchingPairList;
    }

    /*
    public int getResultsMatchingSize()
    {
        return m_resultsConcurrentQueue.size();
    }
    */

    /*
    public void clearResultsQueue()
    {
        m_resultsConcurrentQueue.clear();
    }

    public int[] getResult()
    {
        if(m_resultsConcurrentQueue.isEmpty())
        {
            System.out.println("Cannot remove element from m_resultsConcurrentQueue since it is empty!");
            System.exit(-100);
        }

        // TODO: remove reduces size, is this fine? do I need to re-initialize size
        return m_resultsConcurrentQueue.remove();
    }
    */

    // From https://gist.github.com/lesleh/7724554
    public static MatchingPair[][] chunkArray(MatchingPair[] array, int chunkSize)
    {
        //int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        MatchingPair[][] output = new MatchingPair[array.length][1];

        for(int i = 0; i < array.length; ++i)
        {
            output[i][0] = array[i];
        }

        // TODO: I don't want a generic chunking function
        /*
        for(int i = 0; i < numOfChunks; ++i)
        {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            int[] temp = new int[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }
        */

        /*
        for(int i = 0; i < output.length; ++i)
        {
            System.out.println("Chuncks");
            System.out.println(matchingArrayToString(output[i]));
        }
        */

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

    class WorkerThread implements /*Runnable,*/ Callable<WorkerThread.Result>
    {
        private MatchingPair[] m_leftMatching;
        private MatchingPair[] m_rightMatching;
        private MatchingPair[] m_finalMatching;

        class Result
        {
            MatchingPair[] m_finalMatching;
        }

        public WorkerThread(MatchingPair[] leftMatching, MatchingPair[] rightMatching)
        {
            /*
            if(0 == leftMatching.length || 0 == rightMatching.length)
            {
                System.out.println("Left and Right Matching Sizes Don't Match!");
                System.exit(-100);
            }
            else if(leftMatching.length != rightMatching.length)
            {
                System.out.println("Left and Right Matching Sizes Don't Match!");
                System.exit(-100);
            }
            */

            m_leftMatching = leftMatching.clone();
            m_rightMatching = rightMatching.clone();
        }

        /*
        @Override
        public void run()
        {
            matchAndAdvanceConflictsIfFound();

            // add result to queue
            m_resultsConcurrentQueue.add(m_finalMatching);
        }
        */


        @Override
        public Result call()
        {
            matchAndAdvanceConflictsIfFound();
            Result result = new Result();
            result.m_finalMatching = m_finalMatching;
            return result;
        }

        public void matchAndAdvanceConflictsIfFound()
        {
            // example(M3, W4)   (M4, W2)
            //              \    /
            //               \ /
            //       (M3, W4),(M4, W2)
            //
            // example(M1, W3)   (M2, W3)
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
                /*
                if(1 != m_finalMatching[matchingIndex].length)
                {
                    System.out.println("Optimal Gender is Matched to More Than One!");
                    System.exit(-100);
                }
                */

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
                            /*
                            alreadyOptimalMatchedInteger.put(
                                    SMPDivideAndConquer.getNumberFromString(optimalProposing),
                                    SMPDivideAndConquer.getNumberFromString(currentMatchingPair.second()));
                                    */

                            // Advance conflicting optimal gender to his next
                            // preferred other gender
                            String nextPreferredOtherGender = getNextPreference(optimalMatched, otherGender);

                            currentMatchingPair.m_optimalGenderName = optimalMatched;
                            currentMatchingPair.m_otherGenderName = nextPreferredOtherGender;
                        }

                        /*
                        int optimalGenderMatchedToOtherGender = alreadyOtherGenderMatched.get(concatenatedMatchingArray[matchingIndex]);
                        int mostPreferredOptimalGenderToOtherGender =
                                getMostPreferredOptimalGender(
                                        concatenatedMatchingArray[matchingIndex],
                                        optimalGenderMatchedToOtherGender,
                                        matchingIndex);

                        int optimalGenderToAdvance = 0;
                        if(mostPreferredOptimalGenderToOtherGender != optimalGenderMatchedToOtherGender)
                        {
                            // This means that we need to reject already matched and match
                            // the more preferred
                            //rejectedIndex = optimalGenderMatchedToOtherGender;
                            //alreadyOtherGenderMatched.remove(currentOtherGenderMatching);

                            alreadyOtherGenderMatched.put(
                                    concatenatedMatchingArray[matchingIndex],
                                    mostPreferredOptimalGenderToOtherGender);
                            //m_finalMatching[matchingIndex] = concatenatedMatchingArray[matchingIndex];

                            currentOtherGenderMatching =
                                    getNextPreference(optimalGenderMatchedToOtherGender, concatenatedMatchingArray[matchingIndex]);

                            alreadyOtherGenderMatched.put(
                                    optimalGenderMatchedToOtherGender,
                                    currentOtherGenderMatching);

                            //optimalGenderToAdvance = optimalGenderMatchedToOtherGender;
                        }
                        else
                        {
                            // Keep current matching, advance conflicting optimal gender to his next
                            // preferred other gender
                            //optimalGenderToAdvance = matchingIndex;

                            currentOtherGenderMatching =
                                    getNextPreference(matchingIndex, concatenatedMatchingArray[matchingIndex]);
                        }
                        */

                        // Now find the next most preferred other gender for rejected optimal person
                        //currentOtherGenderMatching =
                         //       getNextPreference(
                         //               optimalGenderToAdvance,
                        //                (rejectedIndex == -1) ? concatenatedMatchingArray[matchingIndex] : rejectedIndex);
                    } // while
                } // if

                alreadyOtherGenderMatched.put(currentMatchingPair.second(), currentMatchingPair.first());
                alreadyOptimalMatched.put(currentMatchingPair.first(), currentMatchingPair.second());
                /*
                alreadyOptimalMatchedInteger.put(
                        SMPDivideAndConquer.getNumberFromString(currentMatchingPair.first()),
                        SMPDivideAndConquer.getNumberFromString(currentMatchingPair.second()));
                        */

                /*
                else
                {
                    // Key is other gender and value is optimal gender
                    alreadyOtherGenderMatched.put(currentMatchingPair.second(), currentMatchingPair.first());
                    //m_finalMatching[matchingIndex] = currentOtherGenderMatching;
                }
                */
            } // for

            // TODO: find better way!
            int i = 0;
            for (String optimalGender: alreadyOptimalMatched.keySet())
            {
                m_finalMatching[i].m_optimalGenderName = optimalGender;
                m_finalMatching[i].m_otherGenderName = alreadyOptimalMatched.get(optimalGender);
                ++i;
            }

            //Arrays.sort(m_finalMatching);
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
            /*
            // m_optimalGenderStartIndex is needed to know where to look in the preference list since
            // matching are split into different subsets. Also, this does not require us to make any
            // unnecessary copies.
            // Values in table are 1-based, so subtract one to get index
            int otherGenderIndex = otherGender - 1;
            //int actualMatchedOptimalIndex = (matchedOptimalIndex + m_optimalGenderOffsetIndex);
            //int actualProposingOptimalIndex = (proposingOptimalIndex + m_optimalGenderOffsetIndex);

            int matchedOptimalPref = findIndex(m_otherPrefList[otherGenderIndex], (matchedOptimalIndex + 1));

            int proposingOptimalPref = findIndex(m_otherPrefList[otherGenderIndex], (proposingOptimalIndex + 1));

            if(matchedOptimalPref == proposingOptimalPref)
            {
                System.out.println("Both Optimal Genders Have Same Index!");
                System.exit(-100);
            }
            if(matchedOptimalPref < proposingOptimalPref)
            {
                return matchedOptimalPref;
            }
            else
            {
                return proposingOptimalPref;
            }
            */
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

            /*
            // m_optimalGenderStartIndex is needed to know where to look in the preference list since
            // matching are split into different subsets. Also, this does not require us to make any
            // unnecessary copies.
            int actualOptimalIndex = (optimalToAdvance + m_optimalGenderOffsetIndex);
            int actualOtherGenderIndex = getOtherGenderIndex(actualOptimalIndex, currentOtherGender);
            int nextPref = m_optimalPrefList[actualOptimalIndex][actualOtherGenderIndex + 1];

            return nextPref;
            */
        }

        // TODO: maybe getting this information can be done in a smarter way?
        int getOtherGenderIndex(int optimalGenderIndex, int OtherGender)
        {
            return findIndex(m_optimalPrefList[optimalGenderIndex], OtherGender);
        }

        public int findIndex(int arr[], int t)
        {
            int index = -1;
            /*
            // binarySearch requires array to be sored
            int index = Arrays.binarySearch(arr, t);
            */

            for(int i = 0; i < arr.length; ++i)
            {
                if(arr[i] == t)
                {
                    return i;
                }
            }

            if(index < 0)
            {
                System.out.println("Index of item was not found. This should not happen!");
                System.exit(-100);
            }

            return index;
        }

        public MatchingPair[] getFinalMatching()
        {
            return m_finalMatching;
        }

        /*
        public int getOptimalGenderOffsetIndex()
        {
            return m_optimalGenderOffsetIndex;
        }
        */

        public String finalMatchingToString()
        {
           return matchingArrayToString(m_finalMatching);
        }

        /*
        // TODO: can this be optimized?
        public int [][] concatenateArrays()
        {
            if(m_leftMatching.length != m_rightMatching.length ||
                    m_leftMatching[0].length != m_rightMatching[0].length)
            {
                System.out.println("Left and Right Matching Sizes Don't Match!");
                System.exit(-100);
            }

            m_finalMatching = new int[m_leftMatching.length + m_rightMatching.length][m_leftMatching[0].length + m_rightMatching[0].length];
            int finalIndex = 0;
            for(int i = 0; i < m_leftMatching.length; ++i)
            {
                m_finalMatching[finalIndex] = m_leftMatching[i];
                ++finalIndex;
            }

            for(int j = 0; j < m_rightMatching.length; ++j)
            {
                m_finalMatching[finalIndex] = m_rightMatching[j];
                ++finalIndex;
            }

            return m_finalMatching;
        }
        */
    }

    /*
    public String finalMatchingToString()
    {
        String matches = "";


        if(1 != m_resultsConcurrentQueue.size())
        {
            System.out.println("m_resultsConcurrentQueue contains unexpected size!");
            System.exit(-100);
        }

        // TODO: this is assuming data is arranged in order
        int[] finalMatching = m_resultsConcurrentQueue.remove();
        for(int i = 0; i < finalMatching.length; ++i)
        {
            Integer currentOptimalGenderIndex = i + 1;
            matches += "(" + currentOptimalGenderIndex;
            matches += "," + finalMatching[i] + 1;
            matches += ")\n";
        }

        return matches;
    }
    */

    public String run()
    {
        MatchingPair[] initialMatchingList = getInitialMatching();

        /*
        // for now only allow even number of data
        if(0 != initialMatchingList.length % 2)
        {
            System.out.println("Data size must be even!");
            System.exit(-100);
        }
        */

        // initially, kChunckSize would be the size of individual matching
        //final int kChunckSize = initialMatching.length;
        final int kChunckSize = 1;

        MatchingPair[][] arrayChuncks = SMPDivideAndConquer.chunkArray(initialMatchingList, kChunckSize);

        int matchingResultSize = initialMatchingList.length;

        String finalMatchingString = "";

        while(1 != matchingResultSize)
        {
            ExecutorService pool = Executors.newFixedThreadPool(matchingResultSize);
            //int[][] returnedMatchings;

            List<Callable<WorkerThread.Result>> callables = new ArrayList<Callable<WorkerThread.Result>>();
            // increment by 2 since we are processing two chuncks at a time

            for(int i = 0; i < (matchingResultSize - 1); i = (i+2))
            {
                // TODO: I want results to be added to queue in right order
                //pool.submit(new WorkerThread(arrayChuncks[i], arrayChuncks[i+1], i));
                int indexOffset = arrayChuncks[0].length * i;
                callables.add(new WorkerThread(arrayChuncks[i], arrayChuncks[i+1]));
            }

            try
            {
                // There is a hard requirement that results are in order
                List<Future<WorkerThread.Result>> results = pool.invokeAll(callables);

                final boolean kOddNumberOfChuncks = (0 != matchingResultSize % 2) && (1 != matchingResultSize);
                MatchingPair[] leftOverChunck = new MatchingPair[1];

                if(kOddNumberOfChuncks)
                {
                    leftOverChunck =
                            Arrays.copyOf(arrayChuncks[matchingResultSize - 1], arrayChuncks[matchingResultSize - 1].length);
                }


                matchingResultSize = kOddNumberOfChuncks ? (results.size() + 1) : results.size();

                //arrayChuncks = Arrays.copyOf(concatenatedMatchingArray, concatenatedMatchingArray.length);
                arrayChuncks = new MatchingPair[matchingResultSize][];

                //for(Future<int[]> result: results)
                //{
                //    int[] row = result.get();
                //    arrayChuncks[i] = row.toArray(new String[row.size()]);
                //}

                // The results are in order
                int i = 0;
                for(Future<WorkerThread.Result> result: results)
                {
                    // copy result into arrayChuncks so we can iterate again
                    //arrayChuncks[result.get().m_optimalGenderOffsetIndex] = result.get().m_finalMatching;
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

            /*
            try
            {
                // wait for results
                // TODO: check timeout given large input data
                final long kTimeoutInSeconds = 10;
                if (pool.awaitTermination(kTimeoutInSeconds, TimeUnit.SECONDS))
                {
                    System.out.println("Everything finished!");
                }
                else
                {
                    System.out.println("Timeout expired before threads are done!");
                    System.exit(-100);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            */

            //finalMatchingsSize = getResultsMatchingSize();
        }

        //final String kFinalMatchingString = finalMatchingToString();

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
        //customArgs[0] = "out\\production\\StableMatchingParallel\\Random_200.txt";
        //customArgs[1] = "w";

        //FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.parseInputFile(customArgs);

        int[][] optimalGenderPrefList;
        int[][] otherGenderPrefList;

        SMPDivideAndConquer smpDivideAndConquer = new SMPDivideAndConquer(parsedInfo);

        /*
        if(0 == parsedInfo.optimality.compareToIgnoreCase("m"))
        {
            smpDivideAndConquer = new SMPDivideAndConquer(parsedInfo.men_pref, parsedInfo.women_pref);
        }
        else
        {
            smpDivideAndConquer = new SMPDivideAndConquer(parsedInfo.women_pref, parsedInfo.men_pref);
        }
        */

        final String kFinalMatchingString = smpDivideAndConquer.run();
        System.out.println(kFinalMatchingString);

        // Log End Time
        final long endTime = System.nanoTime();
        final long totalTime = endTime - startTime;
        System.out.println("Total time taken for <SMPDivideAndConquer> is "+ totalTime);

        /*
        (1,3)
        (2,1)
        (3,4)
        (4,2)
         */
    }
}
