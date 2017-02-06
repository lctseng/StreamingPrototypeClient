package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lctseng on 2017/2/6.
 */

public class Display implements Disposable{

    private Pixmap image;
    private ByteBuffer imageBuf;

    private Connection conn;
    private Profiler profiler;

    // connection buffers
    private byte[] bufHeader;
    private byte[] bufData;

    private StreamingPrototype app;

    Display(StreamingPrototype app){
        this.app = app;

        image = new Pixmap(133, 200, Pixmap.Format.RGBA8888);
        imageBuf = image.getPixels();

        bufHeader = new byte[4];
        bufData = new byte[106400];

        conn = null;
        profiler = new Profiler();
    }

    public void setConnection(Connection conn){
        this.conn = conn;
    }

    public void setProfiler(Profiler profiler){
        this.profiler = profiler;
    }

    public Texture receiveNextTexture(){
        if(conn != null){
            if(conn.readn(bufHeader) == 4){
                int n = byteArrayToLeInt(bufHeader);
                // read image data
                profiler.reportOnRecvStart();
                // start recv
                app.addTextDraw("Image data size:" + n);
                int r = conn.readn(bufData, n);
                if(r == n){
                    // end recv
                    profiler.reportOnRecvEnd();
                    // start proc
                    profiler.reportOnProcStart();
                    imageBuf.rewind();
                    imageBuf.put(bufData);
                    imageBuf.rewind();
                    Texture tex = new Texture(image);
                    // end proc
                    profiler.reportOnProcEnd();
                    return tex;
                }
                else{
                    Gdx.app.error("Display", "Cannot read data. Data size mismatch");
                    return null;
                }
            }
            else{
                Gdx.app.error("Display", "Cannot read header. Header size mismatch");
                return null;
            }
        }
        else{
            Gdx.app.error("Display", "No Connection object assigned");
            return null;
        }
    }

    private static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    @Override
    public void dispose() {
        image.dispose();
    }
}
