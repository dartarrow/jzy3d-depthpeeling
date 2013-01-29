package org.jzy3d.plot3d.rendering.shaders.mandelbrot;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.transform.Transform;

// equivalent épuré de DrawableTexture
public class TexSurface extends AbstractDrawable{
    protected static float SIZE = 100f;
    public TexSurface(){
        bbox = new BoundingBox3d(0f, SIZE, 0f, SIZE, SIZE-1, SIZE+1);
    }
    
    @Override
    public void draw(GL2 gl, GLU glu, Camera cam) {
        doTransform(gl, glu, cam);
        // Reset the current matrix to the "identity"
        gl.glLoadIdentity();

        // Draw A Quad
        //gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        
        gl.glBegin(GL2.GL_QUADS);
        {
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex3f(0.0f, SIZE, SIZE);
            
            gl.glTexCoord2f(SIZE, 0.0f);
            gl.glVertex3f(SIZE, SIZE, SIZE);
            
            gl.glTexCoord2f(SIZE, SIZE);
            gl.glVertex3f(SIZE, 0.0f, SIZE);
            
            gl.glTexCoord2f(0.0f, SIZE);
            gl.glVertex3f(0.0f, 0.0f, SIZE);
        }
        // Done Drawing The Quad
        gl.glEnd();

        // Flush all drawing operations to the graphics card
        gl.glFlush();
    }

    @Override
    public void applyGeometryTransform(Transform transform) {
        //throw new RuntimeException("not implemented");
    }

    @Override
    public void updateBounds() {
        //throw new RuntimeException("not implemented");
    }
}
