package com.covart.streaming_prototype;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public interface MasterComponentAdapter extends Component {
    public void requireStop();

    public void requireStart();
}
