package org.jzy3d.plot3d.rendering.ddp;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import org.jzy3d.plot3d.primitives.IGLRenderer;
import org.jzy3d.plot3d.rendering.ddp.algorithms.DualDepthPeelingAlgorithm;
import org.jzy3d.plot3d.rendering.ddp.algorithms.FrontToBackPeelingAlgorithm;
import org.jzy3d.plot3d.rendering.ddp.algorithms.PeelingMethod;
import org.jzy3d.plot3d.rendering.ddp.algorithms.WeightedAveragePeelingAlgorithm;
import org.jzy3d.plot3d.rendering.ddp.algorithms.WeightedSumPeelingAlgorithm;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;

/**
 * Execute depth peeling methods in a Jzy3d {@link Renderer3d}
 * 
 * This feature is based on Order Independent Transparency
 * algorithms published by Louis Bavoil (NVIDIA Corporation).
 * 
 * The renderer support 4 methods:
 * <ul>
 * <li>Dual depth peeling (accurate)
 * <li>Front to back peeling (accurate)
 * <li>Weighted average peeling (fast)
 * <li>Weighted sum peeling (fast)
 * </ul>
 * 
 * Depth peeling is traditionally used to perform order independent transparency
 * (OIT) with N geometry passes for N transparency layers. Dual depth peeling
 * enables peeling N transparency layers in N/2+1 passes, by peeling from the
 * front and the back simultaneously using a min-max depth buffer. This sample
 * performs either normal or dual depth peeling and blends on the fly.
 * 
 * @author Louis Bavoil - original paper and C++ code
 * @author Martin Pernollet - port to Jzy3d
 */
public class DepthPeelingRenderer3d extends Renderer3d{
    protected IDepthPeelingAlgorithm dualPeelingAlgorithm;
    protected boolean autoSwapBuffer = false;
    protected static boolean AXE_IN_PEELING = false;
    protected static boolean DEBUG = false;
    
    
    public DepthPeelingRenderer3d(final DepthPeelingView view, boolean traceGL, boolean debugGL) {
        this(PeelingMethod.WEIGHTED_AVERAGE_MODE, view, traceGL, debugGL);
    }
    
    public DepthPeelingRenderer3d(PeelingMethod algorithm, final DepthPeelingView view, boolean traceGL, boolean debugGL) {
        super(view, traceGL, debugGL);
        dualPeelingAlgorithm = getDepthPeelingAlgorithm(algorithm);
        dualPeelingAlgorithm.setTasksToRender(getDepthPeelingContentRenderer(view));
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        super.init(drawable);
        drawable.setAutoSwapBufferMode(autoSwapBuffer);
        dualPeelingAlgorithm.init(gl, width, height);       
    }
    
    public static boolean DECOMPOSE_VIEW = true;
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        preDisplay(gl);
        dualPeelingAlgorithm.display(gl, glu); // will call taskToRender
        postDisplay(gl);
        
        if(!autoSwapBuffer)
            drawable.swapBuffers();
    }

    public void postDisplay(GL2 gl) {
        view.renderOverlay(gl);
    }

    public void preDisplay(GL2 gl) {
        ((DepthPeelingView)view).clearPeeledView(gl, glu, width, height);
    }
    
    public static IGLRenderer getDepthPeelingContentRenderer(final View view){
        return new IGLRenderer() {
            @Override
            public void draw(GL2 gl, GLU glu, Camera camera) {
                
                ((DepthPeelingView)view).renderPeeledView(gl, glu);
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
            dualPeelingAlgorithm.reshape(gl, width, height);
        }
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        dualPeelingAlgorithm.dispose(drawable.getGL().getGL2());
    }
    
    public static IDepthPeelingAlgorithm getDepthPeelingAlgorithm(PeelingMethod method){
        if(method==PeelingMethod.DUAL_PEELING_MODE)
            return new DualDepthPeelingAlgorithm();
        else if(method== PeelingMethod.F2B_PEELING_MODE)
            return new FrontToBackPeelingAlgorithm();
        else if(method== PeelingMethod.WEIGHTED_AVERAGE_MODE)
            return new WeightedAveragePeelingAlgorithm();
        else if(method== PeelingMethod.WEIGHTED_SUM_MODE)
            return new WeightedSumPeelingAlgorithm();
        else
            throw new RuntimeException("Unknown method:" + method);
    }
}
