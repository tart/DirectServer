package net.aib42.lockserver.ui;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.aib42.lockserver.Server;
import net.aib42.lockserver.module.Module;
import net.aib42.lockserver.module.info.InfoModule;
import net.aib42.lockserver.module.lock.LockModule;
import net.aib42.lockserver.net.ConnectionListener;

public class Driver
{
	public static void main(String[] args)
	{
		Server server = new Server();
		Module lockModule = new LockModule(server);
		Module infoModule = new InfoModule(server);

		if (!server.registerModule(lockModule) ||
		    !server.registerModule(infoModule)
		) {
			System.err.println("Unable to register a module.");
			return;
		}

		ConnectionListener cl;
		try {
			 cl = new ConnectionListener(server, new InetSocketAddress(6666));
		} catch (IOException ioe) {
			System.err.println("Unable to create connection listener:");
			ioe.printStackTrace();
			return;
		}

		cl.run();
	}
}
