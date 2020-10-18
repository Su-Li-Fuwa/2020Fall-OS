package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        waitListener = 0;
        waitSpeaker = 0;
        activeSpeaker = 0;
        condLock = new Lock();
        cond = new Condition2(condLock);
        wordQueue = new LinkedList<Integer>();
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        condLock.acquire();
        waitSpeaker += 1;
        while (waitListener == 0)
            cond.sleep();
        waitSpeaker -= 1;
        activeSpeaker += 1;
        wordQueue.add(word);
        waitListener -= 1;
        cond.wakeAll();
        condLock.release();
        return;
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        int rWord;
        condLock.acquire();
        waitListener += 1;
        while (activeSpeaker == 0)
            cond.sleep();
        rWord = wordQueue.removeFirst();
        activeSpeaker -= 1;
        cond.wakeAll();
        condLock.release();
        return rWord;
    }

    private int waitListener;
    private int waitSpeaker;
    private int activeSpeaker;
    private Condition2 cond;
    private Lock condLock;
    private LinkedList<Integer> wordQueue;
}
