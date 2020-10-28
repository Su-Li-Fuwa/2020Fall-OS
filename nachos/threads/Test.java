package nachos.threads;
import nachos.machine.*;
import nachos.threads.Communicator;
import nachos.threads.ThreadedKernel;
public class Test {
    public Test(){};
    private static class PingTest implements Runnable {
        PingTest(int which) {
            this.which = which;
        }
        public void run() {
            for (int i=0; i<5; i++) {
            System.out.println("*** thread " + which + " looped "+ i + " times");
            KThread.yield();
            }
        }
        private int which;
    }

    private static class SelfForkTest implements Runnable {
        SelfForkTest(int which, boolean toFork) {
            this.which = which;
            this.toFork = toFork;
            if (toFork){
                KThread tmp = new KThread(new SelfForkTest(1, false)).setName("self forked thread");
                tmp.fork();
                tmp.join();
            }
        }
        
        public void run() {
            for (int i=0; i<5; i++) {
            System.out.println("*** thread " + which + " looped "
                       + i + " times");
            KThread.yield();
            }
        }
        private int which;
        private boolean toFork;
    }

    private static class alarmT implements Runnable {
        alarmT(long st) {
            this.sleepTime = st;
        }
        public void run() {
            startTime = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil(sleepTime);
            wakeTime = Machine.timer().getTime();
            System.out.println("Alarm: start: "+startTime+" Sleep:"+sleepTime+" Wake:"+wakeTime);
        }
        public long startTime;
        public long sleepTime;
        public long wakeTime;
    }

    private static class alarmTjoin extends alarmT {
        alarmTjoin(long st, long childst) {
            super(st);
            this.childst = childst;
        }
        public void run() {
            startTime = Machine.timer().getTime();
            KThread tmp = new KThread(new alarmT(childst)).setName("child thread");
            tmp.fork();
            tmp.join();
            ThreadedKernel.alarm.waitUntil(sleepTime);
            wakeTime = Machine.timer().getTime();
            System.out.println("Alarm start: "+startTime+" Sleep:"+sleepTime+" Wake:"+wakeTime);
        }
        private long childst;
    }

    private static class listener implements Runnable {
        listener(long st) {
            this.sleepTime = st;
        }
        public void run() {
            startTime = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil(sleepTime);
            wakeTime = Machine.timer().getTime();
            int word = -1;
            word = comm.listen();
            finishedTime = Machine.timer().getTime();
            System.out.println("Listener start: "+startTime+" Sleep:"+sleepTime+" Wake:"+wakeTime+" Word:"+word+" Finished:"+finishedTime);
        }
        public long startTime;
        public long sleepTime;
        public long wakeTime;
        public long finishedTime;
    }

    private static class speaker implements Runnable {
        speaker(long st, int word) {
            this.sleepTime = st;
            this.word = word;
        }
        public void run() {
            startTime = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil(sleepTime);
            wakeTime = Machine.timer().getTime();
            comm.speak(word);
            finishedTime = Machine.timer().getTime();
            System.out.println("Speaker start: "+startTime+" Sleep:"+sleepTime+" Wake:"+wakeTime+" Word:"+word+" Finished:"+finishedTime);
        }
        public long startTime;
        public long sleepTime;
        public long wakeTime;
        public long finishedTime;
        public int word;
    }

    /**
     * Tests whether this module is working.
     */

    public static void joinTest() {
        Lib.debug(dbgThread, "Enter KThread.joinTest");
    
        KThread tmp = new KThread(new SelfForkTest(0, true)).setName("forked thread");
        tmp.fork();
        //new PingTest(0).run();
    }
        
    public static void selfTest() {
        Lib.debug(dbgThread, "Enter KThread.selfTest");
        
        new KThread(new PingTest(1)).setName("forked thread").fork();
        new KThread(new PingTest(0)).setName("forked thread").fork();
        KThread tmp = new KThread(new PingTest(10)).setName("forked thread");
        tmp.fork();
        //new PingTest(0).run();
    }

    public static void alarmTest(){
        Lib.debug(dbgThread, "Enter KThread.selfTest");
        System.out.println("alarmTest start!");
        new KThread(new alarmT(130)).setName("forked 1").fork();
        new KThread(new alarmT(410)).setName("forked 2").fork();
        new KThread(new alarmT(600)).setName("forked 3").fork();
    }

    public static void alarmJoinTest(){
        Lib.debug(dbgThread, "Enter KThread.selfTest");
        System.out.println("alarmTest start!");
        new KThread(new alarmT(130)).setName("forked 1").fork();
        new KThread(new alarmTjoin(410, 600)).setName("forked 2").fork();
        //new KThread(new alarmT(600)).setName("forked 3").fork();
    }

    public static void communTest(){
        Lib.debug(dbgThread, "Enter KThread.selfTest");
        System.out.println("communTest start!");
        comm = new Communicator();
        new KThread(new listener(100)).setName("listener 1").fork();
        new KThread(new listener(100)).setName("listener 2").fork();
        new KThread(new speaker(100, 60)).setName("speaker 1").fork();
        new KThread(new speaker(100, 70)).setName("speaker 2").fork();
        System.out.println("communTest finished!");
    }

    private static final char dbgThread = 't';
    public static Communicator comm;
}
