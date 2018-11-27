package smp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
* Inspired by
* "Stable Marriage Problem by Coroutines"
* Lloyd Allison - 1983
* */
public class SMPCoroutines {

    class Man implements Runnable{
        private int id;
        private int[] preferences;
        private int currentProposed;
        private Woman[] womenList;
        private Semaphore sem;

        public Man(int id, int[] preferences) {
            currentProposed = -1;
            this.id = id;
            this.preferences = preferences;
            sem = new Semaphore(0);
        }

        public void setWomenList(Woman[] womenList) {
            this.womenList = womenList;
        }

        @Override
        public void run() {
            for (currentProposed = 0; currentProposed < preferences.length && !allMatched; currentProposed++) {
                   proposeTo(preferences[currentProposed] -1);
            }

            if (allMatched) {
                for (Woman w : womenList) {
                    w.sem.release();
                }
            }
        }

        private void proposeTo(int womanIndex) {
            Woman next = womenList[womanIndex];
            next.receiveProposal(this.id);
            try {
                sem.acquire();
            } catch (InterruptedException e) {

            }
        }

    }

    class Woman implements Runnable {
        private int id;
        private int[] preferences;

        private int acceptedMan;
        private Man[] menList;

        private Semaphore sem;
        private ConcurrentLinkedQueue<Integer> proposals;


        public Woman(int id, int[] preferences) {
            this.id = id;
            this.preferences = preferences;
            acceptedMan = -1;
            sem = new Semaphore(0);
            proposals = new ConcurrentLinkedQueue<>();
        }

        public void setMenList(Man[] menList) {
            this.menList = menList;
        }

        public synchronized void receiveProposal(int proposingMan) {
            proposals.add(proposingMan);
            sem.release();
        }

        @Override
        public void run() {
            while(!allMatched || acceptedMan < 0) {
                try {
                    sem.acquire();
                    Integer proposedMan;
                    if ((proposedMan = proposals.poll())!= null && (!allMatched || acceptedMan < 0)) {
                        if (acceptedMan < 0) {
                            acceptedMan = proposedMan;
                            int count = toMatch.decrementAndGet();
                            System.out.println(count);
                            if (count == 0) {
                                allMatched = true;
                            }
                        } else {
                            int rankAccepted = -1;
                            int rankProposed = -1;
                            for (int i = 0; i < preferences.length; i++) {
                                int manIndex = preferences[i] - 1;
                                if (manIndex == proposedMan) {
                                    rankProposed = i;
                                }

                                if (manIndex == acceptedMan) {
                                    rankAccepted = i;
                                }
                            }

                            int manToWakeUp;
                            if (rankProposed < rankAccepted) {
                                manToWakeUp = acceptedMan;
                                acceptedMan = proposedMan;
                            } else {
                                manToWakeUp = proposedMan;
                            }

                            menList[manToWakeUp].sem.release();
                        }
                    }


                } catch (InterruptedException e) {
                }
            }

            menList[acceptedMan].sem.release();
        }
    }


    private int[][] prefGenderA;
    private int[][] prefGenderB;
    private AtomicInteger toMatch;
    private static boolean allMatched = false;

    public SMPCoroutines(int[][] prefGenderA, int[][] prefGenderB) {
        this.prefGenderA = prefGenderA;
        this.prefGenderB = prefGenderB;
        this.toMatch = new AtomicInteger(prefGenderA.length);
    }

    public String run() {
        int count = prefGenderA.length;
        Man[] menList = new Man[count];
        Woman[] womenList = new Woman[count];

        for (int i = 0; i < count; i++) {
            menList[i] = new Man(i, prefGenderA[i]);
            menList[i].setWomenList(womenList);

            womenList[i] = new Woman(i, prefGenderB[i]);
            womenList[i].setMenList(menList);
        }

        ExecutorService pool = Executors.newWorkStealingPool(count * 2);
        ArrayList<Future> waitForIt = new ArrayList<>();
        for (Woman w : womenList) {
            waitForIt.add(pool.submit(w));
        }

        for (Man m : menList) {
            waitForIt.add(pool.submit(m));
        }

        for (Future f : waitForIt) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();

        StringBuilder sb = new StringBuilder();
        for (Woman w : womenList) {
            sb.append(String.format("(%d,%d)\n", w.acceptedMan + 1, w.id + 1));
        }

        return sb.toString();
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage:\njava SMPCoroutines <input_file>");
            return;
        }

//        String optimality;
//        if (!"m".equals(args[1]) && !"w".equals(args[1])) {
//            System.out.println("You must specify an option for optimality.");
//            System.out.println("m : Man optimal");
//            System.out.println("w : Woman optimal");
//            return;
//        } else {
//            optimality = args[1];
//        }

        SMPData data = SMPData.loadFromFile(args[0]);

        SMPCoroutines smp = new SMPCoroutines(data.getPreferencesOne(), data.getPreferencesTwo());
        System.out.println(smp.run());
    }
}
