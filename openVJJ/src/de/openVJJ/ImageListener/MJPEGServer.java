package de.openVJJ.ImageListener;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

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

public class MJPEGServer implements ImageListener{


	private List<HTTPTransmitter> clientList;
	private HTTPServer server;
	private Thread serverThread;
	
	public MJPEGServer(){
		server = new HTTPServer();
		serverThread = new Thread(server);
		serverThread.start();
	}
	
	private void addClient(HTTPTransmitter client){
		if(clientList == null){
			clientList = new ArrayList<HTTPTransmitter>();
		}
		clientList.add(client);
	}
	
	private void removeClient(HTTPTransmitter client){
		synchronized (clientList) {
			clientList.remove(client);
		}
	}
	
	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {
		if(server == null){
			server = new HTTPServer();
		}
		controllerFrame = new JFrame();
		controllerFrame.setTitle("Gamma RGB");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel portLabel = new JLabel("Port");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(portLabel, gridBagConstraints);
		
		final JTextField portJTextField = new JTextField(String.valueOf(server.port), 10);
		gridBagConstraints.gridx = 1;
		controllerFrame.add(portJTextField, gridBagConstraints);

		JButton saveButton = new JButton("Set");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				server.port = Integer.parseInt(portJTextField.getText());
				controllerFrame.setVisible(false);
				controllerFrame.dispose();
				controllerFrame = null;
				server.shutdown();
				try {
					serverThread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				serverThread = new Thread(server);
				serverThread.start();
			}
		});
		controllerFrame.add(saveButton, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}

	@Override
	public void remove() {
		shuttdown = true;
		if(server != null){
			server.shutdown();
		}
		if(clientList != null){
			clientList.clear();
		}
		
	}

	boolean shuttdown = false;
	@Override
	public void newImageReceived(VideoFrame videoFrame) {
		if(shuttdown){
			return;
		}
		if(clientList == null){
			return;
		}
		BufferedImage image = videoFrame.getImage();
		for(HTTPTransmitter client : clientList){
			client.submitImage(image);
		}
		
	}
	
	private class HTTPServer implements Runnable{

		public final int DEFAULT_PORT = 3998;
		private int port = DEFAULT_PORT;
		
		
		@Override
		public void run() {
			runServer();
		}
		
		public void shutdown(){
			runServer = false;
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private boolean runServer = true;
		private ServerSocket serverSocket = null;
		private void runServer(){
			serverSocket = null;
			int portshift = 0;
			while(serverSocket == null && portshift <1000){
				try {
					serverSocket = new ServerSocket(port);
				} catch (IOException e) {
					e.printStackTrace();
					serverSocket = null;
					portshift++;
					port++;
				}
			}
			if(serverSocket == null){
				return;
			}
			System.out.println("Serverstarted at port:" + port );
			ExecutorService executor = Executors.newCachedThreadPool();
			while (runServer) {
				Socket clientSocket;
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				HTTPTransmitter client = new HTTPTransmitter(clientSocket);
				executor.execute(client);
				addClient(client);
			}
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}

	private class HTTPTransmitter implements Runnable{

		private final static String BOUNDARY = "OPENVJJJPEG";
		private Socket clientSocket;
		private DataOutputStream out;
		private boolean connectionOK = false;
		private ImageWriter imageWriter;
		
		protected HTTPTransmitter(Socket clientSocket){
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			init();
		}
		
		private void init(){
			try {
				out = new DataOutputStream(clientSocket.getOutputStream());
				submitHeader(out);
			} catch (IOException e) {
				e.printStackTrace();
				removeClient(this);
				return;
			}
			Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpeg");
			try{
				imageWriter= imageWriters.next();
			}catch (Exception e) {
				e.printStackTrace();
				removeClient(this);
				return;
			}
			connectionOK = true;
		}
		
		private void submitHeader(DataOutputStream out) throws IOException{
			out.writeBytes("HTTP/1.0 200 OK\r\n");
			out.writeBytes("Server: Open-VJJ MJPEG Stream\r\n");
			out.writeBytes("Content-Type: multipart/x-mixed-replace;boundary=" + BOUNDARY + "\r\n");
			out.writeBytes("\r\n");
			out.writeBytes("--" + BOUNDARY + "\n");
			out.flush();
		}
		
		public synchronized void submitImage(BufferedImage bufferedImage){
			if(!connectionOK){
				return;
			}
			imageWriter.setOutput(out);
			try {
				out.writeBytes("Content-type: image/gif\n\n");
				imageWriter.write(bufferedImage);
				out.writeBytes("--" + BOUNDARY + "\n");
				out.flush();
			} catch (IOException e) {
				connectionOK = false;
				e.printStackTrace();
				try {
					out.close();
					clientSocket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				removeClient(this);
			}
		}
	}
}
