package net.aib42.directserver.ui;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.aib42.directserver.Server;
import net.aib42.directserver.module.Module;
import net.aib42.directserver.module.info.InfoModule;
import net.aib42.directserver.net.ConnectionListener;

public class Driver
{
	public static void main(String[] args)
	{
		Server server = new Server();
		ConnectionListener listener;

		Module infoModule = new InfoModule(server);

		if (
		    !server.registerModule(infoModule)
		) {
			System.err.println("Unable to register a module.");
			return;
		}


		try {
			listener = new ConnectionListener(server, new InetSocketAddress(6666));
		} catch (IOException ioe) {
			System.err.println("Unable to create connection listener:");
			ioe.printStackTrace();
			return;
		}

		listener.run();
	}
}
