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

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Plugin;
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
	private double maxVariance = 0.3;

	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		// TODO Auto-generated method stub
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
		double[] values;
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
			center.values = new double[points.get(0).values.length];
			for (int i = 0; i < points.size(); i++) {
				EMPoint point = points.get(i);
				for (int k = 0; k < center.values.length; k++) {
					center.values[k] += point.values[k];
				}
			}
			double count = points.size();
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
				System.out.println("opti " + j + " e: " + variance.e + " at: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
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
				variance.wrongestCluster.points.delete(variance.wrongestActualIndex);
				variance.wrongestCluster.points.execute();
				newCluster.points.add(variance.wrongest);
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
			double pointVal;
			if (i == 0) {
				pointVal = 0;
				cluster = new EMCluster(new EMPoint(), values.size(), values);
			} else {
				cluster = new EMCluster(new EMPoint(), values.size());
				pointVal = i / (initialClusterCount - 1);
			}
			clusters.add(cluster);
			cluster.center.values = new double[pointValueCount];
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
					newCluster.points.add(point);
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
	
	/**
	 * a extreme fast list but unsafe
	 * @author Jan-Erik Matthies
	 *
	 * @param <T>
	 */
	private class AsyncList<T extends Object>{
		private int maxSize;
		private T[] data;
		private int dataCount = 0;
		private int[] toDelete;
		private int toDeleteCount = 0;
		private T[] toAdd;
		private int toAddCount = 0;

		/**
		 * constructor
		 * @param maxSize maximum amount of elements
		 */
		public AsyncList(int maxSize){
			this.maxSize = maxSize;
			initArrays();
		}
		
		/**
		 * constructor
		 * @param maxSize maximum amount of elements
		 * @param initialData the initial data to add
		 */
		public AsyncList(int maxSize, Collection<T> initialData){
			this.maxSize = maxSize;
			initArrays(initialData.toArray());
		}
		
		/**
		 * Initializes the the lists
		 */
		@SuppressWarnings("unchecked")
		private void initArrays(){
			this.data = null;
			this.toDelete = null;
			this.toAdd = (T[]) new Object[maxSize];
		}
		

		/**
		 * Initializes the the lists and adds initial data
		 * @param initialData data to add
		 */
		@SuppressWarnings("unchecked")
		private void initArrays(Object[] initialData){
			this.data = (T[]) initialData;
			this.dataCount = initialData.length;
			this.toDelete = new int[initialData.length];
			this.toAdd = (T[]) new Object[maxSize - initialData.length];
		}
		
		/**
		 * returns the element at the given index
		 * @param index of the element
		 * @return the selected element
		 */
		public T get(int index){
			return data[index];
		}
		
		/**
		 * adds an element to the end of the list
		 * @param toAdd the element to add
		 */
		synchronized public void add(T toAdd){
			this.toAdd[toAddCount] = toAdd;
			toAddCount++;
		}
		
		/**
		 * deletes the element at the given index
		 * @param indexToDelete index of the element to delete
		 */
		synchronized public void delete(int indexToDelete){
			toDelete[toDeleteCount] = indexToDelete;
			toDeleteCount++;
		}
		
		/**
		 * executes the deletes and removes
		 */
		public void execute(){
			//Deleting
			for(int i = 0; i < toDeleteCount; i++){
				data[toDelete[i]] = null;
			}
			//coppy to new
			@SuppressWarnings("unchecked")
			T[] dataNew = (T[]) new Object[dataCount + toAddCount - toDeleteCount];
			int dataCountNew = 0;
			for(int i = 0; i < dataCount; i++){
				if(data[i] != null){
					dataNew[dataCountNew] = data[i];
					dataCountNew++;
				}
			}
			//append new
			data = dataNew;
			dataCount = dataCountNew;
			for(int i = 0; i < toAddCount; i++){
				data[dataCount] = toAdd[i];
				dataCount++;
			}
			//reset temps
			toDelete = new int[dataCount];
			toDeleteCount = 0;
			@SuppressWarnings("unchecked")
			T[] toAddNew = (T[]) new Object[maxSize - dataCountNew];
			toAdd = toAddNew;
			toAddCount = 0;
		}
		
		/**
		 * @return the size of the list
		 */
		public int size(){
			return dataCount;
		}
		
		/**
		 * 
		 * @return the amount of changes to execute
		 */
		public int changes(){
			return toDeleteCount + toAddCount;
		}
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
				
				point.values = new double[3];
				
				int color = img.getRGB(k, i);
				point.values[0] = ((color & 0x00ff0000) >> 16) / (double) 255;
				point.values[1] = ((color & 0x0000ff00) >> 8) / (double) 255;
				point.values[2] = (color & 0x000000ff) / (double) 255;
//				int alpha = (color>>24) & 0xff;
				

				values.add(point);
			}
		}
		System.out.println("values generatet: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		System.out.println("start with: " + values.size());
		List<EMCluster> clusters = clustering.calculate(values);
		System.out.println(clusters.size());
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("finish: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		for(EMCluster cluster : clusters){
			int r = (int) (cluster.center.values[0] * 255);
			int g = (int) (cluster.center.values[1] * 255);
			int b = (int) (cluster.center.values[2] * 255);
			int a = 0;
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
