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
	public static final int READ_BUFFER_SIZE  = 4096;
	public static final int WRITE_BUFFER_SIZE = 4096;

	private SocketChannel channel;
	private Selector selector;

	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;

	public ClientSocketHandler(SocketChannel channel) throws IOException
	{
		this.channel = channel;
		selector = Selector.open();

		readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
		writeBuffer = ByteBuffer.allocate(WRITE_BUFFER_SIZE);

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
	 * Closes the socket
	 */
	public void disconnect()
	{
		try {
			channel.close();
		} catch (IOException ioe) {
			System.err.println("Error closing client socket:");
			ioe.printStackTrace();
		}
	}

	/**
	 * Writes to the socket
	 */
	public void writeToSocket(byte[] bytes)
	{
		writeBuffer.clear();
		writeBuffer.put(bytes);
		writeBuffer();
	}

	/**
	 * Writes to the socket
	 */
	public void writeToSocket(ByteBuffer bytes)
	{
		writeBuffer.clear();
		writeBuffer.put(bytes);
		writeBuffer();
	}

	/**
	 * Returns the socket read buffer
	 */
	public ByteBuffer getReadBuffer()
	{
		return readBuffer;
	}

	/**
	 * Writes the write buffer to the socket
	 */
	private void writeBuffer()
	{
		writeBuffer.flip();
		while (writeBuffer.hasRemaining()) {
			try {
				channel.write(writeBuffer);
			} catch (IOException ioe) {
				System.err.println("Error writing to client socket:");
				ioe.printStackTrace();
				return;
			}
		}
	}
}
