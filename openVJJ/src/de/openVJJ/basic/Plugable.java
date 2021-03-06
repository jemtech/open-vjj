package de.openVJJ.basic;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jdom2.Element;

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
public abstract class Plugable {
	/**
	 * in- and output description
	 */
	private Map<String, Class<? extends Value>> inputTyps = new HashMap<String, Class<? extends Value>>(); 
	private Map<String, Class<? extends Value>> outputTyps = new HashMap<String, Class<? extends Value>>();
	
	/**
	 * in- and output connections
	 */
	private Map<String, Connection.ConnectionListener> inputListenr = new HashMap<String, Connection.ConnectionListener>();
	private Map<String, Connection> outputConnections = new HashMap<String, Connection>();
	
	/**
	 * GUI config
	 */
	private Rectangle guiPosition = new Rectangle(0, 0, 350, 100);
	
	/**
	 * getter for the GUI position
	 * @return the GUI position as {@link Rectangle}
	 */
	public Rectangle getGuiPosition() {
		return guiPosition;
	}

	/**
	 * setter for the GUI position
	 * @param guiPosition {@link Rectangle} to set as GUI Position
	 */
	public void setGuiPosition(Rectangle guiPosition) {
		this.guiPosition = guiPosition;
	}

	/**
	 * This Method manages the input connections 
	 * @param name Name of the input
	 * @param connection this {@link Connection} will transmit {@link Value}s to the specified input
	 * @return <code>true</code>if everything went well
	 */
	public synchronized boolean setInput(String name, Connection connection){
		Class<? extends Value> inputTypClass = inputTyps.get(name);
		if(inputTypClass == null){
			System.err.println("Input not found: " + name);
			return false;
		}
		if(! connection.classMatch(inputTypClass)){
			System.err.println("valueClass does not equals matchingClass");
			return false;
		}
		releaseInput(name);
		Connection.ConnectionListener listener = createConnectionListener(name, connection);
		if(listener == null){
			System.err.println("connection listener is null");
			return false;
		}
		inputListenr.put(name, listener);
		return true;
	}
	
	public Connection.ConnectionListener getListener(String inputName){
		return inputListenr.get(inputName);
	}
	
	public void releaseInput(String name){
		Connection.ConnectionListener oldListener = inputListenr.get(name);
		if(oldListener != null){
			oldListener.release();
		}
	}
	
	/**
	 * This Method is needed to create your listeners to receive values through your inputs. 
	 * @param inpuName name of the input values will be received
	 * @param connection this is the {@link Connection} witch will submit the {@link Value}s
	 * @return your {@link ConnectionListener} receiving {@link Value}s with
	 */
	protected abstract Connection.ConnectionListener createConnectionListener(String inpuName, Connection connection);
	
	/**
	 * 
	 * @param name of the output
	 * @return the matching {@link Connection} otherwise <code>null</code> 
	 */
	public synchronized Connection getConnection(String name){
		Connection connection = outputConnections.get(name);
		if(connection == null){
			Class<? extends Value> valueClass = outputTyps.get(name);
			if(valueClass == null){
				System.err.println("No output with name \"" + name + "\" found");
				return null;
			}
			connection = new Connection(valueClass);
			outputConnections.put(name, connection);
		}
		return connection;
	}
	
	/**
	 * 
	 * @return the input {@link Map} with name and {@link Value} type
	 */
	public Map<String, Class<? extends Value>> getInputs(){
		return inputTyps;
	}
	

	/**
	 * 
	 * @return the output {@link Map} with name and {@link Value} type
	 */
	public Map<String, Class<? extends Value>> getOutputs(){
		return outputTyps;
	}
	
	/**
	 * adds an input with specified name and {@link Value} type
	 * @param name of the input to add. The name must be unique.
	 * @param valueClass {@link Value} type of the input to add
	 * @return <code>true</code> if everything went well. <code>false</code> if name already exists.
	 */
	protected boolean addInput(String name, Class<? extends Value> valueClass){
		if(inputTyps.containsKey(name)){
			return false;
		}
		inputTyps.put(name, valueClass);
		return true;
	}
	
	/**
	 * adds an output with specified name and {@link Value} type
	 * @param name of the input to add. The name must be unique.
	 * @param valueClass {@link Value} type of the output to add
	 * @return <code>true</code> if everything went well. <code>false</code> if name already exists.
	 */
	protected boolean addOutput(String name, Class<? extends Value> valueClass){
		if(outputTyps.containsKey(name)){
			return false;
		}
		outputTyps.put(name, valueClass);
		return true;
	}
	
	/**
	 * call this to shutdown a {@link Plugable} all {@link ConnectionListener}s will be released and all {@link Connection}s shutdown
	 */
	protected void shutdown(){
		for(String inListenerName : inputListenr.keySet()){
			ConnectionListener inListener = inputListenr.get(inListenerName);
			inListener.release();
		}
		inputListenr.clear();
		for(String connectionName : outputConnections.keySet()){
			Connection outConnection = outputConnections.get(connectionName);
			outConnection.shutdown();
		}
	}
	
	/**
	 * 
	 * @return the configuration {@link JPanel} for the {@link Plugable}
	 */
	public abstract JPanel getConfigPannel();
	
	public String getName(){
		return this.getClass().getSimpleName();
	}
	

	public static final String ELEMENT_NAME_GUI_POSITION = "guiPosition";
	/**
	 * for saving configuration 
	 * @param element to save configuration to.
	 */
	public void getConfig(Element element){
		Element guiPositionElement = new Element(ELEMENT_NAME_GUI_POSITION);
		element.addContent(guiPositionElement);
		Rectangle guiPosition = getGuiPosition();
		guiPositionElement.setAttribute("x", String.valueOf(guiPosition.x));
		guiPositionElement.setAttribute("y", String.valueOf(guiPosition.y));
		guiPositionElement.setAttribute("height", String.valueOf(guiPosition.height));
		guiPositionElement.setAttribute("width", String.valueOf(guiPosition.width));
	}
	/**
	 * for restoring from saved configuration
	 * @param element XML Element
	 */
	public void setConfig(Element element){
		Element guiPositionElement = element.getChild(ELEMENT_NAME_GUI_POSITION);
		if(guiPositionElement != null){
			Rectangle guiPosition = getGuiPosition();
			String val = guiPositionElement.getAttributeValue("x");
			if(val != null){
				guiPosition.x =  Integer.parseInt(val);
			}
			val = guiPositionElement.getAttributeValue("y");
			if(val != null){
				guiPosition.y =  Integer.parseInt(val);
			}
			val = guiPositionElement.getAttributeValue("height");
			if(val != null){
				guiPosition.height =  Integer.parseInt(val);
			}
			val = guiPositionElement.getAttributeValue("width");
			if(val != null){
				guiPosition.width =  Integer.parseInt(val);
			}
		}
	}
}
