package com.developerdepository.knowalledge;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartFragment extends Fragment {

    private ImageView appLogo;
    private TextView appName2;

    private NavController navController;

    private Animation topAnimation, bottomAnimation;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting StatusBar Color
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.backgroundSecondary));

        navController = Navigation.findNavController(view);

        initViews(view);
        initAnimation();
    }

    private void initViews(View view) {
        appLogo = view.findViewById(R.id.app_logo);
        appName2 = view.findViewById(R.id.app_name2);
    }

    private void initAnimation() {
        topAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.start_top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.start_bottom_animation);
    }

    @Override
    public void onStart() {
        super.onStart();
        int SPLASH_TIMER = 4000;

        appLogo.setAnimation(topAnimation);
        appName2.setAnimation(bottomAnimation);

        new Handler().postDelayed(() -> {
            //Navigate to WelcomeFragment
            navController.navigate(R.id.action_startFragment_to_welcomeFragment);
        }, SPLASH_TIMER);
    }
}
