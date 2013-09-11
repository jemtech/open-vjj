package de.openVJJ.graphic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.ImageIcon;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLMemory.Mem;

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

public class VideoFrame {
	
	private int rgbImageArray[][][] = null;
	private int transValue[] = null;
	private CLBuffer<FloatBuffer> rCLBuffer = null;
	private CLBuffer<FloatBuffer> gCLBuffer = null;
	private CLBuffer<FloatBuffer> bCLBuffer = null;
	private BufferedImage bufferedImage = null;
	int width = 0;
	int height = 0;
	
	public VideoFrame(int width, int height){
		this.width = width;
		this.height = height;
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
	
	
	public void setByBufferdImage(BufferedImage bufferedImage){
		width = bufferedImage.getWidth();
		height = bufferedImage.getHeight();
		this.bufferedImage = bufferedImage;
		rgbImageArray = null;
		removeCLBuffer();
	}

	
	public void setCLBuffer(CLBuffer<FloatBuffer> rCLBuffer, CLBuffer<FloatBuffer> gCLBuffer, CLBuffer<FloatBuffer> bCLBuffer, int width, int height){
		this.rCLBuffer = rCLBuffer;
		this.gCLBuffer = gCLBuffer;
		this.bCLBuffer = bCLBuffer;
		this.width = width;
		this.height = height;
		bufferedImage = null;
		rgbImageArray = null;
	}
	
	public synchronized int[][][] getIntArray(){
		if(rgbImageArray != null){
			return rgbImageArray;
		}
		if(bufferedImage != null){
			bufferdImageToArray();
			return rgbImageArray;
		}
		if(rCLBuffer != null && gCLBuffer != null && bCLBuffer != null){
			gpuBufferToArray();
			return rgbImageArray;
		}
		return rgbImageArray = new int[width][height][3];
	}
	
	private void gpuBufferToArray(){
		FloatBuffer gpuBufferR = rCLBuffer.getBuffer();
		gpuBufferR.rewind();
		FloatBuffer gpuBufferG = gCLBuffer.getBuffer();
		gpuBufferG.rewind();
		FloatBuffer gpuBufferB = bCLBuffer.getBuffer();
		gpuBufferB.rewind();
		int xMax = getWidth();
		int yMax = getHeight();
		rgbImageArray = new int[xMax][yMax][3];
		for(int x =0; x < xMax; x++){
			for(int y =0; y < yMax; y++){
				rgbImageArray[x][y][0] = (int) gpuBufferR.get();
				rgbImageArray[x][y][1] = (int) gpuBufferG.get();
				rgbImageArray[x][y][2] = (int) gpuBufferB.get();
			}
		}
	}
	
	private void bufferdImageToArray(){
		rgbImageArray = new int[bufferedImage.getWidth()][bufferedImage.getHeight()][3];
		for(int x = 0; x < rgbImageArray.length; x++ ){
			for(int y = 0; y < rgbImageArray[0].length; y++ ){
				Color c = new Color(bufferedImage.getRGB(x, y));
				rgbImageArray[x][y][0] = c.getRed();
				rgbImageArray[x][y][1] = c.getGreen();
				rgbImageArray[x][y][2] = c.getBlue();
			}
		}
	}
	
	public synchronized BufferedImage getImage(){
		if(bufferedImage != null){
			return bufferedImage;
		}
		if(rgbImageArray != null){
			arrayToBufferdImage();
			return bufferedImage;
		}
		if(rCLBuffer != null && gCLBuffer != null && bCLBuffer != null){
			gpuBufferToImage();
			return bufferedImage;
		}
		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		return bufferedImage;
	}
	
	private void arrayToBufferdImage(){
		bufferedImage = new BufferedImage(rgbImageArray.length, rgbImageArray[0].length, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < rgbImageArray.length; x++ ){
			for(int y = 0; y < rgbImageArray[0].length; y++ ){
				bufferedImage.setRGB(x, y, new Color(rgbImageArray[x][y][0], rgbImageArray[x][y][1], rgbImageArray[x][y][2]).getRGB());
			}
		}
	}
	
	private void gpuBufferToImage(){
		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		FloatBuffer gpuBufferR = rCLBuffer.getBuffer();
		gpuBufferR.rewind();
		FloatBuffer gpuBufferG = gCLBuffer.getBuffer();
		gpuBufferG.rewind();
		FloatBuffer gpuBufferB = bCLBuffer.getBuffer();
		gpuBufferB.rewind();
			for(int x = 0; x < width; x++ ){
				for(int y = 0; y < height; y++ ){
					int r = (int)gpuBufferR.get();
					int g = (int)gpuBufferG.get();
					int b = (int)gpuBufferB.get();
					if(r<0){
						r=0;
					}else if(r>255){
						r=255;
					}
					if(g<0){
						g=0;
					}else if(g>255){
						g=255;
					}
					if(b<0){
						b=0;
					}else if(b>255){
						b=255;
					}
					try{
						Color tempcol = new Color(r, g, b);
						bufferedImage.setRGB(x, y, tempcol.getRGB());

					}catch(IllegalArgumentException illegalArgumentException){
						System.out.println("valls: " + r + " " + g + " " + b);
						illegalArgumentException.printStackTrace();
					}
				}
			}
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getPixelCount(){
		return getHeight() * getWidth();
	}
	
	public synchronized CLBuffer<FloatBuffer> getRedCLBuffer(){
		if(rCLBuffer != null){
			return rCLBuffer;
		}
		if(rgbImageArray != null){
			pixelToRGBBuffer();
			return rCLBuffer;
		}
		if(bufferedImage != null){
			imageToRGBBuffer();
			return rCLBuffer;
		}
		initCLBuffer();
		return rCLBuffer;
	}

	public synchronized CLBuffer<FloatBuffer> getGreenCLBuffer(){
		if(gCLBuffer != null){
			return gCLBuffer;
		}
		if(rgbImageArray != null){
			pixelToRGBBuffer();
			return gCLBuffer;
		}
		if(bufferedImage != null){
			imageToRGBBuffer();
			return gCLBuffer;
		}
		initCLBuffer();
		return gCLBuffer;
	}

	public synchronized CLBuffer<FloatBuffer> getBlueCLBuffer(){
		if(bCLBuffer != null){
			return bCLBuffer;
		}
		if(rgbImageArray != null){
			pixelToRGBBuffer();
			return bCLBuffer;
		}
		if(bufferedImage != null){
			imageToRGBBuffer();
			return bCLBuffer;
		}
		initCLBuffer();
		return bCLBuffer;
	}
	
	private void initCLBuffer(){
		int globalWorkSize = InputComponents.getGlobalWorkSize(getPixelCount());
		rCLBuffer = InputComponents.getCLContext().createFloatBuffer(globalWorkSize, Mem.READ_WRITE);
		gCLBuffer = InputComponents.getCLContext().createFloatBuffer(globalWorkSize, Mem.READ_WRITE);
		bCLBuffer = InputComponents.getCLContext().createFloatBuffer(globalWorkSize, Mem.READ_WRITE);
	}
	
	private void pixelToRGBBuffer(){
		int xMax = getWidth();
		int yMax = getHeight();
		initCLBuffer();
		FloatBuffer rBuffer = rCLBuffer.getBuffer();
		FloatBuffer gBuffer = gCLBuffer.getBuffer();
		FloatBuffer bBuffer = bCLBuffer.getBuffer();
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
		for(int x =0; x < xMax; x++){
			for(int y =0; y < yMax; y++){
				rBuffer.put(rgbImageArray[x][y][0]);
				gBuffer.put(rgbImageArray[x][y][1]);
				bBuffer.put(rgbImageArray[x][y][2]);
			}
		}
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
	}
	
	private void imageToRGBBuffer(){
		initCLBuffer();
		FloatBuffer rBuffer = rCLBuffer.getBuffer();
		FloatBuffer gBuffer = gCLBuffer.getBuffer();
		FloatBuffer bBuffer = bCLBuffer.getBuffer();
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
		int xMax = bufferedImage.getWidth();
		int yMax = bufferedImage.getHeight();
		for(int x = 0; x < xMax; x++ ){
			for(int y = 0; y < yMax; y++ ){
				Color c = new Color(bufferedImage.getRGB(x, y));
				rBuffer.put(c.getRed());
				gBuffer.put(c.getGreen());
				bBuffer.put(c.getBlue());
			}
		}
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
	}
	
	private void removeCLBuffer(){
		if(rCLBuffer != null){
			rCLBuffer.release();
			rCLBuffer = null;
		}
		if(gCLBuffer != null){
			gCLBuffer.release();
			gCLBuffer = null;
		}
		if(bCLBuffer != null){
			bCLBuffer.release();
			bCLBuffer = null;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		removeCLBuffer();
		super.finalize();
	}
	

	@Deprecated
	public void setColor(int x, int y,int red, int green, int blue){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y][0] = red;
		rgbImageArray[x][y][1] = green;
		rgbImageArray[x][y][2] = blue;
		bufferedImage = null;
		removeCLBuffer();
	}
	@Deprecated
	public void setColor(int x, int y, int[] rgb){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y] = rgb;
	}
	@Deprecated
	public void setColor(int x, int y, Color color){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y][0] = color.getRed();
		rgbImageArray[x][y][1] = color.getGreen();
		rgbImageArray[x][y][2] = color.getBlue();
	}
	@Deprecated
	public int[] getRGB(int x, int y){
		int rgbImageArray[][][] = getIntArray();
		int pixel[] = rgbImageArray[x][y];
		if(transValue !=  null && pixel != null){
			if(pixel[0] == transValue[0] && pixel[1] == transValue[1] && pixel[2] == transValue[2]){
				return null;
			}
		}
		return pixel;
	}
	@Deprecated
	public void setRGB(int x, int y, int[] rgb){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y] = rgb;
	}
	
	@Deprecated
	public void addColor(int x, int y, int rgb){
		int rgbImageArray[][][] = getIntArray();
		Color c = new Color(rgb);
		rgbImageArray[x][y][0] += c.getRed();
		rgbImageArray[x][y][1] += c.getBlue();
		rgbImageArray[x][y][2] += c.getGreen();
	}

	@Deprecated
	public void addColor(int x, int y, int[] rgb){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y][0] += rgb[0];
		rgbImageArray[x][y][1] += rgb[1];
		rgbImageArray[x][y][2] += rgb[2];
	}

	@Deprecated
	public void divide(int x, int y, double divisor){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y][0] /= divisor;
		rgbImageArray[x][y][1] /= divisor;
		rgbImageArray[x][y][2] /= divisor;
	}

	@Deprecated
	public void multiply(int x, int y, double multiplier){
		int rgbImageArray[][][] = getIntArray();
		rgbImageArray[x][y][0] *= multiplier;
		rgbImageArray[x][y][1] *= multiplier;
		rgbImageArray[x][y][2] *= multiplier;
	}

	@Deprecated
	public void setRGB(int x, int y, int rgb){
		int rgbImageArray[][][] = getIntArray();
		Color c = new Color(rgb);
		rgbImageArray[x][y][0] = c.getRed();
		rgbImageArray[x][y][1] = c.getBlue();
		rgbImageArray[x][y][2] = c.getGreen();
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

	
	@Deprecated
	public void pixelToBuffer(IntBuffer rBuffer, IntBuffer gBuffer, IntBuffer bBuffer){
		int xMax = getWidth();
		int yMax = getHeight();
		for(int x =0; x < xMax; x++){
			for(int y =0; y < yMax; y++){
				rBuffer.put(rgbImageArray[x][y][0]);
				gBuffer.put(rgbImageArray[x][y][1]);
				bBuffer.put(rgbImageArray[x][y][2]);
			}
		}
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
	}
	
	@Deprecated
	public void bufferToPixel(IntBuffer rBuffer, IntBuffer gBuffer, IntBuffer bBuffer){
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
		int xMax = getWidth();
		int yMax = getHeight();
		for(int x =0; x < xMax; x++){
			for(int y =0; y < yMax; y++){
				rgbImageArray[x][y][0] = rBuffer.get();
				rgbImageArray[x][y][1] = gBuffer.get();
				rgbImageArray[x][y][2] = bBuffer.get();
			}
		}
	}

	@Deprecated
	public void scaleTo(int width, int height){
		int newRgbImageArray[][][] = new int[width][height][];
		float xMul = getWidth()/(float)width;
		float yMul = getHeight()/(float)height;
		int xN = 0;
		int xMax = getIntArray().length;
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
