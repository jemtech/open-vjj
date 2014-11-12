package de.openVJJ.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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
import de.openVJJ.values.PointCloud;
import de.openVJJ.values.PointCloundList;
import de.openVJJ.values.VectorValue;
import de.openVJJ.values.VectorValueList;

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
public class PointsToLineOfBestFit extends Plugin {
	
	public static final String ELEMENT_NAME_PointsToLineOfBestFit_CONFIG = "PointsToLineOfBestFit";

	private int minLength = 3;
	private boolean xDirection = true;
	
	public PointsToLineOfBestFit(){
		addInput("Lines", PointCloundList.class);
		addOutput("Vectors", VectorValueList.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Lines".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					calculate((PointCloundList) value);
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}
	
	/**
	 * for saving configuration 
	 * @param element to save configuration to.
	 */
	public void getConfig(Element element){
		Element myConfigElement = new Element(ELEMENT_NAME_PointsToLineOfBestFit_CONFIG);
		element.addContent(myConfigElement);
		myConfigElement.setAttribute("directionx", String.valueOf(xDirection));
		myConfigElement.setAttribute("minLength", String.valueOf(minLength));
		super.getConfig(element);
	}
	
	/**
	 * for restoring from saved configuration
	 * @param element XML Element
	 */
	public void setConfig(Element element){
		Element myConfigElement = element.getChild(ELEMENT_NAME_PointsToLineOfBestFit_CONFIG);
		if(myConfigElement != null){
			String val = myConfigElement.getAttributeValue("directionx");
			if(val != null){
				xDirection =  Boolean.parseBoolean(val);
			}
			val = myConfigElement.getAttributeValue("minLength");
			if(val != null){
				minLength =  Integer.parseInt(val);
			}
		}
		super.setConfig(element);
	}

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
		limitTextField.setValue(minLength);
		limitTextField.setColumns(3);
		limitTextField.addPropertyChangeListener("value",  new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				minLength = ((Number) ((JFormattedTextField)evt.getSource()).getValue()).intValue();
			}
		});
		
		gridBagConstraints.gridx = 1;
		configPanel.add(limitTextField, gridBagConstraints);
		
		return configPanel;
	}
	
	private void calculate(PointCloundList value){
		List<VectorValue> vectorValues = new ArrayList<VectorValue>();
		for(PointCloud pointCloud : value.getValue()){
			List<Point> points = pointCloud.getValue();
			if(points.size() < minLength){
				continue;
			}
			double sumX=0;
			double sumY=0;
			double sumSQR=0;
			double sumXY=0;
			int min=Integer.MAX_VALUE;
			int max=0;
			//int sumYY=0;
			if(xDirection){
				for(Point point : points){
					if(min > point.x){
						min = point.x;
					}
					if(max < point.x){
						max = point.x;
					}
					sumX += point.x;
					sumY += point.y;
					sumSQR += point.x * point.x;
					sumXY += point.x * point.y;
				//	sumYY += point.y * point.y;
				}
			}else{
				for(Point point : points){
					if(min > point.y){
						min = point.y;
					}
					if(max < point.y){
						max = point.y;
					}
					sumX += point.x;
					sumY += point.y;
					sumSQR += point.y * point.y;
					sumXY += point.x * point.y;
				}
			}

			VectorValue vectorValue;
			if(xDirection){
				double divisor = points.size() * sumSQR - sumX * sumX;
				double m = (points.size() * sumXY - sumX * sumY) / divisor ;
				double b = (sumY * sumSQR - sumX * sumXY) / divisor;
				int yMin = Math.round((float) (m * min + b));
				int yMax = Math.round((float) (m * max + b));
				Point minPoint = new Point(min, yMin);
				Point maxPoint = new Point(max, yMax);
				vectorValue = new VectorValue(minPoint, maxPoint);
			}else{
				double divisor = points.size() * sumSQR - sumY * sumY;
				double m = (points.size() * sumXY - sumX * sumY) / divisor ;
				double b = (sumX * sumSQR - sumY * sumXY) / divisor;
				int xMin = Math.round((float) (m * min + b));
				int xMax = Math.round((float) (m * max + b));
				Point minPoint = new Point(xMin, min);
				Point maxPoint = new Point(xMax, max);
				vectorValue = new VectorValue(minPoint, maxPoint);
			}
			vectorValues.add(vectorValue);
		}
		getConnection("Vectors").transmitValue(new VectorValueList(vectorValues));
	}

}
