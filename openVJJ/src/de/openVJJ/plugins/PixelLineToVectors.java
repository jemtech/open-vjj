/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom2.Element;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.PointCloud;
import de.openVJJ.values.PointCloundList;
import de.openVJJ.values.VectorValue;
import de.openVJJ.values.VectorValueList;
import de.openVJJ.values.VectorValueListList;

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
public class PixelLineToVectors extends Plugin {

	public static final String ELEMENT_NAME_PixelLineToVectors_CONFIG = "PixelLineToVectors";
	private boolean horizontal = true;
	/**
	 * 
	 */
	public PixelLineToVectors() {
		addInput("Lines", PointCloundList.class);
		addOutput("Vectors", VectorValueListList.class);
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
				horizontal = ((JRadioButton) e.getSource()).isSelected();
				
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
		
		xButton.setSelected(horizontal);
		yButton.setSelected(!horizontal);
		return configPanel;
	}
	
	/**
	 * for saving configuration 
	 * @param element to save configuration to.
	 */
	public void getConfig(Element element){
		Element myConfigElement = new Element(ELEMENT_NAME_PixelLineToVectors_CONFIG);
		element.addContent(myConfigElement);
		myConfigElement.setAttribute("horizontal", String.valueOf(horizontal));
		super.getConfig(element);
	}
	
	/**
	 * for restoring from saved configuration
	 * @param element XML Element
	 */
	public void setConfig(Element element){
		Element myConfigElement = element.getChild(ELEMENT_NAME_PixelLineToVectors_CONFIG);
		if(myConfigElement != null){
			String val = myConfigElement.getAttributeValue("horizontal");
			if(val != null){
				horizontal =  Boolean.parseBoolean(val);
			}
		}
		super.setConfig(element);
	}
	
	private void calculate(PointCloundList pointCloundList){
		List<VectorValueList> vectorValueLists = new ArrayList<VectorValueList>();
		for(PointCloud pointCloud : pointCloundList.getValue()){
			List<VectorValue> vectorValues = new ArrayList<VectorValue>();
			List<Point> points = pointCloud.getValue();
			List<Point> copy = new ArrayList<Point>();
			for(Point point : points){
				copy.add(point);
			}
			sort(copy);
			while(copy.size()>0){
				findVector(copy, vectorValues);
			}
			vectorValueLists.add( new VectorValueList(vectorValues));
		}
		VectorValueListList valueListList = new VectorValueListList(vectorValueLists);
		getConnection("Vectors").transmitValue(valueListList);
	}
	
	private void findVector(List<Point> points, List<VectorValue> vectorList){
		Point smalest = points.get(0);
		points.remove(0);
		Point lastNaibor = findLastNaibor(smalest, points);
		if(lastNaibor == null){
			vectorList.add(new VectorValue(smalest, smalest));
			return;
		}else{
			vectorList.add(new VectorValue(smalest, lastNaibor));
		}
		while(lastNaibor != null){
			Point start = lastNaibor;
			lastNaibor = findLastNaibor(lastNaibor, points);
			if(lastNaibor != null){
				vectorList.add(new VectorValue(start, lastNaibor));
			}
		}
		
	}
	
	
	private Point findLastNaibor(Point start, List<Point> points){
		Point naibor = null;
		int direct = Integer.MAX_VALUE; 
		for(Point point : points){
			if(horizontal){
				if(start.x == point.x-1){
					if(start.y == point.y-1){
						direct = -1;
						naibor = point;
					}else if (start.y == point.y) {
						direct = 0;
						naibor = point;
					}else if (start.y == point.y+1) {
						direct = 1;
						naibor = point;
					}
				}else if(start.x < point.x-1){
					break;
				}
			}else{
				if(start.y == point.y-1){
					if(start.x == point.x-1){
						direct = -1;
						naibor = point;
					}else if (start.x == point.x) {
						direct = 0;
						naibor = point;
					}else if (start.x == point.x+1) {
						direct = 1;
						naibor = point;
					}
				}else if(start.y < point.y-1){
					break;
				}
			}
		}
		if(naibor == null){
			return null;
		}
		points.remove(naibor);
		Point lastNaibor = findLastNaibor(naibor, points, direct);
		if(lastNaibor == null){
			return naibor;
		}else{
			return lastNaibor;
		}
	}
	
	
	
	private Point findLastNaibor(Point start, List<Point> points, int direct){
		Point naibor = null;
		for(Point point : points){
			if(horizontal){
				if(start.x == point.x-1){
					if(start.y == point.y+direct){
						naibor = point;
					}
				}else if(start.x < point.x-1){
					break;
				}
			}else{
				if(start.y == point.y-1){
					if(start.x == point.x+direct){
						naibor = point;
					}
				}else if(start.y < point.y-1){
					break;
				}
			}
		}
		if(naibor == null){
			return null;
		}
		points.remove(naibor);
		Point next = findLastNaibor(naibor, points, direct);
		if(next == null){
			return naibor;
		}else{
			return next;
		}
	}
	
	 public void sort(List<Point> points) {
	      qSort(points, 0, points.size()-1);
	   }
	    
	   public void qSort(List<Point> points, int links, int rechts) {
	      if (links < rechts) {
	         int i = partition(points,links,rechts);
	         qSort(points,links,i-1);
	         qSort(points,i+1,rechts);
	      }
	   }
	    
	   public int partition(List<Point> points, int links, int rechts) {
		  Point pivot, help;
		  int i, j;
	      pivot = points.get(rechts);               
	      i     = links;
	      j     = rechts-1;
	      while(i<=j) {
	         if (isBigger(points.get(i), pivot)) {     
	            // tausche x[i] und x[j]
	            help = points.get(i);
	            points.set(i, points.get(j));
	            points.set(j, help);        
	            j--;
	         } else i++;            
	      }
	      // tausche x[i] und x[rechts]
          help = points.get(i);
          points.set(i, points.get(rechts));
          points.set(rechts, help);
	        
	      return i;
	   }
	   
	   private boolean isBigger(Point a, Point b){
		   if(horizontal){
			   if(a.x > b.x){
				   return true;
			   }else if(a.x == b.x){
				   if(a.y > b.y){
					   return true;
				   }
			   }
		   }else{
			   if(a.y > b.y){
				   return true;
			   }else if(a.y == b.y){
				   if(a.x > b.x){
					   return true;
				   }
			   }
		   }
		   return false;
	   }

}
