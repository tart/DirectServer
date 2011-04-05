package net.aib42.directserver.module.lock;

import net.aib42.directserver.Client;
import net.aib42.directserver.Server;
import net.aib42.directserver.module.ModuleCommand;
import net.aib42.wrapper.primitive.ValueByteArray;

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
	private ValueByteArray lockName;
	private Action action;

	public Command(Server server, Client client, LockModule lockModule, ValueByteArray lockName, Action action)
	{
		super(server, client);
		this.lockModule = lockModule;
		this.lockName = lockName;
		this.action = action;
	}

	public ValueByteArray getLockName()
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
