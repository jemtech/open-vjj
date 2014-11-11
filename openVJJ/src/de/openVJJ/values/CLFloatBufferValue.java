package de.openVJJ.values;

import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;

import de.openVJJ.basic.Value;

public class CLFloatBufferValue extends Value {
	private CLBuffer<FloatBuffer> floatBuffer;
	
	public int width;
	public int height;
	
	public CLFloatBufferValue(CLBuffer<FloatBuffer> floatBuffer){
		this.floatBuffer = floatBuffer;
	}
	
	public CLBuffer<FloatBuffer> getValue(){
		return floatBuffer;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(floatBuffer != null){
			if(!floatBuffer.isReleased()){
				floatBuffer.release();
			}
		
		}
		super.finalize();
	}
	
	@Override
	public boolean isThreadSave(){
		return false;
	}
	
}
