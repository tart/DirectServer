package net.aib42.directserver.module;

@SuppressWarnings("serial")
public class CommandParseError extends Exception
{
	public CommandParseError()
	{
		super("Error parsing command");
	}

	public CommandParseError(String message)
	{
		super(message);
	}
}
