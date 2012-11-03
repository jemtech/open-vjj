package de.openVJJ.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.openVJJ.InputComponents;

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
	
	public MainFrame() {
		setTitle("open-VJJ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setBounds(0, 0, 800, 600);
		buildMenue();
	}
	

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
				new ShowComponets();
			}
		});
		componentsMenue.add(menuItem);
	}

}
