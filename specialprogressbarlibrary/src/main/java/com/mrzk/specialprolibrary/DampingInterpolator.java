package com.mrzk.specialprolibrary;


import android.view.animation.Interpolator;

/**
 *
 *阻尼运动插值器
 */
public class DampingInterpolator implements Interpolator {

    /** 过冲部分比例,0.0~1.0之间，默认0.5 */
    float mOvershootPercent = 0.5f;
    /** 用于计算阻力的系数，由mOvershootPercent和mRegion求得 */
    float mOvershootModulus;
    /** 回弹次数,最少回弹一次 */
    int mCount = 1;
    /** 整个运动范围，由mCount决定 */
    float mRegion;
    /**
     *  默认过冲比例是0.5,回弹次数是1
     */
    public DampingInterpolator() {
        this(1, 0.5f);
    }

    /**
     * @param count
     *            回弹次数
     * @param overshoot
     *            过冲比例，0.0～1.0之间
     */
    public DampingInterpolator(int count, float overshoot) {
        setOverShootCount(count);
        setOverShootPercent(overshoot);
    }

    public void setOverShootCount(int count) {
        mCount = Math.max(1, count);
        mRegion = (float) (Math.PI * 2 * (mCount - 1) + Math.PI / 2 * 3);
        mOvershootModulus = (float) Math.pow(mOvershootPercent, mRegion
                / Math.PI);
    }

    public void setOverShootPercent(float overshoot) {
        mOvershootPercent = Math.max(0, Math.min(1, overshoot));
       /*
        * 当 t * mRegion = Math.PI 的时候，达到第一次过冲的峰值， 则 t = Math.PI / mRegion 。 且此时
        * mOvershootModulus^t = mOvershootPercent ， 所以 mOvershootModulus =
        * Math.pow(mOvershootPercent, 1 / t) ， 即 mOvershootModulus =
        * Math.pow(mOvershootPercent, mRegion / Math.PI) 。
        */
        mOvershootModulus = (float) Math.pow(mOvershootPercent, mRegion
                / Math.PI);
    }

    public int getOverShootCount() {
        return mCount;
    }

    public float getOverShootPercent() {
        return mOvershootPercent;
    }

    @Override
    public float getInterpolation(float t) {
        if (t <= 0) {
            return 0;
        }
        if (t >= 1) {
            return 1;
        }
        return (float) (1 - Math.pow(mOvershootModulus, t)
                * Math.cos(mRegion * t));
    }
}

