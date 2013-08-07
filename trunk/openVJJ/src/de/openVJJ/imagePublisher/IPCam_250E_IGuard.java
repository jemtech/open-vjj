package de.openVJJ.imagePublisher;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.openVJJ.ImageListener.ImageListener;
import de.openVJJ.graphic.VideoFrame;

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

public class IPCam_250E_IGuard extends ImagePublisher implements Runnable{
	public static final int DEFAULT_CAM_PORT = 9001;
	static final boolean DEBUG = false;
	static final boolean ERROROUT = true;
	static final int INIT_TIME_OUT = 1000;
	static final int RUNN_TIME_OUT = 250;
	/**
	 * @param args
	 */
	String networkAddress;
	int port;
	public IPCam_250E_IGuard(String networkAddress, int port){
		this.networkAddress = networkAddress;
		this.port = port;
	}
	
	public IPCam_250E_IGuard(){
		this.networkAddress = "000.000.000.000";
		this.port = DEFAULT_CAM_PORT;
	}
	
	private Thread receivingThread = null;
	private void startReciving(){
		if(receivingThread == null){
			receivingThread =  new Thread(this);
			receivingThread.start();
		}
	}
	private void shutdown(){
		receiveImages = false;
		int waitingForShutdown = 0;
		while (running && waitingForShutdown < 100){
			try {
				Thread.sleep(100);
				waitingForShutdown += 1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean running = false;
	@Override
	public void run() {
		running = true;
		DatagramSocket dSocket = null;
		boolean notConnected = true;
		int tryCount = 0;
		while(notConnected && tryCount<5){
			tryCount ++;
			try {
				dSocket = new DatagramSocket();
				dSocket.setSoTimeout(INIT_TIME_OUT);
				byte[] bytePaket = headerPaket();
				sendAndReceive(dSocket, bytePaket);
				sendAndReceive(dSocket, new byte[]{0x38, 0x39, 0x36, 0x32, 0x30, 0x32});
				sendAndReceive(dSocket, new byte[]{0x38, 0x39, 0x36, 0x32, 0x30, 0x34});
				sendAndReceive(dSocket, new byte[]{0x38, 0x39, 0x36, 0x32, 0x30, 0x36});
				sendAndReceive(dSocket, new byte[]{0x38, 0x39, 0x36, 0x32, 0x30, 0x38});
				sendAndReceive(dSocket, new byte[]{0x39, 0x32, 0x35, 0x30, 0x30, 0x32});
				sendAndReceive(dSocket, new byte[]{0x38, 0x39, 0x35, 0x31, 0x30, 0x32});
				dSocket.setSoTimeout(RUNN_TIME_OUT);
				notConnected=false;
			} catch (Exception e) {
				if(ERROROUT)System.err.println("try nr.:" + tryCount);
				if(ERROROUT)e.printStackTrace();
				if(dSocket != null){
					dSocket.close();
				}
				dSocket = null;
			}
		}
			
			while (receiveImages && dSocket != null && dSocket.isBound()){
				try {
					if((getImageListener() == null) || getImageListener().isEmpty()){
						if(DEBUG)System.out.println("No ImageListener shutting down.");
						receiveImages = false;
						if(dSocket != null){
							dSocket.close();
						}
						dSocket = null;
						running = false;
						return;
					}
					if(DEBUG)System.out.println(getImageListener().size());
					byte[] newImage = getNextImage(dSocket);
					if(!validateJPEGData(newImage)){
						continue;
					}
					Image recievedImage = Toolkit.getDefaultToolkit().createImage(newImage);
					publishImage(new VideoFrame(recievedImage));
				} catch (Exception e) {
					if(ERROROUT)e.printStackTrace();
				}
			}
			if(dSocket != null){
				dSocket.close();
			}
			dSocket = null;
			running = false;
			return;
	}
	
	private boolean validateJPEGData(byte[] image){
		if(image == null){
			if(ERROROUT)System.err.println("JPEG data null.");
			return false;
		}
		if(!((image[0] == -1) && (image[1] == -40))){
			if(ERROROUT)System.err.println("JPEG head data currupted: " + image[0] + "," + image[1]);
			return false;
		}
		int imageLength = image.length;
		if(!((image[imageLength-2] == -1) && (image[imageLength-1] == -39))){
			if(ERROROUT)System.err.println("JPEG end data currupted." + image[imageLength-2] + "," + image[imageLength-1]);
			return false;
		}
		return true;
	}
	
	private boolean receiveImages = true;
	@Override
	public synchronized void addListener(ImageListener imageListener) {
		super.addListener(imageListener);
		startReciving();
	}
	
	private byte[] headerPaket(){
		ArrayList<Byte> byteList = new ArrayList<Byte>();
		byte nullByte = (byte) 0x00;
		String partString = "20432101admin";
		byte[] partByts = partString.getBytes();
		for(int i = 0; i < partByts.length; i++){
			byteList.add(partByts[i]);
		}
		for(int i = 0; i<27; i++ ){
			byteList.add(nullByte);
		}
		partString = "admin";
		partByts = partString.getBytes();
		for(int i = 0; i < partByts.length; i++){
			byteList.add(partByts[i]);
		}
		for(int i = 0; i<9; i++ ){
			byteList.add(nullByte);
		}
		byte[] returnBytes = new byte[byteList.size()];
		for(int i = 0; i<byteList.size(); i++ ){
			returnBytes[i] = byteList.get(i);
		}
		if(DEBUG)System.out.println();
		return returnBytes;
	}
	
	private byte[] sendAndReceive(DatagramSocket dSocket, byte[] sendBytePaket) throws IOException{
		if(DEBUG)System.out.print("send " + sendBytePaket.length + ": ");
		if(DEBUG)for(int i = 0; i < sendBytePaket.length; i++){
	    	if(sendBytePaket[i]!=0){
	    		System.out.print((char)sendBytePaket[i]);
			}else{
				System.out.print("_");
			}
	    }
	    if(DEBUG)System.out.println();
		DatagramPacket paket = new DatagramPacket(sendBytePaket, sendBytePaket.length, new InetSocketAddress(networkAddress, port));
		dSocket.send(paket);
		byte[] receiveBytePaket = new byte[1022];
		paket = new DatagramPacket(receiveBytePaket, receiveBytePaket.length);
	    dSocket.receive(paket);
	    byte[] paketData = paket.getData();
	    return paketData;
	}
	
	private void sendSting(String toSend, DatagramSocket dSocket) throws IOException{
		byte[] sendBytePaket = toSend.getBytes();
		if(DEBUG)System.out.print("send " + sendBytePaket.length + ": ");
		if(DEBUG)for(int i = 0; i < sendBytePaket.length; i++){
		    	if(sendBytePaket[i]!=0){
					System.out.print((char)sendBytePaket[i]);
				}else{
					System.out.print("_");
				}
		    }
		if(DEBUG)System.out.println();
		    DatagramPacket paket = new DatagramPacket(sendBytePaket, sendBytePaket.length, new InetSocketAddress(networkAddress, port));
			dSocket.send(paket);
	}
	
	private byte[] receiveBytes(DatagramSocket dSocket) throws IOException{
		byte[] reciveBytePaket = new byte[1022];
		DatagramPacket paket = new DatagramPacket(reciveBytePaket, reciveBytePaket.length);
	    dSocket.receive(paket);
	    byte[] paketData = paket.getData();
	    if(DEBUG)System.out.println("recive info: " + paket.getLength() +"byte");
	    byte[] dataOut = getArrayPart(0, paket.getLength()-1, paketData);
	    return dataOut;
	}
	
	private int zyklus = 12;
	private byte[] getNextImage(DatagramSocket dSocket) throws IOException{
		String nextImage = zyklus + "3003";
		sendSting(nextImage, dSocket);
		byte[] allData = null;
		int totalSize =0;
		while(true){
			byte[] bytes= receiveBytes(dSocket);
			//String idString = new String(bytes, 0, 2);
			String offsetString = new String(bytes, 2, 6);
			byte[] rest = getArrayPart(8, bytes.length-1, bytes);
			int offset = Integer.valueOf(offsetString);
			//int id = Integer.valueOf(idString);
			byte[] dataArray;
			if(offset==0){
				String sizeString = new String(rest, 0, 8);
				//String datetimeString = new String(rest, 8, 8);
				//String moreString = new String(rest, 16, 13);
				totalSize = Integer.valueOf(sizeString);
				dataArray = getArrayPart(29, rest.length-1, rest);
				allData = dataArray;
			}else{
				dataArray = rest;
				allData = appendToArray(allData, rest);
			}
			totalSize -= dataArray.length;
			if(DEBUG)System.out.println("offset:" + offset + " remain: " + totalSize + " actuell Data:" + allData.length);
			if (offset != 0 && totalSize <= 0){
	            if (zyklus == 99){
	            	zyklus = 11;
	            }
	            else{
	            	zyklus += 1;
	            }
	            return allData;
			}
		}
	}
	
	private byte[] getArrayPart(int start, int end, byte[] array){
		if(start >= array.length || end >= array.length || start > end){
			return null;
		}
		byte[] part = new byte[end-start+1];
		for(int i = 0; i< part.length; i++){
			part[i] = array[i+start];
		}
		return part;
		
	}
	
	private byte[] appendToArray(byte[] firstPart, byte[] endPart){
		byte[] allData = new byte[firstPart.length + endPart.length];
		for(int i = 0; i< allData.length; i++){
			if(i<firstPart.length){
				allData[i] = firstPart[i];
			}
			else{
				allData[i] = endPart[i-firstPart.length];
			}
		}
		return allData;
		
	}
	
	@Override
	public void remove() {
		receiveImages = false;
		shutdownListener();
		
	}

	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("IP and Port config");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel ipLabel = new JLabel("IP-Adress");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(ipLabel, gridBagConstraints);
		
		final JTextField ipJTextField = new JTextField(networkAddress, 10);
		gridBagConstraints.gridx = 1;
		controllerFrame.add(ipJTextField, gridBagConstraints);
		
		JLabel portLabel = new JLabel("Port");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(portLabel, gridBagConstraints);
		
		final JTextField portJTextField = new JTextField(String.valueOf(port), 10);
		gridBagConstraints.gridx = 1;
		controllerFrame.add(portJTextField, gridBagConstraints);

		JButton saveButton = new JButton("Set");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				networkAddress = ipJTextField.getText();
				port = Integer.parseInt(portJTextField.getText());
				controllerFrame.setVisible(false);
				controllerFrame.dispose();
				controllerFrame = null;
				if(running){
					shutdown();
					startReciving();
				}
			}
		});
		controllerFrame.add(saveButton, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}
	

}
