package de.openVJJ.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.jdom2.Element;

import de.openVJJ.GUI.ModuleInsightPannel;
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
	private Map<Connection, ModuleConnectionListener> inConectionListenerMap =  new HashMap<Connection, ModuleConnectionListener>();
	
	private Map<Connection, ModuleConnectionListener> outConectionListenerMap =  new HashMap<Connection, ModuleConnectionListener>();
	
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
		Connection inputConnection = getIntenalModuleInputConnection(inpuName);
		if(inputConnection == null){
			return null;
		}
		if(! connection.classMatch(inputConnection.getValueClass())){
			System.err.println("Connection did not match");
			return null;
		}
		ModuleConnectionListener connectionListenerOld = inConectionListenerMap.get(inputConnection);
		if(connectionListenerOld != null){
			connectionListenerOld.release();
		}
		ModuleConnectionListener connectionListener = new ModuleConnectionListener(connection, inputConnection);
		inConectionListenerMap.put(inputConnection, connectionListener);
		return connectionListener;
	}
	
	/**
	 * Gets the internal {@link Connection} for the specified input. If there is no {@link Connection} it creates a new one. If The input not exists a <code>null</code> is returned.
	 * @param inputName name of The input to return the {@link Connection}.
	 * @return the internal {@link Connection} for the specified input or <code>null</code> if the input not exists.
	 */
	public Connection getIntenalModuleInputConnection(String inputName){
		Connection inputConnection = inputConnectionMap.get(inputName);
		if(inputConnection == null){
			Class<? extends Value> inputTypClass = getInputs().get(inputName);
			if(inputTypClass == null){
				System.err.println("Input \"" + inputName + "\" not found");
				return null;
			}
			inputConnection = new Connection(inputTypClass);
			inputConnectionMap.put(inputName, inputConnection);
		}
		return inputConnection;
	}
	
	/**
	 * connects to a internal {@link Module} output
	 * @param outputName name of the output to send {@link Value}s to
	 * @param toConnect the {@link Connection} witch should send {@link Value}s through the specified output
	 * @return <code>true</code> if everything went well. <code>false</code> otherwise 
	 */
	public boolean connectToInternalModuleOutput(String outputName, Connection toConnect){
		Connection outputConnection = getConnection(outputName);
		if(outputConnection == null){
			return false;
		}
		ModuleConnectionListener connectionListenerOld = outConectionListenerMap.get(outputConnection);
		if(connectionListenerOld != null){
			connectionListenerOld.release();
		}
		ModuleConnectionListener connectionListener = new ModuleConnectionListener(toConnect, outputConnection);
		outConectionListenerMap.put(outputConnection, connectionListener);
		return true;
	}
	
	/**
	 * creates a new input for the Module
	 * @param name of the new input
	 * @param inputType type of the input {@link Value}
	 * @return <code>true</code> if everything went well. <code>false</code> otherwise.
	 * @see Plugable#addInput(String, Class)
	 */
	public boolean createInput(String name, Class<? extends Value> inputType){
		return addInput(name, inputType);
	}
	
	/**
	 * creates a new output for the Module
	 * @param name of the new output
	 * @param outputType type of the output {@link Value}
	 * @return <code>true</code> if everything went well. <code>false</code> otherwise.
	 * @see Plugable#addOutput(String, Class)
	 */
	public boolean createOutput(String name, Class<? extends Value> outputType){
		return addOutput(name, outputType);
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
		public ModuleConnectionListener(Connection in, Connection out) {
			super(in);
			this.internalConection = out;
		}

		/** 
		 * @see de.openVJJ.basic.Connection.ConnectionListener#valueReceved(de.openVJJ.basic.Value)
		 */
		@Override
		protected void valueReceved(Value value) {
			internalConection.transmitValue(value);
		}

		/* (non-Javadoc)
		 * @see de.openVJJ.basic.Connection.ConnectionListener#connectionShutdownCalled()
		 */
		@Override
		protected void connectionShutdownCalled() {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public JPanel getConfigPannel() {
		return new ModuleInsightPannel(this);
	}
	
	public List<ConnectionInfo> getConnectionInfo(){
		ArrayList<ConnectionInfo> connectionInfoList = new ArrayList<ConnectionInfo>();
		
		for(Plugable plugable : plugables){
			for(String key : plugable.getInputs().keySet()){
				Connection.ConnectionListener listener = plugable.getListener(key);
				if(listener == null){
					continue;
				}
				Connection inputConnectedTo = listener.getConnection();
				for(Plugable plugableOut : plugables){
					for(String outKey : plugableOut.getOutputs().keySet()){
						Connection outCon = plugableOut.getConnection(outKey);
						if(outCon == inputConnectedTo){
							
							ConnectionInfo connectionInfo = new ConnectionInfo();
							connectionInfo.in = plugable;
							connectionInfo.inName = key;
							connectionInfo.out = plugableOut;
							connectionInfo.outName = outKey;
							connectionInfoList.add(connectionInfo);
						}
					}
				}
			}
		}
		
		return connectionInfoList;
	}
	
	public class ConnectionInfo{
		Plugable in;
		Plugable out;
		String inName;
		String outName;
		
		public Plugable getIn(){
			return in;
		}
		
		public Plugable getOut(){
			return out;
		}
		
		public String getInName(){
			return inName;
		}
		
		public String getOutName(){
			return outName;
		}
	}
	
	public static final String ELEMENT_NAME_PLUGABLES = "plugables";
	public static final String ELEMENT_NAME_PLUGABLE = "plugable";
	public static final String ELEMENT_NAME_PLUGABLE_CONFIG = "plugableConfig";
	public static final String ELEMENT_NAME_CONNECTIONS = "connections";
	public static final String ELEMENT_NAME_CONNECTION = "connection";

	private static final String ELEMENT_ATTRIBUTE_PLUGABLE_CLASS = "plugableClass";
	private static final String ELEMENT_ATTRIBUTE_PLUGABLE_NR = "plugableNr";
	
	private static final String ELEMENT_ATTRIBUTE_IN_PLUGABLE_NR = "inPlugabelNr";
	private static final String ELEMENT_ATTRIBUTE_OUT_PLUGABLE_NR = "outPlugabelNr";
	private static final String ELEMENT_ATTRIBUTE_IN_NAME = "inName";
	private static final String ELEMENT_ATTRIBUTE_OUT_NAME = "outName";
	/**
	 * @see de.openVJJ.basic.Plugable#setConfig(org.jdom2.Element)
	 */
	@Override
	public void setConfig(Element element) {
		super.setConfig(element);
		
		Map<Integer, Plugable> plugabelNrMap = new HashMap<Integer, Plugable>();
		Element plugablesElement = element.getChild(ELEMENT_NAME_PLUGABLES);
		if(plugablesElement != null){
			for(Element plugableElement : plugablesElement.getChildren(ELEMENT_NAME_PLUGABLE)){
				String plugableClass =  plugableElement.getAttributeValue(ELEMENT_ATTRIBUTE_PLUGABLE_CLASS);
				Plugable plugable = null;
				if(plugableClass != null){
					try {
					Class<?> c = Class.forName(plugableClass);
					Object classInstance = c.newInstance();
					plugable = (Plugable) classInstance;
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				if(plugable == null){
					System.err.println("Was not abel to create instance of Plugabe");
					continue;
				}
				String plugableNr =  plugableElement.getAttributeValue(ELEMENT_ATTRIBUTE_PLUGABLE_NR);
				if(plugableNr != null){
					plugabelNrMap.put(Integer.parseInt(plugableNr), plugable);
				}
				addPlugable(plugable);
				
				Element plugableElementConfig = plugableElement.getChild(ELEMENT_NAME_PLUGABLE_CONFIG);
				if(plugableElementConfig != null){
					plugable.setConfig(plugableElementConfig);
				}
			}
		}

		Element connectionsElement = element.getChild(ELEMENT_NAME_CONNECTIONS);
		if(connectionsElement != null){
			for(Element connectionElement : connectionsElement.getChildren(ELEMENT_NAME_CONNECTION)){
				String inNRString = connectionElement.getAttributeValue(ELEMENT_ATTRIBUTE_IN_PLUGABLE_NR);
				Plugable inPlugable = null;
				if(inNRString != null){
					inPlugable = plugabelNrMap.get(Integer.parseInt(inNRString));
				}
				String outNRString = connectionElement.getAttributeValue(ELEMENT_ATTRIBUTE_OUT_PLUGABLE_NR);
				Plugable outPlugable = null;
				if(outNRString != null){
					outPlugable = plugabelNrMap.get(Integer.parseInt(outNRString));
				}
				String inName = connectionElement.getAttributeValue(ELEMENT_ATTRIBUTE_IN_NAME);
				String outName = connectionElement.getAttributeValue(ELEMENT_ATTRIBUTE_OUT_NAME);
				
				if(inPlugable == null || outPlugable == null || inName == null || outName == null){
					System.err.println("Connection not fully defined: " + inNRString + ":" + inName + " -> " + outNRString + ":" + outName );
					continue;
				}
				
				Connection outCon = outPlugable.getConnection(outName);
				System.out.println(inPlugable.getName());
				System.out.println("loadCon:" + inPlugable.setInput(inName, outCon));
				System.out.println(inPlugable.getInputs().keySet().size());
				for(String key : inPlugable.getInputs().keySet()){
					System.out.println( key );
				}
				System.out.println(outCon);
				System.out.println(inName);
				System.out.println(inPlugable.getListener(inName));
				System.out.println(inPlugable.getListener(inName).getConnection());
			}
		}
		System.out.println("Con count:" + getConnectionInfo().size());
	}
	
	/**
	 * @see de.openVJJ.basic.Plugable#getConfig(org.jdom2.Element)
	 */
	@Override
	public void getConfig(Element element) {
		super.getConfig(element);

		Element plugablesElement = new Element(ELEMENT_NAME_PLUGABLES);
		element.addContent(plugablesElement);
		int pugableNr = 0;
		Map<Plugable, Integer> plugabelNrMap = new HashMap<Plugable, Integer>();
		for(Plugable plugable : getPlugables()){
			plugabelNrMap.put(plugable, pugableNr);
			
			Element plugableElement = new Element(ELEMENT_NAME_PLUGABLE);
			plugablesElement.addContent(plugableElement);
			plugableElement.setAttribute(ELEMENT_ATTRIBUTE_PLUGABLE_CLASS, plugable.getClass().getName());
			plugableElement.setAttribute(ELEMENT_ATTRIBUTE_PLUGABLE_NR, String.valueOf(pugableNr));
			
			Element plugableConfigElement = new Element(ELEMENT_NAME_PLUGABLE_CONFIG);
			plugableElement.addContent(plugableConfigElement);
			plugable.getConfig(plugableConfigElement);
			
			pugableNr++;
		}
		

		Element connectionsElement = new Element(ELEMENT_NAME_CONNECTIONS);
		element.addContent(connectionsElement);
		for(ConnectionInfo connectionInfo : getConnectionInfo()){
			Element connectionElement = new Element(ELEMENT_NAME_CONNECTION);
			connectionsElement.addContent(connectionElement);
			
			connectionElement.setAttribute(ELEMENT_ATTRIBUTE_IN_PLUGABLE_NR, String.valueOf(plugabelNrMap.get(connectionInfo.getIn())));
			connectionElement.setAttribute(ELEMENT_ATTRIBUTE_OUT_PLUGABLE_NR, String.valueOf(plugabelNrMap.get(connectionInfo.getOut())));
			connectionElement.setAttribute(ELEMENT_ATTRIBUTE_IN_NAME, connectionInfo.getInName());
			connectionElement.setAttribute(ELEMENT_ATTRIBUTE_OUT_NAME, connectionInfo.getOutName());
			
		}
	}
}
