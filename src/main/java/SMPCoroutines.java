import de.esoco.coroutine.Continuation;
import static de.esoco.coroutine.Coroutine.*;

import de.esoco.coroutine.Coroutine;
import de.esoco.coroutine.CoroutineScope;
import static de.esoco.coroutine.step.CodeExecution.*;
import smp.SMPData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.completedFuture;

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
                System.out.printf("Man %d waiting for response.\n", id);
                sem.acquire();
                System.out.printf("Man %d was rejected :(\n", id);
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
        private int proposedMan;


        public Woman(int id, int[] preferences) {
            this.id = id;
            this.preferences = preferences;
            acceptedMan = -1;
            sem = new Semaphore(0);
        }

        public void setMenList(Man[] menList) {
            this.menList = menList;
        }

        public synchronized void receiveProposal(int proposingMan) {
            System.out.printf("Man %d proposed to woman %d\n", proposingMan, id);
            proposedMan = proposingMan;
            sem.release();
        }

        @Override
        public void run() {
            while(!allMatched) {
                try {
                    sem.acquire();
                    if (!allMatched) {
                        if (acceptedMan < 0) {
                            acceptedMan = proposedMan;
                            System.out.printf("Woman %d accepted man %d\n", id, acceptedMan);
                            int count = toMatch.decrementAndGet();
                            allMatched = allMatched || count == 0;
                            if (allMatched) {
                                System.out.println("All matched!");
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
                                System.out.printf("Woman %d accepted man %d, ditching man %d\n", id, proposedMan, manToWakeUp);
                            } else {
                                manToWakeUp = proposedMan;
                                System.out.printf("Woman %d rejected man %d, she is already with man %d\n", id, proposedMan, acceptedMan);
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
//        int count = prefGenderA.length;
//        Man[] menList = new Man[count];
//        Woman[] womenList = new Woman[count];
//
//        for (int i = 0; i < count; i++) {
//            menList[i] = new Man(i, prefGenderA[i]);
//            menList[i].setWomenList(womenList);
//
//            womenList[i] = new Woman(i, prefGenderB[i]);
//            womenList[i].setMenList(menList);
//        }
//
//        ExecutorService pool = Executors.newWorkStealingPool(count * 2);
//        ArrayList<Future> waitForIt = new ArrayList<>();
//        for (Woman w : womenList) {
//            waitForIt.add(pool.submit(w));
//        }
//
//        for (Man m : menList) {
//            waitForIt.add(pool.submit(m));
//        }
//
//        for (Future f : waitForIt) {
//            try {
//                f.get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (Woman w : womenList) {
//            sb.append(String.format("(%d,%d)\n", w.acceptedMan + 1, w.id + 1));
//        }
//
//        return sb.toString();


//        int count = prefGenderA.length;
//        int[] matching = new int[count];
//        Arrays.fill(matching, -1);
//
//        for (int i = 0; i < count; i++) {
//            completedFuture(i).thenApply(manId -> {
//                for (int proposed = 0; proposed < count; proposed++) {
//                    CompletableFuture<Integer>  proposeFuture =  CompletableFuture.completedFuture(prefGenderA[manId][proposed]);
//
//                    proposeFuture.thenApply(womenId -> {
//                        if (matching[womenId] < 0) {
//                            matching[womenId] = manId;
//                        } else {
//                            int rankAccepted = -1;
//                            int rankProposed = -1;
//                            for (int j = 0; j < prefGenderB.length; j++) {
//                                int manIndex = prefGenderB[womenId][j] - 1;
//                                if (manIndex == manId) {
//                                    rankProposed = j;
//                                }
//
//                                if (manIndex == matching[womenId]) {
//                                    rankAccepted = j;
//                                }
//                            }
//
//                            if (rankProposed < rankAccepted) {
//
//                            }
//                        }
//                    });
//                }
//            })
//        }

        Coroutine<String, Integer> parseInteger =
                first(apply(String::trim))
                        .then(apply(s -> Integer.valueOf(s)));

        Coroutine<Integer, ?> manCoroutine =
                Coroutine.first(apply((Integer manId) -> prefGenderA[manId]))
                        .then(apply((int[] manPref) -> Arrays.toString(manPref)))
                        .then(consume(preferences -> System.out.println(preferences)));

        CoroutineScope.launch(scope -> {

            List<Continuation<?>> all = IntStream.range(0, prefGenderA.length)
                    .mapToObj(manIndex -> manCoroutine.runAsync(scope, manIndex)).collect(Collectors.toList());

        });





        return "";
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
