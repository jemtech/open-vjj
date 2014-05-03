/**
 * 
 */
package de.openVJJ.GUI;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.openVJJ.basic.Module;

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
public class ModulePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1396575249877997206L;
	
	private Module module;
	
	/**
	 * 
	 */
	public ModulePanel(Module module) {
		this.module = module;
		init();
	}
	
	private void init(){
		Rectangle giuPosition = module.getGuiPosition();
		setBounds(giuPosition);
		setBorder(BorderFactory.createLineBorder(Color.black));
		MyMouseListener myMouseListener = new MyMouseListener();
		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);
		setBackground(Color.blue);
	}

	public JFrame getFrame (  ) {
	     return getFrameRe ( this) ;
	}


	private JFrame getFrameRe ( Container target ) {
	     if ( target instanceof JFrame ) {
	       return ( JFrame ) target;
	     }
	     return getFrameRe ( target.getParent ()) ;
	}
	
	protected class MyMouseListener implements MouseListener, MouseMotionListener{

		Point screenPointPressed;
		boolean isPressed = false;
		Rectangle boundOnPressed;
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
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
			if(MouseEvent.BUTTON1 != e.getButton()){
				return;
			}
			if(isPressed){
				System.out.println("already pressed");
				return;
			}
			screenPointPressed = getFrame().getMousePosition();
			boundOnPressed = getBounds();
			isPressed = true;
			Runnable mouseUpdater = new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(isPressed){
						updatePos();
						try {
							Thread.sleep(40);
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
			Point actuelPoint = getFrame().getMousePosition();
			if(actuelPoint == null){
				return;
			}
			int x = boundOnPressed.x + actuelPoint.x - screenPointPressed.x;
			int y = boundOnPressed.y + actuelPoint.y - screenPointPressed.y;
			setBounds(x, y, boundOnPressed.width, boundOnPressed.height);
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
			if(! isPressed){
				return;
			}
			isPressed = false;
			Point actuelPoint = getFrame().getMousePosition();
			if(actuelPoint == null){
				return;
			}
			int x = boundOnPressed.x;
			x += actuelPoint.x;
			x -= screenPointPressed.x;
			int y = boundOnPressed.y + actuelPoint.y - screenPointPressed.y;
			setBounds(x, y, boundOnPressed.width, boundOnPressed.height);
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
