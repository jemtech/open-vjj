package de.openVJJ.controler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import de.openVJJ.ImageListener.ImageViweFrame;
import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.processor.Warping;
import de.openVJJ.processor.Warping.Point;

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

public class WarpingControl extends ImageViweFrame{
	JLabel borderLable;
	protected Warping warper;
	Point[] points;
	public WarpingControl(Warping warping){
		super(warping);
		onClickToggelFullscreen = false;
		sizeByFrame = false;
		startWatching();
		frame.setTitle("Warp-Control");
		frame.setResizable(false);
		warper = warping;
		points = warping.getWarpPoints();
		camImage.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				follow =false;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				for(Point wPoint : points){
					if(wPoint.x-10<x && x<wPoint.x+10 && wPoint.y-10<y && y<wPoint.y+10){
						follow = true;
						new Thread(new Mousfollower(e, wPoint)).start();
					}
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	@Override
	public void newImageReceived(VideoFrame videoFrame) {
		Image image = videoFrame.getImage();
		points = warper.getWarpPoints();
		paintCorners(image.getGraphics());
		super.newImageReceived(image);
	}
	
	
	protected void paintCorners(Graphics graphics){
		graphics.setColor(Color.CYAN);
		for(Point point : points){
			graphics.fillOval(point.x-10, point.y-10, 20, 20);
		}
		graphics.dispose();
	}
	
	boolean follow = false;
	class Mousfollower implements Runnable{
		Point point;
		public Mousfollower(MouseEvent e, Point point) {
			this.point = point;
		}
		@Override
		public void run() {
			while(follow){
				java.awt.Point point = camImage.getMousePosition();
				this.point.x = point.x;
				this.point.y = point.y;
				warper.setWarp(points);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
	}
}
