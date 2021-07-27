package com.herro.mainumiplay;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.skydoves.progressview.ProgressView;

import java.util.concurrent.TimeUnit;

public class FirstFragment extends Fragment {

    private ImageView img_bg;
    private ImageView img_login;
    private ImageView img_b;

    private ProgressView progress;

    private View root;

    private int cnt;
    private final int max = 100;

    private Handler h;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        root = inflater.inflate(R.layout.fragment_first, container, false);
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        h = new Handler();


        progress = (ProgressView) root.findViewById(R.id.progress);

        img_bg = (ImageView) root.findViewById(R.id.img_bg);
        Glide.with(root).load(R.drawable.bg).into(img_bg);

        img_b = (ImageView) root.findViewById(R.id.img_blue);
        Glide.with(root).load(R.drawable.gear_blue).into(img_b);

        final Animation animationRotateCenter1 = AnimationUtils.loadAnimation(
                root.getContext(), R.anim.rotate_center);
        img_b.startAnimation(animationRotateCenter1);

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (cnt = 1; cnt < max; cnt++) {
                        TimeUnit.MILLISECONDS.sleep(500);
                        h.post(updateProgress);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    Runnable updateProgress = new Runnable() {
        public void run() {
            progress.setLabelText(cnt + "%");
            progress.setProgress(cnt);
        }
    };
}