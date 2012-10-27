package org.jzy3d.plot3d.rendering.ddp;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.view.View;
import org.jzy3d.plot3d.rendering.view.ViewPort;

/**
 * {@link DepthPeelingRenderer3d} needs to split calls to view clear and view renderer.
 * {@link DepthPeelingView} facilitate to perform this with two separate method that
 * replace view.renderScene(gl, glu, viewport);
 * @author Martin
 *
 */
public class DepthPeelingView extends View{
    public DepthPeelingView(IChartComponentFactory factory, Scene scene, ICanvas canvas, Quality quality) {
        super(factory, scene, canvas, quality);
    }

    public void clearPeeledView(GL2 gl, GLU glu, int width, int height){
     // decompose super.display, i.e. prevent to render scenegraph now,
        // and delegate to peeling algorithm
        synchronized(this){
            clear(gl);
            
            // render background
            renderBackground(gl, glu, 0f, 1f);

            // fix quality
            updateQuality(gl);

            // prepare viewport
            this.width = width;
            this.height = height;
        }
    }
    
    protected int width = 0;
    protected int height = 0;
    
    public void renderPeeledView(GL2 gl, GLU glu){
        updateCamera(gl, glu,  new ViewPort(width, height), computeScaling());

        renderAxeBox(gl, glu);
        renderSceneGraph(gl, glu, false);  
    }
}
