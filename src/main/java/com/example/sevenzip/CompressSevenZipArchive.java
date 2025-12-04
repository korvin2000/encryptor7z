package com.example.sevenzip;

import com.example.encryption.Encryptor;
import com.example.sevenzip.creation.CreationCallbackEncrypt;
import com.example.sevenzip.model.ArcItem;
import net.sf.sevenzipjbinding.IOutCreateCallback;

import java.io.File;
import java.util.List;

public class CompressSevenZipArchive extends ProcessSevenZipArchiveAbstract{

	public static void main(String[] args) {
		CompressSevenZipArchive c = new CompressSevenZipArchive();
		Encryptor.setPassword("start123");
		c.compressArchive(new File("e:\\books\\test.7z"), new File("A:\\result.7z"));
	}

	public void compressArchive(File inputFile, File outputFile)  {
		this.processArchive(inputFile, outputFile);
	}

	@Override
	public IOutCreateCallback getCreationCallback(List<ArcItem> items) {
		return new CreationCallbackEncrypt(items);
	}
}