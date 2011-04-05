package net.aib42.directserver.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import net.aib42.directserver.Server;

public class ConnectionListener
	implements Runnable
{
	private Server server;
	private SocketAddress bindEndpoint;
	private ServerSocketChannel socketChannel;
	private Selector acceptSelector;
	private boolean keepRunning;

	/**
	 * Creates a ConnectionListener object that works with a given Server, listening on a given SocketAddress
	 *
	 * @throws IOException If any of the underlying socket objects cannot be created or configured
	 */
	public ConnectionListener(Server server, SocketAddress bindEndpoint) throws IOException
	{
		this.server = server;
		this.bindEndpoint = bindEndpoint;

		socketChannel = ServerSocketChannel.open();
		socketChannel.configureBlocking(false);

		acceptSelector = Selector.open();
		socketChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);

		keepRunning = true;
	}

	/**
	 * Runs in a loop, accepting connections and forwarding them to the server
	 */
	@Override
	public void run()
	{
		try {
			socketChannel.socket().bind(bindEndpoint);
		} catch (IOException ioe) {
			System.err.println("Unable to bind listening socket:");
			ioe.printStackTrace();
			return;
		}

		while (keepRunning) {
			int channelsReady = 0;

			try {
				channelsReady = acceptSelector.select();
			} catch (IOException ioe) {
				System.err.println("Unable to select listening socket:");
				ioe.printStackTrace();
			}

			if (channelsReady == 0) { //Thread interrupted or select() failed
				continue;
			}

			Iterator<SelectionKey> iter = acceptSelector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (!key.isAcceptable()) {
					continue;
				}

				try {
					SocketChannel clientChannel = socketChannel.accept();
					server.handleClientConnect(clientChannel);
				} catch (IOException ioe) {
					System.err.println("Unable to accept client connection:");
					ioe.printStackTrace();
				}
			}
		}
	}
}
