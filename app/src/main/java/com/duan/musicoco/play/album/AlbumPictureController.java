package com.duan.musicoco.play.album;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.app.Init;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.image.AlbumBitmapProducer;
import com.duan.musicoco.util.ColorUtils;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * 切换歌曲时改变专辑图片（从缓存中获取，没有则生成并添加到缓存）
 * 控制切换动画
 * 播放动画
 */

public final class AlbumPictureController implements IAlbum {

    private final ImageSwitcher view;

    private final Context context;

    private final ValueAnimator rotateAnim;

    private boolean isSpin = false;

    private final BitmapCache cache;

    private final AlbumBitmapProducer bitmapProducer;

    private int defaultColor = Color.DKGRAY;
    private int defaultTextColor = Color.DKGRAY;
    private int[] colors;
    private final int size;

    public AlbumPictureController(Context context, final ImageSwitcher view, int size) {
        this.view = view;
        this.size = size;
        this.context = context;
        this.cache = new BitmapCache(context, context.getString(R.string.cache_bitmap_album_visualizer));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultColor = context.getColor(R.color.colorPrimaryLight);
            defaultTextColor = context.getColor(R.color.colorAccent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultColor = context.getResources().getColor(R.color.colorPrimaryLight, null);
            defaultTextColor = context.getResources().getColor(R.color.colorAccent, null);
        } else {
            defaultColor = context.getResources().getColor(R.color.colorPrimaryLight);
            defaultTextColor = context.getResources().getColor(R.color.colorAccent);
        }

        this.bitmapProducer = new AlbumBitmapProducer(context, cache, defaultColor);

        colors = new int[]{
                defaultColor,
                defaultTextColor,
                defaultColor,
                defaultTextColor
        };

        rotateAnim = ObjectAnimator.ofFloat(0, 360);
        rotateAnim.setDuration(45 * 1000);
        rotateAnim.setRepeatMode(ValueAnimator.RESTART);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.getCurrentView().setRotation(value);
                Log.i(TAG, "onAnimationUpdate: " + view.hashCode() + " c" + view.getCurrentView().hashCode());
            }
        });
    }

    /**
     * 切换歌曲的同时返回从歌曲专辑图片中提取出的四种颜色值{@link ColorUtils#get2ColorWithTextFormBitmap(Bitmap, int, int, int[])}
     */
    public int[] pre(@NonNull SongInfo song, boolean updateColors) {

        view.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
        view.setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right));

        rotateAnim.cancel();
        view.getNextView().setRotation(0.0f);

        Bitmap bitmap = bitmapProducer.get(song, size);
        if (bitmap != null) {
            if (updateColors)
                ColorUtils.get2ColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, this.colors);

            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            try {
                view.setImageDrawable(new BitmapDrawable(context.getResources(), cache.getDefaultBitmap()));
            } catch (Exception e) {
                Log.d(TAG, "pre: create default bitmap for BitmapCache");
                Bitmap b = new Init().initAlbumVisualizerImageCache((Activity) context);
                view.setImageDrawable(new BitmapDrawable(context.getResources(), b));
            }
        }

        if (isSpin()) {
            startSpin();
        }

        return colors;
    }

    public int[] next(@NonNull SongInfo song, boolean updateColors) {

        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
        view.setInAnimation(in);
        view.setOutAnimation(out);

        rotateAnim.cancel();
        view.getNextView().setRotation(0.0f);

        Bitmap bitmap = bitmapProducer.get(song, size);
        if (bitmap != null) {
            if (updateColors)
                ColorUtils.get2ColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, this.colors);

            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            try {
                view.setImageDrawable(new BitmapDrawable(context.getResources(), cache.getDefaultBitmap()));
            } catch (Exception e) {
                Log.d(TAG, "pre: create default bitmap for BitmapCache");
                Bitmap b = new Init().initAlbumVisualizerImageCache((Activity) context);
                view.setImageDrawable(new BitmapDrawable(context.getResources(), b));
            }
        }

        if (isSpin()) {
            startSpin();
        }

        return colors;
    }

    @Override
    public void startSpin() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (rotateAnim.isPaused()) {
                rotateAnim.resume();
            } else {
                rotateAnim.start();
            }
        } else {
            rotateAnim.start();
        }

        isSpin = true;
    }

    @Override
    public void stopSpin() {

        if (rotateAnim.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                rotateAnim.pause();
            } else {
                rotateAnim.cancel();
            }
            isSpin = false;
        }
    }

    public boolean isSpin() {
        return isSpin;
    }

}