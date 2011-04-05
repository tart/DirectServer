package net.aib42.lockserver.module.lock;

import java.util.Arrays;

public class LockName
{
	private byte[] name;

	public LockName(byte[] name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof LockName) && Arrays.equals(name, ((LockName) obj).name);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(name);
	}
}
