package com.example.sevenzip.extraction;

import net.sf.sevenzipjbinding.*;

import java.nio.ByteBuffer;

public class ItemExtractionCallback implements IArchiveExtractCallback {
	int bytesProcessed = 0; //file length
	int index; //file or dir index
	ByteBuffer wholeFileData;

	boolean skipExtraction;
	IInArchive inArchive;

	public ItemExtractionCallback(IInArchive inArchive, long fileSize) {
		this.inArchive= inArchive;
		this.wholeFileData = ByteBuffer.allocateDirect((int) fileSize);
	}

	/**
	 * Return sequential output stream for the file with index <code>index</code>.
	 * @param index - index of the item to extract
	 * @param extractAskMode - extract ask mode
	 * @return an instance of {@link ISequentialOutStream} sequential out stream or <code>null</code> to skip the
	 *         extraction of the current item (with index <code>index</code>) and proceed with the next one
	 */
	public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
		this.index = index;
		skipExtraction = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
		if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) {
			return null;
		}
		return data -> {
			wholeFileData.put(data);
			bytesProcessed += data.length;
			return data.length; // Return amount of proceed data
		};
	}

	public ByteBuffer data() {
		this.wholeFileData.flip();
		return this.wholeFileData;
	}


	/**
	 * Prepare operation. The index of the current archive item can be taken from the last call of
	 * {@link #getStream(int, ExtractAskMode)}.
	 *
	 * @param extractAskMode - extract ask mode
	 **/
	public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {}
	public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
		if (skipExtraction) return;
		if (extractOperationResult != ExtractOperationResult.OK) {
			System.err.println("Extraction error");
		} else {
			//System.out.println(String.format("%10s ||| %s", bytesProcessed,	inArchive.getProperty(index, PropID.PATH)));
			bytesProcessed = 0;
		}
	}
	public void setCompleted(long completeValue) throws SevenZipException {}
	public void setTotal(long total) throws SevenZipException {}
}