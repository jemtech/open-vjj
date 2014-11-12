package de.openVJJ.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.BufferedImageValue;

public class XuggleWebCam extends Plugin {

	public static final int FRAME_RATE_LIMIT = 50;
	
	private int framerateLimit =  FRAME_RATE_LIMIT;
	private JLabel framerateLimitLabel;
	
	public XuggleWebCam(){
		addOutput("Frames", BufferedImageValue.class);
		startCapture();
	}
	
	public int getFramerateLimit(){
		return framerateLimit;
	}
	
	private void setFramerateLimit(int framerateLimit){
		this.framerateLimit = framerateLimit;
		if(framerateLimitLabel != null){
			framerateLimitLabel.setText("Limit f/s (" + framerateLimit + ")");
		}
	}
	
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
		JPanel configPanel = new JPanel();
		
		configPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		framerateLimitLabel = new JLabel("Limit f/s (" + framerateLimit + ")");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		configPanel.add(framerateLimitLabel, gridBagConstraints);
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(1);
		rSlider.setMaximum((int)FRAME_RATE_LIMIT);
		rSlider.setMajorTickSpacing(64);
		rSlider.setMinorTickSpacing(8);
		rSlider.setPaintTicks(true);
		rSlider.setValue(getFramerateLimit());
		rSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				setFramerateLimit(((JSlider) arg0.getSource()).getValue());
			}
		});
		gridBagConstraints.gridx = 1;
		configPanel.add(rSlider, gridBagConstraints);
		return configPanel;
	}
	
	private String driverName = "vfwcap"; //Windows "vfwcap"; Linux "video4linux2"
    private String deviceName=  "0"; //Windows "0"; Linux "/dev/video0"
    private IContainer container;
    private IStreamCoder videoCoder;
	public void startCapture(){

	    // Let's make sure that we can actually convert video pixel formats.
	    if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
	      throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");

	    // Create a Xuggler container object
	    container = IContainer.make();

//	    // Devices, unlike most files, need to have parameters set in order
//	    // for Xuggler to know how to configure them.  For a webcam, these
//	    // parameters make sense
//	    IContainerParameters params = IContainerParameters.make();
//	    
//	    // The timebase here is used as the camera frame rate
//	    params.setTimeBase(IRational.make(30,1));
//	    
//	    // we need to tell the driver what video with and height to use
//	    params.setVideoWidth(320);
//	    params.setVideoHeight(240);
//	    
//	    // and finally, we set these parameters on the container before opening
//	    container.setParameters(params);
	    
	    // Tell Xuggler about the device format
	    IContainerFormat format = IContainerFormat.make();
	    if (format.setInputFormat(driverName) < 0)
	      throw new IllegalArgumentException("couldn't open webcam device: " + driverName);
	    
	    // Open up the container
	    int retval = container.open(deviceName, IContainer.Type.READ, format);
	    if (retval < 0)
	    {
	      // This little trick converts the non friendly integer return value into
	      // a slightly more friendly object to get a human-readable error name
	      IError error = IError.make(retval);
	      throw new IllegalArgumentException("could not open file: " + deviceName + "; Error: " + error.getDescription());
	    }      

	    // query how many streams the call to open found
	    int numStreams = container.getNumStreams();

	    // and iterate through the streams to find the first video stream
	    int videoStreamId = -1;
	    videoCoder = null;
	    for(int i = 0; i < numStreams; i++)
	    {
	      // Find the stream object
	      IStream stream = container.getStream(i);
	      // Get the pre-configured decoder that can decode this stream;
	      IStreamCoder coder = stream.getStreamCoder();

	      if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
	      {
	        videoStreamId = i;
	        videoCoder = coder;
	        break;
	      }
	    }
	    if (videoStreamId == -1)
	      throw new RuntimeException("could not find video stream in container: "+deviceName);

	    /*
	     * Now we have found the video stream in this file.  Let's open up our decoder so it can
	     * do work.
	     */
	    if (videoCoder.open() < 0)
	      throw new RuntimeException("could not open video decoder for container: "+deviceName);

	    CaptureThread captureThread = new CaptureThread(videoStreamId);
	    Thread thread = new Thread(captureThread);
	    thread.start();

	}
	
	@Override
	protected void shutdown() {
	    /*
	     * Technically since we're exiting anyway, these will be cleaned up by 
	     * the garbage collector... but because we're nice people and want
	     * to be invited places for Christmas, we're going to show how to clean up.
	     */
	    if (videoCoder != null)
	    {
	      videoCoder.close();
	      videoCoder = null;
	    }
	    if (container !=null)
	    {
	      container.close();
	      container = null;
	    }
		super.shutdown();
	}
	
	private class CaptureThread implements Runnable{
		private int videoStreamId = -1;
		private long lastPictureRecevedAt = -1;
		
		protected CaptureThread(int videoStreamId){
			this.videoStreamId = videoStreamId;
		}
		
		private int frameTimeUnder = 0;
		private boolean dropFrame(long frameRecevedAt){
			int minFrametime = 1000 / getFramerateLimit();
			long frametime = frameRecevedAt - lastPictureRecevedAt;
			lastPictureRecevedAt = frameRecevedAt;
			if(frametime > minFrametime){
				return false;
			}
			if(frameTimeUnder > frametime){
				frameTimeUnder -= frametime;
				return true;
			}
			frameTimeUnder += minFrametime - frametime;
			return false;
		}
		
		@Override
		public void run() {
		    IVideoResampler resampler = null;
		    if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
		    {
		      // if this stream is not in BGR24, we're going to need to
		      // convert it.  The VideoResampler does that for us.
		      resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
		          videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
		      if (resampler == null)
		        throw new RuntimeException("could not create color space resampler for: " + deviceName);
		    }

		    /*
		     * Now, we start walking through the container looking at each packet.
		     */
		    IPacket packet = IPacket.make();
		    
			// TODO Auto-generated method stub
		    while(container.readNextPacket(packet) >= 0)
		    {
		      /*
		       * Now we have a packet, let's see if it belongs to our video stream
		       */
		      if (packet.getStreamIndex() == videoStreamId)
		      {
		        /*
		         * We allocate a new picture to get the data out of Xuggler
		         */
		        IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
		            videoCoder.getWidth(), videoCoder.getHeight());

		        int offset = 0;
		        while(offset < packet.getSize())
		        {
		          /*
		           * Now, we decode the video, checking for any errors.
		           * 
		           */
		          int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
		          if (bytesDecoded < 0)
		            throw new RuntimeException("got error decoding video in: " + deviceName);
		          offset += bytesDecoded;

		          /*
		           * Some decoders will consume data in a packet, but will not be able to construct
		           * a full video picture yet.  Therefore you should always check if you
		           * got a complete picture from the decoder
		           */
		          if (picture.isComplete())
		          {
		        	  long pictureRecevedAt = System.currentTimeMillis();
		        	  if(!dropFrame(pictureRecevedAt)){
			        		
			            IVideoPicture newPic = picture;
			            /*
			             * If the resampler is not null, that means we didn't get the video in BGR24 format and
			             * need to convert it into BGR24 format.
			             */
			            if (resampler != null)
			            {
			              // we must resample
			              newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
			              if (resampler.resample(newPic, picture) < 0)
			                throw new RuntimeException("could not resample video from: " + deviceName);
			            }
			            if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
			              throw new RuntimeException("could not decode video as BGR 24 bit data in: " + deviceName);
	
			            // Convert the BGR24 to an Java buffered image
			            BufferedImage javaImage = Utils.videoPictureToImage(newPic);
	
						BufferedImageValue value =new BufferedImageValue(javaImage);
						getConnection("Frames").transmitValue(value);
			            
			            // and display it on the Java Swing window
			          //  updateJavaWindow(javaImage);
		        	  }
	        	  }
		        }
		      }
		      else
		      {
		        /*
		         * This packet isn't part of our video stream, so we just silently drop it.
		         */
		        do {} while(false);
		      }

		    }
		}
		
		
		
	}

}
