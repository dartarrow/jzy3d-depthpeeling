package org.jzy3d.plot3d.primitives.obj;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.apache.log4j.Logger;
import org.jzy3d.io.IGLLoader;
import org.jzy3d.io.obj.OBJFile;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.IGLBindedResource;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.transform.Transform;

import com.jogamp.common.nio.Buffers;


/**
 * {@link AbstractDrawableVBO} has a {@link AbstractDrawable} interface
 * but remains agnostic to jzy3d by not handling {@link Transform}.
 * 
 * @author Martin Pernollet
 *
 */
public class AbstractDrawableVBO extends AbstractDrawable implements IGLBindedResource{
    IGLLoader<AbstractDrawableVBO> loader;
    
    protected int byteOffset;
    protected int normalOffset;
    protected int dimensions;
    protected int size;
    protected int pointer;
    
    protected int arrayName[] = new int[1];
    protected int elementName[] = new int[1];
    
    public AbstractDrawableVBO(IGLLoader<AbstractDrawableVBO> loader){
        this.loader = loader;
    }
    
    // element array buffer is an index: 
    // @see http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-9-vbo-indexing/
    public void draw(GL2 gl, GLU glu, Camera cam) {
        if(hasMountedOnce)
            doDrawElements(gl);
    }
    
    protected void doDrawElements(GL2 gl) {
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, arrayName[0]);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, elementName[0]);
        
        gl.glVertexPointer(dimensions, GL2.GL_FLOAT, byteOffset, pointer);
        gl.glNormalPointer(GL2.GL_FLOAT, byteOffset, normalOffset);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glDrawElements(GL2.GL_TRIANGLES, size, GL2.GL_UNSIGNED_INT, pointer);

        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, elementName[0]);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, arrayName[0]);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
    }
    
    /* IO */
    
    public void mount(GL2 gl){
        try {
            loader.load(gl, this);
            hasMountedOnce = true;
        } catch (Exception e) {
            Logger.getLogger(AbstractDrawableVBO.class).error(e,e);
        }
    }
    
    public void doConfigure(int pointer, int byteOffset, int normalOffset, int dimensions, int size){
        this.byteOffset = byteOffset;
        this.normalOffset = normalOffset;
        this.dimensions = dimensions;
        this.size = size;
        this.pointer = pointer;
    }

    public void doLoadArrayBuffer(GL2 gl, int vertexSize, FloatBuffer vertices) {
        gl.glGenBuffers(1, arrayName, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, arrayName[0]);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertexSize, vertices, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, pointer);
    }

    public void doLoadElementBuffer(GL2 gl, int indexSize, IntBuffer indices) {
        gl.glGenBuffers(1, elementName, 0);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, elementName[0]);
        gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indexSize, indices, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, pointer);
    }

    public void doSetBoundingBox(BoundingBox3d bounds){
        bbox = bounds;
    }
    
    public boolean hasMountedOnce(){
        return hasMountedOnce;
    }
    
    protected boolean hasMountedOnce = false;
}
