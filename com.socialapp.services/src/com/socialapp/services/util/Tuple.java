package com.socialapp.services.util;

import java.util.Map.Entry;

public class Tuple<K, V> implements Entry<K, V>{

	public static final String TUPLE_SEPARATOR = "::";
	
	private K key;
	private V value;
	
	public Tuple(K key, V value){
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V object) {
		V oldVal = value;
		this.value = object;
		return oldVal;
	}

	@Override
	public String toString() {
		return key + TUPLE_SEPARATOR + value;
	}

	
}
