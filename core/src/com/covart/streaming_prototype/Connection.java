package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
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
 * NTU COV-ART Lab, for NCP project
 */

public class Connection {

    enum State{
        Disconnected, Connecting, Connected
    }

    private volatile State state;
    private Socket socket;

    private InputStream recvStream;
    private OutputStream sendStream;

    private ConnectionListener listener;


    Connection(ConnectionListener listener)
    {
        state = State.Disconnected;
        this.listener = listener;
    }

    public void connect(){
        // only issue a connection when disconnected
        if(state == State.Disconnected){
            if(socket  != null){
                // delete previous socket  if exists
                socket.dispose();
                socket = null;
            }
            state = State.Connecting;
            listener.onConnectionStarted();
            Gdx.app.log("Connection", "Connecting...");
            SocketHints hints = new SocketHints();
            try {
                socket = net.newClientSocket(Protocol.TCP, "140.112.90.95", 3333, hints);
                recvStream = socket.getInputStream();
                sendStream = socket.getOutputStream();
                state = State.Connected;
                listener.onConnectionReady();
                Gdx.app.log("Connection", "Connected");
            }
            catch (GdxRuntimeException e){
                Gdx.app.log("Connection", "Disconnected");
                e.printStackTrace();
                close();
            }
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
        }
        if(state != State.Disconnected){
            state = State.Disconnected;
            listener.onConnectionClose();
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
