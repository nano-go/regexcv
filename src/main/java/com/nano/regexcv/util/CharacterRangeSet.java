package com.nano.regexcv.util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;

public class CharacterRangeSet implements Iterable<CharacterRange>{
	
	private static final float LOAD_FACTOR = .75f;
	private static final int MAX_INITAL_CAPACITY = 1 << 30;
	
	private static class Node {
		private CharacterRange range;
		private Node next;
		
		protected Node(char from, char to, Node next) {
			this.range = new CharacterRange(from, to);
			this.next = next;
		}
		
		protected Node findNode(char from, char to) {
			Node n = this;
			while (n != null) {
				if (n.range.equals(from, to)) {
					return n;
				}
				n = n.next;
			}
			return null;
		}
	}
	
	private static char[] toCharArray(Set<Character> charSet){
		char[] arr = new char[charSet.size()];
		Iterator<Character> charIterator = charSet.iterator();
		for (int i = 0; i < arr.length; i ++) {
			arr[i] = charIterator.next();
		}
		return arr;
	}
	
	private static int tableSizeFor(int capacity) {
		int n = 1;
		while (n < capacity) {
			n <<= 1;
		}
		return n;
	}
	
	private static int hashCode(char from, char to) {
		int hash = CharacterRange.hashCode(from, to);
		return (hash >> 16) ^ hash;
	}
	
	private static int hashCode(Object obj) {
		int hash = obj.hashCode();
		return (hash >> 16) ^ hash;
	}
	
	private HashSet<Character> leftChars;
	private HashSet<Character> rightChars;
	
	private Node[] table;
	private int size;
	private int threadshould;
	
	public CharacterRangeSet() {
		this(16);
	}
	
	public CharacterRangeSet(int initalCapacity) {
		if (initalCapacity <= 0 || initalCapacity > MAX_INITAL_CAPACITY) {
			throw new IllegalArgumentException(
				"Illegal inital capacity: " + initalCapacity);
		}
		
		this.table = new Node[tableSizeFor(initalCapacity)];
		this.threadshould = (int) (this.table.length*LOAD_FACTOR);
		
		this.leftChars = new HashSet<>();
		this.rightChars = new HashSet<>();
	}
	
	private void ensureCapacity() {
		if (size < threadshould) {
			return;
		}
		Node[] oldTable = this.table;
		this.table = new Node[oldTable.length*2];
		this.threadshould = (int) (this.table.length*LOAD_FACTOR);
		for (Node n : oldTable) {		
			addNode(n);
		}
	}

	private void addNode(Node n) {
		while (n != null) {
			Node next = n.next;
			int index = this.table.length-1 & hashCode(n.range);
			n.next = this.table[index];
			this.table[index] = n;
			n = next;
		}
	}
	
	private void addNewNode(char from, char to, int tableIndex) {
		leftChars.add(from);
		rightChars.add(to);
		table[tableIndex] = new Node(from, to, table[tableIndex]);
		size ++;
	}
	
	public void addChar(char ch) {
		addRange(ch, ch);
	}
	
	public void addRange(char from, char to) {
		ensureCapacity();
		int hash = hashCode(from, to);
		int index = hash & table.length-1;
		Node n = table[index];
		if (n == null || (n = n.findNode(from, to)) == null) {
			addNewNode(from, to, index);	
		}
	}
	
	public char[] leftChars() {
		return toCharArray(leftChars);
	}
	
	public char[] rightChars() {
		return toCharArray(rightChars);
	}
	
	public List<Character> getChars() {
		List<Character> chars = new ArrayList<>();
		chars.addAll(leftChars);
		chars.addAll(rightChars);
		return chars;
	}
	
	public boolean isLeftChar(char ch) {
		return leftChars.contains(ch);
	}
	
	public boolean isRightChar(char ch) {
		return rightChars.contains(ch);
	}
	
	public int size() {
		return size;
	}
	
	public List<CharacterRange> sortedCharRanges() {
		ArrayList<CharacterRange> ranges = new ArrayList<>(size());
		for (CharacterRange range : this) {
			ranges.add(range);
		}
		Collections.sort(ranges);
		return ranges;
	}

	@Override
	public Iterator<CharacterRange> iterator() {
		return new CharRangeIterator(this.table);
	}

	@Override
	public Spliterator<CharacterRange> spliterator() {
		throw new UnsupportedOperationException();
	}
	
	private static class CharRangeIterator implements Iterator<CharacterRange> {

		private Node[] table;
		private int p;
		private Node cursor;

		public CharRangeIterator(Node[] table) {
			this.table = table;
			moveToNext();
		}

		private void moveToNext() {
			if (cursor != null) {
				cursor = cursor.next;
				if (cursor != null) {
					return;
				}
			}
			while (p < table.length) {
				cursor = table[p ++];
				if (cursor != null) {
					return;
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return cursor != null;
		}

		@Override
		public CharacterRange next() {
			CharacterRange range = cursor.range;
			moveToNext();
			return range;
		}
	}
}
