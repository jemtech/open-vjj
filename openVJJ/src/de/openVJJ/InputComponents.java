package de.openVJJ;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;

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


public class InputComponents {
	public static boolean useGPU = true;
	
	static List<ImagePublisher> imagePublishers;
	
	public static void addComponent(ImagePublisher imagePublisher){
		if(imagePublishers == null){
			imagePublishers = new ArrayList<ImagePublisher>();
		}
		imagePublishers.add(imagePublisher);
	}
	
	public static void addComponent(ImageListener imageListener, ImagePublisher toAddTo){
		toAddTo.addListener(imageListener);
	}
	
	public static List<ImagePublisher> getImagePublisher(){
		return imagePublishers;
	}
	
	public static void reattach(ImageListener imageListener, ImagePublisher toAddTo){
		ImagePublisher oldImagePublisher = getPrevius(imageListener);
		System.out.println("Old publisher" + oldImagePublisher);
		oldImagePublisher.removeListener(imageListener);
		toAddTo.addListener(imageListener);
	}
	
	public static void remove(VJJComponent vjjComponent){
		if(imagePublishers == null || vjjComponent == null){
			return;
		}
		if(ImageListener.class.isInstance(vjjComponent)){
			System.out.println("To remove is a Listener");
			ImageListener imageListener = (ImageListener) vjjComponent;
			ImagePublisher imagePublisher = getPrevius(imageListener);
			if(imagePublisher != null){
				System.out.println("Found publisher " + imagePublisher + " remove listener");
				imagePublisher.removeListener(imageListener);
			}
		}else{
			synchronized (imagePublishers){
				System.out.println("To remove is a Publisher");
				if(imagePublishers.remove(vjjComponent)){
					System.out.println("Found publisher remove from list");
				}else{
					System.out.println("Could not remove publisher from list");
				}
			}
		}
		vjjComponent.remove();
	}
	
	public static ImagePublisher getPrevius(ImageListener imageListener){
		synchronized (imagePublishers){
			for(ImagePublisher imagePublisher : imagePublishers){
				ImagePublisher publisher = recursiveFindPreviusComponet(imagePublisher, imageListener);
				if(publisher != null){
					return publisher;
				}
			}
		}
		return null;
	}
	
	private static ImagePublisher recursiveFindPreviusComponet(ImagePublisher toSearchInimagePublisher, VJJComponent toFindePrevVjjComponent){
		List<ImageListener> listenerList = toSearchInimagePublisher.getImageListenerList();
		if(listenerList == null){
			return null;
		}
		for(ImageListener imageListener : listenerList ){
			if(imageListener == toFindePrevVjjComponent){
				System.out.println("found component " + imageListener +  "return its Publisher " + toSearchInimagePublisher);
				return toSearchInimagePublisher;
			}
			if(ImagePublisher.class.isInstance(imageListener)){
				ImagePublisher found =  recursiveFindPreviusComponet((ImagePublisher)imageListener, toFindePrevVjjComponent);
				if(found == null){
					continue;
				}else{
					return found;
				}
			}
		}
		return null;
	}
	
	public static final String ROOT_ELEMET_NAME = "VJJProject";
	public static final String COMPONENTS_ELEMENT_NAME = "Components";
	public static final String COMPONENT_ELEMENT_NAME = "Component";
	public static final String COMPONENT_SETUP_ELEMENT_NAME = "ComponentSetup";
	public static final String GPU_ELEMENT_NAME = "GPU";
	
	public static final String CLASS_NAME_ATTRIBUTE_NAME = "ClassName";
	public static final String GPU_ACTIV_ATTRIBUTE_NAME = "activ";
	
	public static void save(String fileName){
		Element rootElement = new Element(ROOT_ELEMET_NAME);
		Element gpuElement = new Element(GPU_ELEMENT_NAME);
		gpuElement.setAttribute("activ", String.valueOf(useGPU));
		rootElement.addContent(gpuElement);
		Element compnetsElement = new Element(COMPONENTS_ELEMENT_NAME);
		for(ImagePublisher imagePublisher : imagePublishers){
			compnetsElement.addContent(componetToXML(imagePublisher));
		}
		rootElement.addContent(compnetsElement);
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			new XMLOutputter().output(new Document(rootElement), fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Element componetToXML(VJJComponent component){
		Element element = new Element(COMPONENT_ELEMENT_NAME);
		
		element.setAttribute(CLASS_NAME_ATTRIBUTE_NAME, component.getClass().getName());
		
		Element componentSetup = new Element(COMPONENT_SETUP_ELEMENT_NAME);
		component.getConfig(componentSetup);
		element.addContent(componentSetup);
		
		if(ImagePublisher.class.isInstance(component)){
			ImagePublisher imagePublisher = (ImagePublisher)component;
			Element compnetsElement = new Element(COMPONENTS_ELEMENT_NAME);
			for(ImageListener imageListener : imagePublisher.getImageListenerList()){
				compnetsElement.addContent(componetToXML(imageListener));
			}
			element.addContent(compnetsElement);
		}
		
		return element;
	}
	
	public static void load(String fileName){
		try {
			Document document = new SAXBuilder().build( fileName );
			Element rootElement = document.getRootElement();
			if(rootElement.getName() != ROOT_ELEMET_NAME){
				System.err.println("is notan Open-VJJ Project");
				return;
			}
			Element gpuElement = rootElement.getChild(GPU_ELEMENT_NAME);
			if(gpuElement != null){
				Attribute gpuActiv = gpuElement.getAttribute(GPU_ACTIV_ATTRIBUTE_NAME);
				if(gpuActiv != null){
					useGPU = gpuActiv.getBooleanValue();
				}
			}
			removeAll();
			Element components = rootElement.getChild(COMPONENTS_ELEMENT_NAME);
			List<Element> elements = components.getChildren(COMPONENT_ELEMENT_NAME);
			for(Element rootComponente : elements){
				addComponent((ImagePublisher)loadComponent(rootComponente));
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static VJJComponent loadComponent(Element element){
		String className = element.getAttribute(CLASS_NAME_ATTRIBUTE_NAME).getValue();
		try {
			Class<?> c = Class.forName(className);
			Object classInstance = c.newInstance();
			if(VJJComponent.class.isInstance(classInstance)){
				VJJComponent vjjComponent = (VJJComponent) classInstance;
				Element componentSetup = element.getChild(COMPONENT_SETUP_ELEMENT_NAME);
				if(componentSetup != null){
					vjjComponent.setConfig(componentSetup);
				}
				if(ImagePublisher.class.isInstance(vjjComponent)){
					ImagePublisher imagePublisher = (ImagePublisher) classInstance;
					Element components = element.getChild(COMPONENTS_ELEMENT_NAME);
					if(components != null){
						List<Element> elements = components.getChildren(COMPONENT_ELEMENT_NAME);
						for(Element subComponentElement : elements){
							ImageListener imageListener = (ImageListener)loadComponent(subComponentElement);
							if(imageListener == null){
								continue;
							}
							addComponent(imageListener, imagePublisher);
						}
					}
				}
				return vjjComponent; 
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	private static void removeAll(){
		if(imagePublishers != null){
			for(ImagePublisher imagePublisher : imagePublishers){
				imagePublisher.remove();
			}
		}
		imagePublishers = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		removeAll();
		super.finalize();
	}
	
	static Cleaner cleaner = new Cleaner();
	private static class Cleaner implements Runnable{
		public Cleaner() {
			Thread thread = new Thread(this);
			thread.start();
		}

		@Override
		public void run() {
			while(true){
				System.gc();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		
	}
}
