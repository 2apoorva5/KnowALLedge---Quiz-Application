package com.application.knowalledge;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.knowalledge.Model.QuizListModel;
import com.application.knowalledge.Model.QuizListViewModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tapadoo.alerter.Alerter;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment implements View.OnClickListener {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String currentUserId;
    private String quizName;
    private String quizId;

    private ImageView detailsImage;
    private TextView detailsTitle, detailsDesc, detailsDifficulty, detailsQuestions, detailsScore;
    private CardView startQuizCard;
    private ConstraintLayout startQuiz;

    private NavController navController;
    private QuizListViewModel quizListViewModel;

    private int position;
    private long totalQuestions = 0;

    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting StatusBar transparent
        setWindowFlag(getActivity(), WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        initViews(view);
        setActionOnViews();

        navController = Navigation.findNavController(view);

        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        firebaseAuth = FirebaseAuth.getInstance();
        //Get User Id
        if (firebaseAuth.getCurrentUser() != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        } else {
            getActivity().finishAffinity();
        }

        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        detailsImage = view.findViewById(R.id.details_img);
        detailsTitle = view.findViewById(R.id.details_title);
        detailsDesc = view.findViewById(R.id.details_description);
        detailsDifficulty = view.findViewById(R.id.details_difficulty);
        detailsQuestions = view.findViewById(R.id.details_total_questions);
        detailsScore = view.findViewById(R.id.details_last_score);
        startQuizCard = view.findViewById(R.id.start_quiz_card);
        startQuiz = view.findViewById(R.id.start_quiz);
    }

    private void setActionOnViews() {
        startQuiz.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                Glide.with(getContext())
                        .load(quizListModels.get(position).getImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(detailsImage);

                detailsTitle.setText(quizListModels.get(position).getName());
                detailsDesc.setText(quizListModels.get(position).getDesc());
                detailsDifficulty.setText(quizListModels.get(position).getLevel());
                detailsQuestions.setText(String.valueOf(quizListModels.get(position).getQuestions()));

                quizId = quizListModels.get(position).getQuiz_id();
                quizName = quizListModels.get(position).getName();
                totalQuestions = quizListModels.get(position).getQuestions();

                loadLastScore();
            }
        });
    }

    private void loadLastScore() {
        firebaseFirestore.collection("Quiz Category")
                .document(quizId).collection("Results")
                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document != null && document.exists()) {
                        Long correct = document.getLong("correct");
                        Long wrong = document.getLong("wrong");
                        Long missed = document.getLong("unanswered");

                        Long total = correct + wrong + missed;
                        Long percent = (correct * 100) / total;

                        detailsScore.setText(percent + "%");
                    } else {
                        detailsScore.setText("-");
                    }
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
    }

    public static void setWindowFlag(FragmentActivity fragmentActivity, final int bits, boolean on) {
        Window window = fragmentActivity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        if (on) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_quiz:
                //Navigate to QuizFragment
                DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
                action.setTotalQuestions(totalQuestions);
                action.setQuizid(quizId);
                action.setQuizName(quizName);
                navController.navigate(action);
                break;
        }
    }
}
