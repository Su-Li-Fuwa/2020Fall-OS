package nachos.threads;
import nachos.machine.*;
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

    public static void condTest(){
        
    }



    private static final char dbgThread = 't';
}
