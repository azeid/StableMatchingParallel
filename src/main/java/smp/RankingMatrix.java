package smp;


import sun.awt.image.ImageWatched;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;

public class RankingMatrix {
    private int m_numberOfRows;
    private int m_numberOfColumns;
    private String m_optimalGenderPrefix;
    private String m_otherGenderPrefix;

    HashMap<String, String> m_otherGenderEngagementPartner;

    LinkedList<String> m_optimalGenderQueue;

    HashMap<String, LinkedList<String>> m_otherGenderPreferenceHashMap;

    HashMap<String, LinkedList<String>> m_optimalGenderProposalList;

    //    HashMap<String[], String[]> m_rankingMatrix;
    MatrixPair[][] m_rankingMatrix;

    public RankingMatrix(
            int[][] optimalGenderDataSet2DArray,
            int[][] otherGenderDataSet2DArray,
            int numberOfRowsAndColumns,
            String preferenceString) {
        // Populate data structures
        // Assumption: square matrix is supplied
        m_numberOfRows = numberOfRowsAndColumns;
        m_numberOfColumns = numberOfRowsAndColumns;

        if (0 == preferenceString.compareToIgnoreCase("m")) {
            // man optimal matching
            m_optimalGenderPrefix = new String("m");
            m_otherGenderPrefix = new String("w");
        } else if (0 == preferenceString.compareToIgnoreCase("w")) {
            // woman optimal matching
            m_optimalGenderPrefix = new String("w");
            m_otherGenderPrefix = new String("m");
        } else {
            System.out.println("Invalid Argument: " + preferenceString);
            System.exit(-100);
        }

        // I want a stack of preferences for each optimal

        m_optimalGenderProposalList = new HashMap<String, LinkedList<String>>();

        m_otherGenderPreferenceHashMap = new HashMap<String, LinkedList<String>>();

        m_otherGenderEngagementPartner = new HashMap<String, String>();

        // Initialize Queue to include all bidders
        m_optimalGenderQueue = new LinkedList<String>();

        for (int optimalGenderIndex = 0; optimalGenderIndex < m_numberOfRows; ++optimalGenderIndex) {
            String optimalGenderName = new String(m_optimalGenderPrefix + Integer.toString(optimalGenderIndex + 1));

            m_optimalGenderQueue.add(optimalGenderName);

            LinkedList<String> otherGenderToProposeToInPreferenceOrder = new LinkedList<String>();

            for (int otherGenderIndex = 0; otherGenderIndex < m_numberOfColumns; ++otherGenderIndex) {
                String otherGenderName = new String(m_otherGenderPrefix +
                        Integer.toString(optimalGenderDataSet2DArray[optimalGenderIndex][otherGenderIndex]));
                otherGenderToProposeToInPreferenceOrder.add(otherGenderName);
            }

            m_optimalGenderProposalList.put(optimalGenderName, otherGenderToProposeToInPreferenceOrder);
        }

        for (int otherGenderIndex = 0; otherGenderIndex < m_numberOfRows; ++otherGenderIndex) {
            String otherGenderName = new String(m_otherGenderPrefix + Integer.toString(otherGenderIndex + 1));

            LinkedList<String> optimalGenderInPreferenceOrder = new LinkedList<String>();

            for (int optimalGenderIndex = 0; optimalGenderIndex < m_numberOfColumns; ++optimalGenderIndex) {
                String optimalGenderName = new String(m_optimalGenderPrefix +
                        Integer.toString(otherGenderDataSet2DArray[otherGenderIndex][optimalGenderIndex]));
                optimalGenderInPreferenceOrder.add(optimalGenderName);
            }

            m_otherGenderPreferenceHashMap.put(otherGenderName, optimalGenderInPreferenceOrder);
        }

        m_rankingMatrix = new MatrixPair[m_numberOfRows][m_numberOfRows];

        for (int optimalGenderIndex = 0; optimalGenderIndex < m_numberOfRows; ++optimalGenderIndex) {
            for (int otherGenderIndex = 0; otherGenderIndex < m_numberOfRows; ++otherGenderIndex) {
                m_rankingMatrix[optimalGenderIndex][otherGenderIndex] = new MatrixPair((optimalGenderDataSet2DArray[optimalGenderIndex][otherGenderIndex]),
                        (otherGenderDataSet2DArray[otherGenderIndex][optimalGenderIndex]));

//                TODO:Remove print
                System.out.print(Integer.toString(m_rankingMatrix[optimalGenderIndex][otherGenderIndex].getLeftValue()) + "," + Integer.toString(m_rankingMatrix[optimalGenderIndex][otherGenderIndex].getRightValue()) + "  ");
            }
//            TODO:Remove Print
            System.out.println(" ");
        }

        int[] increasingArray = new int[m_numberOfRows];
        // Creation of root array to generate random permutations off of.
        for (int increasingIndex = 0; increasingIndex < m_numberOfRows; ++increasingIndex) {
            increasingArray[increasingIndex] = increasingIndex;
        }

//      Compute M(R_i)
        int[] permutatedArray = randomPermutation(increasingArray, m_numberOfColumns);
        int[] tranposedIndices = new int[m_numberOfRows];

//        TODO:Remove print
        System.out.println("Permutated Vector");
        for (int item : permutatedArray) {
            System.out.println(Integer.toString(item) + " ");
        }

//        Comput M(C_J)
        for (int index = 0; index < m_numberOfRows; ++index) {
            tranposedIndices[permutatedArray[index]] = index;
        }

//        TODO:Remove Print
        System.out.println("Column-based Match Vector");
        for (int item : tranposedIndices) {
            System.out.println(Integer.toString(item) + " ");
        }

        HashMap<String, LinkedList<MatrixCoordinate>> unstablePairsPerRow = new HashMap<String, LinkedList<MatrixCoordinate>>();

        // Unstable pair detection.
        // TODO: parallelize
        for (int i = 0; i < m_numberOfRows; ++i) {
            for (int j = 0; j < m_numberOfColumns; ++j) {
                //a_i,j is unstable if a_i,j^L < M(R_i)^L and a_i,j^R < M(C_j)^R
                if ((m_rankingMatrix[i][j].getLeftValue() < m_rankingMatrix[i][permutatedArray[i]].getLeftValue()) &&
                        (m_rankingMatrix[i][j].getRightValue() < m_rankingMatrix[tranposedIndices[j]][j].getRightValue())) {
                    MatrixCoordinate currentPair = new MatrixCoordinate(i, j);
                    LinkedList<MatrixCoordinate> unstablePairsInRow = new LinkedList<MatrixCoordinate>();
                    if (unstablePairsPerRow.containsKey(Integer.toString(i))) {
                        unstablePairsInRow.addAll(unstablePairsPerRow.get(Integer.toString(i)));
                        unstablePairsInRow.add(currentPair);
                        unstablePairsPerRow.replace(Integer.toString(i), unstablePairsInRow);
                    } else {
                        unstablePairsInRow.add(currentPair);
                        unstablePairsPerRow.put(Integer.toString(i), unstablePairsInRow);
                    }
                }
            }
        }

        //TODO: Remove
        System.out.println("Found " + "unstable pairs");
        for (String row : unstablePairsPerRow.keySet()) {
            for (MatrixCoordinate pair : unstablePairsPerRow.get(row)) {
                System.out.println(Integer.toString(pair.getI()) + "," + Integer.toString(pair.getJ()));
            }
        }

        LinkedList<MatrixCoordinate> nm1_pairs = getNm1_Pairs(unstablePairsPerRow);

        LinkedList<MatrixCoordinate> nm2_pairs = getNm2_Pairs(nm1_pairs, tranposedIndices, permutatedArray);

        LinkedList<MatrixCoordinate> nm_pairs = new LinkedList<MatrixCoordinate>();
        for (MatrixCoordinate nm1_pair : nm1_pairs) {
            for (MatrixCoordinate nm2_pair : nm2_pairs) {
                if ((nm1_pair.getI() == nm2_pair.getI()) && (nm1_pair.getJ() == nm2_pair.getJ())) {
                    nm_pairs.add(nm1_pair);
                }
            }
        }

        //TODO:remove print
        System.out.println("nm_pairs");
        for (MatrixCoordinate nm_pair : nm_pairs) {
            //TODO: Remove print
            System.out.println(nm_pair.toString());
        }
    }

