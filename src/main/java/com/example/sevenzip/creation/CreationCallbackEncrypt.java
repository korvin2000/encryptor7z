package com.example.sevenzip.creation;

import com.example.encryption.Encryptor;
import com.example.sevenzip.model.ArcItem;
import com.example.utils.ByteBufferBackedInputStream;
import com.example.utils.EncodeName;
import com.example.utils.Utils;
import net.sf.sevenzipjbinding.IOutItem7z;
import org.apache.commons.io.FilenameUtils;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class CreationCallbackEncrypt extends CreationCallbackAbstract {

	public static class LazyHolder {
		public static final Cipher INSTANCE = Encryptor.getEncryptionCipher();
	}

	public CreationCallbackEncrypt(List<ArcItem> items) {
		super(items);
	}

	Cipher getCipher() {
		return LazyHolder.INSTANCE;
	}

	void onItemInformation(int index, ArcItem arcItem, IOutItem7z outItem) {
		outItem.setDataSize(arcItem.size());
		String fullPath = arcItem.path();
		String ext = FilenameUtils.getExtension(fullPath);

		if (Utils.isImage(ext)) {
			String newPath = FilenameUtils.getPath(fullPath) + EncodeName.encrypt(FilenameUtils.getName(fullPath))+".cry";
			outItem.setPropertyPath(newPath);
		} else {
			outItem.setPropertyPath(fullPath);
		}
	}

	InputStream onItemStream(int index, ArcItem arcItem, ByteBuffer byteBuffer ) {
		ByteBuffer encryptedBuffer;
		if (Utils.isImage(FilenameUtils.getExtension(arcItem.path()))) {
			encryptedBuffer = Encryptor.encrypt(getCipher(), new Encryptor.FileStruct(byteBuffer, arcItem.nameOnly()));
		} else {
			encryptedBuffer = byteBuffer;
		}
		ByteBufferBackedInputStream is = new ByteBufferBackedInputStream(encryptedBuffer);
		return is;
	}


}
