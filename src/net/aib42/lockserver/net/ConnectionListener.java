package net.aib42.lockserver.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import net.aib42.lockserver.Server;

public class ConnectionListener
	implements Runnable
{
	private Server server;
	private SocketAddress bindEndpoint;
	private ServerSocketChannel socketChannel;
	private Selector acceptSelector;

	public ConnectionListener(Server server, SocketAddress bindEndpoint) throws IOException
	{
		this.server = server;
		this.bindEndpoint = bindEndpoint;

		socketChannel = ServerSocketChannel.open();
		socketChannel.configureBlocking(false);

		acceptSelector = Selector.open();
		socketChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
	}

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

		while (true) {
			int channelsReady = 0;

			try {
				channelsReady = acceptSelector.select();
			} catch (IOException ioe) {
				System.err.println("Unable to select() listening socket:");
				ioe.printStackTrace();
			}

			if (channelsReady == 0) { //Thread interrupted or select() failed
				break;
			}

			Iterator<SelectionKey> iter = acceptSelector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (key.readyOps() != SelectionKey.OP_ACCEPT) {
					System.err.println("Invalid key operation");
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