    private LinkedList<MatrixCoordinate> getNm1_Pairs(HashMap<String, LinkedList<MatrixCoordinate>> unstablePairsPerRow)
    {
        HashMap<String, LinkedList<MatrixCoordinate>> nm1_genPairs = new HashMap<String, LinkedList<MatrixCoordinate>>();

        for(String row:unstablePairsPerRow.keySet())
        {
            MatrixCoordinate pair;
            pair = getMinLeftValuedPair(unstablePairsPerRow.get(row));

            LinkedList<MatrixCoordinate> unstablePairsInColumn = new LinkedList<MatrixCoordinate>();
            if (nm1_genPairs.containsKey(Integer.toString(pair.getJ()))) {
                unstablePairsInColumn.addAll(nm1_genPairs.get(Integer.toString(pair.getJ())));
                unstablePairsInColumn.add(pair);
                nm1_genPairs.replace(Integer.toString(pair.getJ()), unstablePairsInColumn);
            } else {
                unstablePairsInColumn.add(pair);
                nm1_genPairs.put(Integer.toString(pair.getJ()), unstablePairsInColumn);
            }
        }

        LinkedList<MatrixCoordinate> nm1_pairs = new LinkedList<MatrixCoordinate>();

        //TODO:remove print
        System.out.println("nm1_pairs");
        for(String column:nm1_genPairs.keySet())
        {
            nm1_pairs.add(getMinRightValuedPair(nm1_genPairs.get(column)));
            //TODO: Remove print
            System.out.println(Integer.toString(getMinRightValuedPair(nm1_genPairs.get(column)).getI()) + "," + Integer.toString(getMinRightValuedPair(nm1_genPairs.get(column)).getJ()));
        }
        return nm1_pairs;
    }

