package de.openVJJ.plugins;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.BufferedImageValue;
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
public class VectorPainter extends Plugin {

	public VectorPainter() {
		addInput("Vectors", VectorValueList.class);
		addOutput("Image", BufferedImageValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Vectors".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					VectorValueList vectorValueList = (VectorValueList) value;
					Lock lock = vectorValueList.lock();
					paintVectors(vectorValueList.getVectorValues());
					vectorValueList.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}

	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void paintVectors(List<VectorValue> vectorValues){
		BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setBackground(Color.BLACK);
		graphics.setColor(Color.WHITE);
		System.out.println("painting " + vectorValues.size() + " lines");
		for(VectorValue vectorValue : vectorValues){
			graphics.setColor(new Color((int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255)));
			graphics.drawLine(vectorValue.getStart().x, vectorValue.getStart().y, vectorValue.getEnd().x, vectorValue.getEnd().y);
		}
		image.flush();
		graphics.dispose();
		getConnection("Image").transmitValue(new BufferedImageValue(image));
	}

}
