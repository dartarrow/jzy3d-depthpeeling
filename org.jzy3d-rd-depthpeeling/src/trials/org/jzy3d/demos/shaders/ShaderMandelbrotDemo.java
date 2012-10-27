package org.jzy3d.demos.shaders;

import java.awt.Rectangle;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.ChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;
import org.jzy3d.plot3d.rendering.canvas.CanvasNewt;
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
    	IChartComponentFactory factory = new ChartComponentFactory(){
    		public Renderer3d newRenderer(View view, boolean traceGL, boolean debugGL){
                ShaderRenderer3d r = new ShaderRenderer3d(view, traceGL, debugGL, new Shaderable());
                return r;
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
