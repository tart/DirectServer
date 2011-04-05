package net.aib42.directserver;

import java.nio.channels.SocketChannel;

public class Server
{
	public void handleClientConnect(SocketChannel clientChannel)
	{
		System.out.println("New client: " + clientChannel.socket().getInetAddress().getHostAddress()); //TODO
	}
}
