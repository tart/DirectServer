package net.aib42.directserver.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import net.aib42.directserver.Client;

public class ClientSocketHandler
{
	private Client client;
	private SocketChannel channel;
	private Selector selector;

	private ByteBuffer readBuffer;
//	private ByteBuffer writeBuffer;

	public ClientSocketHandler(Client client, SocketChannel channel) throws IOException
	{
		this.client = client;
		selector = Selector.open();

		readBuffer = ByteBuffer.allocate(1024);
//		writeBuffer = ByteBuffer.allocate(1024);

		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
	}

	/**
	 * Attempts to read from the client socket, filling the read buffer
	 *
	 * @return false if the socket was closed or an I/O error occured
	 */
	public boolean readFromSocket()
	{
		try {
			int channelsReady = selector.select();

			if (channelsReady == 0) { //Thread interrupted or select() failed
				return false;
			}

			Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (!key.isReadable()) {
					continue;
				}

				if (channel.read(readBuffer) == -1) {
					return false;
				}
			}
		} catch (IOException ioe) {
			System.err.println("I/O error reading from client, disconnecting:");
			ioe.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Returns the socket read buffer
	 */
	public ByteBuffer getReadBuffer()
	{
		return readBuffer;
	}
}
