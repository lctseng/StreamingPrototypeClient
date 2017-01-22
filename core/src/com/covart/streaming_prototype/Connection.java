package com.covart.streaming_prototype;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.badlogic.gdx.Gdx.net;

/**
 * Created by lctseng on 2017/1/22.
 */

public class Connection {

    enum State{
        Disconnected, Connecting, Connected
    }

    private State state;
    private Socket socket;

    public InputStream recvStream;
    public OutputStream sendStream;


    Connection(){
        state = State.Disconnected;
    }

    synchronized public void connect(){
        // only issue a connection when disconnected
        if(state == State.Disconnected){
            if(socket  != null){
                // delete previous socket  if exists
                socket.dispose();
                socket = null;
            }
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        state = State.Connecting;
                        SocketHints hints = new SocketHints();
                        try {
                            socket = net.newClientSocket(Protocol.TCP, "covart.csie.org", 3333, hints);
                            recvStream = socket.getInputStream();
                            sendStream = socket.getOutputStream();
                            state = State.Connected;
                        }
                        catch (GdxRuntimeException e){
                            state = State.Disconnected;
                        }
                    }
                }
            ).start();
         }
    }

    public boolean getReady(){
        return state == State.Connected;
    }

    public State getState(){
        return state;
    }

    public String getStateText(){
        switch(getState()){
            case Disconnected:
                return "Disconnected";
            case Connected:
                return "Connected";
            case Connecting:
                return "Connecting";
            default:
                return "Unknown";
        }
    }

    public void dispose(){
        close();
    }

    synchronized public void close(){
        if(socket != null){
            socket.dispose();
            socket = null;
            recvStream = null;
            sendStream = null;
            state = State.Disconnected;
        }
    }

    public int read(byte[] array){
        try{
            int nRead = recvStream.read(array);
            if(nRead < 0){
                // EOF
                close();
            }
            return nRead;
        }
        catch (IOException e){
            close();
            return 0;
        }
    }

    public boolean write(byte[] array){
        try {
            sendStream.write(array);
            return true;
        } catch (IOException e) {
            close();
            return false;
        }
    }

}
