package org.jzy3d.plot3d.primitives.obj;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.io.OBJFile;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.IGLBindedResource;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.transform.Transform;

import com.jogamp.common.nio.Buffers;


/**
 * {@link RawDrawableOBJFile} has a {@link AbstractDrawable} interface
 * but remains agnostic to jzy3d by not handling {@link Transform}.
 * 
 * @author Martin Pernollet
 *
 */
public class RawDrawableOBJFile extends AbstractDrawable implements IGLBindedResource{
    public String filename;
    public OBJFile obj;

    /*public float[] rotation = new float[] { 45.0f, 45.0f };
    public float[] position = new float[] { 0.0f, 0.0f, 2.0f };
*/
    public float[] trans = new float[] { 0.0f, 0.0f, 0.0f };
    public float scale = 1.0f;
    public int[] g_vboId = new int[1];
    public int[] g_eboId = new int[1];
    
    public RawDrawableOBJFile(String filename){
        this.filename = filename;
    }
    
    public void draw(GL2 gl, GLU glu, Camera cam) {
        //gl.glColor3f(1f, 1f, 1f);
        
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, g_vboId[0]);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, g_eboId[0]);
        int stride = obj.getCompiledVertexSize() * Buffers.SIZEOF_FLOAT;
        int normalOffset = obj.getCompiledNormalOffset() * Buffers.SIZEOF_FLOAT;
        gl.glVertexPointer(obj.getPositionSize(), GL2.GL_FLOAT, stride, 0);
        gl.glNormalPointer(GL2.GL_FLOAT, stride, normalOffset);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glDrawElements(GL2.GL_TRIANGLES, obj.getCompiledIndexCount(), GL2.GL_UNSIGNED_INT, 0);

        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
    }
    
    /* IO */
    
    public void mount(GL2 gl){
        load(gl);
    }
    
    public void load(GL2 gl) {
        load(gl, filename);
    }
    
    public void load(GL2 gl, String filename) {
        obj = new OBJFile();
        System.err.println("loading OBJ...\n");

        obj.loadModelFromFile(filename);

        System.err.println("compiling mesh...\n");
        obj.compileModel();

        System.err.println(obj.getPositionCount() + " vertices");
        System.err.println((obj.getIndexCount() / 3) + " triangles");
        int totalVertexSize = obj.getCompiledVertexCount() * Buffers.SIZEOF_FLOAT;
        int totalIndexSize = obj.getCompiledIndexCount() * Buffers.SIZEOF_INT;

        gl.glGenBuffers(1, g_vboId, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, g_vboId[0]);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, totalVertexSize, obj.getCompiledVertices(), GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

        gl.glGenBuffers(1, g_eboId, 0);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, g_eboId[0]);
        gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, totalIndexSize, obj.getCompiledIndices(), GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

        float[] modelMin = new float[3];
        float[] modelMax = new float[3];
        obj.computeBoundingBox(modelMin, modelMax);
        
        float[] diag = new float[] { modelMax[0] - modelMin[0], modelMax[1] - modelMin[1], modelMax[2] - modelMin[2] };
        scale = (float) (1.0 / Math.sqrt(diag[0] * diag[0] + diag[1] * diag[1] + diag[2] * diag[2]) * 1.5);
        trans = new float[] { (float) (-scale * (modelMin[0] + 0.5 * diag[0])), (float) (-scale * (modelMin[1] + 0.5 * diag[1])), (float) (-scale * (modelMin[2] + 0.5 * diag[2])) };
    
        // jzy3d
        bbox = new BoundingBox3d(modelMin[0], modelMax[0], modelMin[1], modelMax[1], modelMin[2], modelMax[2]);
    }
    
    public boolean hasMountedOnce(){
        return obj!=null;
    }
}
