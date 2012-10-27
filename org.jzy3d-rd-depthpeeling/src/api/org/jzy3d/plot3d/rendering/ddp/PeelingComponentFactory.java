package org.jzy3d.plot3d.rendering.ddp;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import org.jzy3d.chart.factories.ChartComponentFactory;
import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;
import org.jzy3d.plot3d.rendering.canvas.CanvasNewt;
import org.jzy3d.plot3d.rendering.canvas.CanvasSwing;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.OffscreenCanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.ddp.algorithms.PeelingMethod;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;

public class PeelingComponentFactory extends ChartComponentFactory{
	public PeelingComponentFactory(PeelingMethod method) {
		super();
		this.method = method;
	}

	public Renderer3d newRenderer(View view, boolean traceGL, boolean debugGL){
        DepthPeelingRenderer3d r = new DepthPeelingRenderer3d(method, (DepthPeelingView)view, traceGL, debugGL);
        return r;
    }
	
	public View newView(Scene scene, ICanvas canvas, Quality quality){
        return new DepthPeelingView(this, scene, canvas, quality);
    }
	
	public ICanvas newCanvas(Scene scene, Quality quality, String chartType, GLCapabilities capabilities){
        if("awt".compareTo(chartType)==0){
            CanvasAWT c = new CanvasAWT(this, scene, quality, capabilities);
            c.setAutoSwapBufferMode(CHART_CANVAS_AUTOSWAP);
            return c;
        }
        else if("newt".compareTo(chartType)==0)
            return new CanvasNewt(this, scene, quality, capabilities);
        else if("swing".compareTo(chartType)==0)
            return new CanvasSwing(this, scene, quality, capabilities);
        else if("offscreen".compareTo(chartType)==0)
            return new OffscreenCanvas(this, scene, quality, GLProfile.getDefault(), 500, 500);
        else
            throw new RuntimeException("unknown chart type:" + chartType);
    }
    public static boolean CHART_CANVAS_AUTOSWAP = false;

	PeelingMethod method;
}
