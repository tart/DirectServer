package net.aib42.lockserver.module.lock;

import net.aib42.lockserver.Client;
import net.aib42.lockserver.Server;
import net.aib42.lockserver.module.ModuleCommand;

public class Command extends ModuleCommand
{
	public static final byte[] RESP_TRUE  = { 0x54 }; //'T'
	public static final byte[] RESP_FALSE = { 0x46 }; //'F'

	public enum Action {
		LOCK,
		UNLOCK,
		TRYLOCK
	};

	private LockModule lockModule;
	private LockName lockName;
	private Action action;

	public Command(Server server, Client client, LockModule lockModule, LockName lockName, Action action)
	{
		super(server, client);
		this.lockModule = lockModule;
		this.lockName = lockName;
		this.action = action;
	}

	public LockName getLockName()
	{
		return lockName;
	}

	public Action getAction()
	{
		return action;
	}

	@Override
	public void run()
	{
		switch (action) {
			case LOCK:
				lockModule.lock(lockName, client);
				client.writeToSocket(RESP_TRUE);
				break;

			case UNLOCK:
				lockModule.unlock(lockName, client);
				client.writeToSocket(RESP_TRUE);
				break;

			case TRYLOCK:
				boolean success = lockModule.tryLock(lockName, client);
				client.writeToSocket(success ? RESP_TRUE : RESP_FALSE);
				break;
		}
	}
}
