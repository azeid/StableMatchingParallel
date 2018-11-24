/*
 * Inspired by
 * "A PARALLEL ALGORITHM TO SOLVE THE STABLE MARRIAGE PROBLEM"
 * S. S. TSENG and R. C. T. LEE - 1984
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class SMPDivideAndConquer
{
    private int[][] m_optimalPrefList;
    private int[][] m_otherPrefList;
    //private ConcurrentLinkedQueue<int[]> m_resultsConcurrentQueue;

    public SMPDivideAndConquer(int[][] optimalPrefList, int[][] otherPrefList)
    {
        m_optimalPrefList = optimalPrefList;
        m_otherPrefList = otherPrefList;
    }

    /*
    public class SplitMatching
    {
        int[][] m_leftMatching;
        int[][] m_rightMatching;
        int[] m_optimalGenderOffsetIndex;
    }
    */

    public int[] getInitialMatching()
    {
        // Here, we just match every optimal gender with their first preference
        int[] initialMatching = new int[m_optimalPrefList.length];

        for(int i = 0; i < initialMatching.length; ++i)
        {
            initialMatching[i] = m_optimalPrefList[i][0];
        }

        System.out.println("Initial Matching");
        System.out.println(matchingArrayToString(initialMatching));

        return initialMatching;
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
    public static int[][] chunkArray(int[] array, int chunkSize)
    {
        //int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        int[][] output = new int[array.length][1];

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

        for(int i = 0; i < output.length; ++i)
        {
            System.out.println("Chuncks");
            System.out.println(matchingArrayToString(output[i]));
        }

        return output;
    }

    public static int [] concatenate1DArray(int[] a, int[] b)
    {
        int aLen = a.length;
        int bLen = b.length;

        int[] c = new int[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static String matchingArrayToString(int arr[])
    {
        String matches = "";

        // This is assuming data is arranged in order which they should be.
        for(int i = 0; i < arr.length; ++i)
        {
            Integer currentOptimalGenderIndex = i + 1;
            matches += "(" + currentOptimalGenderIndex;
            matches += "," + arr[i];
            matches += ")\n";
        }

        return matches;
    }

    class WorkerThread implements /*Runnable,*/ Callable<WorkerThread.Result>
    {
        private int[] m_leftMatching;
        private int[] m_rightMatching;
        private int[] m_finalMatching;
        private int m_optimalGenderOffsetIndex;

        class Result
        {
            int[] m_finalMatching;
            int m_optimalGenderOffsetIndex;
        }

        public WorkerThread(int[] leftMatching, int[] rightMatching, int optimalGenderOffsetIndex)
        {
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

            m_leftMatching = leftMatching.clone();
            m_rightMatching = rightMatching.clone();
            m_optimalGenderOffsetIndex = optimalGenderOffsetIndex;
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
            result.m_optimalGenderOffsetIndex = m_optimalGenderOffsetIndex;
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

            int[] concatenatedMatchingArray = concatenate1DArray(m_leftMatching, m_rightMatching);
            m_finalMatching = new int[concatenatedMatchingArray.length];

            HashMap<Integer, Integer> alreadyMatched = new HashMap<Integer, Integer>();

            for(int matchingIndex = 0; matchingIndex < concatenatedMatchingArray.length; ++matchingIndex)
            {
                /*
                if(1 != m_finalMatching[matchingIndex].length)
                {
                    System.out.println("Optimal Gender is Matched to More Than One!");
                    System.exit(-100);
                }
                */

                int currentOtherGenderMatching = concatenatedMatchingArray[matchingIndex];

                if(alreadyMatched.containsKey(concatenatedMatchingArray[matchingIndex]))
                {
                    int rejectedIndex = -1;
                    while(alreadyMatched.containsKey(currentOtherGenderMatching))
                    {
                        int optimalGenderMatchedToOtherGender = alreadyMatched.get(concatenatedMatchingArray[matchingIndex]);
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
                            //alreadyMatched.remove(currentOtherGenderMatching);

                            alreadyMatched.put(
                                    concatenatedMatchingArray[matchingIndex],
                                    mostPreferredOptimalGenderToOtherGender);
                            //m_finalMatching[matchingIndex] = concatenatedMatchingArray[matchingIndex];

                            currentOtherGenderMatching =
                                    getNextPreference(optimalGenderMatchedToOtherGender, concatenatedMatchingArray[matchingIndex]);

                            alreadyMatched.put(
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

                        // Now find the next most preferred other gender for rejected optimal person
                        //currentOtherGenderMatching =
                         //       getNextPreference(
                         //               optimalGenderToAdvance,
                        //                (rejectedIndex == -1) ? concatenatedMatchingArray[matchingIndex] : rejectedIndex);
                    } // while
                } // if
                else
                {
                    // Key is other gender and value is optimal gender
                    alreadyMatched.put(currentOtherGenderMatching, matchingIndex);
                    //m_finalMatching[matchingIndex] = currentOtherGenderMatching;
                }
            } // for

            // TODO: find better way!
            int i = 0;
            for (Integer otherGender: alreadyMatched.keySet())
            {
                m_finalMatching[i] = alreadyMatched.get(otherGender);
                ++i;
            }

            Arrays.sort(m_finalMatching);

        }

        int getMostPreferredOptimalGender(int otherGender, int matchedOptimalIndex, int proposingOptimalIndex)
        {
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
        }

        int getNextPreference(int optimalToAdvance, int currentOtherGender)
        {
            // m_optimalGenderStartIndex is needed to know where to look in the preference list since
            // matching are split into different subsets. Also, this does not require us to make any
            // unnecessary copies.
            int actualOptimalIndex = (optimalToAdvance + m_optimalGenderOffsetIndex);
            int actualOtherGenderIndex = getOtherGenderIndex(actualOptimalIndex, currentOtherGender);
            int nextPref = m_optimalPrefList[actualOptimalIndex][actualOtherGenderIndex + 1];

            return nextPref;
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

        public int[] getFinalMatching()
        {
            return m_finalMatching;
        }

        public int getOptimalGenderOffsetIndex()
        {
            return m_optimalGenderOffsetIndex;
        }

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
        int[] initialMatching = getInitialMatching();

        // for now only allow even number of data
        if(0 != initialMatching.length % 2)
        {
            System.out.println("Data size must be even!");
            System.exit(-100);
        }

        // initially, kChunckSize would be the size of individual matching
        //final int kChunckSize = initialMatching.length;
        final int kChunckSize = 1;

        int[][] arrayChuncks = SMPDivideAndConquer.chunkArray(initialMatching, kChunckSize);

        int matchingResultSize = initialMatching.length;

        String finalMatchingString = "";

        while(1 != matchingResultSize)
        {
            ExecutorService pool = Executors.newFixedThreadPool(matchingResultSize);
            //int[][] returnedMatchings;

            List<Callable<WorkerThread.Result>> callables = new ArrayList<Callable<WorkerThread.Result>>();
            // increment by 2 since we are processing two chuncks at a time

            for(int i = 0; i < matchingResultSize; i = (i+2))
            {
                // TODO: I want results to be added to queue in right order
                //pool.submit(new WorkerThread(arrayChuncks[i], arrayChuncks[i+1], i));
                int indexOffset = arrayChuncks[0].length * i;
                callables.add(new WorkerThread(arrayChuncks[i], arrayChuncks[i+1], indexOffset));
            }

            try
            {
                // There is a hard requirement that results are in order
                List<Future<WorkerThread.Result>> results = pool.invokeAll(callables);

                matchingResultSize = results.size();

                arrayChuncks = new int[matchingResultSize][results.get(0).get().m_finalMatching.length];

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
                    arrayChuncks[i] = result.get().m_finalMatching;

                    System.out.println("Results #"+ i);
                    System.out.println(SMPDivideAndConquer.matchingArrayToString(result.get().m_finalMatching));
                    ++i;
                }

                if(1 == matchingResultSize)
                {
                    finalMatchingString = matchingArrayToString(arrayChuncks[0]);
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

        //FileInputOutputHelper fileIOHelper = new FileInputOutputHelper();
        //FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.parseInputFile(args);


        FileInputOutputHelper fileIOHelper = new FileInputOutputHelper();
        FileInputOutputHelper.FileParsedInfo parsedInfo = fileIOHelper.getDefaultInfo();

        int[][] optimalGenderPrefList;
        int[][] otherGenderPrefList;

        SMPDivideAndConquer smpDivideAndConquer;

        if(0 == parsedInfo.optimality.compareToIgnoreCase("m"))
        {
            smpDivideAndConquer = new SMPDivideAndConquer(parsedInfo.men_pref, parsedInfo.women_pref);
        }
        else
        {
            smpDivideAndConquer = new SMPDivideAndConquer(parsedInfo.women_pref, parsedInfo.men_pref);
        }

        final String kFinalMatchingString = smpDivideAndConquer.run();
        System.out.println(kFinalMatchingString);

        /*
        (1,3)
        (2,1)
        (3,4)
        (4,2)
         */
    }
}
