package com.covart.streaming_prototype;

/**
 * Created by lctseng on 2017/2/20.
 * NTU COV-ART Lab, for NCP project
 */

public class Buffer {
    public int index;
    public int size;
    public int capacity;
    public byte[] data;
    Buffer(int capacity){
        this.capacity = capacity;
        data = new byte[capacity];
        size = 0;
    }
}
