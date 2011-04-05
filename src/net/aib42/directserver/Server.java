package net.aib42.directserver;

import java.nio.channels.SocketChannel;

import net.aib42.directserver.CommandParser;
import net.aib42.directserver.module.Module;

public class Server
{
	public static final String VERSION_STR = "0.0.1";

	private CommandParser commandParser;

	public Server()
	{
		commandParser = new CommandParser(this);
	}

	public void handleClientConnect(SocketChannel clientChannel)
	{
		System.out.println("New client: " + clientChannel.socket().getInetAddress().getHostAddress()); //TODO
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
