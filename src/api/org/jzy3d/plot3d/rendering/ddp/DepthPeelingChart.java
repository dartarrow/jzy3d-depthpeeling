package org.jzy3d.plot3d.rendering.ddp;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.io.glsl.GLSLProgram;
import org.jzy3d.io.glsl.GLSLProgram.Strictness;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.ddp.algorithms.PeelingMethod;

public class DepthPeelingChart extends Chart{
    public static Chart get(Quality quality, String chartType) {
        return get(quality, chartType, PeelingMethod.DUAL_PEELING_MODE);
    }

    public static Chart get(Quality quality, String chartType, PeelingMethod method) {
        return get(quality, chartType, method, Strictness.CONSOLE_NO_WARN_UNIFORM_NOT_FOUND);
    }

    public static Chart get(Quality quality, String chartType, PeelingMethod method, Strictness strictness) {
        return get(quality, chartType, method, strictness, true);
    }
    
    public static Chart get(Quality quality, String chartType, final PeelingMethod method, Strictness strictness, boolean editFactories) {
        GLSLProgram.DEFAULT_STRICTNESS = strictness;
        
        IChartComponentFactory factory = new PeelingComponentFactory(method);
        
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(true);
        
        Chart chart = new Chart(factory, quality, chartType, capabilities);
        chart.getView().setSquared(false);
        chart.getView().setAxeBoxDisplayed(true);
        return chart;
    }
}
