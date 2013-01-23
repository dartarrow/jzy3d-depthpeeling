package org.jzy3d.io;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;

import org.jzy3d.plot3d.primitives.obj.AbstractDrawableVBO;

import com.jogamp.common.nio.Buffers;

public class OBJFileLoader implements IGLLoader<AbstractDrawableVBO>{
    protected String filename;
    protected OBJFile obj;
    
    public OBJFileLoader(String filename) {
        this.filename = filename;
    }

    @Override
    public void load(GL2 gl, AbstractDrawableVBO drawable) {
        System.err.println("loading OBJ...\n");
        obj = new OBJFile();
        obj.loadModelFromFile(filename);
        System.err.println("compiling mesh...\n");
        obj.compileModel();
        System.err.println(obj.getPositionCount() + " vertices");
        System.err.println((obj.getIndexCount() / 3) + " triangles");
        
        int size = obj.getIndexCount();
        int indexSize = size * Buffers.SIZEOF_INT;
        int vertexSize = obj.getCompiledVertexCount() * Buffers.SIZEOF_FLOAT;
        int byteOffset = obj.getCompiledVertexSize() * Buffers.SIZEOF_FLOAT;
        int normalOffset = obj.getCompiledNormalOffset() * Buffers.SIZEOF_FLOAT;
        int dimensions = obj.getPositionSize(); // byte offset, probably 0
        
        
        
        int pointer = 0;
        int arrayName = 0;//arrayBufferName[0];
        int elementName = 0;//elementArrayName[0];
        

        FloatBuffer vertices = obj.getCompiledVertices();
        IntBuffer indices = obj.getCompiledIndices();
        
        drawable.doConfigure(arrayName, elementName, byteOffset, normalOffset, dimensions, size, pointer);
        drawable.doLoadBuffers(gl, vertexSize, indexSize, vertices, indices);
        drawable.doSetBoundingBox(obj.computeBoundingBox());
    }

}
