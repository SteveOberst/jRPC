package net.sxlver.jrpc.core.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ParallelLock {
    private final Semaphore semaphore;
    private final int permits;
    private volatile Runnable noActiveLockAction = () -> {};

    /**
     * Initializes the NonBlockingLock with a specified number of permits and an action to run
     * when there are no active locks.
     *
     * @param permits           The number of permits to allow parallel execution.
     */
    public ParallelLock(int permits) {
        this.semaphore = new Semaphore(permits, true); // true for fair ordering
        this.permits = permits;
    }

    /**
     * Attempt to acquire the lock with a specified timeout.
     *
     * @param timeout The maximum time to wait for the lock in milliseconds.
     * @return True if the lock was acquired; false if the lock could not be acquired within the timeout.
     */
    public void acquireLock(long timeout) throws TimeoutException {
        try {
            while(!(semaphore.tryAcquire(1, timeout, TimeUnit.MILLISECONDS))) {
            }
        } catch (InterruptedException e) {
            throw new TimeoutException();
        }
    }

    /**
     * Release the lock. If no active locks remain, execute the specified noActiveLockAction.
     */
    public void releaseLock() {
        semaphore.release();
        if (semaphore.availablePermits() == semaphore.availablePermits()) {
            executeNoActiveLockAction();
        }
    }

    private void executeNoActiveLockAction() {
        if (noActiveLockAction != null) {
            noActiveLockAction.run();
        }
    }

    /**
     * Check if there are any active locks at the moment.
     *
     * @return True if there are active locks, false otherwise.
     */
    public boolean hasActiveLocks() {
        return semaphore.availablePermits() < permits;
    }

    /**
     * Executes the specified action when no locks are acquired.
     *
     * @param action the action to be executed when all locks are inactive
     */
    public void onLocksInactive(final Runnable action) {
        this.noActiveLockAction = action;
    }
}
