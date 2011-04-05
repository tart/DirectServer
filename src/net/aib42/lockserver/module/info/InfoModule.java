package net.aib42.lockserver.module.info;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import net.aib42.lockserver.Client;
import net.aib42.lockserver.Server;
import net.aib42.lockserver.module.CommandParseError;
import net.aib42.lockserver.module.Module;

public class InfoModule extends Module
{
	public InfoModule(Server server)
	{
		super(server);
	}

	@Override
	public byte getCommandPrefix()
	{
		return 0x49; //'I'
	}

	@Override
	public boolean parseCommand(Client client, ByteBuffer buffer)
		throws BufferUnderflowException, CommandParseError
	{
		Command cmd = new Command(server, client);
		client.queueCommand(cmd);

		return true;
	}
}
