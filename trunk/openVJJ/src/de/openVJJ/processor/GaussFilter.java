package de.openVJJ.processor;


import org.jdom2.Element;

import de.openVJJ.graphic.VideoFrame;

public class GaussFilter extends ImageProcessor {

	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		return filterFrame(videoFrame);
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}

	int[][] gausMatrix = {{1,2,1},{2,4,2},{1,2,1}};
	private VideoFrame filterFrame(VideoFrame videoFrame){
		int xMax = videoFrame.getWidth();
		int yMax = videoFrame.getHeight();
		int matrixSize = gausMatrix.length;
		int matrixOfset = ((matrixSize - 1) / 2) + 1;
		int gausKomp = 0;
		for(int x = 0; x < matrixSize; x++ ){
			for(int y = 0; y < matrixSize; y++ ){
				gausKomp += gausMatrix[x][y];
			}
		}
		VideoFrame videoFrameRes = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		for(int x = 0; x < xMax; x++ ){
			for(int y = 0; y < yMax; y++ ){
				int[] pointVal = new int[3];
				for(int xm = 0; xm < matrixSize; xm++ ){
					for(int ym = 0; ym < matrixSize; ym++ ){
						int xpm = x + xm - matrixOfset;
						int ypm = y + ym - matrixOfset;
						if(xpm<0){
							break;
						}
						if(ypm<0){
							continue;
						}
						int[] rgb = videoFrame.getRGB(xpm, ypm);
						int matrixVal = gausMatrix[xm][ym];
						pointVal[0] += rgb[0] * matrixVal;
						pointVal[1] += rgb[1] * matrixVal;
						pointVal[2] += rgb[2] * matrixVal;
						
					}
				}
				videoFrameRes.setColor(x, y, pointVal[0] / gausKomp, pointVal[1] / gausKomp, pointVal[2] / gausKomp) ;
			}
		}
		return videoFrameRes;
	}

	@Override
	public void getConfig(Element element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfig(Element element) {
		// TODO Auto-generated method stub
		
	}
}
