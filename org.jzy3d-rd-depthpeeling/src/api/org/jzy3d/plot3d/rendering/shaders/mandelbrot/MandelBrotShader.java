package org.jzy3d.plot3d.rendering.shaders.mandelbrot;

import javax.media.opengl.GL2;

import org.jzy3d.io.GLSLProgram;
import org.jzy3d.io.ShaderFilePair;
import org.jzy3d.plot3d.rendering.shaders.Shaderable;

public class MandelBrotShader extends Shaderable{
    MandelbrotSetting settings = new MandelbrotSetting();
    protected static final ShaderFilePair FILES = new ShaderFilePair(MandelBrotShader.class, "mandelbrot.vs", "mandelbrot.fs");

    protected void loadProgram(GL2 gl) {
        program = new GLSLProgram();
        program.loadAndCompileVertexShader(gl, FILES.getVertexURL());
        program.loadAndCompileFragmentShader(gl, FILES.getFragmentURL());
        program.link(gl);
        
        //gl.setSwapInterval(1);
        
    }

    protected void destroyProgram(GL2 gl) {
        program.destroy(gl);
    }

    protected void executeProgram(GL2 gl) {
        //gl.glShadeModel(GL2.GL_FLAT);
        //gl.setSwapInterval(1);
        
        program.bind(gl);
        
        // passing parameters
        program.setUniform(gl, "mandel_x", settings.getX());
        program.setUniform(gl, "mandel_y", settings.getY());
        program.setUniform(gl, "mandel_width", settings.getWidth());
        program.setUniform(gl, "mandel_height", settings.getHeight());
        program.setUniform(gl, "mandel_iterations", settings.getIterations());

        renderTasks(gl); 
        
        program.unbind(gl);
    }
}
