package smp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;

public class RankingMatrix
{
    private int m_numberOfRows;
    private int m_numberOfColumns;
    private String m_optimalGenderPrefix;
    private String m_otherGenderPrefix;

    HashMap<String, String> m_otherGenderEngagementPartner;

    LinkedList<String> m_optimalGenderQueue;

    HashMap<String, LinkedList<String>> m_otherGenderPreferenceHashMap;

    HashMap<String, LinkedList<String>> m_optimalGenderProposalList;

//    HashMap<String[], String[]> m_rankingMatrix;
    int[][][] m_rankingMatrix;

    public RankingMatrix(
            int[][] optimalGenderDataSet2DArray,
            int[][] otherGenderDataSet2DArray,
            int numberOfRowsAndColumns,
            String preferenceString)
    {
        // Populate data structures
        // Assumption: square matrix is supplied
        m_numberOfRows = numberOfRowsAndColumns;
        m_numberOfColumns = numberOfRowsAndColumns;

        if(0 == preferenceString.compareToIgnoreCase("m"))
        {
            // man optimal matching
            m_optimalGenderPrefix = new String("m");
            m_otherGenderPrefix = new String("w");
        }
        else if (0 == preferenceString.compareToIgnoreCase("w"))
        {
            // woman optimal matching
            m_optimalGenderPrefix = new String("w");
            m_otherGenderPrefix = new String("m");
        }
        else
        {
            System.out.println("Invalid Argument: " + preferenceString);
            System.exit(-100);
        }

        // I want a stack of preferences for each optimal

        m_optimalGenderProposalList = new HashMap<String, LinkedList<String>>();

        m_otherGenderPreferenceHashMap = new HashMap<String, LinkedList<String>>();

        m_otherGenderEngagementPartner = new HashMap<String,String>();

        // Initialize Queue to include all bidders
        m_optimalGenderQueue = new LinkedList<String>();

//        m_rankingMatrix = new HashMap<String[], String[]>();

        for(int optimalGenderIndex = 0; optimalGenderIndex < m_numberOfRows; ++optimalGenderIndex)
        {
            String optimalGenderName = new String(m_optimalGenderPrefix + Integer.toString(optimalGenderIndex + 1));

            m_optimalGenderQueue.add(optimalGenderName);

            LinkedList<String> otherGenderToProposeToInPreferenceOrder = new LinkedList<String>();

            for(int otherGenderIndex = 0; otherGenderIndex < m_numberOfColumns; ++otherGenderIndex)
            {
                String otherGenderName = new String(m_otherGenderPrefix +
                        Integer.toString(optimalGenderDataSet2DArray[optimalGenderIndex][otherGenderIndex]));
                otherGenderToProposeToInPreferenceOrder.add(otherGenderName);
            }

            m_optimalGenderProposalList.put(optimalGenderName, otherGenderToProposeToInPreferenceOrder);
        }

        for(int otherGenderIndex = 0; otherGenderIndex < m_numberOfRows; ++otherGenderIndex)
        {
            String otherGenderName = new String(m_otherGenderPrefix + Integer.toString(otherGenderIndex + 1));

            LinkedList<String> optimalGenderInPreferenceOrder = new LinkedList<String>();

            for(int optimalGenderIndex = 0; optimalGenderIndex < m_numberOfColumns; ++optimalGenderIndex)
            {
                String optimalGenderName = new String(m_optimalGenderPrefix +
                        Integer.toString(otherGenderDataSet2DArray[otherGenderIndex][optimalGenderIndex]));
                optimalGenderInPreferenceOrder.add(optimalGenderName);
            }

            m_otherGenderPreferenceHashMap.put(otherGenderName, optimalGenderInPreferenceOrder);
        }

        m_rankingMatrix = new int[m_numberOfRows][m_numberOfRows][2];

        for (int optimalGenderIndex = 0; optimalGenderIndex < m_numberOfRows; ++optimalGenderIndex)
        {
            for (int otherGenderIndex = 0; otherGenderIndex < m_numberOfRows; ++otherGenderIndex)
            {
                m_rankingMatrix[optimalGenderIndex][otherGenderIndex][0] = (optimalGenderDataSet2DArray[optimalGenderIndex][otherGenderIndex]);
                m_rankingMatrix[optimalGenderIndex][otherGenderIndex][1] = (otherGenderDataSet2DArray[otherGenderIndex][optimalGenderIndex]);

//                TODO:Remove print
                System.out.print(Integer.toString(m_rankingMatrix[optimalGenderIndex][otherGenderIndex][0]) + "," + Integer.toString(m_rankingMatrix[optimalGenderIndex][otherGenderIndex][1]) + "  ");
            }
//            TODO:Remove Print
            System.out.println(" ");
        }

        int[] increasingArray = new int [m_numberOfRows];
        // Creation of root array to generate random permutations off of.
        for (int increasingIndex = 0; increasingIndex < m_numberOfRows; ++increasingIndex)
        {
            increasingArray[increasingIndex] = increasingIndex;
        }

//      Compute M(R_i)
        int[] permutatedArray = randomPermutation(increasingArray, m_numberOfColumns);
        int[] tranposedIndices = new int[m_numberOfRows];

//        TODO:Remove print
        System.out.println("Permutated Vector");
        for (int item: permutatedArray)
        {
            System.out.println(Integer.toString(item) + " ");
        }

//        Comput M(C_J)
        for (int index = 0; index < m_numberOfRows; ++index)
        {
            tranposedIndices[permutatedArray[index]] = index;
        }

//        TODO:Remove Print
        System.out.println("Column-based Match Vector");
        for (int item: tranposedIndices)
        {
            System.out.println(Integer.toString(item) + " ");
        }

        HashMap<String, LinkedList<int[]>> unstablePairsPerRow = new HashMap<String, LinkedList<int[]>>();

        // Unstable pair detection.
        // TODO: parallelize
        for (int i = 0; i < m_numberOfRows; ++i)
        {
            for (int j = 0; j < m_numberOfColumns; ++j)
            {
                //a_i,j is unstable if a_i,j^L < M(R_i)^L and a_i,j^R < M(C_j)^R
                if ((m_rankingMatrix[i][j][0] < m_rankingMatrix[i][permutatedArray[i]][0]) &&
                        (m_rankingMatrix[i][j][1] < m_rankingMatrix[tranposedIndices[j]][j][1]))
                {
                    int[] currentPair = new int[2];
                    currentPair[0] = i;
                    currentPair[1] = j;
                    LinkedList<int[]> unstablePairsInRow = new LinkedList<int[]>();
                    if(unstablePairsPerRow.containsKey(Integer.toString(i)))
                    {
                        unstablePairsInRow.addAll(unstablePairsPerRow.get(Integer.toString(i)));
                        unstablePairsInRow.add(currentPair);
                        unstablePairsPerRow.replace(Integer.toString(i), unstablePairsInRow);
                    }
                    else
                    {
                        unstablePairsInRow.add(currentPair);
                        unstablePairsPerRow.put(Integer.toString(i), unstablePairsInRow);
                    }
                }
            }
        }

    //TODO: Remove
        System.out.println("Found " + "unstable pairs");
        for (String row: unstablePairsPerRow.keySet())
        {
            for (int[] pair: unstablePairsPerRow.get(row))
            {
                System.out.println(Integer.toString(pair[0]) + "," + Integer.toString(pair[1]));
            }
        }

        HashMap<String, LinkedList<int[]>> nm1_genPairs = new HashMap<String, LinkedList<int[]>>();

        System.out.println("per row");
        for (String row: unstablePairsPerRow.keySet())
        {
            int[] pair;
            pair = getMinLeftValuedPair(unstablePairsPerRow.get(row));
            //TODO:Remove Print.
            System.out.println(Integer.toString(pair[0])+"," +Integer.toString(pair[1]));

            LinkedList<int[]> unstablePairsInColumn = new LinkedList<int[]>();
            if(nm1_genPairs.containsKey(Integer.toString(pair[1])))
            {
                unstablePairsInColumn.addAll(nm1_genPairs.get(Integer.toString(pair[1])));
                unstablePairsInColumn.add(pair);
                nm1_genPairs.replace(Integer.toString(pair[1]), unstablePairsInColumn);
            }
            else {
                unstablePairsInColumn.add(pair);
                nm1_genPairs.put(Integer.toString(pair[1]), unstablePairsInColumn);
            }
        }

        LinkedList<int[]> nm1_pairs = new LinkedList<int[]>();

        //TODO:remove print
        System.out.println("nm1_pairs");
        for (String column: nm1_genPairs.keySet())
        {
            nm1_pairs.add(getMinRightValuedPair(nm1_genPairs.get(column)));
            //TODO: Remove print
            System.out.println(Integer.toString(getMinRightValuedPair(nm1_genPairs.get(column))[0])+"," +Integer.toString(getMinRightValuedPair(nm1_genPairs.get(column))[1]));
        }

    }

