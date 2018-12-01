package smp;

import java.util.HashMap;
import java.util.LinkedList;

public class RankingMatrix {
    private int m_numberOfRows;
    private int m_numberOfColumns;
    private String m_optimalGenderPrefix;
    private String m_otherGenderPrefix;
    private int[] m_currentMatching;

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
        m_currentMatching = new int[m_numberOfRows];

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

        // Initialize preference Matrix
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

    }

    public void findStableMatching()
    {
        int[] increasingArray = new int[m_numberOfRows];
        // Creation of root array to generate random permutations off of.
        for (int increasingIndex = 0; increasingIndex < m_numberOfRows; ++increasingIndex) {
            increasingArray[increasingIndex] = increasingIndex;
        }

        int[] permutatedArray;
        int[] matchesByColumn;

        do {
//      Compute M(R_i)
            permutatedArray = randomPermutation(increasingArray, m_numberOfColumns);
            matchesByColumn = getMatchesbyColumn(permutatedArray);

            updateCurrentMatching(permutatedArray);


            int iterationCounter = 0;

            do {

                HashMap<String, LinkedList<MatrixCoordinate>> unstablePairsPerRow = GetUnstablePairsPerRow(permutatedArray, matchesByColumn);

                //TODO: Remove
                System.out.println("Found " + "unstable pairs");
                for (String row : unstablePairsPerRow.keySet()) {
                    for (MatrixCoordinate pair : unstablePairsPerRow.get(row)) {
                        System.out.println(pair.toString());
                    }
                }

                LinkedList<MatrixCoordinate> nm1_pairs = getNm1_Pairs(unstablePairsPerRow);

                LinkedList<MatrixCoordinate> nm2_pairs = getNm2_Pairs(nm1_pairs, matchesByColumn, permutatedArray);

                LinkedList<MatrixCoordinate> nm_pairs = new LinkedList<MatrixCoordinate>();
                nm_pairs.addAll(nm1_pairs);
                nm_pairs.addAll(nm2_pairs);

                //TODO:remove print
                System.out.println("nm_pairs");
                for (MatrixCoordinate nm_pair : nm_pairs) {
                    //TODO: Remove print
                    System.out.println(nm_pair.toString());
                }

                permutatedArray = iterateMatching(permutatedArray, nm1_pairs);
                matchesByColumn = getMatchesbyColumn(permutatedArray);
                updateCurrentMatching(permutatedArray);
            }while(!GetUnstablePairsPerRow(permutatedArray, matchesByColumn).isEmpty() && (++iterationCounter<m_numberOfColumns*3));
        }while(!GetUnstablePairsPerRow(permutatedArray, matchesByColumn).isEmpty());
    }

    private int[] iterateMatching(int[] previousMatching, LinkedList<MatrixCoordinate> nm_pairs)
    {
        for (MatrixCoordinate new_pair: nm_pairs)
        {
            previousMatching[new_pair.getI()]= new_pair.getJ();
        }

        return previousMatching;
    }

    private HashMap<String, LinkedList<MatrixCoordinate>> GetUnstablePairsPerRow(int[] permutatedArray, int[] tranposedIndices)
    {
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
        return unstablePairsPerRow;
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
                System.out.println("CounterNode Found at " + nm2_counter.toString());
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
                    MatrixCoordinate lastInChain = new MatrixCoordinate(getI_fromString(chain.getLast()), getJ_fromString(chain.getFirst()));
                    chainHistory.add(chain.getFirst());
                    chainHistory.add(chain.getLast());


                    System.out.println("First node in chain " + firstInChain.toString());
                    for (String node:chain
                         ) {
                        System.out.println(node);

                    }

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
        return Integer.parseInt(s.substring(1,s.length()-1).split(",")[0]);
    }

    public int getJ_fromString(String s) {
        return Integer.parseInt(s.substring(1,s.length()-1).split(",")[1]);
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

    private int[] getMatchesbyColumn(int[] matchesByRow)
    {
        int[] matchesbyColumn= new int[m_numberOfRows];
        //        Comput M(C_j)
        for (int index = 0; index < m_numberOfRows; ++index) {
            matchesbyColumn[matchesByRow[index]] = index;
        }
        return matchesbyColumn;
    }

    private void updateCurrentMatching(int[] matchingVec)
    {
            for(int i = 0; i < m_numberOfRows; ++i)
            {
                m_currentMatching[i] = matchingVec[i];
            }
    }

    public String getFinalMatchingGraphToString()
    {
        String matches = new String();

        for(int i = 0; i < m_currentMatching.length; ++i)
        {
            Integer currentOptimalGenderIndex = i + 1;
            matches += "(" + (currentOptimalGenderIndex);
            matches += "," + m_currentMatching[currentOptimalGenderIndex-1];
            matches += ")\n";
        }

        return matches;
    }


}
