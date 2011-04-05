package net.aib42.lockserver.module.info;

import java.nio.ByteBuffer;

import net.aib42.lockserver.Client;
import net.aib42.lockserver.Server;
import net.aib42.lockserver.module.ModuleCommand;

public class Command extends ModuleCommand
{
	public Command(Server server, Client client)
	{
		super(server, client);
	}

	@Override
	public void run()
	{
		ByteBuffer infoBytes = server.getServerInformation();
		infoBytes.flip();
		client.writeToSocket(infoBytes);
	}
}
