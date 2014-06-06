package de.openVJJ.values;

import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;

import de.openVJJ.basic.Value;

public class CLFloatBufferValue extends Value {
	private CLBuffer<FloatBuffer> floatBuffer;
	
	public CLFloatBufferValue(CLBuffer<FloatBuffer> floatBuffer){
		this.floatBuffer = floatBuffer;
	}
	
	public CLBuffer<FloatBuffer> getValue(){
		return floatBuffer;
	}
	
	@Override
	protected void finalize() throws Throwable {
		floatBuffer.release();
		super.finalize();
	}
	
}
