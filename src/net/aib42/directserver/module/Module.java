package net.aib42.directserver.module;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import net.aib42.directserver.Client;
import net.aib42.directserver.module.CommandParseError;

public abstract class Module
{
	/**
	 * Parses a client command from the buffer
	 *
	 * Reads /buffer/ from /position/ to /limit/, returning true if a command was successfully parsed, in which case
	 * the buffer is cleared until the new /position/ value at the time of return. If it returns false, the buffer is
	 * reset back to its original state.
	 *
	 * A ParseError disconnects the client.
	 * BufferUnderflowException's are gracefully handled and are equivalent to returning false.
	 */
	public abstract boolean parseCommand(Client client, ByteBuffer buffer)
		throws BufferUnderflowException, CommandParseError;
}
