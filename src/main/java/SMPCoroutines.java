import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

        public Man(int id, int[] preferences) {
            currentProposed = -1;
            this.id = id;
            this.preferences = preferences;
        }

        public void setWomenList(Woman[] womenList) {
            this.womenList = womenList;
        }

        @Override
        public void run() {
            for (currentProposed = 0; currentProposed < preferences.length; currentProposed++) {
                   proposeTo(preferences[currentProposed] -1);
            }
        }

        private void proposeTo(int womanIndex) {
            Woman next = womenList[womanIndex];
            System.out.printf("Man %d proposed to %d\n", id, womanIndex);
            next.receiveProposal(this.id);
            try {
                System.out.printf("Man %d waiting for response.\n", id);
                synchronized (this) {
                    this.wait();
                }
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

        public void receiveProposal(int proposingMan) {
            synchronized (sem) {
                proposedMan = proposingMan;
                sem.release();
            }
        }

        @Override
        public void run() {
            while(true) {
                try {
                    sem.acquire();
                    if (acceptedMan < 0) {
                        acceptedMan = proposedMan;
                        System.out.printf("Woman %d accepted man %d\n", id, acceptedMan);
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

                        synchronized (menList[manToWakeUp]) {
                            menList[manToWakeUp].notifyAll();
                        }
                    }


                } catch (InterruptedException e) {
                }
            }
        }
    }


    private int[][] prefGenderA;
    private int[][] prefGenderB;

    public SMPCoroutines(int[][] prefGenderA, int[][] prefGenderB) {
        this.prefGenderA = prefGenderA;
        this.prefGenderB = prefGenderB;
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



        ExecutorService pool = Executors.newFixedThreadPool(count * 2);

        for (Woman w : womenList) {
            pool.submit(w);
        }

        for (Man m : menList) {
            pool.submit(m);
        }


        try {
            if (pool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Everything finished!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return "";
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage:\njava SMPCoroutines <input_file>");
            return;
        }

        Scanner input;
        try {
            input = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.out.printf("File \"%s\" not found.\n", args[0]);
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

        int n = input.nextInt();

        int[][] man_preferences = new int[n][n];
        int[][] woman_preferences = new int[n][n];

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                man_preferences[i][j] = input.nextInt();

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                woman_preferences[i][j] = input.nextInt();

        SMPCoroutines smp = new SMPCoroutines(man_preferences, woman_preferences);
        System.out.println(smp.run());
    }
}
