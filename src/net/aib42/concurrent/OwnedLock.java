package net.aib42.concurrent;

/**
 * A lock with an owner.
 */
public class OwnedLock<O>
{
	private O lockOwner;

	/**
	 * Creates an unlocked, unowned lock.
	 */
	public OwnedLock()
	{
		lockOwner = null;
	}

	/**
	 * Locks this lock and sets the owner.
	 *
	 * This method blocks until the lock is available.
	 *
	 * @param requester the object requesting ownership of this lock
	 * @throw InterruptedException if the thread waiting on the object monitor is interrupted
	 */
	public synchronized void lock(O requester) throws InterruptedException
	{
		while (lockOwner != null) {
			wait();
		}
		lockOwner = requester;
	}

	/**
	 * Unlocks this lock and unsets the owner.
	 *
	 * This method has no effect if the requesting object is not already the owner.
	 *
	 * @param requester the object releasing ownership of this lock
	 */
	public synchronized void unlock(O requester)
	{
		if (lockOwner != null && lockOwner.equals(requester)) {
			lockOwner = null;
			notify();
		}
	}

	/**
	 * Attempts to lock this lock and set the owner.
	 *
	 * This method is the non-blocking version of lock() and returns immediately with the result.
	 *
	 * @param requester the object requesting ownership of this lock
	 * @return <code>true</code> if the lock was successfully locked and ownership taken
	 */
	public synchronized boolean tryLock(O requester)
	{
		if (lockOwner == null) {
			lockOwner = requester;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns whether this lock is locked and owned by an object.
	 *
	 * This method should not be used for synchronization purposes; only diagnostics.
	 *
	 * @return <code>true</code> if the lock is locked and owned by an object
	 */
	public boolean isLocked()
	{
		return (lockOwner != null);
	}

	/**
	 * Returns the owner of this lock, if any.
	 *
	 * This method should not be used for synchronization purposes; only diagnostics.
	 *
	 * @return owner of the lock or <code>null</code>
	 */
	public O getOwner()
	{
		return lockOwner;
	}
}
