package com.example.utils;

import java.io.InputStream;
import java.nio.ByteBuffer;

public final class ByteBufferBackedInputStream extends InputStream {
	private ByteBuffer buf;

	public ByteBufferBackedInputStream(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public int read() {
		if (buf == null || !buf.hasRemaining()) {
			return -1;
		}
		return buf.get() & 0xFF;
	}

	@Override
	public int read(byte[] bytes, int off, int len) {
		if (buf == null || !buf.hasRemaining()) {
			return -1;
		}
		int toRead = Math.min(len, buf.remaining());
		buf.get(bytes, off, toRead);
		return toRead;
	}

	@Override
	public int available() {
		return buf == null ? 0 : buf.remaining();
	}

	@Override
	public void close() {
		if (buf != null) {
			buf.clear();
			buf = null;
		}
	}
}