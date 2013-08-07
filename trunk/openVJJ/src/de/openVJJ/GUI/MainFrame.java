package de.openVJJ.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import de.openVJJ.InputComponents;
import de.openVJJ.VJJComponent;
import de.openVJJ.GUI.ShowComponets.ShowComponetsListener;
import de.openVJJ.ImageListener.ImageListener;
import de.openVJJ.imagePublisher.ImagePublisher;

/*
 * Copyright (C) 2012  Jan-Erik Matthies
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
 */

public class MainFrame extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MainFrame() {
		setTitle("open-VJJ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		buildMenue();
		setBounds(0, 0, 800, 600);
		contentPanel = new JPanel();
		this.add(contentPanel);
	}
	
	JPanel contentPanel;
	JMenuBar menuBar;
	private void buildMenue(){
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu componentsMenue = new JMenu("Components");
		menuBar.add(componentsMenue);
		JMenuItem menuItem = new JMenuItem("show Components");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showComponets();
			}
		});
		componentsMenue.add(menuItem);
		
		menuItem = new JMenuItem("add Component");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showAddComponent();
			}
		});
		componentsMenue.add(menuItem);
		
		menuItem = new JMenuItem("remove Component");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showRemoveComponent();
			}
		});
		componentsMenue.add(menuItem);

		menuItem = new JMenuItem("reattach Component");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showReatachComponent();
			}
		});
		componentsMenue.add(menuItem);
	}
	private void refresh(){
		if(contentPanel != null){
			contentPanel.repaint();
		}
		this.validate();
	}
	
	public void showAddComponent(){
		contentPanel.removeAll();
		AddComponent addComponent = new AddComponent();
		contentPanel.add(addComponent);
		refresh();
	}
	
	public void showComponets(){
		contentPanel.removeAll();
		ShowComponets showComponets = new ShowComponets(/*0,Color.lightGray*/);
		showComponets.addShowComponetsListener(new ShowComponets.ShowComponetsListener() {
			
			@Override
			public void componentClicked(VJJComponent vjjComponent) {
				vjjComponent.openConfigPanel();
			}
		});
		contentPanel.add(showComponets);
		//contentPanel.setBackground(Color.lightGray);
		refresh();
	}
	

	public void showRemoveComponent(){
		contentPanel.removeAll();
		ShowComponets showComponets = new ShowComponets(/*0,Color.lightGray*/);
		showComponets.addShowComponetsListener(new ShowComponets.ShowComponetsListener() {
			
			@Override
			public void componentClicked(VJJComponent vjjComponent) {
				InputComponents.remove(vjjComponent);
				showRemoveComponent();
			}
		});
		contentPanel.add(showComponets);
		//contentPanel.setBackground(Color.lightGray);
		refresh();
	}
	
	public void showReatachComponent(){

		contentPanel.removeAll();
		ShowComponets showComponets = new ShowComponets(/*0,Color.lightGray*/);
		showComponets.addShowComponetsListener(new ShowComponets.ShowComponetsListener() {
			
			@Override
			public void componentClicked(VJJComponent vjjComponent) {
				contentPanel.removeAll();
				ShowComponets showComponets = new ShowComponets(0, null, vjjComponent);
				showComponets.addShowComponetsListener(new MyReatachShowComponetsListener(vjjComponent) {
					
					@Override
					public void componentClicked(VJJComponent vjjComponent) {
						if(ImageListener.class.isInstance(passthrou) && ImagePublisher.class.isInstance(vjjComponent)){
							InputComponents.reattach((ImageListener)passthrou, (ImagePublisher) vjjComponent);
						}
						showReatachComponent();
					}
				});
				contentPanel.add(showComponets);
				//contentPanel.setBackground(Color.lightGray);
				refresh();
			}
		});
		contentPanel.add(showComponets);
		//contentPanel.setBackground(Color.lightGray);
		refresh();
	}
	
	private abstract class MyReatachShowComponetsListener implements ShowComponetsListener{

		Object passthrou;
		
		public MyReatachShowComponetsListener(Object passthrou){
			this.passthrou = passthrou;
		}
		
	}

}
