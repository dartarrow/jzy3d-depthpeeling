package org.jzy3d.plot3d.rendering.shaders;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import org.jzy3d.plot3d.primitives.IGLRenderer;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;
import org.jzy3d.plot3d.rendering.view.ViewPort;

public class ShaderRenderer3d extends Renderer3d{
    protected IShaderable shaderable;
    protected boolean autoSwapBuffer = true;
    protected static boolean DEBUG = false;
    
    
    public ShaderRenderer3d(final View view, boolean traceGL, boolean debugGL) {
        this(view, traceGL, debugGL, new Shaderable());
    }
    
    public ShaderRenderer3d(final View view, boolean traceGL, boolean debugGL, IShaderable shaderable) {
        super(view, traceGL, debugGL);
        this.shaderable = shaderable;//new Shaderable();
        this.shaderable.setTasksToRender(getShaderContentRenderer(view));
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        super.init(drawable);
        drawable.setAutoSwapBufferMode(autoSwapBuffer);
        shaderable.init(gl, width, height);       
    }
    
    public static boolean DECOMPOSE_VIEW = true;
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        preDisplay(gl);
        shaderable.display(gl, glu); // will call taskToRender
        postDisplay(gl);
        
        if(!autoSwapBuffer)
            drawable.swapBuffers();
    }


    public void preDisplay(GL2 gl) {
        // decompose super.display, i.e. prevent to render scenegraph now,
        // and delegate to peeling algorithm
        synchronized(view){
            view.clear(gl);
            
            // render background
            view.renderBackground(gl, glu, 0f, 1f);
            
            // render scene
            view.updateQuality(gl);
            view.updateCamera(gl, glu,  new ViewPort(width, height), view.computeScaling());
        }
    }
    
    public void postDisplay(GL2 gl) {
        view.renderOverlay(gl);
    }

    /* */
    
    public static IGLRenderer getShaderContentRenderer(final View view){
        return new IGLRenderer() {
            @Override
            public void draw(GL2 gl, GLU glu, Camera camera) {
                view.renderSceneGraph(gl, glu, true);  
            }
        };
    }
    
    /**
     * Rebuild all depth peeling buffers for the new screen size.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            shaderable.reshape(gl, width, height);
        }
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        shaderable.dispose(drawable.getGL().getGL2());
    }
}
