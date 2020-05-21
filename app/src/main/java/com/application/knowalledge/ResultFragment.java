package com.application.knowalledge;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tapadoo.alerter.Alerter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends Fragment {

    private NavController navController;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String currentUserId;
    private String quizId;

    private TextView resultCorrect, resultWrong, resultMissed, resultPercent;
    private ProgressBar resultProgress;
    private CardView resultGoToHomeCard;
    private ConstraintLayout resultGoToHome;

    public ResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting StatusBar Color
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.backgroundSecondary));

        navController = Navigation.findNavController(view);

        initViews(view);

        firebaseAuth = FirebaseAuth.getInstance();
        //Get User Id
        if(firebaseAuth.getCurrentUser() != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else {
            getActivity().finishAffinity();
        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizId = ResultFragmentArgs.fromBundle(getArguments()).getQuizId();

        firebaseFirestore.collection("Quiz Category").document(quizId).collection("Results")
                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    Long correct = documentSnapshot.getLong("correct");
                    Long wrong = documentSnapshot.getLong("wrong");
                    Long missed = documentSnapshot.getLong("unanswered");

                    resultCorrect.setText(correct.toString());
                    resultWrong.setText(wrong.toString());
                    resultMissed.setText(missed.toString());

                    Long total = correct + wrong + missed;
                    Long percent = (correct*100)/total;

                    resultPercent.setVisibility(View.VISIBLE);
                    resultProgress.setVisibility(View.VISIBLE);
                    resultPercent.setText(percent + "%");
                    resultProgress.setProgress(percent.intValue());
                } else {
                    Alerter.create(getActivity())
                            .setText("Whoops! There was some ERROR!")
                            .setTextAppearance(R.style.ErrorAlert)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.error_icon)
                            .setDuration(3000)
                            .enableSwipeToDismiss()
                            .enableIconPulse(true)
                            .enableVibration(true)
                            .disableOutsideTouch()
                            .enableProgress(true)
                            .setProgressColorInt(getResources().getColor(android.R.color.white))
                            .show();
                }
            }
        });

        resultGoToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigate to ListFragment
                navController.navigate(R.id.action_resultFragment_to_listFragment);
            }
        });
    }

    private void initViews(View view) {
        resultCorrect = view.findViewById(R.id.result_correct);
        resultWrong = view.findViewById(R.id.result_wrong);
        resultMissed = view.findViewById(R.id.result_missed);
        resultPercent = view.findViewById(R.id.result_progress_percent);
        resultProgress = view.findViewById(R.id.result_progress);
        resultGoToHomeCard = view.findViewById(R.id.result_goto_home_card);
        resultGoToHome = view.findViewById(R.id.result_goto_home);
    }
}
