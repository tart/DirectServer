package net.aib42.lockserver;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.aib42.lockserver.module.CommandParseError;
import net.aib42.lockserver.module.Module;
import net.aib42.lockserver.net.ClientListener;

public class Server
{
	public static final String VERSION_STR = "0.0.1.0";

	private HashMap<Byte, Module> modules;
	private CommandParser commandParser;
	private AtomicLong nextClientId;

	public Server()
	{
		modules = new HashMap<Byte, Module>();
		commandParser = new CommandParser(this);
		nextClientId = new AtomicLong();
	}

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

	public Module getModuleByCommandPrefix(byte commandPrefix)
	{
		return modules.get(new Byte(commandPrefix));
	}

	public ByteBuffer getServerInformation()
	{
		ByteBuffer serverInfo = ByteBuffer.allocate(64000);

		try {
			serverInfo.put(
				new String("Server v" + Server.VERSION_STR + "\n").getBytes(Charset.forName("UTF-8"))
			);

			serverInfo.put(
				new String("Module count: " + modules.size() + "\n").getBytes(Charset.forName("UTF-8"))
			);

			for (Module m : modules.values()) {
				ByteBuffer mi = m.getModuleInformation();
				if (mi != null) {
					mi.flip();
					serverInfo.put(mi);
				}
			}
		} catch (BufferOverflowException boe) {
			System.err.println("Warning: Server information exceeds buffer capacity.");
		}

		return serverInfo;
	}

	public void checkClientCommandBuffer(Client client)
	{
		try {
			commandParser.parseClientCommand(client);
		} catch (CommandParseError cpe) {
			System.err.println("Command parse error, disconnecting client #" + client.getId() + ":");
			cpe.printStackTrace();
			forceClientDisconnect(client);
			return;
		}

		//If the buffer is full, disconnect the client
		if (!client.getCommandBuffer().hasRemaining()) {
			System.err.println("Client buffer full, disconnecting client #" + client.getId());
			forceClientDisconnect(client);
			return;
		}
	}

	public void handleClientDisconnect(Client client)
	{
		System.out.println("Client #" + client.getId() + " disconnected.");

		client.stopCommandThread();

		for (Module m : modules.values()) {
			m.clientDisconnected(client);
		}
	}

	public void handleClientConnect(SocketChannel clientChannel)
	{
		Client client = new Client(this, nextClientId.incrementAndGet(), clientChannel);
		System.out.println("New client #" + client.getId() + " connected.");

		try {
			clientChannel.configureBlocking(false);
		} catch (IOException ioe) {
			System.err.println("Unable to set non-blocking mode on client socket:");
			ioe.printStackTrace();
		}

ClientListener l;
try {
	l = new ClientListener(this);
} catch (IOException ioe) {
	System.err.println("Unable to create client listener:");
	ioe.printStackTrace();
	return;
}
l.addClient(client);
Thread t = new Thread(l);
t.setName("Client #" + client.getId() + " listener");
t.start();

		client.startCommandThread();
	}

	private void forceClientDisconnect(Client client)
	{
		client.stopCommandThread();

		try {
			client.getChannel().close();
		} catch (IOException ioe) {
			System.err.println("Error closing client channel:");
			ioe.printStackTrace();
		}
	}

	public CommandParser getCommandParser()
	{
		return commandParser;
	}
}
