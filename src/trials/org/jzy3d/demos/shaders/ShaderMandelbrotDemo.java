package org.jzy3d.demos.shaders;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.maths.Dimension;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;
import org.jzy3d.plot3d.rendering.canvas.CanvasNewtAwt;
import org.jzy3d.plot3d.rendering.canvas.CanvasSwing;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.OffscreenCanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.shaders.IShaderable;
import org.jzy3d.plot3d.rendering.shaders.ShaderRenderer3d;
import org.jzy3d.plot3d.rendering.shaders.Shaderable;
import org.jzy3d.plot3d.rendering.shaders.mandelbrot.MandelBrotShader;
import org.jzy3d.plot3d.rendering.shaders.mandelbrot.TexSurface;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;


public class ShaderMandelbrotDemo {

    
    public static void main(String[] args) {
        Chart chart = initChart(new MandelBrotShader());
        
        chart.getScene().getGraph().add(new TexSurface());

        chart.getView().setAxeBoxDisplayed(false);
        ChartLauncher.openChart(chart, new Rectangle(0,0,600,600));
    }
    
    
    public static Chart initChart(final IShaderable s) {
    	IChartComponentFactory factory = new AWTChartComponentFactory(){
    		public Renderer3d newRenderer(View view, boolean traceGL, boolean debugGL){
                ShaderRenderer3d r = new ShaderRenderer3d(view, traceGL, debugGL, new Shaderable());
                return r;
            }
    		
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
    	};
        
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(false);
        
        Chart chart = new Chart(factory, Quality.Intermediate, "awt", capabilities);
        chart.getView().setSquared(false);
        
        //chart.getView().setCameraMode(CameraMode.PERSPECTIVE);
        return chart;
    }    
    public static boolean CHART_CANVAS_AUTOSWAP = true;
}
