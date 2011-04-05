package net.aib42.directserver.module;

import net.aib42.directserver.Client;
import net.aib42.directserver.Server;

public abstract class ModuleCommand
{
	protected Server server;
	protected Client client;

	public ModuleCommand(Server server, Client client)
	{
		this.server = server;
		this.client = client;
	}

	public abstract void run() throws InterruptedException;
}
