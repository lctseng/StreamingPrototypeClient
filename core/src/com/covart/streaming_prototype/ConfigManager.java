package com.covart.streaming_prototype;


import com.covart.streaming_prototype.Image.Display;
import com.covart.streaming_prototype.UI.PositionController;
import com.google.vrtoolkit.cardboard.Eye;

import static com.covart.streaming_prototype.UI.PositionController.Direction.NONE;

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

    // Must be an EVEN number
    // This value is limited by shader
    private static final int numOfMaxLFTextures = 8;

    private static final int numOfLFs = 8;
    private static final int numOfSubLFImgs = 8;

    private static final float columnPositionRatio = (float)numOfLFs / numOfMaxLFTextures;

    private static final int bufferQueueSize = 3;
    private static final int imageBufferSize =  imageWidth * imageHeight * 3;
    private static final int decoderBufferSize = imageBufferSize;

    private static final String[] serverList = new String[]{
            "140.112.90.82:8051",
            "140.112.90.95:8051",
            "140.112.90.95:8052",
            "140.112.90.95:8053",
            "140.112.90.86:8051",
            "140.112.90.89:8051",
            "140.112.90.89:8052",
            "140.112.90.89:8053"
    };

    private static final Integer[] sceneList = new Integer[]{
        0,1,2,3
    };

    private static final Display.Mode[] displayModeList = new Display.Mode[] {
            Display.Mode.NORMAL, Display.Mode.VR
    };



    // variables

    private static StreamingPrototype app;
    private static float cameraStep = 1f;

    private static float apertureSize = 0.100f;

    private static float focusChangeRatio = 2.267f;

    private static boolean stopOnDisconnected = false;

    private static float sensorReportInterval = 2.0f;

    private static Integer sceneIndex = 0;
    private static String selectedIP = serverList[0];

    private static int freeUnusedTextureThreshold = 0;

    private static Display.Mode displayMode = Display.Mode.NORMAL;

    private static boolean editingModeEnabled = false;

    private static float editingReportInterval = 1.000f;

    private static float dataCameraFOV = 110.0f;

    private static float manuallyMoveStep = 0.01f;

    private static PositionController.Direction currentMoveDirection = NONE;

    private static boolean enableManuallyMove = false;

    private static float eyeDisparityFactor = 5.0f;

    private static float eyeRotationToTranslationRatio = 1.0f;

    private static float eyeWrapperPitchLimit = 10f;

    private static float eyeWrapperYawLimit = 10f;

    private static boolean eyeWrapperEnableAngleLimit = true;


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

    public static int getNumOfMaxLFTextures() {
        return numOfMaxLFTextures;
    }

    public static float getColumnPositionRatio() {
        return columnPositionRatio;
    }

    public static float getCameraStep() {
        return cameraStep;
    }

    public static void setCameraStep(float cameraStep) {
        ConfigManager.cameraStep = cameraStep;
    }

    public static float getApertureSize() {
        return apertureSize;
    }

    public static void setApertureSize(float apertureSize) {
        ConfigManager.apertureSize = apertureSize;
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

    public static boolean isStopOnDisconnected() {
        return stopOnDisconnected;
    }

    public static void setStopOnDisconnected(boolean stopOnDisconnected) {
        ConfigManager.stopOnDisconnected = stopOnDisconnected;
    }

    public static float getSensorReportInterval() {
        return sensorReportInterval;
    }

    public static void setSensorReportInterval(float sensorReportInterval) {
        ConfigManager.sensorReportInterval = sensorReportInterval;
    }

    public static String getSelectedIP() {
        return selectedIP;
    }

    public static void setSelectedIP(String selectedIP) {
        ConfigManager.selectedIP = selectedIP;
    }

    public static Integer getSceneIndex() {
        return sceneIndex;
    }

    public static void setSceneIndex(Integer sceneIndex) {
        ConfigManager.sceneIndex = sceneIndex;
    }

    public static StreamingPrototype getApp() {
        return app;
    }

    public static void setApp(StreamingPrototype app) {
        ConfigManager.app = app;
    }

    public static Integer[] getSceneList() {
        return sceneList;
    }

    public static Display.Mode getDisplayMode() {
        return displayMode;
    }

    public static void setDisplayMode(Display.Mode displayMode) {
        ConfigManager.displayMode = displayMode;
    }


    public static int getFreeUnusedTextureThreshold() {
        return freeUnusedTextureThreshold;
    }

    public static void setFreeUnusedTextureThreshold(int freeUnusedTextureThreshold) {
        ConfigManager.freeUnusedTextureThreshold = freeUnusedTextureThreshold;
    }

    public static Display.Mode[] getDisplayModeList() {
        return displayModeList;
    }

    public static boolean isEditingModeEnabled() {
        return editingModeEnabled;
    }

    public static void setEditingModeEnabled(boolean editingModeEnabled) {
        ConfigManager.editingModeEnabled = editingModeEnabled;
    }

    public static void toggleEditingModeEnabled() {
        ConfigManager.editingModeEnabled = !ConfigManager.editingModeEnabled;
    }

    public static float getEditingReportInterval() {
        return editingReportInterval;
    }

    public static void setEditingReportInterval(float editingReportInterval) {
        ConfigManager.editingReportInterval = editingReportInterval;
    }

    public static float getDataCameraFOV() {
        return dataCameraFOV;
    }

    public static void setDataCameraFOV(float dataCameraFOV) {
        ConfigManager.dataCameraFOV = dataCameraFOV;
    }

    public static float getManuallyMoveStep() {
        return manuallyMoveStep;
    }

    public static void setManuallyMoveStep(float manuallyMoveStep) {
        ConfigManager.manuallyMoveStep = manuallyMoveStep;
    }

    public static PositionController.Direction getCurrentMoveDirection() {
        return currentMoveDirection;
    }

    public static void setCurrentMoveDirection(PositionController.Direction currentMoveDirection) {
        ConfigManager.currentMoveDirection = currentMoveDirection;
    }

    public static boolean isEnableManuallyMove() {
        return enableManuallyMove;
    }

    public static void setEnableManuallyMove(boolean enableManuallyMove) {
        ConfigManager.enableManuallyMove = enableManuallyMove;
    }

    public static float getEyeDisparityFactor() {
        return eyeDisparityFactor;
    }

    public static void setEyeDisparityFactor(float eyeDisparityFactor) {
        ConfigManager.eyeDisparityFactor = eyeDisparityFactor;
    }

    public static float getEyeRotationToTranslationRatio() {
        return eyeRotationToTranslationRatio;
    }

    public static void setEyeRotationToTranslationRatio(float eyeRotationToTranslationRatio) {
        ConfigManager.eyeRotationToTranslationRatio = eyeRotationToTranslationRatio;
    }

    public static float getEyeWrapperPitchLimit() {
        return eyeWrapperPitchLimit;
    }

    public static void setEyeWrapperPitchLimit(float eyeWrapperPitchLimit) {
        ConfigManager.eyeWrapperPitchLimit = eyeWrapperPitchLimit;
    }

    public static float getEyeWrapperYawLimit() {
        return eyeWrapperYawLimit;
    }

    public static void setEyeWrapperYawLimit(float eyeWrapperYawLimit) {
        ConfigManager.eyeWrapperYawLimit = eyeWrapperYawLimit;
    }

    public static boolean isEyeWrapperEnableAngleLimit() {
        return eyeWrapperEnableAngleLimit;
    }

    public static void setEyeWrapperEnableAngleLimit(boolean eyeWrapperEnableAngleLimit) {
        ConfigManager.eyeWrapperEnableAngleLimit = eyeWrapperEnableAngleLimit;
    }

    // end of getters and setters

    public static String getEyeString(Eye eye){
        switch(eye.getType()){
            case Eye.Type.LEFT:
                return "Left";
            case Eye.Type.RIGHT:
                return "Right";
            case Eye.Type.MONOCULAR:
                return "Monocular";
            default:
                return "Unknown";
        }
    }

    private ConfigManager() {
    }



}
