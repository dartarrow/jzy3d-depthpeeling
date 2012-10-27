package org.jzy3d.demos.shaders;

import java.awt.Rectangle;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.ChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.CompositeParallelepiped;
import org.jzy3d.plot3d.primitives.CompositeParallelepiped.PolygonType;
import org.jzy3d.plot3d.primitives.Cylinder;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;
import org.jzy3d.plot3d.rendering.canvas.CanvasNewt;
import org.jzy3d.plot3d.rendering.canvas.CanvasSwing;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.OffscreenCanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.shaders.ShaderRenderer3d;
import org.jzy3d.plot3d.rendering.shaders.Shaderable;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;


public class ShaderDemo {

    static Color STACK_FACE = new Color(.5f,.5f,.5f,.5f);
    static Color STACK_WIRE = Color.BLACK;
    static int STACK_WIDTH = 100;
    static int STACK_HEIGHT = 10;    
    static int CYLINDER_STEPS = 30;
    
    public static void main(String[] args) {
        Chart chart = initChart();
        
        createStack(chart, STACK_WIDTH, STACK_HEIGHT, 0, STACK_FACE, STACK_WIRE);
        createStack(chart, STACK_WIDTH, STACK_HEIGHT, 20, STACK_FACE, STACK_WIRE);
        Coord3d c1 = new Coord3d(0,0,-2.5);
        Coord3d c2 = new Coord3d(10,10,20);
        createCylinder(chart, c1, 5, 15, Color.CYAN);
        createCylinder(chart, c2, 5, 15, Color.CYAN);
        createLine(chart, c1, c2, 3);
        
        chart.getView().setAxeBoxDisplayed(false);
        //chart.getScene().getGraph().add(new TexSurface());
        ChartLauncher.openChart(chart, new Rectangle(0,0,600,600));
    }
    
    public static void createStack(Chart chart, float width, float height, float position, Color face, Color wireframe) {
        BoundingBox3d bounds = new BoundingBox3d(-width/2, width/2, -width/2, width/2, position-height/2, position+height/2);
        CompositeParallelepiped p1 = new CompositeParallelepiped(bounds, PolygonType.SIMPLE);
        p1.setColor(face);
        p1.setWireframeColor(wireframe);
        p1.setWireframeDisplayed(false);
        chart.getScene().add(p1);
    }


    public static void createLine(Chart chart, Coord3d c1, Coord3d c2, int width) {
        LineStrip ls1 = new LineStrip();
        ls1.add(new Point(c1, Color.CYAN));
        ls1.add(new Point(c2, Color.CYAN));
        ls1.setWidth(width);
        chart.getScene().add(ls1);
    }

    public static void createCylinder(Chart chart, Coord3d c1Position, float height, float radius, Color color) {
        Cylinder c1 = new Cylinder();
        c1.setData(c1Position, height, radius, CYLINDER_STEPS, 1, color);
        chart.getScene().add(c1);
    }

    public static void createStack(Chart chart, int width, int height, int position, Color face, Color wireframe) {
        BoundingBox3d bounds = new BoundingBox3d(-width/2, width/2, -width/2, width/2, position-height/2, position+height/2);
        CompositeParallelepiped p1 = new CompositeParallelepiped(bounds);
        p1.setColor(face);
        p1.setWireframeColor(wireframe);
        chart.getScene().add(p1);
    }
    
    public static Chart initChart() {
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
        
        Chart chart = new Chart(factory, Quality.Nicest, "awt", capabilities);
        chart.getView().setSquared(false);
        //chart.getView().setCameraMode(CameraMode.PERSPECTIVE);
        return chart;
    }    
    public static boolean CHART_CANVAS_AUTOSWAP = true;
}
