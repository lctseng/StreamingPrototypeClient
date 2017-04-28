package com.covart.streaming_prototype;

/**
 * Created by lctseng on 2017/4/28.
 * NTU COV-ART Lab, for NCP project
 */

class ConfigManager {
    private static final ConfigManager ourInstance = new ConfigManager();

    static ConfigManager getInstance() {
        return ourInstance;
    }

    private static final int imageWidth = 256;
    private static final int imageHeight = 256;

    private static final int numOfLFs = 16;
    private static final int numOfSubLFImgs = 4;

    private static final int numOfTotalImages = numOfLFs * numOfSubLFImgs;

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

    public static int getNumOfTotalImages() {
        return numOfTotalImages;
    }

    private ConfigManager() {
    }


}
