/**
 * 
 */
package de.openVJJ.GUI;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.openVJJ.basic.Plugable;

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
public class PlugablePanel extends JPanel{
	public static int REFRESH_TIME = 40; //in ms
	public static int PLUG_LABEL_HEIGHT = 30;
	public static int PLUG_LABEL_WIDTH = 150;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1396575249877997206L;
	
	private Plugable plugable;
	
	/**
	 * 
	 */
	public PlugablePanel(Plugable plugable, ModuleInsightPannel displayedAt) {
		this.plugable = plugable;
		this.displayedAt = displayedAt; 
		init();
	}
	
	private void init(){
		setLayout(null);
		Rectangle giuPosition = plugable.getGuiPosition();
		setBounds(giuPosition);
		setBorder(BorderFactory.createLineBorder(Color.black));
		MyMouseListener myMouseListener = new MyMouseListener();
		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);
		setBackground(Color.blue);
		createInOutputs();
	}

	private ModuleInsightPannel displayedAt;
	/*public JFrame getFrame (  ) {
	     return getFrameRe ( this) ;
	}


	private JFrame getFrameRe ( Container target ) {
	     if ( target instanceof JFrame ) {
	       return ( JFrame ) target;
	     }
	     return getFrameRe ( target.getParent ()) ;
	}*/
	
	public String labelToInputName(JLabel label){
		return labelInputMap.get(label);
	}
	
	public String labelToOutputName(JLabel label){
		return labelOutputMap.get(label);
	}
	
	protected Map<String, JLabel> getInputLabelMap(){
		return inputLabelMap;
	}
	
	protected Map<String, JLabel> getOutputLabelMap(){
		return outputLabelMap;
	}
	
	@Override
	public void setBounds(Rectangle rectangle) {
		super.setBounds(rectangle);
		plugable.setGuiPosition(rectangle);
	}
	
	private Map<JLabel, String> labelInputMap = new HashMap<JLabel, String>();
	private Map<String, JLabel> inputLabelMap = new HashMap<String, JLabel>();
	private Map<JLabel, String> labelOutputMap = new HashMap<JLabel, String>();
	private Map<String, JLabel> outputLabelMap = new HashMap<String, JLabel>();
	private void createInOutputs(){
		/*
		 * ins
		 */
		Set<String> keys = plugable.getInputs().keySet();
		int plugCount = keys.size();
		int labelBlockHight = plugCount * PLUG_LABEL_HEIGHT;
		int posY = (getHeight() - labelBlockHight)/2;
		for(String key : keys){
			JLabel inputLabel = new JLabel("<html><body>" + key + "<br/>(" + plugable.getInputs().get(key).getSimpleName() + ")</body></html>");
			inputLabel.setBounds(0, posY, PLUG_LABEL_WIDTH, PLUG_LABEL_HEIGHT);
			inputLabel.setBackground(Color.yellow);
			inputLabel.setOpaque(true);
			inputLabel.addMouseListener(displayedAt.new InLabelMouseListener(plugable));
			add(inputLabel);
			labelInputMap.put(inputLabel, key);
			inputLabelMap.put(key, inputLabel);
			posY += PLUG_LABEL_HEIGHT;
		}
		
		/*
		 * outs
		 */
		keys = plugable.getOutputs().keySet();
		plugCount = keys.size();
		labelBlockHight = plugCount * PLUG_LABEL_HEIGHT;
		posY = (getHeight() - labelBlockHight)/2;
		int posX = getWidth() - PLUG_LABEL_WIDTH;
		for(String key : keys){
			JLabel outputLabel = new JLabel("<html><body>" + key + "<br/>(" + plugable.getOutputs().get(key).getSimpleName() + ")</body></html>");
			outputLabel.setBounds(posX, posY, PLUG_LABEL_WIDTH, PLUG_LABEL_HEIGHT);
			outputLabel.setBackground(Color.cyan);
			outputLabel.setOpaque(true);
			outputLabel.addMouseListener(displayedAt.new OutLabelMouseListener(plugable));
			add(outputLabel);
			labelOutputMap.put(outputLabel, key);
			outputLabelMap.put(key, outputLabel);
			posX += PLUG_LABEL_HEIGHT;
		}
	}

	
	protected class MyMouseListener implements MouseListener, MouseMotionListener{

		Point screenPointPressed;
		Rectangle boundOnPressed;
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			JPanel configPanel = plugable.getConfigPannel();
			if(configPanel != null){
				JFrame configFrame = new JFrame();
				configFrame.add(configPanel);
				configFrame.setVisible(true);
				configFrame.pack();
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if(displayedAt == null){
				return;
			}
			if(MouseEvent.BUTTON1 != e.getButton()){
				return;
			}
			if(displayedAt.isPressed){
				System.out.println("already pressed");
				return;
			}
			screenPointPressed = displayedAt.getMousePosition();
			boundOnPressed = getBounds();
			displayedAt.isPressed = true;
			Runnable mouseUpdater = new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(displayedAt.isPressed){
						updatePos();
						try {
							Thread.sleep(REFRESH_TIME);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
			new Thread(mouseUpdater).start();
		}

		public void updatePos(){
			Point actuelPoint = displayedAt.getMousePosition();
			if(actuelPoint == null){
				return;
			}
			int x = boundOnPressed.x + actuelPoint.x - screenPointPressed.x;
			int y = boundOnPressed.y + actuelPoint.y - screenPointPressed.y;
			Rectangle rectangle = new Rectangle(x, y, boundOnPressed.width, boundOnPressed.height);
			setBounds(rectangle);
			displayedAt.repaint();
		}
		
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			if(MouseEvent.BUTTON1 != e.getButton()){
				return;
			}
			if(! displayedAt.isPressed){
				return;
			}
			displayedAt.isPressed = false;
			Point actuelPoint = displayedAt.getMousePosition();
			if(actuelPoint == null){
				return;
			}
			int x = boundOnPressed.x;
			x += actuelPoint.x;
			x -= screenPointPressed.x;
			int y = boundOnPressed.y + actuelPoint.y - screenPointPressed.y;
			Rectangle rectangle = new Rectangle(x, y, boundOnPressed.width, boundOnPressed.height);
			setBounds(rectangle);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
		}
		
	}
	
	
	
	
}
