package nachos.threads;

import nachos.machine.*;

import java.util.Random;

import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    @Override
	protected LotteryThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new LotteryThreadState(thread);

		return (LotteryThreadState) thread.schedulingState;
	}
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    @Override
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	// implement me
        return new LotteryQueue(transferPriority);
    }

    protected class LotteryQueue extends PriorityScheduler.PriorityQueue {
		LotteryQueue(boolean transferPriority) {
			super(transferPriority);
		}

		@Override
		protected LotteryThreadState pickNextThread() {
			if (srcQueue.isEmpty())
				return null;
			int total = 0;
			int[] lotteryEach = new int[srcQueue.size()];

            int idx = 0;
			for (KThread thread : srcQueue){
                lotteryEach[idx] = total + getThreadState(thread).getEffectivePriority();
                total = lotteryEach[idx];
                idx += 1;

                //System.out.println(thread);
            }
            //System.out.println(total);
			int randomLottery = random.nextInt(total);

			idx = 0;
			for (KThread thread : srcQueue){
				if (randomLottery < lotteryEach[idx])
                    return getThreadState(thread);
                idx += 1;
            }

			Lib.assertNotReached();
			return null;
        }
        public static final int maxPriority = Integer.MAX_VALUE;
    }
    
    protected class LotteryThreadState extends PriorityScheduler.ThreadState {
		public LotteryThreadState(KThread thread) {
			super(thread);
		}

		@Override
		public int getEffectivePriority() {
            Lib.assertTrue(Machine.interrupt().disabled());

            if (effpriority != invalidEff)	return effpriority;
            effpriority = priority;
            
            for (Iterator i = waitingQueueSet.iterator();i.hasNext();)
                for (Iterator j = ((PriorityQueue)i.next()).srcQueue.iterator();j.hasNext();){
                    //ThreadState tmp = getThreadState((KThread)j.next());
                    effpriority += getThreadState((KThread)j.next()).getEffectivePriority();
                }
            
            for (Iterator j = ((PriorityQueue)this.thread.waitForJoin).srcQueue.iterator();j.hasNext();){
                //ThreadState tmp = getThreadState((KThread)j.next());
                effpriority += getThreadState((KThread)j.next()).getEffectivePriority();
            }
        
            return effpriority;
        }
	}
    protected Random random = new Random();
}
