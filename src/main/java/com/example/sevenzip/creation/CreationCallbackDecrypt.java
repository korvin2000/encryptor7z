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

public class CreationCallbackDecrypt extends CreationCallbackAbstract {

	public static class LazyHolder {
		public static final Cipher INSTANCE = Encryptor.getDecryptionCipher();
	}

	public CreationCallbackDecrypt(List<ArcItem> items) {
		super(items);
	}

	Cipher getCipher() {
		return LazyHolder.INSTANCE;
	}

	void onItemInformation(int index, ArcItem arcItem, IOutItem7z outItem) {
		outItem.setDataSize(arcItem.size());
		String fullPath = arcItem.path();

		String ext = FilenameUtils.getExtension(fullPath);
		if (Utils.isEncrypted(ext)) {
			String baseName = FilenameUtils.getBaseName(fullPath);
			String newPath = FilenameUtils.getPath(fullPath) + EncodeName.decrypt(baseName);
			outItem.setPropertyPath(newPath);
		} else {
			outItem.setPropertyPath(fullPath);
		}
	}

	InputStream onItemStream(int index, ArcItem arcItem, ByteBuffer byteBuffer ) {
		ByteBuffer outBuffer;
		if (Utils.isEncrypted(FilenameUtils.getExtension(arcItem.path()))) {
			Encryptor.FileStruct fs = Encryptor.decrypt(Encryptor.getDecryptionCipher(), byteBuffer);
			outBuffer = fs.data();
		} else {
			outBuffer = byteBuffer;
		}
		ByteBufferBackedInputStream is = new ByteBufferBackedInputStream(outBuffer);
		return is;
	}


}
