package org.jzy3d.plot3d.primitives.obj;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.io.IGLLoader;
import org.jzy3d.plot3d.rendering.view.Camera;

public class DrawableVBO extends AbstractDrawableVBO{
    public DrawableVBO(IGLLoader<AbstractDrawableVBO> loader) {
        super(loader);
    }

    public void draw(GL2 gl, GLU glu, Camera cam){
        if(transform!=null)
            transform.execute(gl);
        configure(gl);
        super.draw(gl, glu, cam);
    }
    
    /** An OBJ file appears to be really really slow to
     * render without a FRONT_AND_BACK spec, probably
     * because such a big polygon set has huge cost to have
     * culling status computed (culling enabled by depth peeling).
     */
    public void configure(GL2 gl){
        //gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        //gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
        //gl.glColor4f(1f,0f,1f,0.6f);
        //gl.glLineWidth(0.00001f);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glColor4f(1f,0f,1f,0.6f);
    }
}
