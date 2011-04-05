package net.aib42.directserver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.aib42.directserver.module.CommandParseError;
import net.aib42.directserver.net.ClientSocketHandler;

public class Client
	implements Runnable
{
	private Server server;
	private long id;
	private ClientSocketHandler socketHandler;

	public Client(Server server, long id, SocketChannel channel) throws IOException
	{
		this.server = server;
		this.id = id;
		socketHandler = new ClientSocketHandler(this, channel);
	}

	/**
	 * Client loop: Reads and parses commands until the client disconnects
	 */
	@Override
	public void run()
	{
		while (true) {
			if (!socketHandler.readFromSocket()) {
				System.out.println("Client #" + id + " disconnecting.");
				break;
			}

			try {
				server.getCommandParser().parseClientCommands(this, socketHandler.getReadBuffer());
			} catch (CommandParseError cpe) {
				System.out.println("Command parse error client #" + id + ", disconnecting:");
				cpe.printStackTrace(System.out);
				break;
			}

			if (!socketHandler.getReadBuffer().hasRemaining()) {
				System.out.println("Client #" + id + " buffer full, disconnecting.");
				break;
			}
		}
	}

	/**
	 * Returns the client's id
	 */
	public long getId()
	{
		return id;
	}
}
