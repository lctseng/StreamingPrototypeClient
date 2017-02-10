package com.covart.streaming_prototype;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lctseng on 2017/2/10.
 * NTU COV-ART Lab, for NCP project
 */

public class PackInteger {
    public static int unpack(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static byte[] pack(int i){
        byte[] buffer = new byte[4];
        buffer[3] = (byte) ((i >> 24) & 0xFF);
        buffer[2] = (byte) ((i >> 16) & 0xFF);
        buffer[1] = (byte) ((i >> 8) & 0xFF);
        buffer[0] = (byte) ((i) & 0xFF);
        return buffer;
    }
}
