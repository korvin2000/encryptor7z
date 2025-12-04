package com.example.sevenzip.extraction;

import com.example.sevenzip.model.ArcItem;
import com.example.sevenzip.model.Holder;
import com.example.utils.PathSorter;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchiveStructReader {


	public static List<ArcItem> readArchive(File archive) {
		if (!archive.isFile()) {
			System.out.println("is not a file");
			return Collections.emptyList();
		}
		List<ArcItem> items = new ArrayList<>();
		RandomAccessFile randomAccessFile = null;
		IInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(archive, "r");
			inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			Holder<IInArchive> holder  = new Holder<>(inArchive);
			int itemCount = inArchive.getNumberOfItems();
			for (int index = 0; index < itemCount; index++) {
				Boolean isDir = (Boolean) inArchive .getProperty(index, PropID.IS_FOLDER);
				if  (isDir) {
					continue;
				}
				Long size = (Long) inArchive.getProperty(index, PropID.SIZE);
				String path = (String) inArchive.getProperty(index, PropID.PATH);

				ArcItem arcItem = new ArcItem(holder, index, path, size);
				items.add(arcItem);
			}
			items = PathSorter.sortPathsGrouped(items, false);


		} catch (Exception e) {
			System.err.println("Error occurs: " + e);
		} finally {
//			if (!Utils.closeResource(inArchive)) {
//				System.err.println("Error closing archive.");
//			}
//			if (!Utils.closeResource(randomAccessFile)) {
//				System.err.println("Error closing file.");
//			}
		}
		return items;
	}
}