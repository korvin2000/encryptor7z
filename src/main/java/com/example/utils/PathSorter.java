package com.example.utils;

import com.example.sevenzip.model.ArcItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PathSorter {

	private PathSorter() {
		// utility class
	}

	/**
	 * Сортирует список путей так, что:
	 *  - все файлы из одной директории идут подряд
	 *  - внутри директории файлы отсортированы по алфавиту
	 *
	 * Сложность: O(n log n * L), где n — число путей, L — средняя длина пути.
	 */
	public static List<ArcItem> sortPathsGrouped(List<ArcItem> paths, boolean caseSensitive) {
		// Если нужно оставить исходный список неизменным:
		List<ArcItem> copy = new ArrayList<>(paths);

		copy.sort(buildComparator(caseSensitive));
		return copy;
	}

	private static Comparator<ArcItem> buildComparator(boolean caseSensitive) {
		return new Comparator<ArcItem>() {
			@Override
			public int compare(ArcItem p1, ArcItem p2) {
				PathKey k1 = buildKey(p1.path(), caseSensitive);
				PathKey k2 = buildKey(p2.path(), caseSensitive);

				int cmp = k1.directory().compareTo(k2.directory());
				if (cmp != 0) {
					return cmp;
				}
				return k1.filename().compareTo(k2.filename());
			}
		};
	}

	private static PathKey buildKey(String path, boolean caseSensitive) {
		String normalized = normalize(path, caseSensitive);

		int idx = normalized.lastIndexOf('/');
		String dirPart;
		String basename;

		if (idx == -1) {
			dirPart = "";
			basename = normalized;
		} else {
			dirPart = normalized.substring(0, idx);
			basename = normalized.substring(idx + 1);
		}

		return new PathKey(dirPart, basename);
	}

	private static String normalize(String path, boolean caseSensitive) {
		// унифицируем разделители
		String p = path.replace('\\', '/');
		if (!caseSensitive) {
			p = p.toLowerCase(Locale.ROOT); // аналог casefold в рамках Java
		}
		return p;
	}

	/**
	 * Ключ сортировки: (директория, имя файла) после нормализации.
	 */
	private record PathKey(String directory, String filename) {}

}
