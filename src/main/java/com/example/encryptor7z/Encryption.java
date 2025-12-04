package com.example.encryptor7z;

import com.example.encryption.Encryptor;
import com.example.sevenzip.CompressSevenZipArchive;
import com.example.sevenzip.creation.CreationCallbackDecrypt;
import com.example.sevenzip.creation.CreationCallbackEncrypt;
import com.example.utils.EncodeName;
import com.example.utils.Utils;
import org.apache.commons.io.FilenameUtils;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Encryption {

	public static void setPassword(String password) {
		Encryptor.setPassword(password);
	}

	/**
	 * Encrypt image or any file.
	 *
	 * @param sourceImage     the source image
	 * @param destinationPath the destination path
	 * @throws IOException the io exception
	 */
	public static void encryptImage(Path sourceImage, Path destinationPath) throws IOException {

		String fullPath = sourceImage.getFileName().toString();
		String fileName = FilenameUtils.getName(fullPath);
		String encodedName = EncodeName.encrypt(fileName)+".cry";
		Path destinationFile = nextAvailable(destinationPath, encodedName, 0);
		Cipher cipher = CreationCallbackEncrypt.LazyHolder.INSTANCE;
		ByteBuffer buffer = Utils.mapReadOnly(sourceImage);

		ByteBuffer result = Encryptor.encrypt(cipher, new Encryptor.FileStruct(buffer, fileName));
		Utils.writeBufferToFile(result, destinationFile);
	}

	/**
	 * Decrypt "cry" image.
	 *
	 * @param sourceImage     the source image
	 * @param destinationPath the destination path
	 * @throws IOException the io exception
	 */
	public static void decryptImage(Path sourceImage, Path destinationPath) throws IOException {
		String fullPath = sourceImage.getFileName().toString();
		String baseName = FilenameUtils.getBaseName(fullPath);
		String decodedName = EncodeName.decrypt(baseName);
		Path destinationFile = nextAvailable(destinationPath, decodedName, 0);
		Cipher cipher = CreationCallbackDecrypt.LazyHolder.INSTANCE;
		ByteBuffer buffer = Utils.mapReadOnly(sourceImage);

		Encryptor.FileStruct result = Encryptor.decrypt(cipher, buffer);
		Utils.writeBufferToFile(result.data(), destinationFile);
	}

	/**
	 * Encrypt archive.
	 *
	 * @param sourceArchive   the source archive
	 * @param destinationPath the destination path
	 */
	public static void encryptArchive(Path sourceArchive, Path destinationPath) {
		Path destinationArchive = resolveNonExistingDestination(sourceArchive, destinationPath);
		CompressSevenZipArchive archiver =  new CompressSevenZipArchive();
		archiver.compressArchive(sourceArchive.toFile(), destinationArchive.toFile());
	}

	/**
	 * Decrypt archive.
	 *
	 * @param sourceArchive   the source archive
	 * @param destinationPath the destination path
	 */
	public static void decryptArchive(Path sourceArchive, Path destinationPath) {
		Path destinationArchive = resolveNonExistingDestination(sourceArchive, destinationPath);
		CompressSevenZipArchive archiver =  new CompressSevenZipArchive();
		archiver.compressArchive(sourceArchive.toFile(), destinationArchive.toFile());
	}

	private static Path resolveNonExistingDestination(Path sourceFile, Path destinationDir) {
		Path destinationFile = destinationDir.resolve(sourceFile.getFileName());
		if (!Files.exists(destinationFile)) {
			return destinationFile;
		}
		String originalName = sourceFile.getFileName().toString();
		return nextAvailable(destinationDir, originalName, 0);
	}

	private static Path nextAvailable(Path dir, String originalName, int counter) {
		String candidateName;
		if (counter == 0) {
			candidateName = originalName;
		} else {
			String base = FilenameUtils.getBaseName(originalName);
			String ext  = FilenameUtils.getExtension(originalName);
			String extPart = ext.isEmpty() ? "" : "." + ext;
			candidateName = FilenameUtils.getBaseName(originalName) + "_" + counter + extPart;
		}

		Path candidate = dir.resolve(candidateName);
		return Files.exists(candidate) ? nextAvailable(dir, originalName, counter + 1) : candidate;
	}

}
