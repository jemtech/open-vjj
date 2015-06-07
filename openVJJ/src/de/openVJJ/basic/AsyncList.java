/**
 * 
 */
package de.openVJJ.basic;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * Copyright (C) 2015 Jan-Erik Matthies
 * 
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Jan-Erik Matthies
 * 
 * a extreme fast list, but not safe.
 * 
 * @param <T> is the type of stored data
 */
public class AsyncList<T extends Object> extends Value implements Iterable<T>, Collection<T>{
	private int maxSize;
	private T[] data;
	private int dataCount = 0;
	private int[] toDelete;
	private int toDeleteCount = 0;
	private T[] toAdd;
	private int toAddCount = 0;

	/**
	 * constructor
	 * @param maxSize maximum amount of elements
	 */
	public AsyncList(int maxSize){
		this.maxSize = maxSize;
		initArrays();
	}
	
	/**
	 * constructor
	 * @param maxSize maximum amount of elements
	 * @param initialData the initial data to add
	 */
	public AsyncList(int maxSize, Collection<T> initialData){
		this.maxSize = maxSize;
		initArrays(initialData.toArray());
	}
	
	/**
	 * Initializes the the lists
	 */
	@SuppressWarnings("unchecked")
	private void initArrays(){
		this.data = null;
		this.toDelete = null;
		this.toAdd = (T[]) new Object[maxSize];
	}
	

	/**
	 * Initializes the the lists and adds initial data
	 * @param initialData data to add
	 */
	@SuppressWarnings("unchecked")
	private void initArrays(Object[] initialData){
		this.data = (T[]) initialData;
		this.dataCount = initialData.length;
		this.toDelete = new int[initialData.length];
		this.toAdd = (T[]) new Object[maxSize - initialData.length];
	}
	
	/**
	 * returns the element at the given index
	 * @param index of the element
	 * @return the selected element
	 */
	public T get(int index){
		return data[index];
	}
	
	/**
	 * adds an element to the end of the list
	 * @param toAdd the element to add
	 */
	synchronized public void fastAdd(T toAdd){
		this.toAdd[toAddCount] = toAdd;
		toAddCount++;
	}
	
	/**
	 * adds multiple elements to the end of the list
	 * @param toAdd the element to add
	 */
	synchronized public void fastAdd(Object[] toAdd){
		System.arraycopy(toAdd, 0, this.toAdd, toAddCount, toAdd.length);
		toAddCount += toAdd.length;
	}
	
	/**
	 * deletes the element at the given index
	 * @param indexToDelete index of the element to delete
	 */
	synchronized public void delete(int indexToDelete){
		toDelete[toDeleteCount] = indexToDelete;
		toDeleteCount++;
	}
	
	/**
	 * executes the deletes and removes
	 */
	public void execute(){
		//Deleting
		for(int i = 0; i < toDeleteCount; i++){
			data[toDelete[i]] = null;
		}
		//coppy to new
		@SuppressWarnings("unchecked")
		T[] dataNew = (T[]) new Object[dataCount + toAddCount - toDeleteCount];
		int dataCountNew = 0;
		for(int i = 0; i < dataCount; i++){
			if(data[i] != null){
				dataNew[dataCountNew] = data[i];
				dataCountNew++;
			}
		}
		//append new
		data = dataNew;
		dataCount = dataCountNew;
		for(int i = 0; i < toAddCount; i++){
			data[dataCount] = toAdd[i];
			dataCount++;
		}
		//reset temps
		toDelete = new int[dataCount];
		toDeleteCount = 0;
		@SuppressWarnings("unchecked")
		T[] toAddNew = (T[]) new Object[maxSize - dataCountNew];
		toAdd = toAddNew;
		toAddCount = 0;
	}
	
	/**
	 * @return the size of the list
	 */
	public int size(){
		return dataCount;
	}
	
	/**
	 * 
	 * @return the amount of changes to execute
	 */
	public int changes(){
		return toDeleteCount + toAddCount;
	}
	
	/**
	 * @param toFind the element to locate
	 * @return the index if the element. -1 if not found;
	 */
	public int indexOf(Object toFind){
		for(int i = 0; i < dataCount; i++){
			if(data[i] == toFind){
				return i;
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new AsyncListIterator();
	}
	
	public class AsyncListIterator implements Iterator<T>{
		int position = -1;

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return (position + 1 < dataCount);
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			position++;
			return data[position];
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			delete(position);
		}
		
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(T e) {
		fastAdd(e);
		execute();
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		fastAdd(c.toArray());
		execute();
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		initArrays();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		for(T e : this.data){
			if(e == o){
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object e : c){
			if(! contains(e)){
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if(dataCount < 1){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		int index = indexOf(o);
		if(index > -1){
			delete(index);
			return true;
		}
		execute();
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		for(Object e : c){
			remove(e);
		}
		execute();
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		for(int i = 0; i < dataCount; i++){
			if(! c.contains(data[i])){
				delete(i);
			}
		}
		execute();
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		Object[] copy = new Object[dataCount];
		System.arraycopy(data, 0, copy, 0, dataCount);
		return copy;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	/**
	 * 
	 * @param a
	 * @return data copied to a or a new array if a is to small 
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		if(dataCount <= a.length){
			System.arraycopy(data, 0, a, 0, dataCount);
			return a;
		}
		Object[] copy = new Object[dataCount];
		System.arraycopy(data, 0, copy, 0, dataCount);
		return (T[]) copy;
	}
}
