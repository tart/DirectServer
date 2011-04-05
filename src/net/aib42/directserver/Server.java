package net.aib42.directserver;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import net.aib42.directserver.CommandParser;
import net.aib42.directserver.module.Module;

public class Server
{
	public static final String VERSION_STR = "0.0.1";

	private AtomicLong nextClientId;
	private CommandParser commandParser;

	public Server()
	{
		nextClientId = new AtomicLong();
		commandParser = new CommandParser(this);
	}

	public void handleClientConnect(SocketChannel clientChannel)
	{
		Client client;

		try {
			client = new Client(this, nextClientId.incrementAndGet(), clientChannel);
		} catch (IOException ioe) {
			System.err.println("Error creating new client:");
			ioe.printStackTrace();
			return;
		}
	}

	public Module getModuleByCommandPrefix(byte commandPrefix)
	{
		return null;
	}

	public CommandParser getCommandParser()
	{
		return commandParser;
	}

}
