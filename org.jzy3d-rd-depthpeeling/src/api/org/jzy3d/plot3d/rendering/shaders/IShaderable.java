package org.jzy3d.plot3d.rendering.shaders;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jzy3d.plot3d.primitives.IGLRenderer;

public interface IShaderable {
    public void init(GL2 gl, int width, int height);
    public void display(GL2 gl, GLU glu);
    public void reshape(GL2 gl, int width, int height);
    public void dispose(GL2 gl);
    public IGLRenderer getTasksToRender();
    public void setTasksToRender(IGLRenderer tasksToRender);
}
