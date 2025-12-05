package com.example.sevenzip;

import com.example.sevenzip.extraction.ArchiveStructReader;
import com.example.sevenzip.model.ArcItem;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public abstract class ProcessSevenZipArchiveAbstract {

	public abstract IOutCreateCallback getCreationCallback(List<ArcItem> items);

	public void processArchive(File inputFile, File outputFile)  {

		ArchiveStructReader reader = new ArchiveStructReader();
		List<ArcItem> items= reader.readArchive(inputFile);

		boolean success = false;
		RandomAccessFile archiveFile = null;
		IOutCreateArchive7z outArchive = null;
		try {
			archiveFile = new RandomAccessFile(outputFile, "rw");

			// Open out-archive object
			outArchive = SevenZip.openOutArchive7z();

			// Configure archive
			outArchive.setLevel(0); // 9 - max compression level

			// Create archive
			outArchive.createArchive(new RandomAccessFileOutStream(archiveFile), items.size(), getCreationCallback(items));

			success = true;

		} catch (SevenZipException e) {
			System.err.println("7z-Error occurs:");
			e.printStackTraceExtended(); // Get more information using extended method
		} catch (Exception e) {
			System.err.println("Error occurs: " + e);
		} finally {
			if (!closeResource(outArchive)) {
				System.err.println("Error closing archive: ");
				success = false;
			}
			if (!closeResource(archiveFile)) {
				System.err.println("Error closing file: ");
				success = false;
			}
			reader.finalize();
		}
		if (success) {
			System.out.println("ok");
		}
	}

	private boolean closeResource(Closeable closeable) {
		boolean success = true;
		if (closeable != null)
			try {
				closeable.close();
			} catch (IOException ignore) {
				success = false;
			}
		return success;
	}
}