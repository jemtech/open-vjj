/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom2.Element;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.Integer2DArrayValue;
import de.openVJJ.values.PointCloud;
import de.openVJJ.values.PointCloundList;

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
public class LineFromSorbel2DIntArray extends Plugin {
	
	public static final String ELEMENT_NAME_LineFromSorbel2DIntArray_CONFIG = "LineFromSorbel2DIntArray";
	
	/**
	 * 
	 */
	public LineFromSorbel2DIntArray() {
		addInput("Sorbel", Integer2DArrayValue.class);
		addOutput("Lines", PointCloundList.class);
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Sorbel".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Integer2DArrayValue sorbel = (Integer2DArrayValue) value;
					Lock lock = sorbel.lock();
					calculate(sorbel.getIngegerArray());
					sorbel.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		JPanel configPanel = new JPanel();
		
		configPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		

		JRadioButton xButton = new JRadioButton("X");
		xButton.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				xDirection = ((JRadioButton) e.getSource()).isSelected();
				
			}
		});
		
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		configPanel.add(xButton, gridBagConstraints);
		
		JRadioButton yButton = new JRadioButton("y");
		
		gridBagConstraints.gridy = 1;
		configPanel.add(yButton, gridBagConstraints);
		
		ButtonGroup group = new ButtonGroup();
		group.add(yButton);
		group.add(xButton);
		
		xButton.setSelected(xDirection);
		yButton.setSelected(!xDirection);
		
		JLabel limitLabel = new JLabel("max Lines");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		configPanel.add(limitLabel, gridBagConstraints);
		
		JFormattedTextField limitTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
		limitTextField.setValue(lineLimit);
		limitTextField.setColumns(3);
		limitTextField.addPropertyChangeListener("value",  new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				lineLimit = ((Number) ((JFormattedTextField)evt.getSource()).getValue()).intValue();
			}
		});
		
		gridBagConstraints.gridx = 1;
		configPanel.add(limitTextField, gridBagConstraints);
		
		JLabel histeryLabel = new JLabel("min Val");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		configPanel.add(histeryLabel, gridBagConstraints);

		JFormattedTextField histeryTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
		histeryTextField.setValue(histery);
		histeryTextField.setColumns(3);
		histeryTextField.addPropertyChangeListener("value",  new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				histery = ((Number) ((JFormattedTextField)evt.getSource()).getValue()).intValue();
			}
		});
		gridBagConstraints.gridx = 1;
		configPanel.add(histeryTextField, gridBagConstraints);
		
		JLabel minLengthLabel = new JLabel("min length");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		configPanel.add(minLengthLabel, gridBagConstraints);

		JFormattedTextField minLengthTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
		minLengthTextField.setValue(minLength);
		minLengthTextField.setColumns(3);
		minLengthTextField.addPropertyChangeListener("value",  new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				minLength = ((Number) ((JFormattedTextField)evt.getSource()).getValue()).intValue();
			}
		});
		gridBagConstraints.gridx = 1;
		configPanel.add(minLengthTextField, gridBagConstraints);

		return configPanel;
	}
	
	/**
	 * for saving configuration 
	 * @param element to save configuration to.
	 */
	public void getConfig(Element element){
		Element myConfigElement = new Element(ELEMENT_NAME_LineFromSorbel2DIntArray_CONFIG);
		element.addContent(myConfigElement);
		myConfigElement.setAttribute("directionx", String.valueOf(xDirection));
		super.getConfig(element);
	}
	
	/**
	 * for restoring from saved configuration
	 * @param element XML Element
	 */
	public void setConfig(Element element){
		Element myConfigElement = element.getChild(ELEMENT_NAME_LineFromSorbel2DIntArray_CONFIG);
		if(myConfigElement != null){
			String val = myConfigElement.getAttributeValue("directionx");
			if(val != null){
				xDirection =  Boolean.parseBoolean(val);
			}
		}
		super.setConfig(element);
	}
	
	private boolean xDirection = true;
	private int lineLimit = 500;
	private int histery = 30;
	private boolean copyValue = true;
	private int minLength = 0;
	
	private void calculate(int[][] sorbel){
		if(copyValue){
			sorbel = copySorbel(sorbel);
		}
		PointCloundList lines;
		if(xDirection){
			lines = processX(sorbel);
		}else{
			lines = processY(sorbel);
		}
		getConnection("Lines").transmitValue(lines);
	}
	
	private int[][] copySorbel(int [][] toCopy){
		int [][] myInt = new int[toCopy.length][];
		for(int i = 0; i < toCopy.length; i++)
		{
		  int[] aMatrix = toCopy[i];
		  int   aLength = aMatrix.length;
		  myInt[i] = new int[aLength];
		  System.arraycopy(aMatrix, 0, myInt[i], 0, aLength);
		}
		return myInt;
	}
	
	
	private PointCloundList processX(int[][] sorbel){
		
		int xMax = sorbel.length -1;
		int yMax = sorbel[0].length -1;
		ArrayList<PointCloud> lines = new ArrayList<PointCloud>();
		for(int cykle = 0; cykle < lineLimit ; cykle++){
			Point point = getStrongest(sorbel);
			if(point == null){
				break;
			}
			
			deactivatePoint(sorbel, point);
			ArrayList<Point> points = new ArrayList<Point>();
			points.add(point);
			PointCloud line = new PointCloud(points);
			
			combinedLineU(point, sorbel, line, 0, 0, yMax);
			combinedLineD(point, sorbel, line, xMax, 0, yMax);

			if(line.getValue().size() > minLength){
				lines.add(line);
			}else{
				cykle--;
			}
			
		}
		return new PointCloundList(lines);
	}
	

	
	private PointCloundList processY(int[][] sorbel){
		
		int xMax = sorbel.length -1;
		int yMax = sorbel[0].length -1;
		ArrayList<PointCloud> lines = new ArrayList<PointCloud>();
		for(int cykle = 0; cykle < lineLimit ; cykle++){
			Point point = getStrongest(sorbel);
			if(point == null){
				break;
			}
			
			deactivatePoint(sorbel, point);
			ArrayList<Point> points = new ArrayList<Point>();
			points.add(point);
			PointCloud line = new PointCloud(points);
			
			combinedLineL(point, sorbel, line, 0, 0, xMax);
			combinedLineR(point, sorbel, line, yMax, 0, xMax);
			
			if(line.getValue().size() > minLength){
				lines.add(line);
			}else{
				cykle--;
			}
			
		}
		return new PointCloundList(lines);
	}
	
	private final static int USED_POINT = -1;
	private void deactivatePoint(int[][] valueMatrix, Point point){
		valueMatrix[point.x][point.y] = USED_POINT;
	}
	
	private Point getStrongest(int[][] sorbelresult){
		Point point = new Point();
		int ValMax = -1;
		
		int xMax = sorbelresult.length;
		int yMax = sorbelresult[0].length;
		for(int x = 0; x < xMax; x++){
			for(int y = 0; y < yMax; y++){
				int res = sorbelresult[x][y];
				if(res > ValMax){
					ValMax = res;
					point.x = x;
					point.y = y;
				}
			}
		}
		if(ValMax<histery){
			return null;
		}
		return point;
	}
	
	private void combinedLineU(Point position, int[][] sorbel, PointCloud line, int xMin, int yMin, int yMax){
		if(position.x <= xMin){
			return;
		}
		Point next = new Point();
		int x = position.x - 1;
		int y = position.y - 1;
		int val = USED_POINT;
		if( y >= yMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		y++;
		if(y >= yMin){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		y++;
		if(y <= yMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.getValue().add(next);
			thinlineL(next, sorbel, yMin, val);
			thinlineR(next, sorbel, yMax, val);
			combinedLineU(next, sorbel, line, xMin, yMin, yMax);
		}
	}
	
	private void thinlineL(Point position, int[][] sorbel, int yMin, int last){
		Point watch = new Point();
		watch.x = position.x;
		watch.y = position.y-1;
		if(watch.y <= yMin){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			if(last<histery){
				return;
			}
			thinlineL(watch, sorbel, yMin, last);
		}
	}
	

	private void thinlineR(Point position, int[][] sorbel, int yMax, int last){
		Point watch = new Point();
		watch.x = position.x;
		watch.y = position.y+1;
		if(watch.y >= yMax){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			if(last<histery){
				return;
			}
			thinlineR(watch, sorbel, yMax, last);
		}
	}
	
	private void combinedLineD(Point position, int[][] sorbel, PointCloud line, int xMax, int yMin, int yMax){
		if(position.x >= xMax){
			return;
		}
		Point next = new Point();
		int x = position.x + 1;
		int y = position.y - 1;
		int val = USED_POINT;
		if( y >= yMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		y++;
		if(sorbel[x][y] > val){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		y++;
		if(y <= yMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.getValue().add(next);
			thinlineL(next, sorbel, yMin, val);
			thinlineR(next, sorbel, yMax, val);
			combinedLineD(next, sorbel, line, xMax, yMin, yMax);
		}
	}

	private void combinedLineL(Point position, int[][] sorbel, PointCloud line, int yMin, int xMin, int xMax){
		if(position.y <= yMin){
			return;
		}
		Point next = new Point();
		int x = position.x - 1;
		int y = position.y - 1;
		int val = USED_POINT;
		if( x >= xMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(sorbel[x][y] > val){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(x <= xMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.getValue().add(next);
			thinlineU(next, sorbel, xMin, val);
			thinlineD(next, sorbel, xMax, val);
			combinedLineL(next, sorbel, line, yMin, xMin, xMax);
		}
		
	}
	
	private void thinlineU(Point position, int[][] sorbel, int xMin, int last){
		Point watch = new Point();
		watch.x = position.x-1;
		watch.y = position.y;
		if(watch.x <= xMin){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			if(last<histery){
				return;
			}
			thinlineU(watch, sorbel, xMin, last);
		}
	}
	

	private void thinlineD(Point position, int[][] sorbel, int xMax, int last){
		Point watch = new Point();
		watch.x = position.x+1;
		watch.y = position.y;
		if(watch.x >= xMax){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			if(last<histery){
				return;
			}
			thinlineD(watch, sorbel, xMax, last);
		}
	}

	private void combinedLineR(Point position, int[][] sorbel, PointCloud line, int yMax, int xMin, int xMax){
		if(position.y >= yMax){
			return;
		}

		Point next = new Point();
		int x = position.x - 1;
		int y = position.y + 1;
		int val = USED_POINT;
		if( x >= xMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(sorbel[x][y] > val){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(x <= xMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.getValue().add(next);
			thinlineU(next, sorbel, xMin, val);
			thinlineD(next, sorbel, xMax, val);
			combinedLineR(next, sorbel, line, yMax, xMin, xMax);
		}
		
	}

}
