package org.jzy3d.plot3d.primitives.obj;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.io.IGLLoader;
import org.jzy3d.io.OBJFile;
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

    //public float[] trans = new float[] { 0.0f, 0.0f, 0.0f };
    //public float scale = 1.0f;
    protected int[] arrayBufferName = new int[1]; // vbo (array buffer)
    protected int[] elementArrayName = new int[1]; // indices (element array)
    
    protected int byteOffset;
    protected int normalOffset;
    protected int dimensions;
    protected int size;
    protected int pointer;
    
    protected int arrayName = 0;//arrayBufferName[0];
    protected int elementName = 0;//elementArrayName[0];
    
    public AbstractDrawableVBO(IGLLoader<AbstractDrawableVBO> loader){
        this.loader = loader;
    }
    
    // element array buffer is an index: 
    // @see http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-9-vbo-indexing/
    public void draw(GL2 gl, GLU glu, Camera cam) {
        /*OBJFile obj = null;
        byteOffset = obj.getCompiledVertexSize() * Buffers.SIZEOF_FLOAT;
        normalOffset = obj.getCompiledNormalOffset() * Buffers.SIZEOF_FLOAT;
        dimensions = obj.getPositionSize(); // byte offset, probably 0
        size = obj.getCompiledIndexCount();
        pointer = 0;*/
        
        
        if(hasMountedOnce)
            doDrawElements(gl);
    }
    
    protected void doDrawElements(GL2 gl) {
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, arrayName);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, elementName);
        
        gl.glVertexPointer(dimensions, GL2.GL_FLOAT, byteOffset, pointer);
        gl.glNormalPointer(GL2.GL_FLOAT, byteOffset, normalOffset);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glDrawElements(GL2.GL_TRIANGLES, size, GL2.GL_UNSIGNED_INT, pointer);

        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, elementName);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, arrayName);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
    }
    
    /* IO */
    
    public void mount(GL2 gl){
        loader.load(gl, this);
        hasMountedOnce = true;
        /*
        System.err.println("loading OBJ...\n");
        obj = new OBJFile();
        obj.loadModelFromFile(filename);
        System.err.println("compiling mesh...\n");
        obj.compileModel();
        System.err.println(obj.getPositionCount() + " vertices");
        System.err.println((obj.getIndexCount() / 3) + " triangles");
        int totalVertexSize = obj.getCompiledVertexCount() * Buffers.SIZEOF_FLOAT;
        int totalIndexSize = obj.getCompiledIndexCount() * Buffers.SIZEOF_INT;
        int arrayName = arrayBufferName[0];
        int elementName = elementArrayName[0];
        

        FloatBuffer vertices = obj.getCompiledVertices();
        IntBuffer indices = obj.getCompiledIndices();
        
        doLoadBuffers(gl, totalVertexSize, totalIndexSize, arrayName, elementName, vertices, indices);

        
        //float[] diag = new float[] { modelMax[0] - modelMin[0], modelMax[1] - modelMin[1], modelMax[2] - modelMin[2] };
        //scale = (float) (1.0 / Math.sqrt(diag[0] * diag[0] + diag[1] * diag[1] + diag[2] * diag[2]) * 1.5);
        //trans = new float[] { (float) (-scale * (modelMin[0] + 0.5 * diag[0])), (float) (-scale * (modelMin[1] + 0.5 * diag[1])), (float) (-scale * (modelMin[2] + 0.5 * diag[2])) };
        doSetBoundingBox(obj.computeBoundingBox());*/
    }
    
    public void doConfigure(int arrayName, int elementName, int byteOffset, int normalOffset, int dimensions, int size, int pointer){
        this.arrayName = arrayName;
        this.elementName = elementName;
        
        this.byteOffset = byteOffset;
        this.normalOffset = normalOffset;
        this.dimensions = dimensions;
        this.size = size;
        this.pointer = pointer;
    }

    public void doLoadBuffers(GL2 gl, int vertexSize, int indexSize, FloatBuffer vertices, IntBuffer indices) {
        int[] arrayNames = {arrayName};
        gl.glGenBuffers(1, arrayNames, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, arrayName);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertexSize, vertices, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

        int[] elementNames = {elementName};
        gl.glGenBuffers(1, elementNames, 0);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, elementName);
        gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indexSize, indices, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void doSetBoundingBox(BoundingBox3d bounds){
        bbox = bounds;
    }
    
    public boolean hasMountedOnce(){
        return hasMountedOnce;
    }
    
    protected boolean hasMountedOnce = false;
}
