package com.example.tattoo_ar.view.recognition

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log

import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext

import cn.easyar.Vec2F
import cn.easyar.Matrix44F
import java.nio.ShortBuffer
import kotlin.math.sqrt

class TattooRenderer(
    private val textureBitmap: Bitmap,
) {
    companion object {
        private val TAG = TattooRenderer::class.java.simpleName
        private val SCALE = 2.0f
        private val TINT = listOf(254f, 227f, 212f).map { it / 255f}.toFloatArray()
    }

    private var currentContext: EGLContext? = null
    private val program: Int
    private val coordLocation: Int
    private val transLocation: Int
    private val projLocation: Int
    private val texCoordLocation: Int
    private val samplerLocation: Int
    private val textures = IntArray(1)
    private val markerRadiusLocation: Int
    private val fadeRadiusLocation: Int
    private val tintLocation: Int
    private val coordVbo: Int
    private val facesVbo: Int
    private val texVbo: Int

    private val plane_vert = """
        uniform mat4 u_trans;
        uniform mat4 u_proj;
        
        attribute vec4 a_coord;
        attribute vec2 a_texCoord;
        
        varying vec2 v_texCoord;
        
        void main(void) {
            gl_Position = u_proj * u_trans * a_coord;
            v_texCoord = a_texCoord;
        }
        """

    private val plane_frag = """
        #ifdef GL_ES
        precision highp float;
        #endif
        
        uniform sampler2D u_texture;
        uniform float u_markerRadius;
        uniform float u_fadeRadius;
        uniform vec3 u_tint;
        
        varying vec2 v_texCoord;
        
        void main(void) {
            vec2 center = vec2(0.5, 0.5);
            vec4 color = texture2D(u_texture, v_texCoord);
            float dist = distance(v_texCoord, center);
            float value = normalize(length(color.rgb));
            float valueThreshold = 0.8;
            
            if (value > valueThreshold) {
                if (dist < u_markerRadius) {
                    color.a = 1.0;
                } else if (dist > u_fadeRadius) {
                    color.a = 0.0;
                } else {
                    float alpha = (dist - u_fadeRadius) / (u_markerRadius - u_fadeRadius);
                    color.a = 1.0 - value * (1.0 - alpha);
                }
                
                color.r = color.r * u_tint.x;
                color.g = color.g * u_tint.y;
                color.b = color.b * u_tint.z;
            }
            
            gl_FragColor = color;
        }
        """

    init {
        currentContext = (EGLContext.getEGL() as EGL10).eglGetCurrentContext()
        program = GLES30.glCreateProgram()
        val vertShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also {
            GLES30.glShaderSource(it, plane_vert)
            GLES30.glCompileShader(it)
        }
        val fragShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also {
            GLES30.glShaderSource(it, plane_frag)
            GLES30.glCompileShader(it)
        }

        GLES30.glAttachShader(program, vertShader)
        GLES30.glAttachShader(program, fragShader)
        GLES30.glLinkProgram(program)
        GLES30.glUseProgram(program)
        GLES30.glDeleteShader(vertShader)
        GLES30.glDeleteShader(fragShader)
        checkGLError("Create shaders")

        coordLocation = GLES30.glGetAttribLocation(program, "a_coord")
        transLocation = GLES30.glGetUniformLocation(program, "u_trans")
        projLocation = GLES30.glGetUniformLocation(program, "u_proj")
        texCoordLocation = GLES30.glGetAttribLocation(program, "a_texCoord")
        samplerLocation = GLES30.glGetUniformLocation(program, "u_texture");
        markerRadiusLocation = GLES30.glGetUniformLocation(program, "u_markerRadius");
        fadeRadiusLocation = GLES30.glGetUniformLocation(program, "u_fadeRadius");
        tintLocation = GLES30.glGetUniformLocation(program, "u_tint");
        checkGLError("Create location handlers")

        coordVbo = generateOneBuffer()
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, coordVbo)

        facesVbo = generateOneBuffer()
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, facesVbo)
        val facesBuffer = arrayOf(
            shortArrayOf(0, 1, 2, 3),
        ).let { ShortBuffer.wrap(flatten(it)) }
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, facesBuffer.limit() * 2, facesBuffer, GLES30.GL_STATIC_DRAW)
        checkGLError("Create coord and faces vertex buffer objects")

        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);
        GLES30.glGenTextures(textures.size, textures, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, textureBitmap, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        checkGLError("Create texture")

        texVbo = generateOneBuffer()
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, texVbo)
        val texCoordBuffer = arrayOf(
            floatArrayOf(1f, 1f),
            floatArrayOf(1f, 0f),
            floatArrayOf(0f, 0f),
            floatArrayOf(0f, 1f),
        ).let { FloatBuffer.wrap(flatten(it)) }
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoordBuffer.limit() * 4, texCoordBuffer, GLES30.GL_STATIC_DRAW)
        checkGLError("Create texture coord vertex buffer object")
    }

    fun render(projectionMatrix: Matrix44F, cameraView: Matrix44F, size: Vec2F) {
        val size0 = size.data[0] * SCALE
        val size1 = size.data[1] * SCALE

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, coordVbo)
        val faceVerticesBuffer = arrayOf(
            floatArrayOf(-size0 / 2, -size1 / 2, 0f, 1f),
            floatArrayOf(-size0 / 2, size1 / 2, 0f, 1f),
            floatArrayOf(size0 / 2, size1 / 2, 0f, 1f),
            floatArrayOf(size0 / 2, -size1 / 2, 0f, 1f),
        ).let { FloatBuffer.wrap(flatten(it)) }
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, faceVerticesBuffer.limit() * 4, faceVerticesBuffer, GLES30.GL_DYNAMIC_DRAW)
        checkGLError("Set face vertices buffer")

        GLES30.glUseProgram(program)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, coordVbo)
        GLES30.glEnableVertexAttribArray(coordLocation)
        GLES30.glVertexAttribPointer(coordLocation, 4, GLES30.GL_FLOAT, false, 0, 0)
        checkGLError("Set coord")

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glUniform1i(samplerLocation, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, texVbo)
        GLES30.glEnableVertexAttribArray(texCoordLocation)
        GLES30.glVertexAttribPointer(texCoordLocation, 2, GLES30.GL_FLOAT, false, 0, 0)
        checkGLError("Set texture")

        GLES30.glUniform1f(markerRadiusLocation, sqrt(2.0f) / (2f * SCALE))
        GLES30.glUniform1f(fadeRadiusLocation, sqrt(2.0f) * (1f / 4f + 1f / (4f * SCALE)))
        GLES30.glUniform3fv(tintLocation, 1, TINT, 0)
        GLES30.glUniformMatrix4fv(transLocation, 1, false, getGLMatrix(cameraView), 0)
        GLES30.glUniformMatrix4fv(projLocation, 1, false, getGLMatrix(projectionMatrix), 0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, facesVbo)
        GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, 4, GLES30.GL_UNSIGNED_SHORT, 0)
        checkGLError("Draw face")

        GLES30.glDisableVertexAttribArray(coordLocation)
        GLES30.glDisableVertexAttribArray(texCoordLocation)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        checkGLError("Clean up")
    }

    fun dispose() {
        if ((EGLContext.getEGL() as EGL10).eglGetCurrentContext() == currentContext) { //destroy resources unless the context has lost
            GLES30.glDeleteProgram(program)
            deleteOneBuffer(coordVbo)
            deleteOneBuffer(texVbo)
            deleteOneBuffer(facesVbo)
        }
    }

    private fun flatten(a: Array<ShortArray>): ShortArray {
        return flatten(a.map { it.toTypedArray() }.toTypedArray()).toShortArray()
    }

    private fun flatten(a: Array<FloatArray>): FloatArray {
        return flatten(a.map { it.toTypedArray() }.toTypedArray()).toFloatArray()
    }

    private inline fun <reified T> flatten(a: Array<Array<T>>): Array<T> {
        var size = 0
        run {
            var k = 0
            while (k < a.size) {
                size += a[k].size
                k += 1
            }
        }
        val l = Array(size) { a[0][0] }
        var offset = 0
        var k = 0
        while (k < a.size) {
            System.arraycopy(a[k], 0, l, offset, a[k].size)
            offset += a[k].size
            k += 1
        }
        return l
    }

    private fun generateOneBuffer(): Int {
        val buffer = intArrayOf(0)
        GLES30.glGenBuffers(1, buffer, 0)
        return buffer[0]
    }

    private fun deleteOneBuffer(id: Int) {
        val buffer = intArrayOf(id)
        GLES30.glDeleteBuffers(1, buffer, 0)
    }

    private fun getGLMatrix(m: Matrix44F): FloatArray {
        val d = m.data
        return floatArrayOf(d[0], d[4], d[8], d[12], d[1], d[5], d[9], d[13], d[2], d[6], d[10], d[14], d[3], d[7], d[11], d[15])
    }

    private fun checkGLError(label: String) {
        var lastError = GLES30.GL_NO_ERROR
        var error: Int

        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "$label: glError $error")
            lastError = error
        }

        if (lastError != GLES30.GL_NO_ERROR) {
            throw RuntimeException("$label: glError $lastError")
        }
    }
}
