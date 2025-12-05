package com.example.utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
public class Utils {

	final static HashSet<String> extensions = new HashSet<String>(13, 1f) {{
		add("jpg");
		add("jpeg");
		add("jpe");
		add("png");
		add("bmp");
		add("gif");
		add("jxl");
		add("avif");
		add("aim");
		add("tiff");
		add("psd");
		add("heic");
		add("jp2");
	}};

	public static boolean isImage(String extension) {
		return extensions.contains(extension.toLowerCase());
	}

	public static boolean isArchive(String extension) {
		return switch (extension.toLowerCase()) {
			case "zip", "rar", "7z" -> true;
			default -> false;
		};
	}

	public static boolean isEncrypted(String extension) {
		return "cry".compareToIgnoreCase(extension) == 0;
	}

	public static boolean closeResource(Closeable closeable) {
		boolean success = true;
		if (closeable != null)
			try {
				closeable.close();
			} catch (IOException ignore) {
				success = false;
			}
		return success;
	}

	public static MappedByteBuffer mapReadOnly(Path path) throws IOException {
		try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
			long size = channel.size();              // watch out: >2GB needs segmenting
			return channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
		}
	}

	public static void writeBufferToFile(ByteBuffer buffer, Path path) throws IOException {
		// make sure buffer is in read mode
		if (buffer.position() != 0 && buffer.limit() == buffer.capacity()) {
			buffer.flip(); // or manage this outside depending on your API
		}

		try (FileChannel channel = FileChannel.open( path, Set.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
		)) {
			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			// optionally force to disk
			channel.force(true);
		}
	}

}



