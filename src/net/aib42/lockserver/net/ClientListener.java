package net.aib42.lockserver.net;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

import net.aib42.lockserver.Client;
import net.aib42.lockserver.Server;

public class ClientListener
	implements Runnable
{
	private Server server;
	private ConcurrentHashMap<SocketChannel, Client> clients;
	private Selector selector;

	public ClientListener(Server server) throws IOException
	{
		this.server = server;
		clients = new ConcurrentHashMap<SocketChannel, Client>();
		selector = Selector.open();
	}

	public void addClient(Client client)
	{
		clients.put(client.getChannel(), client);

		try {
			client.getChannel().register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException cce) {
			removeClient(client);
		}
	}

	private void removeClient(Client client)
	{
		server.handleClientDisconnect(client);
	}

	@Override
	public void run()
	{
		while (true) {
			int channelsReady = 0;

			try {
				channelsReady = selector.select();
			} catch (IOException ioe) {
				System.err.println("ClientListener was unable to select:");
				ioe.printStackTrace();
			}

			if (channelsReady == 0) { //Thread interrupted or unable to select
				break;
			}

			Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				SocketChannel clientChannel = (SocketChannel) key.channel();
				Client client = clients.get(clientChannel);

				if (client == null) {
					System.err.println("ClientListener selected an invalid client");
					break;
				}

				switch (key.readyOps()) {
					case SelectionKey.OP_READ:
						boolean clientDisconnected = false;

						try {
							if (clientChannel.read(client.getCommandBuffer()) == -1) {
								clientDisconnected = true;
							}
						} catch (IOException ioe) {
							System.err.println("I/O error reading from client, disconnecting:");
							ioe.printStackTrace();
							clientDisconnected = true;
						}

						if (clientDisconnected) {
							System.out.println("Client gracefully disconnected.");
							key.cancel();
							removeClient(client);
						} else {
							server.checkClientCommandBuffer(client);
						}
						break;

					default:
						System.err.println("Invalid key operation");
						break;
				}
			}
		}
	}
}
