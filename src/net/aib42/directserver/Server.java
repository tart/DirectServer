package net.aib42.directserver;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.aib42.directserver.CommandParser;
import net.aib42.directserver.module.Module;

public class Server
{
	public static final String VERSION_STR = "0.0.1";

	private AtomicLong nextClientId;
	private CommandParser commandParser;
	private HashMap<Byte, Module> modules;

	public Server()
	{
		nextClientId = new AtomicLong();
		commandParser = new CommandParser(this);
		modules = new HashMap<Byte, Module>();
	}

	/**
	 * Registers a module with this server
	 *
	 * @return true on success
	 */
	public boolean registerModule(Module module)
	{
		Byte commandPrefix = new Byte(module.getCommandPrefix());

		if (modules.containsKey(commandPrefix)) {
			return false;
		}

		modules.put(commandPrefix, module);
		module.moduleRegistered();

		return true;
	}

	/**
	 * Called when a new connection is made
	 */
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

		Thread clientThread = new Thread(client, "Client #" + client.getId());
		clientThread.start();
	}

	/**
	 * Called by a client after it disconnects
	 */
	public void handleClientDisconnect(Client client)
	{
		System.out.println("Client #" + client.getId() + " disconnected.");
		client.disconnect();

		for (Module m : modules.values()) {
			m.clientDisconnected(client);
		}
	}

	/**
	 * Returns a module given a module command prefix
	 *
	 * @return null on error
	 */
	public Module getModuleByCommandPrefix(byte commandPrefix)
	{
		return modules.get(new Byte(commandPrefix));
	}

	/**
	 * Returns server-wide and module-specific information
	 *
	 * Used by the info module
	 */
	public ByteBuffer getServerInformation()
	{
		ByteBuffer serverInfo = ByteBuffer.allocate(64000);

		try {
			serverInfo.put(
				new String("DirectServer v" + Server.VERSION_STR + "\n")
					.getBytes(java.nio.charset.Charset.forName("UTF-8"))
			);

			serverInfo.put(
				new String("Module count: " + modules.size() + "\n")
					.getBytes(java.nio.charset.Charset.forName("UTF-8"))
			);

			for (Module m : modules.values()) {
				serverInfo.put(m.getCommandPrefix());

				ByteBuffer mi = m.getModuleInformation();

				if (mi != null) {
					mi.flip();
					serverInfo.putShort((short) mi.remaining());
					serverInfo.put(mi);
				} else {
					serverInfo.putShort((short) 0);
				}
			}
		} catch (BufferOverflowException boe) {
			System.err.println("Warning: Server information exceeds buffer capacity.");
		}

		return serverInfo;
	}

	public CommandParser getCommandParser()
	{
		return commandParser;
	}
}
