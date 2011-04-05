package net.aib42.directserver;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;

import net.aib42.directserver.module.CommandParseError;
import net.aib42.directserver.module.Module;

public class CommandParser
{
	private Server server;

	public CommandParser(Server server)
	{
		this.server = server;
	}

	public void parseClientCommands(Client client, ByteBuffer buffer) throws CommandParseError
	{
		int oldPosition = buffer.position();
		buffer.flip();

		boolean parsedSuccessfully;
		do {
			buffer.mark();

			try {
				Module module = getModuleByCommandPrefix(buffer);

				if (module == null) {
					throw new CommandParseError("Invalid command prefix received");
				}

				parsedSuccessfully = module.parseCommand(client, buffer);
			} catch (BufferUnderflowException bue) {
				parsedSuccessfully = false;
			}

			if (!parsedSuccessfully) {
				buffer.reset();
			}
		} while (parsedSuccessfully);

		if (buffer.position() != 0) { //Some commands parsed and we need compacting
			buffer.compact();
		} else {
			buffer.clear().position(oldPosition); //unflip
		}
	}

	public Module getModuleByCommandPrefix(ByteBuffer buffer) throws BufferUnderflowException
	{
		byte commandPrefix = buffer.get();
		return server.getModuleByCommandPrefix(commandPrefix);
	}

	public int getUnsignedByte(ByteBuffer buffer) throws BufferUnderflowException
	{
		byte b = buffer.get();
		return (b < 0 ? (b + 256) : b);
	}
}
