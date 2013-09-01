package de.openVJJ.processor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.openVJJ.graphic.VideoFrame;

/*
 * Copyright (C) 2013  Jan-Erik Matthies
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

public class ObjectFromLine extends ImageProcessor {

	//int borderValue = -1;
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		return findObjects(videoFrame);
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}
	
	private VideoFrame findObjects(VideoFrame videoFrame){
		VideoFrame videoFrameRes = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		//int freeObjectNumber = 1;
		//Map<Integer, List<int[]>> objectBorders = new HashMap<Integer, List<int[]>>();
		//Map<Integer, Color> objectColor = new HashMap<Integer, Color>();
		Random random = new Random();
		PictureObject[][] objectMap = new PictureObject[videoFrame.getWidth()][videoFrame.getHeight()];
		int xMax = videoFrame.getWidth();
		int yMax = videoFrame.getHeight();
		List<PictureObject> objects = new ArrayList<ObjectFromLine.PictureObject>();
		for(int x=0; x < xMax; x++){
			for(int y=0; y < yMax; y++){
				int[] rgb = videoFrame.getRGB(x, y);
				if(rgb[0]>1 || rgb[1]>1){
					objectMap[x][y] = null;
					//videoFrameRes.setColor(x, y, Color.red);
					continue;
				}
				//processPoint(x, y, objects);
				
				PictureObject xNabor = null;
				if(0 < x){
					xNabor = objectMap[x-1][y];
				}
				if(xNabor != null){
					objectMap[x][y] = xNabor;
					xNabor.add(new Point(x, y));
					//videoFrameRes.setColor(x, y, objectColor.get(nabor));
					//continue;
				}
				PictureObject yNabor = null;
				if(0 < y){
					yNabor = objectMap[x][y-1];
				}
				if(yNabor != null){
					if(xNabor == null){
						objectMap[x][y] = yNabor;
						yNabor.add(new Point(x, y));
					}else{
						if(xNabor != yNabor){
							if(objects.contains(yNabor)){
								xNabor.add(yNabor);
								objects.remove(yNabor);
							}
						}
					}
					/*try{
					videoFrameRes.setColor(x, y, objectColor.get(nabor));
					continue;
					}catch (Exception e) {
						System.out.println("cant find nabor:" + nabor);
					}*/
				}
				if(xNabor == null && yNabor == null){
					PictureObject pictureObject =  new PictureObject(new Point(x, y));
					objectMap[x][y] = pictureObject;
					objects.add(pictureObject);
				}
				
				/*objectMap[x][y] = freeObjectNumber;
				Color randomColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
				videoFrameRes.setColor(x, y, randomColor);
				objectColor.put(freeObjectNumber, randomColor);
				freeObjectNumber++;*/
			}
		}
		for(PictureObject pictureObject : objects){
			Color randomColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
			for(Point point : pictureObject.points){
				videoFrameRes.setColor(point.x, point.y, randomColor);
			}
		}
		System.out.println("Found " + objects.size() + " Objects.");
		return videoFrameRes;
	}
	
	private void processPoint(int x, int y, List<PictureObject> objects){
		Point point = new Point(x,y);
		Point nTop = point.naiborTop();
		Point nLeft = point.naiborLeft();

		PictureObject pictureObjectFound = null;
		List<PictureObject> pictureObjectAditionalFound = new ArrayList<ObjectFromLine.PictureObject>();
		for(PictureObject pictureObject : objects){
			if(pictureObject.contains(nLeft)){
				if(pictureObjectFound == null){
					pictureObject.add(point);
					pictureObjectFound = pictureObject;
				}else{
					pictureObjectAditionalFound.add(pictureObject);
				}
			}
			if(pictureObject.contains(nTop)){
				if(pictureObjectFound == null){
					pictureObject.add(point);
					pictureObjectFound = pictureObject;
				}else{
					pictureObjectAditionalFound.add(pictureObject);
				}
			}
		}
		if(pictureObjectFound == null){
			objects.add(new PictureObject(point));
		}else{
			for(PictureObject pictureObject : pictureObjectAditionalFound){
				pictureObjectFound.add(pictureObject);
				objects.remove(pictureObject);
			}
		}
	}
	
	private class PictureObject{
		List<Point> points;
		PictureObject upperObject;
		
		public PictureObject(Point point) {
			points = new ArrayList<ObjectFromLine.Point>();
			points.add(point);
		}
		
		public boolean contains(Point point){
			for(Point element : points){
				if(element.equals(point)){
					return true;
				}
			}
			return false;
		}
		
		public void setUpperObject(PictureObject upperObject){
			this.upperObject = upperObject;
		}
		
		public void add(Point point){
			if(point != null){
				if(upperObject != null){
					upperObject.add(point);
				}else{
					points.add(point);
				}
			}
		}
		
		public void add(PictureObject pictureObject){
			if(pictureObject == this){
				System.out.println("cant addto my self");
				return;
			}
			if(upperObject != null){
				upperObject.add(pictureObject);
			}else{
				pictureObject.setUpperObject(this);
				for(Point point : pictureObject.points){
					point.setPictureObject(this);
				}
				points.addAll(pictureObject.points);
			}
		}
		
		public List<Point> getPoints(){
			return points;
		}
	}
	
	private class Point{
		int x;
		int y;
		PictureObject pictureObject;
		
		public void setPictureObject(PictureObject pictureObject){
			this.pictureObject = pictureObject;
		}
		
		public Point(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		public boolean equals(Point point) {
			return (this.x == point.x) && (this.y == point.y);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(Point.class.isInstance(obj)){
				return equals((Point) obj);
			}
			return super.equals(obj);
		}
		
		public Point naiborTop(){
			return new Point(x, y-1);
		}
		
		public Point naiborDown(){
			return new Point(x, y+1);
		}
		
		public Point naiborLeft(){
			return new Point(x-1, y);
		}
		
		public Point naiborRight(){
			return new Point(x+1, y);
		}
	}

}
