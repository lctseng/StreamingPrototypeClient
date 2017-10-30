package com.covart.streaming_prototype.Net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.covart.streaming_prototype.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

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

    private boolean writable;
    private boolean readable;


    Connection(ConnectionListener listener)
    {
        state = State.Disconnected;
        this.listener = listener;
        recvStream = null;
        sendStream = null;
        writable = false;
        readable = false;
    }

    public static boolean validateServerString(String text){
        return text.matches("\\A\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}\\z");
    }

    public void connect(){
        String ipString =ConfigManager.getSelectedIP();
        if(validateServerString(ipString)) {

            // only issue a connection when disconnected
            if (state == State.Disconnected) {
                if (socket != null) {
                    // delete previous socket  if exists
                    socket.dispose();
                    socket = null;
                }
                state = State.Connecting;
                listener.onConnectionStarted();
                Gdx.app.log("Connection", "Connecting...");
                SocketHints hints = new SocketHints();
                try {
                    String[] tokens = ipString.split(Pattern.quote(":"));
                    String ip_str = tokens[0];
                    String port_str = tokens[1];
                    socket = net.newClientSocket(Protocol.TCP, ip_str, Integer.parseInt(port_str), hints);
                    recvStream = socket.getInputStream();
                    sendStream = socket.getOutputStream();
                    state = State.Connected;
                    writable = true;
                    readable = true;
                    listener.onConnectionReady();
                    Gdx.app.log("Connection", "Connected");
                } catch (GdxRuntimeException e) {
                    Gdx.app.log("Connection", "Disconnected");
                    e.printStackTrace();
                    close();
                }
            }
        }
        else{
            Gdx.app.log("Connection", "Cannot connect, IP string invalid: " + ipString);
        }
    }

    public boolean getReady(){
        return state == State.Connected;
    }

    public State getState(){
        return state;
    }

    public String getStateText(){
        String rw_state = ": R: " + Boolean.toString(readable) + ", W: " + Boolean.toString(writable);
        switch(getState()){
            case Disconnected:
                return "Disconnected" + rw_state;
            case Connected:
                return "Connected" + rw_state;
            case Connecting:
                return "Connecting" + rw_state;
            default:
                return "Unknown" + rw_state;
        }
    }

    public void dispose(){
        close();
    }

    synchronized public void closeRead(){
        if(readable){
            readable = false;
            try {
                recvStream.close();
            } catch (IOException e) {
                Gdx.app.error("Connection", "Error when closing read end");
                e.printStackTrace();
            }
        }
        if(!writable){
            disposeSocket();
        }
    }

    synchronized public void closeWrite(){
        if(writable){
            writable = false;
            try {
                sendStream.close();
            } catch (IOException e) {
                Gdx.app.error("Connection", "Error when closing write end");
                e.printStackTrace();
            }
        }
        if(!readable){
            disposeSocket();
        }
    }

    synchronized public void close(){
        closeRead();
        closeWrite();
        if(state != State.Disconnected){
            state = State.Disconnected;
            listener.onConnectionClose();
        }
    }

    private void disposeSocket(){
        if(socket != null){
            socket.dispose();
            socket = null;
        }
    }

    public int read(byte[] array){
        if(state != State.Connected || !readable){
            return -1;
        }
        try{
            int nRead = recvStream.read(array);
            if(nRead < 0){
                // EOF
                Gdx.app.error("Connection", "Read closed due to nRead < 0");
                closeRead();
            }
            return nRead;
        }
        catch (IOException e){
            Gdx.app.error("Connection", "Read closed due to IOException");
            closeRead();
            return 0;
        }
    }

    public int readn(byte[] array, int n){
        if(state != State.Connected || !readable){
            return -1;
        }
        try{
            int total_read_n = 0;
            while(total_read_n < n){
                int nRead = recvStream.read(array, total_read_n, n - total_read_n);
                if(nRead < 0){
                    // EOF
                    Gdx.app.error("Connection", "Read closed due to nRead < 0");
                    closeRead();
                    return -1;
                }
                else{
                    total_read_n += nRead;
                }
            }
            return total_read_n;
        }
        catch (IOException e){
            Gdx.app.error("Connection", "Read closed due to IOException");
            closeRead();
            return -1;
        }
    }

    public int readn(byte[] array){
        return readn(array, array.length);
    }


    public int read(byte[] array, int offset, int len){
        if(state != State.Connected || !readable){
            return -1;
        }
        try{
            int nRead = recvStream.read(array, offset, len);
            if(nRead < 0){
                // EOF
                Gdx.app.error("Connection", "Read closed due to nRead < 0");
                closeRead();
            }
            return nRead;
        }
        catch (IOException e){
            Gdx.app.error("Connection", "Read closed due to IOException");
            closeRead();
            return 0;
        }
    }

    public boolean write(byte[] array){
        if(state != State.Connected || !writable){
            return false;
        }
        try {
            sendStream.write(array);
            return true;
        } catch (IOException e) {
            Gdx.app.error("Connection", "Write closed due to IOException");
            closeWrite();
            return false;
        }
    }

}
