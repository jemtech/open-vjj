package de.openVJJ.plugins;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	private double maxVariance = 0.5;

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

	private class EMPoint {
		double[] values;
	}

	private class EMCluster {
		public EMCluster(EMPoint center, int maxSize) {
			this.center = center;
			points = new AsyncList<EMPoint>(maxSize);
		}

		public EMCluster(EMPoint center, int maxSize, Collection<EMPoint> initialData) {
			this.center = center;
			points = new AsyncList<EMPoint>(maxSize, initialData);
		}
		
		AsyncList<EMPoint> points;
		EMPoint center;
	}

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
						updateClusterCenter(cluster);
					}
				}
				System.out.println("opti " + j + " e: " + variance.e + " at: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
			} while (variance.change);
			System.out.println(variance.e + " run " + i);
			for(EMCluster cluster : clusters){
				System.out.print(cluster.points.size() + ": ");
				for(double val : cluster.center.values){
					System.out.print(val + "; ");
				}
				System.out.print("\n");
			}
			if (variance.e > (maxVariance * maxVariance)) { //because we don't use square root
				clusters.add(new EMCluster(variance.wrongest, values.size()));
			} else {
				return clusters;
			}
		}
	}

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

	private class UpdateClusterResult {
		EMPoint wrongest;
		double e;
		boolean change = false;
	}

//	private class Change {
//		public Change(EMPoint point, EMCluster from, EMCluster to) {
//			this.point = point;
//			this.from = from;
//			this.to = to;
//		}
//
//		EMPoint point;
//		EMCluster from;
//		EMCluster to;
//	}

	private UpdateClusterResult updateClusters(List<EMCluster> clusters) {
		UpdateClusterResult result = new UpdateClusterResult();
//		List<Change> changes = new ArrayList<>();
		ExecutorService updateService = Executors.newFixedThreadPool(clusters.size());
		List<UpdateClusterPoints> clusterUpdaters = new ArrayList<>(clusters.size());
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
//			changes.addAll(clusterUpdater.changes);
			if(result.e < clusterUpdater.result.e){
				result.e = clusterUpdater.result.e;
				result.wrongest = clusterUpdater.result.wrongest;
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
	
	private class UpdateClusterPoints implements Runnable{
		
		EMCluster pointCluster;
		List<EMCluster> clusters;
		UpdateClusterResult result = new UpdateClusterResult();
//		List<Change> changes = new ArrayList<>();
		
		public UpdateClusterPoints(EMCluster pointCluster, List<EMCluster> clusters){
			this.pointCluster = pointCluster;
			this.clusters = clusters;
		}

		@Override
		public void run() {
			ExecutorService updateService = Executors.newFixedThreadPool(4);
			List<UpdatePointsCluster> piontUpdaters = new ArrayList<>(pointCluster.points.size());
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

				if (result.e < pointUpdater.pointE) {
					result.e = pointUpdater.pointE;
					result.wrongest = pointUpdater.point;
				}
			}
		}
		
	}
	
	private class UpdatePointsCluster implements Runnable{

		EMPoint point;
		List<EMCluster> clusters;
		double pointE = Double.MAX_VALUE;
		EMCluster newCluster = null;
		int actualIndex;
		EMCluster oldCluster;
		
		public UpdatePointsCluster(EMPoint point, List<EMCluster> clusters, int actualIndex, EMCluster oldCluster){
			this.point = point;
			this.clusters = clusters;
			this.actualIndex = actualIndex;
			this.oldCluster = oldCluster;
		}

		@Override
		public void run() {
			for (EMCluster cluster : clusters) {
				double e = diffnorm2(cluster.center, point);
				if (e < pointE) {
					newCluster = cluster;
					pointE = e;
				}
			}

			if (newCluster != oldCluster) {
				oldCluster.points.delete(actualIndex);
				newCluster.points.add(point);
//				changes.add(new Change(pointUpdater.point, pointCluster, pointUpdater.newCluster));
			}
		}
	}

	double diffnorm2(EMPoint point1, EMPoint point2) {
		double r = 0.0;
		for (int i = 0; i < point1.values.length; i++) {
			double diff = point1.values[i] - point2.values[i];
			r += diff * diff;
		}
		//return Math.sqrt(r); we don't need to because values are relative
		return r;
	}

	private void updateClusterCenter(EMCluster cluster) {
		if(cluster.points.size() < 1) return;
		EMPoint center = new EMPoint();
		center.values = new double[cluster.points.get(0).values.length];
		for (int i = 0; i < cluster.points.size(); i++) {
			EMPoint point = cluster.points.get(i);
			for (int k = 0; k < center.values.length; k++) {
				center.values[k] += point.values[k];
			}
		}
		double count = cluster.points.size();
		for (int i = 0; i < center.values.length; i++) {
			center.values[i] = center.values[i] / count;
		}
		cluster.center = center;
	}
	
	public static void main(String[] args){
		long start = System.currentTimeMillis();
		System.out.println("start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		EMClustering clustering = new EMClustering();
		List<EMPoint> values = new ArrayList<EMPoint>(800*600);
		for(int i = 0; i < 800*600; i++){
			EMPoint point = clustering.new EMPoint();
			point.values = new double[5];
			for(int k = 0; k < point.values.length; k++){
				point.values[k] = Math.random();
			}
			values.add(point);
		}
		System.out.println("values generatet: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		System.out.println("start with: " + values.size());
		System.out.println(clustering.calculate(values).size());
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("finish: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	private class AsyncList<T extends Object>{
		private int maxSize;
		private T[] data;
		private int dataCount = 0;
		private int[] toDelete;
		private int toDeleteCount = 0;
		private T[] toAdd;
		private int toAddCount = 0;
		
		public AsyncList(int maxSize){
			this.maxSize = maxSize;
			initArrays();
		}
		
		public AsyncList(int maxSize, Collection<T> initialData){
			this.maxSize = maxSize;
			initArrays(initialData.toArray());
		}
		
		@SuppressWarnings("unchecked")
		private void initArrays(){
			this.data = null;
			this.toDelete = null;
			this.toAdd = (T[]) new Object[maxSize];
		}
		

		@SuppressWarnings("unchecked")
		private void initArrays(Object[] initialData){
			this.data = (T[]) initialData;
			this.dataCount = initialData.length;
			this.toDelete = new int[initialData.length];
			this.toAdd = (T[]) new Object[maxSize - initialData.length];
		}
		
		public T get(int index){
			return data[index];
		}
		
		public void add(T toAdd){
			this.toAdd[toAddCount] = toAdd;
			toAddCount++;
		}
		
		public void delete(int indexToDelete){
			toDelete[toDeleteCount] = indexToDelete;
			toDeleteCount++;
		}
		
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
		
		public int size(){
			return dataCount;
		}
		
		public int changes(){
			return toDeleteCount + toAddCount;
		}
	}

}
