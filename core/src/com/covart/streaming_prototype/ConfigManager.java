package com.covart.streaming_prototype;


/**
 * Created by lctseng on 2017/4/28.
 * NTU COV-ART Lab, for NCP project
 */

public class ConfigManager {
    private static final ConfigManager ourInstance = new ConfigManager();

    static ConfigManager getInstance() {
        return ourInstance;
    }

    // constants

    private static final int imageWidth = 256;
    private static final int imageHeight = 256;

    private static final int numOfLFs = 16;
    private static final int numOfSubLFImgs = 4;

    private static final int bufferQueueSize = 3;
    private static final int imageBufferSize =  imageWidth * imageHeight * 3;
    private static final int decoderBufferSize = imageBufferSize;

    private static final String[] serverList = new String[]{
            "140.112.90.95:8051",
            "140.112.90.95:8053",
            "140.112.90.86:8051",
            "140.112.90.89:8051",
            "140.112.90.89:8052"
    };

    // variables

    private static float cameraStepX = 0.00759f * 2f * 4;
    private static float cameraStepY = 0.0097f * 2f;

    private static float apertureSize = 10.0f;

    private static boolean enableFocusChange = false;
    private static float focusChangeRatio = 1.0f;

    private static int numOfMaxInterpolatedLFRadius = 2;

    private static boolean stopOnDisconnected = false;

    private static long sensorReportInterval = 150;

    // getters and setters

    public static int getImageWidth() {
        return imageWidth;
    }

    public static int getImageHeight() {
        return imageHeight;
    }

    public static int getNumOfLFs() {
        return numOfLFs;
    }

    public static int getNumOfSubLFImgs() {
        return numOfSubLFImgs;
    }

    public static float getCameraStepX() {
        return cameraStepX;
    }

    public static void setCameraStepX(float cameraStepX) {
        ConfigManager.cameraStepX = cameraStepX;
    }

    public static float getCameraStepY() {
        return cameraStepY;
    }

    public static void setCameraStepY(float cameraStepY) {
        ConfigManager.cameraStepY = cameraStepY;
    }

    public static float getApertureSize() {
        return apertureSize;
    }

    public static void setApertureSize(float apertureSize) {
        ConfigManager.apertureSize = apertureSize;
    }

    public static boolean isEnableFocusChange() {
        return enableFocusChange;
    }

    public static void setEnableFocusChange(boolean enableFocusChange) {
        ConfigManager.enableFocusChange = enableFocusChange;
    }

    public static void toggleEnableFocusChange() {
        enableFocusChange = !enableFocusChange;
    }

    public static float getFocusChangeRatio() {
        return focusChangeRatio;
    }

    public static void setFocusChangeRatio(float focusChangeRatio) {
        ConfigManager.focusChangeRatio = focusChangeRatio;

    }

    public static int getBufferQueueSize() {
        return bufferQueueSize;
    }

    public static int getImageBufferSize() {
        return imageBufferSize;
    }

    public static int getDecoderBufferSize() {
        return decoderBufferSize;
    }

    public static String[] getServerList() {
        return serverList;
    }

    public static int getNumOfMaxInterpolatedLFRadius() {
        return numOfMaxInterpolatedLFRadius;
    }

    public static void setNumOfMaxInterpolatedLFRadius(int numOfMaxInterpolatedLFRadius) {
        ConfigManager.numOfMaxInterpolatedLFRadius = numOfMaxInterpolatedLFRadius;
    }

    public static boolean isStopOnDisconnected() {
        return stopOnDisconnected;
    }

    public static void setStopOnDisconnected(boolean stopOnDisconnected) {
        ConfigManager.stopOnDisconnected = stopOnDisconnected;
    }

    public static long getSensorReportInterval() {
        return sensorReportInterval;
    }

    public static void setSensorReportInterval(long sensorReportInterval) {
        ConfigManager.sensorReportInterval = sensorReportInterval;
    }

    // end of getters and setters

    private ConfigManager() {
    }


}
