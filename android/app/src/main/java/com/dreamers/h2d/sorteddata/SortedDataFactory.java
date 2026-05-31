package com.dreamers.h2d.sorteddata;

import java.util.Comparator;

public class SortedDataFactory {
	public static <T> SortedData<T> makeSortedData(Comparator<T> comparator) {
		return new com.dreamers.h2d.sorteddata.sortedarraylist.SortedArrayList<>(comparator);
		//return new com.dreamers.h2d.sorteddata.avltree.AVLTree<>(comparator);
	}
}
