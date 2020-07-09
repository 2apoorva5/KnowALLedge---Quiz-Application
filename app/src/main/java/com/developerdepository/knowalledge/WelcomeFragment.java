package com.developerdepository.knowalledge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tapadoo.alerter.Alerter;
import com.wang.avi.AVLoadingIndicatorView;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {

    private AVLoadingIndicatorView welcomeProgress;
    private TextView welcomeFeedback;

    private FirebaseAuth firebaseAuth;

    private NavController navController;

    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting StatusBar Color
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.backgroundSecondary));

        navController = Navigation.findNavController(view);

        initViews(view);
        initFirebase();
        setActionOnViews();
    }

    private void initViews(View view) {
        welcomeProgress = view.findViewById(R.id.welcome_progress);
        welcomeFeedback = view.findViewById(R.id.welcome_feedback);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setActionOnViews() {
        welcomeProgress.show();
        welcomeFeedback.setText(getActivity().getResources().getText(R.string.welcome_feedback1));
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            //Create a new Account
            welcomeFeedback.setText(getActivity().getResources().getText(R.string.welcome_feedback2));
            firebaseAuth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //Navigate to ListFragment
                    welcomeFeedback.setText(getActivity().getResources().getText(R.string.welcome_feedback3));
                    navController.navigate(R.id.action_welcomeFragment_to_listFragment);
                } else {
                    Alerter.create(getActivity())
                            .setText(String.valueOf(getActivity().getResources().getText(R.string.alerter_error)))
                            .setTextAppearance(R.style.ErrorAlert)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.error_icon)
                            .setDuration(3000)
                            .enableSwipeToDismiss()
                            .enableIconPulse(true)
                            .enableVibration(true)
                            .disableOutsideTouch()
                            .enableProgress(true)
                            .setProgressColorInt(getActivity().getResources().getColor(android.R.color.white))
                            .show();
                }
            });
        } else {
            //Navigate to ListFragment
            welcomeFeedback.setText(getActivity().getResources().getText(R.string.welcome_feedback4));
            navController.navigate(R.id.action_welcomeFragment_to_listFragment);
        }
    }
}
