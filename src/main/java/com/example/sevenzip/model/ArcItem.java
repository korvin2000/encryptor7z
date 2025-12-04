package com.example.sevenzip.model;

import com.example.sevenzip.extraction.ItemExtractionCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZipException;
import org.apache.commons.io.FilenameUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class ArcItem implements Comparable<ArcItem> {

	private final int idx;
	private final String path;
	private final Long size;
	Holder<IInArchive> holder;

	public ArcItem(Holder<IInArchive> holder, int idx, String path, Long size) {
		this.holder = holder;
		this.idx = idx;
		this.path = path;
		this.size = size;
	}

	public ByteBuffer data() throws SevenZipException {
		ItemExtractionCallback extract = new ItemExtractionCallback(holder.object(), size);
		holder.object().extract(new int[] {idx}, false, extract);
		return extract.data();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ArcItem arcItem)) return false;
		return idx() == arcItem.idx() && Objects.equals(size(), arcItem.size()) && Objects.equals(path(), arcItem.path());
	}

	@Override
	public int hashCode() {
		return Objects.hash(idx(), path(), size());
	}

	/**
	 * @return
	 */
	@Override
	public String toString() {
		return path();
	}

	public String nameOnly() {
		return FilenameUtils.getName(path);
	}

	/**
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(ArcItem o) {
		return name().compareToIgnoreCase(o.name());
	}

	private String name() {
		return path != null ? path : "";
	}

	public int idx() {
		return idx;
	}

	public String path() {
		return path;
	}

	public Long size() {
		return size;
	}

}
