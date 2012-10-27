package org.jzy3d.io;

import static javax.media.opengl.GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

/**
 * GLSL program lifecycle:
 * <ul>
 * <li>load shaders with {@link attachVertexShader()} and {@link attachFragmentShader()}
 *     <ul>
 *     <li>glCreateShader
 *     <li>glShaderSource
 *     <li>glCompileShader
 *     <li>glGetShaderiv (verify status)
 *     <li>glGetShaderInfoLog (log errors)
 *     </ul>
 * <li>link(gl)
 *      <ul>
 *      <li>glCreateProgram
 *      <li>glAttachShader
 *      <li>glLinkProgram
 *      <li>glGetProgramiv (verify status)
 *      <li>glGetProgramInfoLog (log errors)
 *      <li>glValidateProgram 
 *      </ul>
 * <li>bind(gl) mount the program @ rendering
 *      <ul>
 *      <li>glUseProgram
 *      </ul>
 * <li>{@link bindTextureRECT(gl)}
 * <li>unbind(gl) unmount the program @ rendering
 *      <ul>
 *      <li>glUseProgram(0)
 *      </ul>
 * <li>destroy(gl)
 *      <ul>
 *      <li>glDeleteShader
 *      <li>glDeleteProgram
 *      </ul>
 * </ul>
 *
 */
public class GLSLProgramOld {
    public enum Strictness{
        /** Let the GLSL program throw {@link RuntimeException}s on warnings. */ 
        MAXIMAL,
        /** Let the GLSL program be verbose through {@link System.out.println()}. */ 
        CONSOLE,
        /** Let the GLSL program be verbose through {@link System.out.println()}, unless the warning is due to
         * a uniform that is set by GL but not actually used by the compiled shader. */ 
        CONSOLE_NO_WARN_UNIFORM_NOT_FOUND,
        /** Let the GLSL program push warnings to a {@link StringBuffer} to be read. */ 
        BUFFER,
        /** Keeps the GLSL program quiet on warnings */ 
        NONE
    }
    
    public static Strictness DEFAULT_STRICTNESS = Strictness.CONSOLE;
    

    
    public GLSLProgramOld() {
        this(DEFAULT_STRICTNESS);
    }
    public GLSLProgramOld(Strictness strictness) {
        this.strictness = strictness;
        this.programId = 0; // will be defined @ link stage by GL
        if(strictness==Strictness.BUFFER)
            warnBuffer = new StringBuffer();
    }
    
    /**
     * Create a program and attach previously loaded and compiled shaders.
     * Performs validation and warn according to program strictness.
     */
    public void link(GL2 gl) {
        programId = gl.glCreateProgram();
        for (int i = 0; i < vertexShaders_.size(); i++) {
            gl.glAttachShader(programId, vertexShaders_.get(i));
        }

        for (int i = 0; i < fragmentShaders_.size(); i++) {
            gl.glAttachShader(programId, fragmentShaders_.get(i));
        }

        gl.glLinkProgram(programId);
        verifyLinkStatus(gl, programId);
        
        // validation
        validateProgram(gl);
    }
    
    public void bind(GL2 gl) {
        gl.glUseProgram(programId);
    }

    public void unbind(GL2 gl) {
        gl.glUseProgram(0);
    }

    public void destroy(GL2 gl) {
        for (int i = 0; i < vertexShaders_.size(); i++) {
            gl.glDeleteShader(vertexShaders_.get(i));
        }
        for (int i = 0; i < fragmentShaders_.size(); i++) {
            gl.glDeleteShader(fragmentShaders_.get(i));
        }
        if (programId != 0) {
            gl.glDeleteProgram(programId);
        }
    }

    /* UNIFORM SETTING */

    public void setUniform(GL2 gl, String name, float value) {
        int id = gl.glGetUniformLocation(programId, name);
        gl.glUniform1f(id, value);
    }
    
    public void setUniform(GL2 gl, String name, float[] values, int count) {
        int id = gl.glGetUniformLocation(programId, name);
        if (id == -1) {
            warn("Uniform parameter not found in program: " + name, GLSLWarnType.UNIFORM_NOT_FOUND);
            return;
        }
        switch (count) {
        case 1:
            gl.glUniform1fv(id, 1, values, 0);
            break;
        case 2:
            gl.glUniform2fv(id, 1, values, 0);
            break;
        case 3:
            gl.glUniform3fv(id, 1, values, 0);
            break;
        case 4:
            gl.glUniform4fv(id, 1, values, 0);
            break;
        }
    }
    
