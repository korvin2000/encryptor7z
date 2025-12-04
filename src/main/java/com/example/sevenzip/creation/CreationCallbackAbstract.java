package com.example.sevenzip.creation;

import com.example.sevenzip.model.ArcItem;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.InputStreamSequentialInStream;
import net.sf.sevenzipjbinding.impl.OutItemFactory;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public abstract class CreationCallbackAbstract implements IOutCreateCallback<IOutItem7z> {

	protected List<ArcItem> items;

	public CreationCallbackAbstract(List<ArcItem> items) {
		this.items = items;
	}

	public void setOperationResult(boolean operationResultOk) throws SevenZipException {
		// Track each operation result here
	}

	public void setTotal(long total) throws SevenZipException {
		// Track operation progress here
	}

	public void setCompleted(long complete) throws SevenZipException {
		// Track operation progress here
	}

	public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) {
		IOutItem7z item = outItemFactory.createOutItem();
		ArcItem arcItem = this.items.get(index);
		onItemInformation(index, arcItem, item);
		return item;
	}

	abstract Cipher getCipher();

	abstract void onItemInformation(int index, ArcItem arcItem, IOutItem7z outItem);

	public ISequentialInStream getStream(int index) throws SevenZipException {
		ArcItem arcItem = this.items.get(index);
		if (arcItem.size() == 0) return null;
		ByteBuffer byteBuffer = arcItem.data();
		InputStream is = onItemStream(index, arcItem, byteBuffer);
		return new InputStreamSequentialInStream(is);
	}

	abstract InputStream onItemStream(int index, ArcItem arcItem, ByteBuffer byteBuffer );

}