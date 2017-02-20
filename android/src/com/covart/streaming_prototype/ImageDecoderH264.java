package com.covart.streaming_prototype;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.createDecoderByType;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderH264 extends ImageDecoderBase {



    private volatile MediaCodec codec;


    ImageDecoderH264(){
        super();


    }

    @Override
    public void run() {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "video/avc");
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100000);
        format.setInteger(MediaFormat.KEY_WIDTH, 399);
        format.setInteger(MediaFormat.KEY_HEIGHT, 600);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888);
        try {
            codec = createDecoderByType("video/avc");
            codec.configure(format, null, null, 0);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
            codec = null;
        }
        if(codec !=null) {
            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while (true) {
                try {
                    // read from network
                    Profiler.reportOnProcStart();
                    int inputBufferId = codec.dequeueInputBuffer(-1);
                    if (inputBufferId >= 0) {
                        // retrieve data from netowrk
                        Buffer encodedBuf = acquireEncodedResult();
                        Gdx.app.log("H264", "Raw H264 stream, size: " + encodedBuf.size);
                        inputBuffers[inputBufferId].rewind();
                        inputBuffers[inputBufferId].put(encodedBuf.data);
                        // fill inputBuffers[inputBufferId] with valid data
                        codec.queueInputBuffer(inputBufferId, 0 , encodedBuf.size, 40000 , 0);
                        // release buffer to network
                        releaseEncodedBuffer(encodedBuf);
                    }
                    // store to display
                    // get image buffer if can

                    int outputBufferId = codec.dequeueOutputBuffer(info, -1);
                    if (outputBufferId >= 0) {
                        // outputBuffers[outputBufferId] is ready to be processed or rendered.
                        // get display buffer
                        Buffer decodeBuf = acquireImageBuffer();
                        // copy data
                        outputBuffers[outputBufferId].get(decodeBuf.data);
                        decodeBuf.size = outputBuffers[outputBufferId].position();
                        Gdx.app.log("H264", "Decoded image data, size: " + decodeBuf.size);
                        // send to display
                        sendImageResult(decodeBuf);
                        // release
                        codec.releaseOutputBuffer(outputBufferId, false);
                    } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = codec.getOutputBuffers();
                    }
                    Profiler.reportOnProcEnd();

                } catch (InterruptedException e) {
                    Gdx.app.error("Decoder", "Worker interrupted");
                    break;
                }
            }
        }
        else{
            Gdx.app.error("H264", "Codec is not valid!");
        }
    }

    @Override
    protected void cleanup(){
        if(codec != null){
            codec.stop();
            codec.release();
        }
    }
}
