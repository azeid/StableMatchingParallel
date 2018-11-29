package smp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class BipartiteGraph
{

    private int m_numberOfRows;
    private int m_numberOfColumns;
    private String m_optimalGenderPrefix;
    private String m_otherGenderPrefix;

    HashMap<String, String> m_otherGenderEngagementPartner;

    LinkedList<String> m_optimalGenderQueue;

    HashMap<String, LinkedList<String>> m_otherGenderPreferenceHashMap;

    HashMap<String, LinkedList<String>> m_optimalGenderProposalList;

    public BipartiteGraph(
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
        HashMap<Integer, Integer> orderedFinalMatching = new HashMap<Integer, Integer>();

        // Arrange output to follow results given from TA
        for (String otherGender: m_otherGenderEngagementPartner.keySet())
        {
            String optimalGender = m_otherGenderEngagementPartner.get(otherGender);

            // use .substring(1) to remove first non-digit characters i.e. just leave the integers
            orderedFinalMatching.put(Integer.parseInt(new String(optimalGender.substring(1))),
                    Integer.parseInt(new String(otherGender.substring(1))));
        }

        StringBuilder matches = new StringBuilder();
        for(int i = 0; i < orderedFinalMatching.size(); ++i)
        {

            Integer currentOptimalGenderIndex = i + 1;
            matches.append("(" + currentOptimalGenderIndex);
            matches.append("," + orderedFinalMatching.get(currentOptimalGenderIndex));
            matches.append(")\n");
        }

        return matches.toString();
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
