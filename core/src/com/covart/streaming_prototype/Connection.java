package com.covart.streaming_prototype;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.InputStream;
import java.io.OutputStream;

import static com.badlogic.gdx.Gdx.net;

/**
 * Created by lctseng on 2017/1/22.
 */

public class Connection {
    private boolean ready;
    private Socket socket;

    public InputStream recvStream;
    public OutputStream sendStream;


    Connection(){
        ready = false;
    }

    synchronized public void connect(){
        if(!getReady()){
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        SocketHints hints = new SocketHints();
                        try {
                            socket = net.newClientSocket(Protocol.TCP, "covart.csie.org", 3333, hints);
                            recvStream = socket.getInputStream();
                            sendStream = socket.getOutputStream();
                            ready = true;
                        }
                        catch (GdxRuntimeException e){
                            ready = false;
                        }
                    }
                }
            ).start();
         }
    }

    public boolean getReady(){
        return ready;
    }

    public void dispose(){
        if(socket != null){
            socket.dispose();
            ready = false;
        }
    }

}
