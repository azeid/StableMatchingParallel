package smp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SMPData {

    private int[][] preferences_one;
    private int[][] preferences_two;

    private SMPData(int[][] pref1, int[][] pref2) {
        preferences_one = pref1;
        preferences_two = pref2;
    }

    public int[][] getPreferencesOne() {
        return preferences_one;
    }

    public int[][] getPreferencesTwo() {
        return preferences_two;
    }

    public int getSize() {
        return preferences_one.length;
    }

    public static SMPData loadFromFile(String filePath) {
        Scanner input;
        try {
            input = new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            System.out.printf("File \"%s\" not found.\n", filePath);
            return null;
        }

        int n = input.nextInt();

        int[][] pref1 = new int[n][n];
        int[][] pref2 = new int[n][n];

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                pref1[i][j] = input.nextInt();

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                pref2[i][j] = input.nextInt();

        return new SMPData(pref1, pref2);
    }
}
