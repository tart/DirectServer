package net.aib42.directserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import net.aib42.directserver.module.CommandParseError;
import net.aib42.directserver.net.ClientSocketHandler;
import net.aib42.directserver.module.ModuleCommand;

public class Client
	implements Runnable
{
	private Server server;
	private long id;
	private ClientSocketHandler socketHandler;
	private Queue<ModuleCommand> commandQueue;

	public Client(Server server, long id, SocketChannel channel) throws IOException
	{
		this.server = server;
		this.id = id;
		socketHandler = new ClientSocketHandler(this, channel);
		commandQueue = new LinkedList<ModuleCommand>();
	}

	/**
	 * Client loop: Reads, parses and executes commands until the client disconnects or there is an error
	 */
	@Override
	public void run()
	{
clientLoop:
		while (true) {
			if (!socketHandler.readFromSocket()) {
				System.out.println("Client #" + id + " disconnecting.");
				break clientLoop;
			}

			try {
				server.getCommandParser().parseClientCommands(this, socketHandler.getReadBuffer());
			} catch (CommandParseError cpe) {
				System.out.println("Command parse error client #" + id + ", disconnecting:");
				cpe.printStackTrace(System.out);
				break clientLoop;
			}

			ModuleCommand command;
			while ((command = commandQueue.poll()) != null) {
				try {
					command.run();
				} catch (InterruptedException ie) {
					System.out.println("Client #" + id + " command interrupted:");
					ie.printStackTrace(System.out);
					break clientLoop;
				}
			}

			if (!socketHandler.getReadBuffer().hasRemaining()) {
				System.out.println("Client #" + id + " buffer full, disconnecting.");
				break clientLoop;
			}
		}

		server.handleClientDisconnect(this);
	}

	/**
	 * Inserts a command into this client's command queue
	 */
	public void queueCommand(ModuleCommand command)
	{
		commandQueue.add(command);
	}

	/**
	 * Writes to the client socket
	 */
	public void writeToSocket(byte[] bytes)
	{
		socketHandler.writeToSocket(bytes);
	}

	/**
	 * Writes to the client socket
	 */
	public void writeToSocket(ByteBuffer bytes)
	{
		socketHandler.writeToSocket(bytes);
	}

	/**
	 * Returns the client's id
	 */
	public long getId()
	{
		return id;
	}
}
