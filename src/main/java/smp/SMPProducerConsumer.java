package smp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SMPProducerConsumer {

    private int[][] pref1;
    private int[][] pref2;
    private int[] proposed;
    private boolean[][] assignment;
    private boolean[] assigned;
    private int[] matching;
    private Object monitor;


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
        assigned = new boolean[count];
        monitor = new Object();

        matching = new int[count];
        Arrays.fill(matching, -1);
    }

    class Worker implements Runnable {

        private ConcurrentLinkedQueue<Integer> _queue;
        private Object[] rings;

        public Worker(ConcurrentLinkedQueue<Integer> queue, Object[] rings) {
            _queue = queue;
            this.rings = rings;
        }

        @Override
        public void run() {
            Integer freePerson;

            while(!allMatched()) {

                while ((freePerson = _queue.poll()) == null && !allMatched()) {
                    synchronized (monitor) {
                        try {
                            //System.out.printf("T %d| Waiting.. \n", Thread.currentThread().getId());
                            monitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (freePerson == null) {
                    //System.out.printf("T %d| All Matched, I'm done.. \n", Thread.currentThread().getId());
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                    return;
                }

                int preferredPerson = findPreferredPerson(freePerson);

                //System.out.printf("T %d| Man %d about to propose to %d\n", Thread.currentThread().getId(), freePerson, preferredPerson);
                synchronized (rings[preferredPerson]) {
                    //System.out.printf("T %d| Man %d proposing to %d\n", Thread.currentThread().getId(), freePerson, preferredPerson);
                    int pairedPerson = proposeToPerson(freePerson, preferredPerson);
                    if (pairedPerson < 0) {
                        //System.out.printf("T %d| Man %d accepted by %d\n", Thread.currentThread().getId(), freePerson, preferredPerson);
                        assign(freePerson, preferredPerson);
                    } else {
                        int rejected;
                        if (prefers(preferredPerson, freePerson, pairedPerson)) {
                            rejected = freePerson;
                        } else {
                            rejected = pairedPerson;
                            assign(freePerson, preferredPerson);
                            //System.out.printf("T %d| Man %d accepted by %d\n", Thread.currentThread().getId(), freePerson, preferredPerson);
                        }
                        //System.out.printf("T %d| Man %d rejected by %d\n", Thread.currentThread().getId(), rejected, preferredPerson);
                        _queue.add(rejected);
                        synchronized (monitor) {
                            monitor.notifyAll();
                        }
                    }
                }
            }
            synchronized (monitor) {
                monitor.notifyAll();
            }
            //System.out.printf("T %d| Nothing to do left. I'm done.\n", Thread.currentThread().getId());
        }

        private boolean allMatched(){
            for (boolean matched : assigned) {
                if (!matched) return false;
            }
            return true;
        }

        private boolean prefers(int preferredPerson, int freePerson, int pairedPerson) {
            int rankFreePerson = -1;
            int rankPairedPerson = -1;
            for (int i = 0; i < pref2.length && (rankFreePerson < 0 || rankPairedPerson < 0); i++) {
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
//            for (int i = 0; i < assignment.length; i++) {
//                assignment[i][preferredPerson] = i == freePerson;
//            }
            assigned[preferredPerson] = true;
            matching[preferredPerson] = freePerson;
        }

        private int findPreferredPerson(int freePerson) {
            int nextToPropose = proposed[freePerson];
            return pref1[freePerson][nextToPropose] - 1;
        }

        private int proposeToPerson(int freePerson, int preferredPerson) {
//            int pairedWith = -1;
//            for (int i = 0; i < assignment.length; i++) {
//                if (assignment[i][preferredPerson]) {
//                    pairedWith = i;
//                    break;
//                }
//            }
            proposed[freePerson]++;
            return matching[preferredPerson];
        }
    }

    public String run() {
        int count = pref1.length;
        List<Integer> allFree = IntStream.range(0, count).boxed().collect(Collectors.toList());
        ConcurrentLinkedQueue<Integer> freeList = new ConcurrentLinkedQueue<>(allFree);
        Object[] rings = new Object[count];
        for(int i = 0; i < count; i++) {
            rings[i] = new Object();
        }

        ArrayList<Future> waitFor = new ArrayList<>();

        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 0; i < count; i++) {
            waitFor.add(pool.submit(new Worker(freeList, rings)));
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

//        for (int i = 0; i < assignment.length; i++) {
//            for (int j = 0; j < assignment[i].length; j++) {
//                if (assignment[i][j]) {
//                    sb.append(String.format("(%d,%d)\n", i+1, j+1));
//                }
//            }
//        }

        for (int i = 0; i < matching.length; i++) {
            sb.append(String.format("(%d, %d)\n", i + 1, matching[i] + 1));
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage:\njava smp.SMP <input_file> <m|w>");
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

        SMPData data = SMPData.loadFromFile(args[0]);

        SMPProducerConsumer smp = new SMPProducerConsumer(data.getPreferencesOne(), data.getPreferencesTwo(), optimality);
        System.out.println(smp.run());
    }


}
