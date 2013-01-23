package org.jzy3d.plot3d.rendering.shaders;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.io.glsl.ShaderFilePair;
import org.jzy3d.plot3d.primitives.AbstractDrawable;

public class ShadedDrawable extends Shaderable{
    AbstractDrawable drawable;
    
    public ShadedDrawable(AbstractDrawable drawable, ShaderFilePair shaders){
        this.drawable = drawable;
        this.shaders = shaders;
    }
    
    @Override
    public void display(GL2 gl, GLU glu) {
        executeProgram(gl); 
        
        if(drawable!=null)
            drawable.draw(gl, glu, null);
    }
}
