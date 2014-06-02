/**
 * 
 */
package de.openVJJ.basic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

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
public class ProjectConf {
	public static final String ROOT_ELEMET_NAME = "Open-VJJ2";
	public static final String ELEMET_NAME_BASE_MODULE = "baseModule";
	
	private static Module baseModule;
	private static JFrame projectFrame;
	
	/**
	 * this class should only be used static
	 */
	private ProjectConf(){
		
	}
	
	/**
	 * Use this to start a blank new project
	 */
	public static void init(){
		baseModule = new Module();
		baseModule.setBaseModule(true);
		openFrame();
	}
	
	private static void openFrame(){
		projectFrame = new JFrame("Oper VJJ");
		projectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildMenue(projectFrame);
		JPanel moduleJPanel = baseModule.getConfigPannel();
		projectFrame.add(moduleJPanel);
		projectFrame.setVisible(true);
		projectFrame.pack();
	}
	
	private static JMenuBar menuBar;
	private static void buildMenue(JFrame frame){
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu projectMenue = new JMenu("Project");
		menuBar.add(projectMenue);
		
		JMenuItem menuItem = new JMenuItem("Save");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveProject();
			}
		});
		projectMenue.add(menuItem);
		
		menuItem = new JMenuItem("Load");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadProject();
			}
		});
		projectMenue.add(menuItem);
		
	}
	
	private static void saveProject(){
		String path = fileChooser(true);
		if(path == null){
			System.err.println("no File to save selected");
			return;
		}
		save(path);
	}
	
	private static void loadProject(){
		String path = fileChooser(false);
		if(path == null){
			System.err.println("no File to load selected");
			return;
		}
		load(path);
	}
	

	private static String fileChooser(boolean save){
		JFileChooser chooser = new JFileChooser();
		if(save){
			chooser.showSaveDialog(null);
		}else{
			chooser.showOpenDialog(null);
		}
		File selectedFile = chooser.getSelectedFile();
		if(selectedFile == null){
			return null;
		}
		String path = selectedFile.getPath();
		if(path == null){
			return null;
		}
		return path;
	}
	
	/**
	 * Saves the Project
	 * @param fileName the file the Project is saved to
	 */
	public static void save(String fileName){
		Element rootElement = new Element(ROOT_ELEMET_NAME);
//		Element gpuElement = new Element(GPU_ELEMENT_NAME);
//		gpuElement.setAttribute("activ", String.valueOf(useGPU));
//		rootElement.addContent(gpuElement);
		Element baseModuleElement = new Element(ELEMET_NAME_BASE_MODULE);
		rootElement.addContent(baseModuleElement);
		baseModule.getConfig(baseModuleElement);
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			new XMLOutputter().output(new Document(rootElement), fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void load(String fileName){

		try {
			Document document = new SAXBuilder().build( fileName );
			Element rootElement = document.getRootElement();
			if(rootElement.getName() != ROOT_ELEMET_NAME){
				System.err.println("is not an Open-VJJ 2 Project");
				return;
			}
//			Element gpuElement = rootElement.getChild(GPU_ELEMENT_NAME);
//			if(gpuElement != null){
//				Attribute gpuActiv = gpuElement.getAttribute(GPU_ACTIV_ATTRIBUTE_NAME);
//				if(gpuActiv != null){
//					useGPU = gpuActiv.getBooleanValue();
//				}
//			}
			Element baseModuleElement = rootElement.getChild(ELEMET_NAME_BASE_MODULE);
			baseModule = new Module();
			baseModule.setBaseModule(true);
			baseModule.setConfig(baseModuleElement);
			if(projectFrame != null){
				projectFrame.dispose();
			}
			openFrame();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