    private LinkedList<MatrixCoordinate> getNm2_Pairs(LinkedList<MatrixCoordinate> nm1_pairs, int[] tranposedIndices, int[] permutatedArray)
    {
        HashMap<String, LinkedList<MatrixCoordinate>> nm2_generatingPairs = new HashMap<String, LinkedList<MatrixCoordinate>>();

        HashMap<String, LinkedList<MatrixCoordinate>> nm2_edges = new HashMap<String, LinkedList<MatrixCoordinate>>();

        if (!nm1_pairs.isEmpty()) {
            //start computation of nm2 generating pairs
            for (MatrixCoordinate nm1_pair : nm1_pairs) {
                MatrixCoordinate nm2_counter = new MatrixCoordinate(tranposedIndices[nm1_pair.getJ()], permutatedArray[nm1_pair.getI()]);
                //Establish relationship from Matching pairs to nm2-pair
                MatrixCoordinate matching1 = new MatrixCoordinate(tranposedIndices[nm1_pair.getJ()], nm1_pair.getJ());
                MatrixCoordinate matching2 = new MatrixCoordinate(nm1_pair.getI(), permutatedArray[nm1_pair.getI()]);
                String counterName = nm2_counter.toString();
                String m1St = matching1.toString();
                String m2St = matching2.toString();
                if (nm2_generatingPairs.containsKey(m1St)) {
                    LinkedList<MatrixCoordinate> relatedPairs = new LinkedList<MatrixCoordinate>();
                    relatedPairs.addAll(nm2_generatingPairs.get(m1St));
                    for (MatrixCoordinate pair : relatedPairs) {
                        //Add related pairs to the list of current nm2 pair.
                        if (nm2_edges.containsKey(counterName)) {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.addAll(nm2_edges.get(counterName));
                            edgesToPair.add(pair);
                            nm2_edges.replace(counterName, edgesToPair);
                        } else {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.add(pair);
                            nm2_edges.put(counterName, edgesToPair);
                        }

                        //Add new nm2 to any list of its related pairs.
                        String pairName = new String();
                        pairName = pair.toString();
                        if (nm2_edges.containsKey(pairName)) {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.addAll(nm2_edges.get(pairName));
                            edgesToPair.add(nm2_counter);
                            nm2_edges.replace(pairName, edgesToPair);
                        } else {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.add(nm2_counter);
                            nm2_edges.put(pairName, edgesToPair);
                        }
                    }
                    relatedPairs.add(nm2_counter);
                    nm2_generatingPairs.replace(m1St, relatedPairs);
                } else {
                    LinkedList<MatrixCoordinate> relatedPairs = new LinkedList<MatrixCoordinate>();
                    relatedPairs.add(nm2_counter);
                    nm2_generatingPairs.put(m1St, relatedPairs);
                }
                if (nm2_generatingPairs.containsKey(m2St)) {
                    LinkedList<MatrixCoordinate> relatedPairs = new LinkedList<MatrixCoordinate>();
                    relatedPairs.addAll(nm2_generatingPairs.get(m2St));
                    for (MatrixCoordinate pair : relatedPairs) {
                        //Add related pairs to the list of current nm2 pair.
                        if (nm2_edges.containsKey(counterName)) {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.addAll(nm2_edges.get(counterName));
                            edgesToPair.add(pair);
                            nm2_edges.replace(counterName, edgesToPair);
                        } else {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.add(pair);
                            nm2_edges.put(counterName, edgesToPair);
                        }

                        //Add new nm2 to any list of its related pairs.
                        String pairName = new String();
                        pairName = pair.toString();
                        if (nm2_edges.containsKey(pairName)) {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.addAll(nm2_edges.get(pairName));
                            edgesToPair.add(nm2_counter);
                            nm2_edges.replace(pairName, edgesToPair);
                        } else {
                            LinkedList<MatrixCoordinate> edgesToPair = new LinkedList<MatrixCoordinate>();
                            edgesToPair.add(nm2_counter);
                            nm2_edges.put(pairName, edgesToPair);
                        }
                    }
                    relatedPairs.add(nm2_counter);
                    nm2_generatingPairs.replace(m2St, relatedPairs);
                } else {
                    LinkedList<MatrixCoordinate> relatedPairs = new LinkedList<MatrixCoordinate>();
                    relatedPairs.add(nm2_counter);
                    nm2_generatingPairs.put(m2St, relatedPairs);
                }
            }
        }

        //TODO: Remove Prints
        for (String match: nm2_generatingPairs.keySet()
             )
        {
            System.out.print("Matching Pair " + match + "relates to ");
            for (MatrixCoordinate nm2: nm2_generatingPairs.get(match)
                 )
            {

                System.out.println(  nm2.toString() + " , ");
            }
        }

        LinkedList<MatrixCoordinate> nm2_pairs = new LinkedList<MatrixCoordinate>();

        LinkedList<String> chainHistory = new LinkedList<String>();

        for (String edgesName : nm2_edges.keySet()) {
            if (nm2_edges.get(edgesName).size() == 1) {

                LinkedList<String> chain = new LinkedList<String>();

                if (!chainHistory.contains(edgesName)) {
                    chain.add(edgesName);
                    chain = FindChain(chain, nm2_edges, edgesName);
                    MatrixCoordinate firstInChain = new MatrixCoordinate(getI_fromString(chain.getFirst()), getJ_fromString(chain.getFirst()));
                    MatrixCoordinate lastInChain = new MatrixCoordinate(getI_fromString(chain.getFirst()), getJ_fromString(chain.getFirst()));
                    chainHistory.add(chain.getFirst());
                    chainHistory.add(chain.getLast());

                    System.out.println("First node in chain " + firstInChain.toString());
                    System.out.println("Last node in chain " + lastInChain.toString());

                    boolean rowEnd = true;

                    MatrixCoordinate newNm2_item = new MatrixCoordinate();
                    for (MatrixCoordinate nm1_pair : nm1_pairs) {
                        if (nm1_pair.getI() == firstInChain.getI()) {
                            rowEnd = false;
                        }
                    }

                    if (rowEnd) {
                        newNm2_item.setI(firstInChain.getI());
                        newNm2_item.setJ(lastInChain.getJ());
                    } else {
                        newNm2_item.setI(lastInChain.getI());
                        newNm2_item.setJ(firstInChain.getJ());
                    }
                    nm2_pairs.add(newNm2_item);
                }
            }
        }

        //Add Isolated pairs, done at the end to reduce comparisons.
        for (String matchingPair : nm2_generatingPairs.keySet()) {
            if (nm2_generatingPairs.get(matchingPair).size() == 1) {
                nm2_pairs.add(nm2_generatingPairs.get(matchingPair).get(0));
                System.out.println("Isolated node found at " + nm2_generatingPairs.get(matchingPair).get(0).toString());
            }
        }

        //TODO:remove print
        System.out.println("nm2_pairs");
        for (MatrixCoordinate nm2_pair : nm2_pairs) {
            //TODO: Remove print
            System.out.println(nm2_pair.toString());
        }

        return nm2_pairs;
    }

