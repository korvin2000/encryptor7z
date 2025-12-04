package com.example.sevenzip;

/* BEGIN_SNIPPET(CompressSevenZipArchive) */

import com.example.encryption.Encryptor;
import com.example.sevenzip.creation.CreationCallbackDecrypt;
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

public class DecompressSevenZipArchive extends ProcessSevenZipArchiveAbstract {

	public static void main(String[] args) {
		DecompressSevenZipArchive c = new DecompressSevenZipArchive();
		Encryptor.setPassword("start123");
		c.decompressArchive(new File("A:\\result.7z"), new File("A:\\resultDecoded.7z"));
	}

	public void decompressArchive(File inputFile, File outputFile)  {
		this.processArchive(inputFile, outputFile);
	}

	@Override
	public IOutCreateCallback getCreationCallback(List<ArcItem> items) {
		return new CreationCallbackDecrypt(items);
	}

}