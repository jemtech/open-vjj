/**
 * 
 */
package de.openVJJ.GUI;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.openVJJ.basic.Module;
import de.openVJJ.plugins.ArtNetDMXPaketToArtNetPaket;
import de.openVJJ.plugins.ArtNetPacketToUnicastArtNetPacket;
import de.openVJJ.plugins.ArtnetController;
import de.openVJJ.plugins.BufferdImageToIntegerArray;
import de.openVJJ.plugins.DMXPaketToArtNetDMXPaket;
import de.openVJJ.plugins.DebugArtNetPaket;
import de.openVJJ.plugins.DebugInt;
import de.openVJJ.plugins.DebugIntArray;
import de.openVJJ.plugins.DisplayFrameBI;
import de.openVJJ.plugins.InetAddressInput;
import de.openVJJ.plugins.IntArrayToRGBIntArray;
import de.openVJJ.plugins.IntInput;
import de.openVJJ.plugins.IntOszilator;
import de.openVJJ.plugins.IntegerArrayToBufferdImage;
import de.openVJJ.plugins.LinearRGBCorrectionIntegerArray;
import de.openVJJ.plugins.RGBIntArrayToDMXPaket;
import de.openVJJ.plugins.RGBIntColor;
import de.openVJJ.plugins.SinIntArrayGen;
import de.openVJJ.plugins.StringInput;
import de.openVJJ.plugins.XuggleVideoIn;

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
public class SelectPlugable extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5167693321984938892L;
	
	public static final Class<?>[] plugableClasses = new Class<?>[]{
		Module.class,
		XuggleVideoIn.class,
		DisplayFrameBI.class,
		StringInput.class,
		BufferdImageToIntegerArray.class,
		LinearRGBCorrectionIntegerArray.class,
		IntegerArrayToBufferdImage.class,
		ArtnetController.class,
		ArtNetPacketToUnicastArtNetPacket.class,
		InetAddressInput.class,
		DMXPaketToArtNetDMXPaket.class,
		ArtNetDMXPaketToArtNetPaket.class,
		IntInput.class,
		SinIntArrayGen.class,
		IntArrayToRGBIntArray.class,
		RGBIntColor.class,
		IntOszilator.class,
		RGBIntArrayToDMXPaket.class,
		DebugArtNetPaket.class,
		DebugInt.class,
		DebugIntArray.class};

	//public static List<Class<? extends Plugable>> plugableClasses = new ArrayList<Class<? extends Plugable>>();
	
	public SelectPlugable(){
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		createSeletabels();
	}
	JList<String> plugableClassesList;
	private void createSeletabels(){
//		for(Class<?> plugableClass : plugableClasses){
//			JButton selectButton = new JButton(plugableClass.getSimpleName());
//			selectButton.addActionListener(new SelectButtonActionListener(plugableClass));
//			add(selectButton);
//		}
		
		String[] plugableClassNames = new String[plugableClasses.length];
		for(int i =0; i < plugableClasses.length; i++){
			plugableClassNames[i] = plugableClasses[i].getSimpleName();
		}
		plugableClassesList = new JList<String>(plugableClassNames);
		plugableClassesList.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if('\n' == e.getKeyChar()){
					selected(plugableClasses[plugableClassesList.getSelectedIndex()]);
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		plugableClassesList.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					selected(plugableClasses[plugableClassesList.getSelectedIndex()]);
				  }
			}
		});
		
		JScrollPane listScroller = new JScrollPane(plugableClassesList);
		listScroller.setPreferredSize(new Dimension(350, 200));
		add(listScroller);
	}
	
	private JFrame frame;
	public void openAsFrame(){
		frame = new JFrame("Select Plugable");
		frame.add(this);
		frame.setVisible(true);
		frame.pack();
		Point location = MouseInfo.getPointerInfo().getLocation(); 
		int x = (int) location.getX();
		int y = (int) location.getY();
		frame.setLocation(x, y);
	}
	
	protected void selected(Class<?> plugableClass) {
		if(frame != null){
			frame.dispose();
			frame = null;
		}
		for(SelectPlugableListener listener : listeners){
			listener.plugableSelected(plugableClass);
		}
	}
	
	private List<SelectPlugableListener> listeners = new ArrayList<SelectPlugable.SelectPlugableListener>();
	public void addListener(SelectPlugableListener listener){
		listeners.add(listener);
	}
	
	private class SelectButtonActionListener implements ActionListener {
		
		Class<?> plugableClass;
		
		public SelectButtonActionListener(Class<?> plugableClass){
			this.plugableClass = plugableClass;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			selected(plugableClass);
		}
	}
	
	public interface SelectPlugableListener{
		public void plugableSelected(Class<?> plugableClass);
	}
}
