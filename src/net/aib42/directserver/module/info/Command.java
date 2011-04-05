package net.aib42.directserver.module.info;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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
		byte[] infoBytes = ("DirectServer v" + Server.VERSION_STR + " by aib").getBytes(Charset.forName("UTF-8"));
		client.writeToSocket(infoBytes);
	}
}
