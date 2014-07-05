package de.openVJJ.basic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Copyright (C) 2014 Jan-Erik Matthies
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
 */
public class Connection{
	private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
	private Class<? extends Value> valueClass;
	
	/**
	 * the {@link Constructor} 
	 * @param valueClass {@link Class} representation of the {@link Value}s to transmit 
	 */
	public Connection(Class<? extends Value> valueClass){
		this.valueClass = valueClass;
	}
	
	public static final boolean multiThreading = true;
	ExecutorService executor = Executors.newCachedThreadPool();
	/**
	 * Call this method to submit the {@link Value} to all {@link ConnectionListener}
	 * @param value is submitted to all {@link ConnectionListener} if value is <code>null</code> nothing is done
	 */
	public void transmitValue(Value value){
		if(value == null){
			return;
		}
		Value.Lock lock = value.lock();
		for(ConnectionListener listener : listeners){
			try {
				if(multiThreading){
					ConnectionThread connectionThread = new ConnectionThread(listener, value);
					executor.execute(connectionThread);
				}else{
					listener.valueReceved(value);
				}
			} catch (Exception e) {
				System.err.println("Listener throws error while revive new Value. Removing from ListenerList");
				listeners.remove(listener);
				e.printStackTrace();
			}
		}
		value.free(lock);
	}
	
	/**
	 * Use to add a {@link ConnectionListener} to the {@link Connection} to receive {@link Value}s in future
	 * @param listener the {@link ConnectionListener} to add to the {@link Connection}
	 * @return <code>true</code> if everything went well <code>false</code> otherwise
	 */
	public boolean addListener(ConnectionListener listener){
		return listeners.add(listener);
	}
	
	/**
	 * Use to remove a {@link ConnectionListener} from the {@link Connection} to receive no further {@link Value}s in future
	 * @param listener the {@link ConnectionListener} to remove from the {@link Connection}
	 * @return <code>true</code> if everything went well <code>false</code> otherwise
	 */
	public boolean removeListener(ConnectionListener listener){
		return listeners.remove(listener);
	}
	
	/**
	 * @return the {@link Class} representation of the {@link Value} type the {@link Connection} transmits
	 */
	public Class<? extends Value> getValueClass(){
		return valueClass;
	}
	
	/**
	 * Use to check if your {@link Value} is assignable from this {@link Connection}
	 * @param matchingClass the {@link Class} representation of your {@link Value} to match
	 * @return <code>true</code> if {@link Class} type match <code>false</code> otherwise
	 */
	public boolean classMatch(Class<? extends Value> matchingClass){
		if(valueClass == null){
			System.err.println("valueClass is null");
			return false;
		}
		if(matchingClass == null){
			System.err.println("matchingClass is null");
			return false;
		}
		/*
		 * Super.class.isAssignableFrom(Sub.class)
		 */
		if(matchingClass.isAssignableFrom(valueClass)){
			return true;
		}
		return false;
	}
	
	/**
	 * call this to shutdown the Connection all listeners will removed
	 */
	public void shutdown(){
		for(ConnectionListener connectionListener : listeners){
			connectionListener.connectionShutdownCalled();
		}
		listeners.clear();
		executor.shutdown();
	}
	
	/**
	 * 
	 * Copyright (C) 2014 Jan-Erik Matthies
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
	 */
	public static abstract class ConnectionListener{
		
		/**
		 * connection to listen to
		 */
		private Connection connection;
		
		/**
		 * {@link Constructor}
		 * @param connection the {@link Connection} to listen to
		 */
		public ConnectionListener(Connection connection) {
			this.connection = connection;
			connection.addListener(this);
		}
		
		/**
		 * 
		 * @return the {@link Connection} listening to
		 */
		public Connection getConnection(){
			return connection;
		}
		
		/**
		 * stops listening to the {@link Connection}
		 */
		public void release(){
			connection.removeListener(this);
		}
		
		/**
		 * Implement to receive {@link Value}s through the {@link Connection}
		 * @param value the new {@link Value} received through the {@link Connection}
		 */
		protected abstract void valueReceved(Value value);
		
		protected abstract void connectionShutdownCalled();
	}
	
	private class ConnectionThread implements Runnable{

		private ConnectionListener listener;
		private Value value;
		public ConnectionThread(ConnectionListener listener, Value value){
			this.listener = listener;
			this.value = value;
		}
		@Override
		public void run() {
			listener.valueReceved(value);
		}
		
	}
}
