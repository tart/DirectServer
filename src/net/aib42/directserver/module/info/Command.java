package net.aib42.directserver.module.info;

import java.nio.ByteBuffer;

import net.aib42.directserver.Client;
import net.aib42.directserver.Server;
import net.aib42.directserver.module.ModuleCommand;

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