    public int getI_fromString(String s) {
        return Integer.parseInt(s.split(",")[0]);
    }

    public int getJ_fromString(String s) {
        return Integer.parseInt(s.split(",")[1]);
    }


    private LinkedList<String> FindChain( LinkedList<String> chains,  HashMap<String, LinkedList<MatrixCoordinate>> nm2_edges, String currentName )
    {
        LinkedList<String> updatedPath= new LinkedList<String>();
        updatedPath.addAll(chains);
        for (MatrixCoordinate pair: nm2_edges.get(currentName))
        {
            if(!chains.contains(pair.toString()))
            {
                LinkedList<String> subPath = new LinkedList<>();
                updatedPath.add(pair.toString());
                subPath = FindChain(updatedPath, nm2_edges, pair.toString());
                return subPath;
            }
        }
        return updatedPath;
    }

    //Required in NM1 set calculation
    private MatrixCoordinate getMinRightValuedPair(LinkedList<MatrixCoordinate> nm1_pairsInColumn)
    {
        MatrixCoordinate minRightValuedPair = nm1_pairsInColumn.get(0);
        for (MatrixCoordinate pair:nm1_pairsInColumn)
        {
            if (m_rankingMatrix[pair.getI()][pair.getJ()].getRightValue() < m_rankingMatrix[minRightValuedPair.getI()][minRightValuedPair.getJ()].getRightValue())
            {
                minRightValuedPair = new MatrixCoordinate(pair.getI(),pair.getJ());
            }
        }
        return minRightValuedPair;
    }

    //Required in NM1 set calculation
    private MatrixCoordinate getMinLeftValuedPair(LinkedList<MatrixCoordinate> nm1_pairsInRow)
    {
        MatrixCoordinate minLeftValuedPair = nm1_pairsInRow.get(0);
        for (MatrixCoordinate pair:nm1_pairsInRow)
        {
            if (m_rankingMatrix[pair.getI()][pair.getJ()].getLeftValue() < m_rankingMatrix[minLeftValuedPair.getI()][minLeftValuedPair.getJ()].getLeftValue())
            {
                minLeftValuedPair = new MatrixCoordinate(pair.getI(),pair.getJ());
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
