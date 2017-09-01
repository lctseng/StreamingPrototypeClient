package com.covart.streaming_prototype.Utils.Easing;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class EasingQuadInOut extends EasingBase {

    public EasingQuadInOut() {
    }

    public EasingQuadInOut(float startValue, float endValue, float duration) {
        super(startValue, endValue, duration);
    }

    @Override
    public float valueAtWithoutBoundaryCheck(float time) {
        if ((time/=duration/2) < 1) return diffValue/2*time*time + startValue;
        return -diffValue/2 * ((--time)*(time-2) - 1) + startValue;
    }
}
