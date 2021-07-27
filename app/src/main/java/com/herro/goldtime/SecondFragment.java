package com.herro.goldtime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class SecondFragment extends Fragment {

    private String deep;

    public SecondFragment(String deep) {
        this.deep = deep;
        initWebView(deep);
    }

    private void initWebView(String deep) {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }
}

//<activity
//            android:name=".MainActivity"
//                    android:label="@string/app_name"
//                    android:theme="@style/AppTheme.NoActionBar">
//<intent-filter>
//<action android:name="android.intent.action.MAIN" />
//
//<category android:name="android.intent.category.LAUNCHER" />
//</intent-filter>
//</activity>