package net.aib42.directserver;

import java.io.IOException;
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

	public CommandParser getCommandParser()
	{
		return commandParser;
	}
}
