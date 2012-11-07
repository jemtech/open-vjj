package de.openVJJ.graphic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

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

public class VideoFrame {
	int rgbImageArray[][][];
	public VideoFrame(int width, int height){
		rgbImageArray = new int[width][height][3];
	}
	
	GraphicsConfiguration gConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	public VideoFrame(Image image){
		ImageIcon icon = new ImageIcon(image);
		BufferedImage bufferedImage = gConfiguration.createCompatibleImage(icon.getIconWidth(), icon.getIconHeight());
		Graphics2D g = bufferedImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		setByBufferdImage(bufferedImage);
	} 
	
	public VideoFrame(BufferedImage bufferedImage){
		setByBufferdImage(bufferedImage);
	}
	
	private void setByBufferdImage(BufferedImage bufferedImage){
		rgbImageArray = new int[bufferedImage.getWidth()][bufferedImage.getHeight()][3];
		for(int x = 0; x < rgbImageArray.length; x++ ){
			for(int y = 0; y < rgbImageArray[0].length; y++ ){
				Color c = new Color(bufferedImage.getRGB(x, y));
				rgbImageArray[x][y][0] = c.getRed();
				rgbImageArray[x][y][1] = c.getGreen();
				rgbImageArray[x][y][2] = c.getBlue();
				bufferedImage.setRGB(x, y, new Color(rgbImageArray[x][y][0], rgbImageArray[x][y][1], rgbImageArray[x][y][2]).getRGB());
			}
		}
	}
	
	public void setColor(int x, int y,int red, int green, int blue){
		rgbImageArray[x][y][0] = red;
		rgbImageArray[x][y][1] = green;
		rgbImageArray[x][y][2] = blue;
	}
	
	public void setColor(int x, int y, int[] rgb){
		rgbImageArray[x][y] = rgb;
	}
	
	public void setColor(int x, int y, Color color){
		rgbImageArray[x][y][0] = color.getRed();
		rgbImageArray[x][y][1] = color.getGreen();
		rgbImageArray[x][y][2] = color.getBlue();
	}
	
	public int[] getRGB(int x, int y){
		return rgbImageArray[x][y];
	}
	public void setRGB(int x, int y, int[] rgb){
		rgbImageArray[x][y] = rgb;
	}
	
	public void addColor(int x, int y, int rgb){
		Color c = new Color(rgb);
		rgbImageArray[x][y][0] += c.getRed();
		rgbImageArray[x][y][1] += c.getBlue();
		rgbImageArray[x][y][2] += c.getGreen();
	}
	
	public void addColor(int x, int y, int[] rgb){
		rgbImageArray[x][y][0] += rgb[0];
		rgbImageArray[x][y][1] += rgb[1];
		rgbImageArray[x][y][2] += rgb[2];
	}
	
	public void divide(int x, int y, double divisor){
		rgbImageArray[x][y][0] /= divisor;
		rgbImageArray[x][y][1] /= divisor;
		rgbImageArray[x][y][2] /= divisor;
	}
	
	public void multiply(int x, int y, double multiplier){
		rgbImageArray[x][y][0] *= multiplier;
		rgbImageArray[x][y][1] *= multiplier;
		rgbImageArray[x][y][2] *= multiplier;
	}
	
	public void setRGB(int x, int y, int rgb){
		Color c = new Color(rgb);
		rgbImageArray[x][y][0] = c.getRed();
		rgbImageArray[x][y][1] = c.getBlue();
		rgbImageArray[x][y][2] = c.getGreen();
	}
	
	public BufferedImage getImage(){
		BufferedImage bufferedImage = new BufferedImage(rgbImageArray.length, rgbImageArray[0].length, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < rgbImageArray.length; x++ ){
			for(int y = 0; y < rgbImageArray[0].length; y++ ){
				bufferedImage.setRGB(x, y, new Color(rgbImageArray[x][y][0], rgbImageArray[x][y][1], rgbImageArray[x][y][2]).getRGB());
			}
		}
		return bufferedImage;
	}
	
	@Deprecated
	public void draw(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		for(int x = 0; x < rgbImageArray.length; x++ ){
			for(int y = 0; y < rgbImageArray[0].length; y++ ){
				g2d.setColor(new Color(rgbImageArray[x][y][0], rgbImageArray[x][y][1], rgbImageArray[x][y][2]));
				g2d.drawLine(x, y, x, y);
			}
		}
		g2d.dispose();
	}
	
	public int getWidth(){
		return rgbImageArray.length;
	}
	
	public int getHeight(){
		return rgbImageArray[0].length;
	}
	
	public void scaleTo(int width, int height){
		int newRgbImageArray[][][] = new int[width][height][];
		float xMul = getWidth()/(float)width;
		float yMul = getHeight()/(float)height;
		int xN = 0;
		int xMax = rgbImageArray.length;
		int yMax = rgbImageArray[0].length;
		for(float x = 0; x < xMax && xN<width; x += xMul){
			int yN = 0;
			for(float y = 0; y < yMax && yN<height; y += yMul){
				newRgbImageArray[xN][yN] = rgbImageArray[(int)x][(int)y];
				yN++;
			}
			xN++;
		}
		rgbImageArray = newRgbImageArray;
	}
}
