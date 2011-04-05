package net.aib42.lockserver.module.lock;

public class Lock<O>
{
	private O lockOwner;

	public Lock()
	{
		lockOwner = null;
	}

	public synchronized void lock(O requester) throws InterruptedException
	{
		while (lockOwner != null) {
			wait();
		}
		lockOwner = requester;
	}

	public synchronized void unlock(O requester)
	{
		if (lockOwner.equals(requester)) {
			lockOwner = null;
			notify();
		}
	}

	public synchronized boolean tryLock(O requester)
	{
		if (lockOwner == null) {
			lockOwner = requester;
			return true;
		} else {
			return false;
		}
	}

	public boolean isLocked()
	{
		return (lockOwner != null);
	}
}
