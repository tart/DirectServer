package net.aib42.directserver.module.lock;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import net.aib42.concurrent.OwnedLock;
import net.aib42.directserver.Client;
import net.aib42.directserver.Server;
import net.aib42.directserver.module.CommandParseError;
import net.aib42.directserver.module.Module;
import net.aib42.directserver.module.lock.Command.Action;
import net.aib42.wrapper.primitive.ValueByteArray;

public class LockModule extends Module
{
	private HashMap<ValueByteArray, OwnedLock<Long>> lockMap;

	public LockModule(Server server)
	{
		super(server);
		lockMap = new HashMap<ValueByteArray, OwnedLock<Long>>();
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
			int ValueByteArraySize = server.getCommandParser().getUnsignedByte(buffer);

			byte[] ValueByteArrayBytes = new byte[ValueByteArraySize];
			buffer.get(ValueByteArrayBytes);
			ValueByteArray ValueByteArray = new ValueByteArray(ValueByteArrayBytes);

			Command command = null;
			switch (commandByte) {
				case 0x4c: //'L' - lock
					command = new Command(server, client, this, ValueByteArray, Action.LOCK);
					break;

				case 0x55: //'U' - unlock
					command = new Command(server, client, this, ValueByteArray, Action.UNLOCK);
					break;

				case 0x54: //'T' - trylock
					command = new Command(server, client, this, ValueByteArray, Action.TRYLOCK);
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

	private OwnedLock<Long> getLockByName(ValueByteArray lockName)
	{
		OwnedLock<Long> lock = null;

		synchronized (lockMap) {
			lock = lockMap.get(lockName);

			if (lock == null) {
				lock = new OwnedLock<Long>();
				lockMap.put(lockName, lock);
			}
		}

		return lock;
	}

	public void lock(ValueByteArray lockName, Client client)
	{
		try {
			getLockByName(lockName).lock(client.getId());
		} catch (InterruptedException ie) {}
	}

	public void unlock(ValueByteArray lockName, Client client)
	{
		getLockByName(lockName).unlock(client.getId());
	}

	public boolean tryLock(ValueByteArray lockName, Client client)
	{
		return getLockByName(lockName).tryLock(client.getId());
	}

	@Override
	public void clientDisconnected(Client client)
	{
		synchronized (lockMap) {
			for (OwnedLock<Long> l : lockMap.values()) {
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
			for (OwnedLock<Long> l : lockMap.values()) {
				if (l.isLocked()) {
					++acquired;
				} else {
					++free;
				}
			}
		}

		info.put(
			new String((acquired+free) + " locks, " + acquired + " acquired, " + free + " free.\n")
				.getBytes(java.nio.charset.Charset.forName("UTF-8"))
		);

		return info;
	}
}