    private int[] getMinRightValuedPair(LinkedList<int[]> nm1_pairsInColumn)
    {
        int[] minRightValuedPair = nm1_pairsInColumn.get(0);
        for (int[] pair:nm1_pairsInColumn)
        {
            if (m_rankingMatrix[pair[0]][pair[1]][1] < m_rankingMatrix[minRightValuedPair[0]][minRightValuedPair[1]][1])
            {
                minRightValuedPair = pair.clone();
            }
        }
        return minRightValuedPair;
    }

    private int[] getMinLeftValuedPair(LinkedList<int[]> nm1_pairsInRow)
    {
        int[] minLeftValuedPair = nm1_pairsInRow.get(0);
        for (int[] pair:nm1_pairsInRow)
        {
            if (m_rankingMatrix[pair[0]][pair[1]][0] < m_rankingMatrix[minLeftValuedPair[0]][minLeftValuedPair[1]][0])
            {
                minLeftValuedPair = pair.clone();
            }
        }
        return minLeftValuedPair;
    }

    public int[] randomPermutation(int[] a, int n)
    {
        int j = 0;
        int b;
        for (int i = n-1 ; i >=1 ;  --i)
        {
            j = (int) (i*Math.random());
            b = a[i];
            a[i] = a[j];
            a[j] = b;
        }

        return a;
    }

