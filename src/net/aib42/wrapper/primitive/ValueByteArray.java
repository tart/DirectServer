package net.aib42.wrapper.primitive;

import java.util.Arrays;

/**
 * A byte array wrapper that overrides {@link Object#equals} and {@link Object#hashCode} to work by-value.
 */
public class ValueByteArray
{
	private byte[] value;

	/**
	 * Constructs a ValueByteArray given a byte array.
	 */
	public ValueByteArray(byte[] value)
	{
		this.value = value;
	}

	/**
	 * Overrides {@link Object#equals} to return <code>true</code> if two ValueByteArrays have the same value.
	 */
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof ValueByteArray) && Arrays.equals(value, ((ValueByteArray) obj).value);
	}

	/**
	 * Overrides {@link Object#hashCode} to return the same hash code for two ValueByteArrays having the same value.
	 */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(value);
	}

	/**
	 * Returns the value wrapped in this object.
	 */
	public byte[] getValue()
	{
		return value;
	}
}
