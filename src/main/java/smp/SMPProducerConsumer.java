package smp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SMPProducerConsumer {

    private int[][] pref1;
    private int[][] pref2;
    private int[] proposed;
    private boolean[][] assignment;


    public SMPProducerConsumer(int[][] man_preferences, int[][] women_preferences, String optimality) {
        if (optimality.compareTo("m") == 0) {
            this.pref1 = man_preferences;
            this.pref2 = women_preferences;
        } else {
            this.pref1 = women_preferences;
            this.pref2 = man_preferences;
        }

        int count = man_preferences.length;

        proposed = new int[count];
        assignment = new boolean[count][count];
    }

    class Worker implements Runnable {

        private ConcurrentLinkedQueue<Integer> _queue;

        public Worker(ConcurrentLinkedQueue<Integer> queue) {
            _queue = queue;
        }

        @Override
        public void run() {
            Integer freePerson;
            while ((freePerson = _queue.poll()) != null) {
                int preferredPerson = findPreferredPerson(freePerson);

                synchronized (assignment) {
                    int pairedPerson = proposeToPerson(freePerson, preferredPerson);
                    if (pairedPerson < 0) {
                        assign(freePerson, preferredPerson);
                    } else {
                        int rejected;
                        if (prefers(preferredPerson, freePerson, pairedPerson)) {
                            rejected = freePerson;
                        } else {
                            rejected = pairedPerson;
                            assign(freePerson, preferredPerson);
                        }
                        _queue.add(rejected);
                    }
                }
            }
        }

        private boolean prefers(int preferredPerson, int freePerson, int pairedPerson) {
            int rankFreePerson = -1;
            int rankPairedPerson = -1;
            for (int i = 0; i < pref2.length; i++) {
                if (pref2[preferredPerson][i] - 1 == freePerson) {
                    rankFreePerson = i;
                }
                if (pref2[preferredPerson][i] - 1 == pairedPerson) {
                    rankPairedPerson = i;
                }
            }

            return rankFreePerson > rankPairedPerson;
        }

        private void assign(int freePerson, int preferredPerson) {
            for (int i = 0; i < assignment.length; i++) {
                assignment[i][preferredPerson] = i == freePerson;
            }
        }

        private int findPreferredPerson(int freePerson) {
            return pref1[freePerson][proposed[freePerson]] - 1;
        }

        private int proposeToPerson(int freePerson, int preferredPerson) {
            int pairedWith = -1;
            for (int i = 0; i < assignment.length; i++) {
                if (assignment[i][preferredPerson]) {
                    pairedWith = i;
                    break;
                }
            }
            proposed[freePerson]++;
            return pairedWith;
        }


    }

    public String run() {
        int count = pref1.length;
        List<Integer> allFree = IntStream.range(0, count).boxed().collect(Collectors.toList());
        ConcurrentLinkedQueue<Integer> freeList = new ConcurrentLinkedQueue<>(allFree);

        ArrayList<Future> waitFor = new ArrayList<>();

        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 0; i < count; i++) {
            waitFor.add(pool.submit(new Worker(freeList)));
        }

        for (Future w : waitFor) {
            try {
                w.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();

        return formatAssignment();
    }

    private String formatAssignment() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < assignment.length; i++) {
            for (int j = 0; j < assignment[i].length; j++) {
                if (assignment[i][j]) {
                    sb.append(String.format("(%d,%d)\n", i+1, j+1));
                }
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage:\njava smp.SMP <input_file> <m|w>");
            return;
        }

        Scanner input;
        try {
            input = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.out.printf("File \"%s\" not found.\n", args[0]);
            return;
        }

        String optimality;
        if (!"m".equals(args[1]) && !"w".equals(args[1])) {
            System.out.println("You must specify an option for optimality.");
            System.out.println("m : Man optimal");
            System.out.println("w : Woman optimal");
            return;
        } else {
            optimality = args[1];
        }

        int n = input.nextInt();

        int[][] man_preferences = new int[n][n];
        int[][] woman_preferences = new int[n][n];

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                man_preferences[i][j] = input.nextInt();

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                woman_preferences[i][j] = input.nextInt();

        SMPProducerConsumer smp = new SMPProducerConsumer(man_preferences, woman_preferences, optimality);
        System.out.println(smp.run());
    }


}
