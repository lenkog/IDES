package io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HeadTailInputStream extends FilterInputStream
{

	protected byte[] head;

	protected byte[] tail;

	private int headLeft;

	private int tailLeft;

	private boolean hitEOF = false;

	public HeadTailInputStream(InputStream in, byte[] head, byte[] tail)
	{
		super(in);
		this.head = head;
		this.tail = tail;
		headLeft = head.length;
		tailLeft = tail.length;
	}

	@Override
	public int available() throws IOException
	{
		if (!hitEOF)
		{
			return headLeft + super.available();
		}
		else
		{
			return tailLeft;
		}
	}

	public void mark()
	{
	}

	@Override
	public void reset()
	{
	}

	@Override
	public boolean markSupported()
	{
		return false;
	}

	@Override
	public long skip(long n) throws IOException
	{
		long skipped = 0;
		while (n - skipped > 0)
		{
			if (read() < 0) // hit the end of stream
			{
				break;
			}
			else
			{
				++skipped;
			}
		}
		return skipped;
	}

	@Override
	public int read() throws IOException
	{
		if (headLeft > 0) // still reading header
		{
			headLeft--;
			return head[head.length - headLeft - 1];
		}
		else if (!hitEOF) // header finished, reading input stream
		{
			int r = super.read();
			if (r < 0) // input stream finished
			{
				hitEOF = true;
				tailLeft--;
				return tail[0];
			}
			else
			{
				return r;
			}
		}
		else if (tailLeft > 0) // still reading tail
		{
			tailLeft--;
			return tail[tail.length - tailLeft - 1];
		}
		else
		// tail finished
		{
			return -1;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int bytesRead = 0;
		while (bytesRead < len)
		{
			int r = read();
			if (r < 0)
			{
				break;
			}
			b[off + bytesRead] = (byte)r;
			bytesRead++;
		}
		if (bytesRead == 0)
		{
			return -1;
		}
		return bytesRead;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}
}
