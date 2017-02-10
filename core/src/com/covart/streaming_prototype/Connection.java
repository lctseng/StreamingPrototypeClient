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
            state = State.Connecting;
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        SocketHints hints = new SocketHints();
                        try {
                            socket = net.newClientSocket(Protocol.TCP, "140.112.90.95", 3333, hints);
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

    public int readn(byte[] array, int n){
        try{
            int total_read_n = 0;
            while(total_read_n < n){
                int nRead = recvStream.read(array, total_read_n, n - total_read_n);
                if(nRead <= 0){
                    // EOF
                    close();
                    return -1;
                }
                else{
                    total_read_n += nRead;
                }
            }
            return total_read_n;
        }
        catch (IOException e){
            close();
            return -1;
        }
    }

    public int readn(byte[] array){
        return readn(array, array.length);
    }


    public int read(byte[] array, int offset, int len){
        try{
            int nRead = recvStream.read(array, offset, len);
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