    public boolean isOptimalGenderQueueEmpty()
    {
        return m_optimalGenderQueue.isEmpty();
    }

    public String dequeueOptimalPerson()
    {
        return m_optimalGenderQueue.remove();
    }

    public void enqueueOptimalGenderPerson(String optimalPerson)
    {
        m_optimalGenderQueue.add(optimalPerson);
    }

    public String getNextMostPreferredOtherGender(String optimalGender)
    {
        return m_optimalGenderProposalList.get(optimalGender).remove();
    }

    public void engageOtherGenderToOptimalGender(String otherGender, String optimalGender)
    {
        m_otherGenderEngagementPartner.put(otherGender, optimalGender);
    }

    public boolean isOtherGenderEngaged(String otherGender)
    {
        return m_otherGenderEngagementPartner.containsKey(otherGender);
    }

    public String dumpCurrentFiance(String otherGender)
    {
        String currentFiance;
        currentFiance = m_otherGenderEngagementPartner.get(otherGender);
        m_otherGenderEngagementPartner.remove(otherGender);
        return currentFiance;
    }

    public boolean doesOtherGenderPreferCurrentFianceOverOptimalChoice(String otherEngagedGender, String optimalGender)
    {
        // get currently engaged optimal gender
        String currentFiance = m_otherGenderEngagementPartner.get(otherEngagedGender);

        LinkedList<String> currentOtherGenderPreferenceList = m_otherGenderPreferenceHashMap.get(otherEngagedGender);

        // since we are using a stack and things are pushed in order, then we can use the indices to figure out
        // the preference
        Integer indexOfCurrentFiance = currentOtherGenderPreferenceList.indexOf(currentFiance);
        Integer indexOfOptimalGender = currentOtherGenderPreferenceList.indexOf(optimalGender);
        return indexOfCurrentFiance < indexOfOptimalGender;
    }

    public String getFinalMatchingGraphToString()
    {
        String matches = new String();

        HashMap<Integer, Integer> orderedFinalMatching = new HashMap<Integer, Integer>();

        // Arrange output to follow results given from TA
        for (String otherGender: m_otherGenderEngagementPartner.keySet())
        {
            String optimalGender = m_otherGenderEngagementPartner.get(otherGender);

            // use .substring(1) to remove first non-digit characters i.e. just leave the integers
            orderedFinalMatching.put(Integer.parseInt(new String(optimalGender.substring(1))),
                    Integer.parseInt(new String(otherGender.substring(1))));
        }

        for(int i = 0; i < orderedFinalMatching.size(); ++i)
        {
            Integer currentOptimalGenderIndex = i + 1;
            matches += "(" + currentOptimalGenderIndex;
            matches += "," + orderedFinalMatching.get(currentOptimalGenderIndex);
            matches += ")\n";
        }

        return matches;
    }

    public void writeFinalMatchingToFile(String outputFileName)
    {
        String outputStr = this.getFinalMatchingGraphToString();

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