    /* TEXTURES */

    public void setTextureUnit(GL2 gl, String texname, int texunit) {
        int[] params = new int[] { 0 };
        gl.glGetProgramiv(programId, GL2.GL_LINK_STATUS, params, 0);
        if (params[0] != 1) {
            throw new RuntimeException("Error: setTextureUnit needs program to be linked.");
        }
        int id = gl.glGetUniformLocation(programId, texname);
        if (id == -1) {
            warn("Invalid texture " + texname, GLSLWarnType.UNDEFINED);
            return;
        }
        gl.glUniform1i(id, texunit);
    }

    public void bindTexture(GL2 gl, int target, String texname, int texid, int texunit) {
        gl.glActiveTexture(GL2.GL_TEXTURE0 + texunit);
        gl.glBindTexture(target, texid);
        setTextureUnit(gl, texname, texunit);
        gl.glActiveTexture(GL2.GL_TEXTURE0);
    }

    public void bindTexture2D(GL2 gl, String texname, int texid, int texunit) {
        bindTexture(gl, GL2.GL_TEXTURE_2D, texname, texid, texunit);
    }

    public void bindTexture3D(GL2 gl, String texname, int texid, int texunit) {
        bindTexture(gl, GL2.GL_TEXTURE_3D, texname, texid, texunit);
    }

    public void bindTextureRECT(GL2 gl, String texname, int texid, int texunit) {
        bindTexture(gl, GL2.GL_TEXTURE_RECTANGLE_ARB, texname, texid, texunit);
    }

    /* SHADER LOAD & COMPILE  */
    
