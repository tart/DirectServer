package net.aib42.lockserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.aib42.lockserver.module.ModuleCommand;

public class Client
{
	private static final int COMMAND_BUFFER_SIZE = 256;
	private static final int WRITE_BUFFER_SIZE = 1024;

	private static final long COMMAND_THREAD_TIMEOUT = 2L;
	private static final TimeUnit COMMAND_THREAD_TIMEOUT_UNITS = TimeUnit.MINUTES;

	private class CommandThread extends Thread {
		public CommandThread() {
			super("Client #" + id + " command");
		}
		@Override
		public void run() {
			while (true) {
				try {
					ModuleCommand command = commandQueue.poll(
						COMMAND_THREAD_TIMEOUT,
						COMMAND_THREAD_TIMEOUT_UNITS
					);
					if (command != null) {
						command.run();
					}
				} catch (InterruptedException ie) {
					break;
				}
			}
		}
	}

	private Server server;
	private long id;
	private SocketChannel channel;
	private ByteBuffer commandBuffer;
	private ByteBuffer writeBuffer;

	private LinkedBlockingQueue<ModuleCommand> commandQueue;
	private CommandThread commandThread;

	public Client(Server server, long id, SocketChannel channel)
	{
		this.server = server;
		this.id = id;
		this.channel = channel;

		commandBuffer = ByteBuffer.allocate(COMMAND_BUFFER_SIZE);
		writeBuffer = ByteBuffer.allocate(WRITE_BUFFER_SIZE);

		commandQueue = new LinkedBlockingQueue<ModuleCommand>();
		commandThread = new CommandThread();
	}

	public void startCommandThread()
	{
		commandThread.start();
	}

	public void stopCommandThread()
	{
		System.out.println("Client #" + id + " command thread stopping...");
		commandThread.interrupt();
	}

	public void queueCommand(ModuleCommand command)
	{
		try {
			commandQueue.put(command);
		} catch (InterruptedException ie) {
			System.err.println("Interrupted while queueing command:");
			ie.printStackTrace();
		}
	}

	public void writeToSocket(ByteBuffer bytes)
	{
		writeBuffer.clear();
		writeBuffer.put(bytes);
		writeBuffer.flip();
		writeBufferToSocket();
	}

	public void writeToSocket(byte[] bytes)
	{
		writeBuffer.clear();
		writeBuffer.put(bytes);
		writeBuffer.flip();
		writeBufferToSocket();
	}

	private void writeBufferToSocket()
	{
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

	public long getId()
	{
		return id;
	}

	public SocketChannel getChannel()
	{
		return channel;
	}

	public ByteBuffer getCommandBuffer()
	{
		return commandBuffer;
	}

	public ByteBuffer getWriteBuffer()
	{
		return writeBuffer;
	}
}
