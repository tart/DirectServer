package net.aib42.lockserver.module;

import net.aib42.lockserver.Client;
import net.aib42.lockserver.Server;

public abstract class ModuleCommand
{
	protected Server server;
	protected Client client;

	public ModuleCommand(Server server, Client client)
	{
		this.server = server;
		this.client = client;
	}

	public abstract void run();
}
