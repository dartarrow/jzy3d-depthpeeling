package org.jzy3d.plot3d.rendering.ddp;

import org.jzy3d.maths.Dimension;

import javax.media.opengl.GLCapabilities;

import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;
import org.jzy3d.plot3d.rendering.canvas.CanvasNewtAwt;
import org.jzy3d.plot3d.rendering.canvas.CanvasSwing;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.OffscreenCanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.ddp.algorithms.PeelingMethod;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;

public class PeelingComponentFactory extends AWTChartComponentFactory{
	public PeelingComponentFactory(PeelingMethod method) {
		super();
		this.method = method;
	}

	@Override
	public Renderer3d newRenderer(View view, boolean traceGL, boolean debugGL){
        DepthPeelingRenderer3d r = new DepthPeelingRenderer3d(method, (DepthPeelingView)view, traceGL, debugGL);
        return r;
    }
	
	@Override
	public View newView(Scene scene, ICanvas canvas, Quality quality){
        return new DepthPeelingView(this, scene, canvas, quality);
    }
	
	/*public ICanvas newCanvas(Scene scene, Quality quality, String chartType, GLCapabilities capabilities){
        if("awt".compareTo(chartType)==0){
            CanvasAWT c = new CanvasAWT(this, scene, quality, capabilities);
            c.setAutoSwapBufferMode(CHART_CANVAS_AUTOSWAP);
            return c;
        }
        else if("newt".compareTo(chartType)==0)
            return new CanvasNewt(this, scene, quality, capabilities);
        else if("swing".compareTo(chartType)==0)
            return new CanvasSwing(this, scene, quality, capabilities);
        else if("offscreen".compareTo(chartType)==0){
            Dimension dimension = getCanvasDimension(windowingToolkit);
            return new OffscreenCanvas(this, scene, quality, capabilities, dimension.width, dimension.height, traceGL, debugGL);
        }
        else
            throw new RuntimeException("unknown chart type:" + chartType);
    }*/
	
	@Override
	protected ICanvas initializeCanvas(Scene scene, Quality quality, String windowingToolkit, GLCapabilities capabilities, boolean traceGL, boolean debugGL) {
        Toolkit chartType = getToolkit(windowingToolkit);
        switch (chartType) {
        case awt:{
            CanvasAWT c = new CanvasAWT(this, scene, quality, capabilities);
            c.setAutoSwapBufferMode(CHART_CANVAS_AUTOSWAP);
            return c;
        }
        case swing:
            return new CanvasSwing(this, scene, quality, capabilities, traceGL, debugGL);
        case newt:
            return new CanvasNewtAwt(this, scene, quality, capabilities, traceGL, debugGL);
        case offscreen:
            Dimension dimension = getCanvasDimension(windowingToolkit);
            return new OffscreenCanvas(this, scene, quality, capabilities, dimension.width, dimension.height, traceGL, debugGL);
        default:
            throw new RuntimeException("unknown chart type:" + chartType);
        }
    }
	
    public static boolean CHART_CANVAS_AUTOSWAP = false;

	PeelingMethod method;
}
