package net.aib42.lockserver.module.lock;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

import net.aib42.lockserver.Client;
import net.aib42.lockserver.Server;
import net.aib42.lockserver.module.CommandParseError;
import net.aib42.lockserver.module.Module;
import net.aib42.lockserver.module.lock.Command;
import net.aib42.lockserver.module.lock.Command.Action;

public class LockModule extends Module
{
	private HashMap<LockName, Lock<Long>> lockMap;

	public LockModule(Server server)
	{
		super(server);
		lockMap = new HashMap<LockName, Lock<Long>>();
	}

	@Override
	public byte getCommandPrefix()
	{
		return 0x4c; //'L'
	}

	@Override
	public boolean parseCommand(Client client, ByteBuffer buffer)
		throws BufferUnderflowException, CommandParseError
	{
		//Read from pos to limit
		try {
			byte commandByte = buffer.get();
			int lockNameSize = server.getCommandParser().getUnsignedByte(buffer);

			byte[] lockNameBytes = new byte[lockNameSize];
			buffer.get(lockNameBytes);
			LockName lockName = new LockName(lockNameBytes);

			Command command = null;
			switch (commandByte) {
				case 0x4c: //'L' - lock
					command = new Command(server, client, this, lockName, Action.LOCK);
					break;

				case 0x55: //'U' - unlock
					command = new Command(server, client, this, lockName, Action.UNLOCK);
					break;

				case 0x54: //'T' - trylock
					command = new Command(server, client, this, lockName, Action.TRYLOCK);
					break;

				default:
					throw new CommandParseError();
			}
			client.queueCommand(command);

			return true;
		} catch (BufferUnderflowException bue) {
			return false;
		}
	}

	private Lock<Long> getLockByName(LockName lockName)
	{
		Lock<Long> lock = null;

		synchronized (lockMap) {
			lock = lockMap.get(lockName);

			if (lock == null) {
				lock = new Lock<Long>();
				lockMap.put(lockName, lock);
			}
		}

		return lock;
	}

	public void lock(LockName lockName, Client client)
	{
		try {
			getLockByName(lockName).lock(client.getId());
		} catch (InterruptedException ie) {}
	}

	public void unlock(LockName lockName, Client client)
	{
		getLockByName(lockName).unlock(client.getId());
	}

	public boolean tryLock(LockName lockName, Client client)
	{
		return getLockByName(lockName).tryLock(client.getId());
	}

	@Override
	public void clientDisconnected(Client client)
	{
		synchronized (lockMap) {
			for (Lock<Long> l : lockMap.values()) {
				l.unlock(client.getId());
			}
		}
	}

	@Override
	public ByteBuffer getModuleInformation()
	{
		ByteBuffer info = ByteBuffer.allocate(128);

		int acquired = 0, free = 0;
		synchronized (lockMap) {
			for (Lock<Long> l : lockMap.values()) {
				if (l.isLocked()) {
					++acquired;
				} else {
					++free;
				}
			}
		}

		info.put(
			new String((acquired+free) + " locks, " + acquired + " acquired, " + free + " free.\n")
				.getBytes(Charset.forName("UTF-8"))
		);

		return info;
	}
}
