/**
 * 
 */
package de.openVJJ.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import de.openVJJ.GUI.SelectPlugable.SelectPlugableListener;
import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Module;
import de.openVJJ.basic.Plugable;
import de.openVJJ.basic.Value;

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
public class ModuleInsightPannel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4791375899655587881L;
	Module module;

	/**
	 * 
	 */
	public ModuleInsightPannel(Module module) {
		this.module = module;
		init();
	}
	
	private void init(){
		setLayout(null);
		setSize( 1000, 500 );
		initModule();
		addMouseListener(new PannelMouseListener());
	}
	
	private void initModule(){
		this.removeAll();
		conectionLines = new ArrayList<ConectionLine>();
		setLayout(null);
		createInOutputs();
		initPluables();
		repaint();
	}
	
	private Map<JLabel, String> labelInputMap = new HashMap<JLabel, String>();
	private Map<String, JLabel> inputLabelMap = new HashMap<String, JLabel>();
	private Map<JLabel, String> labelOutputMap = new HashMap<JLabel, String>();
	private Map<String, JLabel> outputLabelMap = new HashMap<String, JLabel>();
	private void createInOutputs(){
		/*
		 * ins
		 */
		Set<String> keys = module.getOutputs().keySet();
		int plugCount = keys.size();
		int labelBlockHight = plugCount * PlugablePanel.PLUG_LABEL_HEIGHT;
		int posY = (getHeight() - labelBlockHight)/2;
		for(String key : keys){
			JLabel inputLabel = new JLabel(key + "(" + module.getOutputs().get(key).getSimpleName() + ")");
			inputLabel.setBounds(0, posY, PlugablePanel.PLUG_LABEL_WIDTH, PlugablePanel.PLUG_LABEL_HEIGHT);
			inputLabel.setBackground(Color.yellow);
			inputLabel.setOpaque(true);
			inputLabel.addMouseListener(new InLabelMouseListener(module));
			add(inputLabel);
			labelInputMap.put(inputLabel, key);
			inputLabelMap.put(key, inputLabel);
			posY += PlugablePanel.PLUG_LABEL_HEIGHT;
		}
		
		/*
		 * outs
		 */
		keys = module.getInputs().keySet();
		plugCount = keys.size();
		labelBlockHight = plugCount * PlugablePanel.PLUG_LABEL_HEIGHT;
		posY = (getHeight() - labelBlockHight)/2;
		int posX = getWidth() - PlugablePanel.PLUG_LABEL_WIDTH;
		for(String key : keys){
			JLabel outputLabel = new JLabel(key + "(" + module.getInputs().get(key).getSimpleName() + ")");
			outputLabel.setBounds(posX, posY, PlugablePanel.PLUG_LABEL_WIDTH, PlugablePanel.PLUG_LABEL_HEIGHT);
			outputLabel.setBackground(Color.cyan);
			outputLabel.setOpaque(true);
			outputLabel.addMouseListener(new OutLabelMouseListener(module));
			add(outputLabel);
			labelOutputMap.put(outputLabel, key);
			outputLabelMap.put(key, outputLabel);
			posX += PlugablePanel.PLUG_LABEL_HEIGHT;
		}
	}
	
	private Map<Plugable, PlugablePanel> plugablePlugablePannelMap = new HashMap<Plugable, PlugablePanel>();
	private void initPluables(){
		List<Plugable> plugables = module.getPlugables();
		for(Plugable plugable : plugables){
			PlugablePanel pugablePanel = new PlugablePanel(plugable, this);
			plugablePlugablePannelMap.put(plugable, pugablePanel);
			add(pugablePanel);
		}
		initConnections();
	}
	
	private void initConnections(){
		List<Plugable> plugables = module.getPlugables();
		for(Plugable plugable : plugables){
			PlugablePanel pannel = plugablePlugablePannelMap.get(plugable);
			for(String key : plugable.getInputs().keySet()){
				JLabel inputLabel = pannel.getInputLabelMap().get(key);
				Connection.ConnectionListener listener = plugable.getListener(key);
				if(listener == null){
					continue;
				}
				Connection inputConnectedTo = listener.getConnection();
				for(Plugable plugableOut : plugables){
					for(String outKey : plugableOut.getOutputs().keySet()){
						Connection outCon = plugableOut.getConnection(outKey);
						if(outCon == inputConnectedTo){
							PlugablePanel outPannel = plugablePlugablePannelMap.get(plugableOut);
							JLabel outLabel = outPannel.getOutputLabelMap().get(outKey);
							
							ConectionLine conectionLine = new ConectionLine(inputLabel, plugable, outLabel, plugableOut);
							conectionLines.add(conectionLine);
						}
					}
				}
			}
		}
	}
	

	public List<ConectionLine> conectionLines = new ArrayList<ConectionLine>();
	public Line2D tempLine;
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		int cap = BasicStroke.CAP_BUTT;
        int join = BasicStroke.JOIN_MITER;
        BasicStroke thick = new BasicStroke(3,cap,join);
        g2.setColor(Color.BLACK);
        g2.setStroke(thick);
		for(ConectionLine cLine : conectionLines){
			Line2D line = cLine.getGraphicLine();
			g2.draw(line);
		}
		g2.setColor(Color.YELLOW);
		if(tempLine != null){
			g2.draw(tempLine);
		}
	}
	
	public void createConectionLine(JLabel input, Plugable in, JLabel output, Plugable out){
		ConectionLine newLine = new ConectionLine(input, in, output, out);
		for(ConectionLine conectionLine : conectionLines){
			if(conectionLine.equals(newLine)){
				repaint();
				return;
			}
		}
		if(newLine.connect()){
			String inName = newLine.getInName();
			List<ConectionLine> toRem = new ArrayList<ConectionLine>();
			for(ConectionLine conectionLine : conectionLines){
				if(conectionLine.in == newLine.in){
					if(conectionLine.getInName().equals(inName)){
						toRem.add(conectionLine);
					}
				}
			}
			for(ConectionLine conectionLine : toRem){
				conectionLines.remove(conectionLine);
			}
			conectionLines.add(newLine);
		}
		repaint();
	}
	
	public class ConectionLine{
		private Line2D graphicLine;
		private JLabel input;
		private JLabel output;
		private Plugable in;
		private Plugable out;
		
		private boolean connect(){
			String outName = plugablePlugablePannelMap.get(out).labelToOutputName(output);
			Connection outCon = out.getConnection(outName);
			String inName = getInName();
			return in.setInput(inName, outCon);
		}
		
		private String getInName(){
			return plugablePlugablePannelMap.get(in).labelToInputName(input);
		}
		/**
		 * 
		 */
		public ConectionLine(JLabel input, Plugable in, JLabel output, Plugable out) {
			this.input = input;
			this.output = output;
			this.in = in;
			this.out = out;
		}
		
		/**
		 * @return the graphicLine
		 */
		public Line2D getGraphicLine() {
			if(graphicLine == null){
				graphicLine = new Line2D.Float();
			}
			graphicLine.setLine(getLineStart(), getLineEnd());
			return graphicLine;
		}
		
		private Point getLineStart(){
			Point locAtScreen = output.getLocationOnScreen();
			Point pannelLoc = getLocationOnScreen();
			return new Point(locAtScreen.x - pannelLoc.x + PlugablePanel.PLUG_LABEL_WIDTH, locAtScreen.y - pannelLoc.y + PlugablePanel.PLUG_LABEL_HEIGHT / 2);
		}
		
		private Point getLineEnd(){
			Point locAtScreen = input.getLocationOnScreen();
			Point pannelLoc = getLocationOnScreen();
			return new Point(locAtScreen.x - pannelLoc.x, locAtScreen.y - pannelLoc.y + PlugablePanel.PLUG_LABEL_HEIGHT / 2);
		}
		
		public boolean equals(ConectionLine line) {
			if(line == null){
				return false;
			}
			if(input == line.input && output == line.output){
				return true;
			}
			return false;
		}
		
	}
	
	protected boolean isPressed = false;
	protected JLabel mouseOverLabel = null;
	protected Plugable mouseOverPlugable = null;
	protected Class<? extends Value> mousePresedOverInputTypClass = null;
	public class InLabelMouseListener implements MouseListener, MouseMotionListener{
		
		public InLabelMouseListener(Plugable plugable){
			this.plugable = plugable;
		}
		Plugable plugable;
		//Point screenPointPressed;
		Line2D line;
		JLabel pressedOver = null;
		//Rectangle boundOnPressed;
		
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
			if(MouseEvent.BUTTON1 != e.getButton()){
				return;
			}
			if(isPressed){
				return;
			}
			isPressed = true;
			JLabel label = (JLabel) e.getSource();
			pressedOver = label;
			Point labelLocation = label.getLocationOnScreen();
			Point frameLocation = getLocationOnScreen();
			Point mouseLocation = e.getLocationOnScreen();
			line = new Line2D.Float((float) labelLocation.x - frameLocation.x + PlugablePanel.PLUG_LABEL_WIDTH/2,(float) labelLocation.y - frameLocation.y + PlugablePanel.PLUG_LABEL_HEIGHT/2,(float) mouseLocation.x - frameLocation.x,(float) mouseLocation.y- frameLocation.y);
			tempLine = line;
			mousePresedOverInputTypClass = plugable.getInputs().get(plugablePlugablePannelMap.get(plugable).labelToInputName(pressedOver));
			repaint();
			
			Runnable mouseUpdater = new Runnable() {
				@Override
				public void run() {
					while(isPressed){
						updatePos();
						try {
							Thread.sleep(PlugablePanel.REFRESH_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			new Thread(mouseUpdater).start();
		}

		public void updatePos(){
			Point actuelPoint = getMousePosition();
			if(actuelPoint == null){
				return;
			}
			line.setLine(line.getP1(), actuelPoint);
			tempLine = line;
			repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if(MouseEvent.BUTTON1 != e.getButton()){
				return;
			}
			if(! isPressed){
				return;
			}
			isPressed = false;
			tempLine = null;
			mousePresedOverInputTypClass = null;
			if(pressedOver == null || mouseOverLabel == null){
				repaint();
				return;
			}
			createConectionLine(pressedOver, plugable, mouseOverLabel, mouseOverPlugable);
		}
		
	}
	

	public class OutLabelMouseListener implements MouseListener, MouseMotionListener{
		
		public OutLabelMouseListener(Plugable plugable){
			this.plugable = plugable;
		}
		Plugable plugable;

		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent arg0) {
			mouseOverLabel = (JLabel) arg0.getSource();
			mouseOverPlugable = plugable;
			
			if(mousePresedOverInputTypClass != null){
				String outName = plugablePlugablePannelMap.get(plugable).labelToOutputName(mouseOverLabel);
				Connection outCon = plugable.getConnection(outName);
				if(! outCon.classMatch(mousePresedOverInputTypClass)){
					mouseOverLabel.setBackground(Color.red);
				}else{
					mouseOverLabel.setBackground(Color.green);
				}
			}
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent arg0) {
			((JLabel) arg0.getSource()).setBackground(Color.cyan);
			mouseOverLabel = null;
			mouseOverPlugable = null;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class PannelMouseListener implements MouseListener{

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
			
			MousePopUp menu = new MousePopUp();
	        menu.show(e.getComponent(), e.getX(), e.getY());
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class MousePopUp extends JPopupMenu {
	    /**
		 * 
		 */
		private static final long serialVersionUID = -2510704964240859771L;
		JMenuItem anItem;
	    public MousePopUp(){
	        anItem = new JMenuItem("Add");
	        anItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SelectPlugable selectPlugable = new SelectPlugable();
					MySelectPlugableListener listener = new MySelectPlugableListener();
					selectPlugable.addListener(listener);
					selectPlugable.openAsFrame();
				}
			});
	        add(anItem);
	    }
	}
	
	private class MySelectPlugableListener implements SelectPlugableListener{

		/* (non-Javadoc)
		 * @see de.openVJJ.GUI.SelectPlugable.SelectPlugableListener#plugableSelected(java.lang.Class)
		 */
		@Override
		public void plugableSelected(Class<?> plugableClass) {
			try {
				Plugable plugable = (Plugable) plugableClass.newInstance();
				module.addPlugable(plugable);
				initModule();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