    public void loadAndCompileVertexShader(GL2 gl, URL fileURL) {
        if (fileURL != null) {
            String content = "";
            BufferedReader input = null;
            try {

                input = new BufferedReader(new InputStreamReader(fileURL.openStream()));
                String line = null;

                while ((line = input.readLine()) != null) {
                    content += line + "\n";
                }
            } catch (FileNotFoundException kFNF) {
                throw new RuntimeException("Unable to find the shader file " + fileURL.getPath());
            } catch (IOException kIO) {
                throw new RuntimeException("Problem reading the shader file " + fileURL.getPath());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException closee) {
                }
            }

            int iID = gl.glCreateShader(GL2.GL_VERTEX_SHADER);

            String[] akProgramText = new String[1];
            // find and replace program name with "main"
            akProgramText[0] = content;

            int[] params = new int[] { 0 };

            int[] aiLength = new int[1];
            aiLength[0] = akProgramText[0].length();
            int iCount = 1;
            gl.glShaderSource(iID, iCount, akProgramText, aiLength, 0);
            gl.glCompileShader(iID);
            verifyShaderCompiled(gl, fileURL, iID, params);
            vertexShaders_.add(iID);
        } else {
            throw new RuntimeException("Null shader file!");
        }
    }
    
    public void loadAndCompileFragmentShader(GL2 gl, URL fileURL) {
        if (fileURL != null) {
            String content = "";
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(fileURL.openStream()));
                String line = null;
                while ((line = input.readLine()) != null) {
                    content += line + "\n";
                }
            } catch (FileNotFoundException kFNF) {
                throw new RuntimeException("Unable to find the shader file " + fileURL.getPath());
            } catch (IOException kIO) {
                throw new RuntimeException("Problem reading the shader file " + fileURL.getPath());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException closee) {
                }
            }

            int iID = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);

            String[] akProgramText = new String[1];
            // find and replace program name with "main"
            akProgramText[0] = content;
            int[] params = new int[] { 0 };
            int[] aiLength = new int[1];
            aiLength[0] = akProgramText[0].length();
            int iCount = 1;
            gl.glShaderSource(iID, iCount, akProgramText, aiLength, 0);
            gl.glCompileShader(iID);
            verifyShaderCompiled(gl, fileURL, iID, params);
            fragmentShaders_.add(iID);
        } else {
            throw new RuntimeException("Null shader file!");
        }
    }

    /* VERIFICATIONS */
    
    public void verifyShaderCompiled(GL2 gl, URL fileURL, int iID, int[] params) {
        gl.glGetShaderiv(iID, GL2.GL_COMPILE_STATUS, params, 0);
        
        if (params[0] != 1) {
            warn(fileURL.getPath(), GLSLWarnType.UNDEFINED);
            warn("compile status: " + params[0], GLSLWarnType.UNDEFINED);
            
            gl.glGetShaderiv(iID, GL2.GL_INFO_LOG_LENGTH, params, 0);
            warn("log length: " + params[0], GLSLWarnType.UNDEFINED);
            
            byte[] abInfoLog = new byte[params[0]];
            gl.glGetShaderInfoLog(iID, params[0], params, 0, abInfoLog, 0);
            warn(new String(abInfoLog), GLSLWarnType.UNDEFINED);
        }
    }
    
    public void verifyLinkStatus(GL2 gl, int programId) {
        int[] params = new int[] { 0 };
        gl.glGetProgramiv(programId, GL2.GL_LINK_STATUS, params, 0);

        if (params[0] != 1) {
            warn("link status: " + params[0], GLSLWarnType.UNDEFINED);
            gl.glGetProgramiv(programId, GL2.GL_INFO_LOG_LENGTH, params, 0);
            warn("log length: " + params[0], GLSLWarnType.UNDEFINED);

            byte[] abInfoLog = new byte[params[0]];
            gl.glGetProgramInfoLog(programId, params[0], params, 0, abInfoLog, 0);
            warn(new String(abInfoLog), GLSLWarnType.UNDEFINED);
        }
    }
    
    public void validateProgram(GL2 gl) {
        gl.glValidateProgram(programId);
        checkShaderLogInfo(gl, programId);
    }

    //demoscene
    protected void checkShaderLogInfo(GL2 inGL, int inShaderObjectID) {
        IntBuffer tReturnValue = Buffers.newDirectIntBuffer(1);
        inGL.glGetObjectParameterivARB(inShaderObjectID, GL_OBJECT_INFO_LOG_LENGTH_ARB, tReturnValue);
        int tLogLength = tReturnValue.get();
        if (tLogLength <= 1) {
            return;
        }
        ByteBuffer tShaderLog = Buffers.newDirectByteBuffer(tLogLength);
        tReturnValue.flip();
        inGL.glGetInfoLogARB(inShaderObjectID, tLogLength, tReturnValue, tShaderLog);
        byte[] tShaderLogBytes = new byte[tLogLength];
        tShaderLog.get(tShaderLogBytes);
        String tShaderValidationLog = new String(tShaderLogBytes);
        StringReader tStringReader = new StringReader(tShaderValidationLog);
        LineNumberReader tLineNumberReader = new LineNumberReader(tStringReader);
        String tCurrentLine;
        try {
            while ((tCurrentLine = tLineNumberReader.readLine()) != null) {
                if (tCurrentLine.trim().length()>0) {
                    warn("GLSL VALIDATION: "+tCurrentLine.trim(), GLSLWarnType.UNDEFINED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /* */
    
    protected void warn(String info, GLSLWarnType type){
        
        if(strictness==Strictness.MAXIMAL)
            throw new RuntimeException(info);
        else if (strictness==Strictness.CONSOLE)
            System.err.println(this.getClass().getSimpleName() + ": " + info);
        else if (strictness==Strictness.CONSOLE_NO_WARN_UNIFORM_NOT_FOUND
                && type!=GLSLWarnType.UNIFORM_NOT_FOUND){
            System.err.println(this.getClass().getSimpleName() + ": " + info);            
        }
        else if (strictness==Strictness.BUFFER)
            warnBuffer.append(info + "\n");
        else if (strictness==Strictness.NONE)
            ; // do nothing
    }
    
    public enum GLSLWarnType{
        UNDEFINED,
        UNIFORM_NOT_FOUND
    }
    
    /* */
     
    public Integer getProgId() {
        return programId;
    }

    protected Integer programId;
    protected List<Integer> vertexShaders_ = new ArrayList<Integer>();
    protected List<Integer> fragmentShaders_ = new ArrayList<Integer>();
    protected StringBuffer warnBuffer;
    protected Strictness strictness;
};
