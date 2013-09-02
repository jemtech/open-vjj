package de.openVJJ.imagePublisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jdom2.Element;

import de.openVJJ.ImageListener.MJPEGServer;

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
public class MJPEGClient extends ImagePublisher {

	private int port = MJPEGServer.DEFAULT_PORT;
	private String serverAdress = "localhost";
	
	public MJPEGClient() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub
		
	}
	
	private void connectToServer(){
		
	}
	
	private void readHeader(InputStream inputStream){
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			boolean header = true;
			boolean statusOk = false;
			String serverInfo = null;
			boolean contentTypeOK = false;
			String boundary = null;
			while(header){
				String line = reader.readLine();
				if(!statusOk){
					if(line.contains("HTTP")){
						if(line.contains("200")){
							statusOk = true;
						}else{
							System.err.println("HTTP status error: \"" + line + "\"");
							header = false;
							break;
						}
					}
					continue;
				}
				if(line.startsWith("Server:")){
					serverInfo = line.substring(7);
					serverInfo = serverInfo.trim();
					System.out.println("Server-Info: \"" + serverInfo + "\"");
					continue;
				}
				if(line.startsWith("Content-Type:")){
					String contentType = line.substring(13);
					int typeEnd = contentType.indexOf(";");
					String type = line.substring(0, typeEnd);
					if(!type.contains("multipart/x-mixed-replace")){
						header = false;
						System.err.println("Wrong Content-Type: \"" + type + "\"");
						break;
					}else{
						contentTypeOK = true;
					}
					int boundaryStart = contentType.indexOf("boundary=") + 9;
					if(boundaryStart < 9){
						header = false;
						System.err.println("No boundary: \"" + line + "\"");
						break;
					}
					boundary = line.substring(boundaryStart).trim();
					System.out.println("boundary: \"" + boundary + "\"");
					continue;
				}
				if(line.length()<1){
					System.out.println("Empty line => header end");
					header = false;
					break;
				}
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void getConfig(Element element) {
		// TODO Auto-generated
		
	}

	@Override
	public void setConfig(Element element) {
		// TODO Auto-generated
	}

}
