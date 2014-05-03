package de.openVJJ.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.openVJJ.basic.Connection.ConnectionListener;

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
public class Module extends Plugable{
	/**
	 * {@link Plugable}s in this {@link Module}
	 */
	private List<Plugable> plugables = new ArrayList<Plugable>();
	
	private Map<String, Connection> inputConnectionMap = new HashMap<String, Connection>();
	private Map<Connection, ModuleConnectionListener> conectionListenerMap =  new HashMap<Connection, ModuleConnectionListener>();
	
	/**
	 * Use this method to add a {@link Plugable} to the {@link Module}
	 * @param plugable the {@link Plugable} to add to the {@link Module}
	 * @return <code>true</code> if everything went well <code>false</code> otherwise
	 */
	public boolean addPlugable(Plugable plugable) {
		return plugables.add(plugable);
	}
	
	/**
	 * Use this method to remove a {@link Plugable} from the {@link Module}
	 * @param plugable the {@link Plugable} to remove from the {@link Module}
	 * @return <code>true</code> if everything went well <code>false</code> otherwise
	 */
	public boolean removePlugable(Plugable plugable){
		return plugables.remove(plugable);
	}
	
	/**
	 * 
	 * @return the {@link List} of all {@link Plugable}s added to this {@link Module}
	 */
	public List<Plugable> getPlugables(){
		return plugables;
	}

	/** 
	 * creates a {@link ModuleConnectionListener} with a internal {@link Connection} and releases the old one if exists
	 * 
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		Connection inputConnection = getInputConnection(inpuName);
		if(inputConnection == null){
			return null;
		}
		if(! connection.classMatch(inputConnection.getValueClass())){
			System.err.println("Connection did not match");
			return null;
		}
		ModuleConnectionListener connectionListenerOld = conectionListenerMap.get(inputConnection);
		if(connectionListenerOld != null){
			connectionListenerOld.release();
		}
		ModuleConnectionListener connectionListener = new ModuleConnectionListener(connection, inputConnection);
		conectionListenerMap.put(inputConnection, connectionListener);
		return connectionListener;
	}
	
	/**
	 * Gets the internal {@link Connection} for the specified input. If there is no {@link Connection} it creates a new one. If The input not exists a <code>null</code> is returned.
	 * @param inpuName name of The input to return the {@link Connection}.
	 * @return the internal {@link Connection} for the specified input or <code>null</code> if the input not exists.
	 */
	private Connection getInputConnection(String inpuName){
		Connection inputConnection = inputConnectionMap.get(inpuName);
		if(inputConnection == null){
			Class<? extends Value> inputTypClass = getInputs().get(inpuName);
			if(inputTypClass == null){
				System.err.println("Input \"" + inpuName + "\" not found");
				return null;
			}
			inputConnection = new Connection(inputTypClass);
			inputConnectionMap.put(inpuName, inputConnection);
		}
		return inputConnection;
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
	private class ModuleConnectionListener extends ConnectionListener{

		private Connection internalConection;
		/**
		 * 
		 * @param externalConnection
		 * @param internalConection
		 */
		public ModuleConnectionListener(Connection externalConnection, Connection internalConection) {
			super(externalConnection);
			this.internalConection = internalConection;
		}

		/** 
		 * @see de.openVJJ.basic.Connection.ConnectionListener#valueReceved(de.openVJJ.basic.Value)
		 */
		@Override
		protected void valueReceved(Value value) {
			internalConection.transmitValue(value);
		}
		
	}
}
