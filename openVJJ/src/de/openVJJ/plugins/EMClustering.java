package de.openVJJ.plugins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.openVJJ.basic.AsyncList;
import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.ArtNetPacketValue;
import de.openVJJ.values.DataPoint;
import de.openVJJ.values.ValueList;
/**
 * 
 * Copyright (C) 2015 Jan-Erik Matthies
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
public class EMClustering extends Plugin {
	
	private int initialClusterCount = 2;
	private double maxVariance = 0.2;

	public EMClustering(){
		addInput("Data", ValueList.class);
		addOutput("Cluster", ValueList.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Data".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					ValueList<Value> data = (ValueList<Value>) value;
					List<EMPoint> points = new ArrayList<EMPoint>();
					for(Value de : data){
						DataPoint<float[]> point = (DataPoint<float[]>) de;
						EMPoint myPoint = new EMPoint();
						myPoint.identifier = point.getXPosition();
						myPoint.values = point.getData();
					}
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}

	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * This class holds the Data for calculation
	 * @author Jan-Erik Matthies
	 *
	 */
	private class EMPoint {
		float[] values;
		double e = Double.MAX_VALUE;
		Object identifier;
	}

	/**
	 * This class contains the matching data points
	 * @author JEMTech
	 *
	 */
	private class EMCluster {
		AsyncList<EMPoint> points;
		EMPoint center;
		double saveDistance;
		double moved;
		/**
		 * constructor for an empty cluster
		 * @param center the initial cluster center
		 * @param maxSize the count of all data-points
		 */
		public EMCluster(EMPoint center, int maxSize) {
			this.center = center;
			points = new AsyncList<EMPoint>(maxSize);
		}

		/**
		 * constructor with initial data-points
		 * @param center  the initial cluster center
		 * @param maxSize the count of all data-points
		 * @param initialData the initial cluster points
		 */
		public EMCluster(EMPoint center, int maxSize, Collection<EMPoint> initialData) {
			this.center = center;
			points = new AsyncList<EMPoint>(maxSize, initialData);
		}
		
		/**
		 * updates the save distance to the other clusters
		 * @param allClusters
		 */
		public void updateSaveDistance(Collection<EMCluster> allClusters){
			saveDistance = Double.MAX_VALUE;
			for(EMCluster cluster : allClusters){
				if(cluster == this){
					continue;
				}
				double distance = diffnorm2(cluster.center, center);
				if(distance < saveDistance){
					saveDistance = distance;
				}
			}
			saveDistance = saveDistance/2;
		}
		
		/**
		 * calculates the center of the given cluster
		 * @param cluster
		 */
		private void updateClusterCenter() {
			if(points.size() < 1) return;
			EMPoint center = new EMPoint();
			center.values = new float[points.get(0).values.length];
			for (int i = 0; i < points.size(); i++) {
				EMPoint point = points.get(i);
				for (int k = 0; k < center.values.length; k++) {
					center.values[k] += point.values[k];
				}
			}
			float count = points.size();
			for (int i = 0; i < center.values.length; i++) {
				center.values[i] = center.values[i] / count;
			}
			moved = diffnorm2(center, this.center);
			this.center = center;
		}
	}

	/**
	 * runs the optimization algorithm with the given values and returns the resulting clusters
	 * @param values the values to cluster
	 * @return the resulting clusters
	 */
	private List<EMCluster> calculate(List<EMPoint> values) {
		List<EMCluster> clusters = initClusters(values);
		System.out.println("init finish. Start with " + clusters.size() + "Clusters");
		int i =0;
		while (true) {
			i++;
			UpdateClusterResult variance;
			int j=0;
			do {
				j++;
				variance = updateClusters(clusters);
				if (variance.e > 0) {
					for (EMCluster cluster : clusters) {
						cluster.updateClusterCenter();
					}
					for (EMCluster cluster : clusters) {
						cluster.updateSaveDistance(clusters);
					}
				}
				System.out.println("opti " + j + " e: " + variance.e + " in C with save " + (variance.wrongestCluster != null ? variance.wrongestCluster.saveDistance : "") + " at: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));

			} while (variance.change);
			System.out.println(variance.e + " run " + i);
//			for(EMCluster cluster : clusters){
//				System.out.print(cluster.points.size() + ": ");
//				for(double val : cluster.center.values){
//					System.out.print(val + "; ");
//				}
//				System.out.print("\n");
//			}
			if (variance.e > (maxVariance * maxVariance)) { //because we don't use square root
				EMCluster newCluster = new EMCluster(variance.wrongest, values.size());
				clusters.add(newCluster);
				variance.wrongest.e = 0;
				variance.wrongestCluster.points.delete(variance.wrongestActualIndex);
				variance.wrongestCluster.points.execute();
				newCluster.points.fastAdd(variance.wrongest);
				newCluster.points.execute();
			} else {
				return clusters;
			}
		}
	}

	/**
	 * Initializes the initial clusters with the given data
	 * @param values the values to calculate
	 * @return the initial clusters
	 */
	private List<EMCluster> initClusters(List<EMPoint> values) {
		List<EMCluster> clusters = new ArrayList<EMCluster>();

		int pointValueCount = values.get(0).values.length;
		for (int i = 0; i < initialClusterCount; i++) {
			EMCluster cluster;
			float pointVal;
			if (i == 0) {
				pointVal = 0;
				cluster = new EMCluster(new EMPoint(), values.size(), values);
			} else {
				cluster = new EMCluster(new EMPoint(), values.size());
				pointVal = i / (initialClusterCount - 1);
			}
			clusters.add(cluster);
			cluster.center.values = new float[pointValueCount];
			for (int v = 0; v < pointValueCount; v++) {
				cluster.center.values[v] = pointVal;
			}
		}
		return clusters;
	}

	/**
	 * The result of a cluster update
	 * @author Jan-Erik Matthies
	 *
	 */
	private class UpdateClusterResult {
		EMPoint wrongest;
		EMCluster wrongestCluster;
		int wrongestActualIndex;
		double e;
		boolean change = false;
	}

	/**
	 * sorts the points to the nearest cluster
	 * @param clusters
	 * @return update informations
	 */
	private UpdateClusterResult updateClusters(List<EMCluster> clusters) {
		UpdateClusterResult result = new UpdateClusterResult();
		ExecutorService updateService = Executors.newFixedThreadPool(clusters.size());
		List<UpdateClusterPoints> clusterUpdaters = new ArrayList<UpdateClusterPoints>(clusters.size());
		for (EMCluster pointCluster : clusters) {
			//paralell
			UpdateClusterPoints clusterUpdater = new UpdateClusterPoints(pointCluster, clusters);
			clusterUpdaters.add(clusterUpdater);
			updateService.execute(clusterUpdater);
		}
		try {
			updateService.shutdown();
			if(!updateService.awaitTermination(1, TimeUnit.HOURS)){
				System.err.println("update takes longer than 1 hour!");
				return null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(UpdateClusterPoints clusterUpdater : clusterUpdaters){
			if(result.e < clusterUpdater.result.e){
				result = clusterUpdater.result;
			}
		}
//		System.out.println("start doing " + changes.size() + " changes at " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
//		result.change = !changes.isEmpty();
//		for (Change change : changes) {
//			change.from.points.remove(change.point);
//			change.to.points.add(change.point);
//		}
		System.out.println("start executing changes " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		for(EMCluster cluster : clusters){
			if(cluster.points.changes() > 0){
				cluster.points.execute();
				result.change = true;
			}
		}
		
		System.out.println("finish changes at " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		return result;
	}
	
	/**
	 * updates the points of the given cluster
	 * @author Jan-Erik Matthies
	 *
	 */
	private class UpdateClusterPoints implements Runnable{
		
		EMCluster pointCluster;
		List<EMCluster> clusters;
		UpdateClusterResult result = new UpdateClusterResult();
		
		/**
		 * constructor 
		 * @param pointCluster the cluster to update
		 * @param clusters all clusters
		 */
		public UpdateClusterPoints(EMCluster pointCluster, List<EMCluster> clusters){
			this.pointCluster = pointCluster;
			this.clusters = clusters;
		}

		/**
		 * runs the optimization
		 */
		@Override
		public void run() {
			ExecutorService updateService = Executors.newFixedThreadPool(4);
			List<UpdatePointsCluster> piontUpdaters = new ArrayList<UpdatePointsCluster>(pointCluster.points.size());
			for(int i = 0; i < pointCluster.points.size(); i++){
				EMPoint point = pointCluster.points.get(i);
				UpdatePointsCluster pointUpdater = new UpdatePointsCluster(point, clusters, i, pointCluster);
				piontUpdaters.add(pointUpdater);
				updateService.execute(pointUpdater);
			}

			try {
				updateService.shutdown();
				if(!updateService.awaitTermination(1, TimeUnit.HOURS)){
					System.err.println("update takes longer than 1 hour!");
					return;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(UpdatePointsCluster pointUpdater : piontUpdaters){

				if (result.e < pointUpdater.point.e) {
					result.e = pointUpdater.point.e;
					result.wrongest = pointUpdater.point;
					result.wrongestActualIndex = pointUpdater.actualIndex;
					result.wrongestCluster = pointCluster;
				}
			}
		}
		
	}
	
	/**
	 * add the point to the nearest Cluster
	 * @author Jan-Erik Matthies
	 *
	 */
	private class UpdatePointsCluster implements Runnable{

		EMPoint point;
		List<EMCluster> clusters;
		EMCluster newCluster = null;
		int actualIndex;
		EMCluster oldCluster;
		
		/**
		 * constructor
		 * @param point the point to optimize
		 * @param clusters all clusters
		 * @param actualIndex actual index at its cluster
		 * @param oldCluster points actual cluster
		 */
		public UpdatePointsCluster(EMPoint point, List<EMCluster> clusters, int actualIndex, EMCluster oldCluster){
			this.point = point;
			this.clusters = clusters;
			this.actualIndex = actualIndex;
			this.oldCluster = oldCluster;
		}

		/**
		 * runs the optimization
		 */
		@Override
		public void run() {
			newCluster = oldCluster;
			point.e = point.e - oldCluster.moved;
			if(point.e < oldCluster.saveDistance){
				//there can not be a better cluster
				return;
			}
			point.e = diffnorm2(oldCluster.center, point);
			if(oldCluster.saveDistance < point.e){
				for (EMCluster cluster : clusters) {
					if(cluster == oldCluster) continue;
					double e = diffnorm2(cluster.center, point);
					if (e < point.e) {
						newCluster = cluster;
						point.e = e;
					}
				}
				if (newCluster != oldCluster) {
					oldCluster.points.delete(actualIndex);
					newCluster.points.fastAdd(point);
				}
			}

		}
	}

	/**
	 * calculates the distance between two points
	 * @param point1
	 * @param point2
	 * @return the distence
	 */
	double diffnorm2(EMPoint point1, EMPoint point2) {
		double r = 0.0;
		for (int i = 0; i < point1.values.length; i++) {
			double diff = point1.values[i] - point2.values[i];
			r += diff * diff;
		}
		//return Math.sqrt(r); we don't need to because values are relative
		return r;
	}
	
		

	private static class Position{
		int x;
		int y;
	}
	/**
	 * Just for testing
	 * @param args
	 */
	public static void main(String[] args){
		long start = System.currentTimeMillis();
		System.out.println("start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		EMClustering clustering = new EMClustering();
		
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("/home/janerik/Desktop/test.jpeg"));
		} catch (IOException e) {
		}
		int pixelCount = img.getHeight() * img.getWidth();

		List<EMPoint> values = new ArrayList<EMPoint>(pixelCount);
		for(int i = 0; i < img.getHeight(); i++){
			for(int k = 0; k < img.getWidth(); k++ ){
				Position position = new Position();
				position.x = k;
				position.y = i;
				
				EMPoint point = clustering.new EMPoint();
				
				point.identifier = position;
				
				point.values = new float[4];
				
				int color = img.getRGB(k, i);
				float r = ((color & 0x00ff0000) >> 16) / 255f; //red
				float g = ((color & 0x0000ff00) >> 8) / 255f; // green
				float b = (color & 0x000000ff) / 255f; //blue
//				point.values[3] = (color>>24) & 0xff; //alpha

//				point.values[0] = r;
//				point.values[1] = g;
//				point.values[2] = b;
				
				
				//calculate intense
				float intenseMax;
				float intenseD;
				float col;
				if(r > g && r > b){
					intenseMax = r;
					point.values[0] = 1;
					if(g < b){
						intenseD = intenseMax - g;
						point.values[2] = (intenseD != 0d ? (b - g) / intenseD : 0);
					}else{
						intenseD = intenseMax - b;
						point.values[1] = (intenseD != 0d ? (g - b) / intenseD : 0);
					}
					col = 1f + (intenseD != 0d ? (g - b) / intenseD : 0);
					point.values[3] = intenseD;
				}else if(g > b){
					intenseMax = g;
					point.values[1] = 1;
					if(r < b){
						intenseD = intenseMax - r;
						point.values[2] = (intenseD != 0d ? (b - r) / intenseD : 0);
					}else{
						intenseD = intenseMax - b;
						point.values[0] = (intenseD != 0d ? (r - b) / intenseD : 0);
					}
					col = 3f + (intenseD != 0d ? (b - r) / intenseD : 0);
					point.values[3] = intenseD;
				}else{
					intenseMax = b;
					point.values[2] = 1;
					if(g < r){
						intenseD = intenseMax - g;
						point.values[0] = (intenseD != 0d ? (r - g) / intenseD : 0);
					}else{
						intenseD = intenseMax - r;
						point.values[1] = (intenseD != 0d ? (g - r) / intenseD : 0);
					}
					col = 5f + (intenseD != 0d ? (r - g) / intenseD : 0);
					point.values[3] = intenseD;
				}
//				point.values[0] = col / 6d;
//				point.values[1] = intenseD/intenseMax;
//				point.values[2] = intenseMax;
//				point.values[1] = 0;
//				point.values[2] = 0;
				
				
				values.add(point);
			}
		}
		for(EMPoint point : values){
			int r = (int) (point.values[0] * 255d * point.values[3]);
			int g = (int) (point.values[1] * 255d * point.values[3]);
			int b = (int) (point.values[2] * 255d * point.values[3]);
			int a = 255;
			int color = (a << 24) | (r << 16) | (g << 8) | b;
			img.setRGB(((Position)point.identifier).x ,((Position)point.identifier).y, color);
		}
		

		JFrame frameo = new JFrame();
		frameo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameo.setVisible(true);
        
        JLabel picLabelo = new JLabel(new ImageIcon(img));
        frameo.add(picLabelo);
        frameo.setBounds(20, 20, img.getWidth(), img.getHeight());
		
		System.out.println("values generatet: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		System.out.println("start with: " + values.size());
		List<EMCluster> clusters = clustering.calculate(values);
		System.out.println(clusters.size());
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("finish: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		for(EMCluster cluster : clusters){
			int r = (int) (Math.random() * 255); //(int) (cluster.center.values[0] * 255d * cluster.center.values[3]);
			int g = (int) (Math.random() * 255); //(int) (cluster.center.values[1] * 255d * cluster.center.values[3]);
			int b = (int) (Math.random() * 255); //(int) (cluster.center.values[2] * 255d * cluster.center.values[3]);
			System.out.println(r + " " + g + " " + b);
			int a = 255;
			int color = (a << 24) | (r << 16) | (g << 8) | b;
			for(int i = 0; i < cluster.points.size(); i++){
				EMPoint point = cluster.points.get(i);
				img.setRGB(((Position)point.identifier).x ,((Position)point.identifier).y, color);
			}
		}

		JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        JLabel picLabel = new JLabel(new ImageIcon(img));
        frame.add(picLabel);
        frame.setBounds(20, 20, img.getWidth(), img.getHeight());
	}

}
