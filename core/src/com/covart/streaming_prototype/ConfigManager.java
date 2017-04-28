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

    private ConfigManager() {
    }
}
