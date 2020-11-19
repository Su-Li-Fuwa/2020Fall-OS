package nachos.threads;
import nachos.machine.*;
import nachos.threads.Communicator;
import nachos.threads.Condition2;
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

    private static class PingCondTest implements Runnable {
        PingCondTest(int which, Condition2 cond, Lock lock) {
            this.which = which;
            this.cond = cond;
            this.lock = lock; 
        }
        public void run() {
            lock.acquire();
            for (int i=0; i<5; i++) {
                System.out.println("*** thread " + which + " looped "+ i + " times");
                cond.wakeAll();
                cond.sleep();
            }
        }
        private Condition2 cond;
        private int which; 
        private Lock lock;
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
     * Tests whether modules are working.
     */
    public static void firstTest() {
        Lib.debug(dbgThread, "Enter KThread.selfTest");
        
        new KThread(new PingTest(1)).setName("forked thread").fork();
        new KThread(new PingTest(0)).setName("forked thread").fork();
        KThread tmp = new KThread(new PingTest(10)).setName("forked thread");
        tmp.fork();
    }

    public static void joinTest1() {
        Lib.debug(dbgThread, "Enter KThread.joinTest1");
    
        KThread tmp = new KThread(new SelfForkTest(0, true)).setName("forked thread");
        tmp.fork();
        //new PingTest(0).run();
    }

    public static void joinTest2() {
        Lib.debug(dbgThread, "Enter KThread.joinTest2");
    
        KThread t1 = new KThread(new PingTest(0)).setName("main fork thread");
        KThread t2 = new KThread(new PingTest(1)).setName("main join thread");
        t2.fork();
        t2.join();
        t1.fork();
        //new PingTest(0).run();
    }

    public static void joinTest3() {
        Lib.debug(dbgThread, "Enter KThread.joinTest3");
    
        KThread t1 = new KThread(new PingTest(0)).setName("main fork thread");
        KThread t2 = new KThread(new PingTest(1)).setName("main join thread");
        t1.fork();
        t2.fork();
        t2.join();
        //new PingTest(0).run();
    }

    public static void condTest1() {
        Lib.debug(dbgThread, "Enter KThread.condTest1");
        Lock lock = new Lock();
        Condition2 cond = new Condition2(lock);
        KThread t1 = new KThread(new PingCondTest(0, cond, lock)).setName("fork thread 1");
        KThread t2 = new KThread(new PingCondTest(1, cond, lock)).setName("fork thread 2");
        t1.fork();
        t2.fork();
    }
        

    public static void alarmTest1(){
        Lib.debug(dbgThread, "Enter KThread.alarmTest1");

        new KThread(new alarmT(130)).setName("forked 1").fork();
        new KThread(new alarmT(410)).setName("forked 2").fork();
        new KThread(new alarmT(600)).setName("forked 3").fork();
    }

    public static void alarmTest2(){
        Lib.debug(dbgThread, "Enter KThread.alarmTest2");

        new KThread(new alarmT(130)).setName("forked 1").fork();
        new KThread(new alarmTjoin(410, 600)).setName("forked 2").fork();
    }

    public static void communTest1(){
        Lib.debug(dbgThread, "Enter KThread.ommunTest1");
        comm = new Communicator();
        new KThread(new listener(100)).setName("listener 1").fork();
        new KThread(new listener(100)).setName("listener 2").fork();
        new KThread(new speaker(100, 60)).setName("speaker 1").fork();
        new KThread(new speaker(100, 70)).setName("speaker 2").fork();
    }

    public static void communTest2(){
        Lib.debug(dbgThread, "Enter KThread.ommunTest2");
        comm = new Communicator();
        int total = 100, tmp;
        int nSpeaker = 0, nListener = 0;
        for (; nSpeaker <= total/2 && nListener <= total/2;){
            tmp = (int) (Math.random()*(2));
            if (tmp == 0){
                new KThread(new listener((int)(Math.random()*10*100))).fork();
                nListener += 1;
            }
            else{
                new KThread(new speaker((int)(Math.random()*10*100), nSpeaker)).fork();
                nSpeaker += 1;
            }
        }
        for (;nListener <= total/2;nListener++)
            new KThread(new listener((int)(Math.random()*10*100))).fork();
        for (;nSpeaker <= total/2;nSpeaker++)
            new KThread(new speaker((int)(Math.random()*10*100), nSpeaker)).fork();
    }

    public static void priorityTest1(){
        Lib.debug(dbgThread, "Enter KThread.priorityTest1");

        boolean status = Machine.interrupt().disable();

        KThread th1 = new KThread(new PingTest(0)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th1, 1);

        KThread th2 = new KThread(new PingTest(1)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th2, 2);

        KThread th3 = new KThread(new PingTest(2)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th3, 3);

        Machine.interrupt().restore(status);

        th1.fork();
        th2.fork();
        th3.fork();
    }

    public static void priorityTest2(){
        Lib.debug(dbgThread, "Enter KThread.priorityTest2");

        boolean status = Machine.interrupt().disable();

        KThread th1 = new KThread(new PingTest(0)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th1, 2);

        KThread th2 = new KThread(new PingTest(1)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th2, 1);

        KThread th3 = new KThread(new PingTest(2)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th3, 2);

        Machine.interrupt().restore(status);

        th1.fork();
        th2.fork();
        th2.join();
        th3.fork();
    }

    public static void priorityTest3(){
        Lib.debug(dbgThread, "Enter KThread.priorityTest3");

        boolean status = Machine.interrupt().disable();

        KThread th1 = new KThread(new PingTest(0)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th1, 3);

        KThread th2 = new KThread(new PingTest(1)).setName("forked thread");
        ThreadedKernel.scheduler.setPriority(th2, 1);

        ThreadQueue lockQueue = ThreadedKernel.scheduler.newThreadQueue(true);
        
        lockQueue.acquire(th2);
        lockQueue.waitForAccess(th1);

        Machine.interrupt().restore(status);

        th1.fork();
        th2.fork();
    }

    private static final char dbgThread = 't';
    public static Communicator comm;
}
