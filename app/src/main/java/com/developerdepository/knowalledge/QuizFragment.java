package com.developerdepository.knowalledge;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.developerdepository.knowalledge.Model.QuestionsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends Fragment implements View.OnClickListener {

    private NavController navController;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String currentUserId;
    private String quizId;
    private String quizName;

    private TextView quizTitle, questionNumber, questionTime, question,
            option1, option2, option3, option4, questionFeedback, nextQuestionText;
    private ProgressBar progressBar;
    private ConstraintLayout nextQuestion;
    private CardView nextQuestionCard;

    private List<QuestionsModel> allQuestionsList = new ArrayList<>();
    private long totalQuestionsToAnswer = 10;
    private List<QuestionsModel> questionsToAnswer = new ArrayList<>();

    private CountDownTimer countDownTimer;
    private boolean canAnswer = false;
    private int currentQuestion = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;

    private AlertDialog progressDialog;

    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting StatusBar Color
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.backgroundSecondary));

        navController = Navigation.findNavController(view);

        progressDialog = new SpotsDialog.Builder().setContext(getActivity())
                .setMessage(String.valueOf(getActivity().getResources().getText(R.string.progress_message)))
                .setCancelable(false)
                .setTheme(R.style.SpotsDialog)
                .build();

        initViews(view);
        setActionOnViews();

        firebaseAuth = FirebaseAuth.getInstance();
        //Get User Id
        if (firebaseAuth.getCurrentUser() != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        } else {
            getActivity().finishAffinity();
        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizid();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        firebaseFirestore.collection("Quiz Category").document(quizId).collection("Questions")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allQuestionsList = task.getResult().toObjects(QuestionsModel.class);
                pickQuestions();
                loadUI();
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
    }

    private void initViews(View view) {
        quizTitle = view.findViewById(R.id.quiz_title);
        questionNumber = view.findViewById(R.id.quiz_question_number);
        questionTime = view.findViewById(R.id.quiz_progress_timer);
        question = view.findViewById(R.id.quiz_question);
        questionFeedback = view.findViewById(R.id.quiz_feedback);
        progressBar = view.findViewById(R.id.quiz_progress);
        option1 = view.findViewById(R.id.quiz_option1);
        option2 = view.findViewById(R.id.quiz_option2);
        option3 = view.findViewById(R.id.quiz_option3);
        option4 = view.findViewById(R.id.quiz_option4);
        nextQuestion = view.findViewById(R.id.quiz_next_question);
        nextQuestionCard = view.findViewById(R.id.quiz_next_question_card);
        nextQuestionText = view.findViewById(R.id.next_question_text);
    }

    private void setActionOnViews() {
        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);

        nextQuestion.setOnClickListener(this);
    }

    private void pickQuestions() {
        for (int i = 0; i < totalQuestionsToAnswer; i++) {
            int randomNumber = getRandomInteger(allQuestionsList.size(), 0);
            questionsToAnswer.add(allQuestionsList.get(randomNumber));
            allQuestionsList.remove(randomNumber);
        }
    }

    public static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    private void loadUI() {
        quizTitle.setText(quizName);

        loadQuestion(1);

        enableOptions();
    }

    private void loadQuestion(int qno) {
        questionNumber.setVisibility(View.VISIBLE);
        questionNumber.setText(String.valueOf(qno));
        question.setText(questionsToAnswer.get(qno - 1).getQuestion());

        option1.setText(questionsToAnswer.get(qno - 1).getOptiona());
        option2.setText(questionsToAnswer.get(qno - 1).getOptionb());
        option3.setText(questionsToAnswer.get(qno - 1).getOptionc());
        option4.setText(questionsToAnswer.get(qno - 1).getOptiond());

        canAnswer = true;
        currentQuestion = qno;

        startTimer(qno);
    }

    private void startTimer(int qno) {
        final Long timeToAnswer = questionsToAnswer.get(qno - 1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        progressBar.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeToAnswer * 1000, 20) {

            @Override
            public void onTick(long millisUntilFinished) {
                questionTime.setText(millisUntilFinished / 1000 + "");

                Long percent = millisUntilFinished / (timeToAnswer * 10);
                progressBar.setProgress(percent.intValue());
            }

            @Override
            public void onFinish() {
                canAnswer = false;
                questionFeedback.setText(getActivity().getResources().getText(R.string.quiz_feedback_timeup));
                questionFeedback.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
                notAnswered++;
                showNextBtn();
            }
        };

        countDownTimer.start();
    }

    private void enableOptions() {
        option1.setVisibility(View.VISIBLE);
        option2.setVisibility(View.VISIBLE);
        option3.setVisibility(View.VISIBLE);
        option4.setVisibility(View.VISIBLE);

        option1.setEnabled(true);
        option2.setEnabled(true);
        option3.setEnabled(true);
        option4.setEnabled(true);

        questionFeedback.setVisibility(View.INVISIBLE);
        nextQuestionCard.setVisibility(View.INVISIBLE);
        nextQuestion.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quiz_option1:
                verifyAnswer(option1);
                break;
            case R.id.quiz_option2:
                verifyAnswer(option2);
                break;
            case R.id.quiz_option3:
                verifyAnswer(option3);
                break;
            case R.id.quiz_option4:
                verifyAnswer(option4);
                break;
            case R.id.quiz_next_question:
                if (currentQuestion == totalQuestionsToAnswer) {
                    submitResults();
                } else {
                    currentQuestion++;
                    loadQuestion(currentQuestion);
                    resetOptions();
                }
                break;
        }
    }

    private void submitResults() {
        progressDialog.show();

        final HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct", correctAnswers);
        resultMap.put("wrong", wrongAnswers);
        resultMap.put("unanswered", notAnswered);

        final DocumentReference documentReference = firebaseFirestore.collection("Quiz Category")
                .document(quizId).collection("Results").document(currentUserId);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (!snapshot.exists()) {
                    documentReference.set(resultMap)
                            .addOnCompleteListener(task12 -> {
                                if (task12.isSuccessful()) {
                                    progressDialog.dismiss();
                                    //Navigate to ResultFragment
                                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                                    action.setQuizId(quizId);
                                    navController.navigate(action);
                                } else {
                                    progressDialog.dismiss();
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
                    documentReference.update("correct", correctAnswers,
                            "wrong", wrongAnswers,
                            "unanswered", notAnswered)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    progressDialog.dismiss();
                                    //Navigate to ResultFragment
                                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                                    action.setQuizId(quizId);
                                    navController.navigate(action);
                                } else {
                                    progressDialog.dismiss();
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
                }
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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetOptions() {
        option1.setBackground(getResources().getDrawable(R.drawable.option_bg, null));
        option2.setBackground(getResources().getDrawable(R.drawable.option_bg, null));
        option3.setBackground(getResources().getDrawable(R.drawable.option_bg, null));
        option4.setBackground(getResources().getDrawable(R.drawable.option_bg, null));

        option1.setTextColor(getResources().getColor(R.color.textColorSecondary, null));
        option2.setTextColor(getResources().getColor(R.color.textColorSecondary, null));
        option3.setTextColor(getResources().getColor(R.color.textColorSecondary, null));
        option4.setTextColor(getResources().getColor(R.color.textColorSecondary, null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextQuestionCard.setVisibility(View.INVISIBLE);
        nextQuestion.setEnabled(false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void verifyAnswer(TextView selectedAnswerTextView) {
        if (canAnswer) {
            if (questionsToAnswer.get(currentQuestion - 1).getAnswer().equals(selectedAnswerTextView.getText())) {
                correctAnswers++;
                selectedAnswerTextView.setTextColor(getActivity().getResources().getColor(android.R.color.black, null));
                selectedAnswerTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.correct_answer_bg, null));

                questionFeedback.setText(getActivity().getResources().getText(R.string.quiz_feedback_correct));
                questionFeedback.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary, null));
            } else {
                wrongAnswers++;
                selectedAnswerTextView.setTextColor(getActivity().getResources().getColor(android.R.color.white, null));
                selectedAnswerTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.wrong_answer_bg, null));

                questionFeedback.setText(String.format("Wrong Answer \n Correct Answer : %s", questionsToAnswer.get(currentQuestion - 1).getAnswer()));
                questionFeedback.setTextColor(getActivity().getResources().getColor(R.color.colorAccent, null));
            }
            canAnswer = false;

            countDownTimer.cancel();

            showNextBtn();
        }
    }

    private void showNextBtn() {
        if (currentQuestion == totalQuestionsToAnswer) {
            nextQuestionText.setText(getActivity().getResources().getText(R.string.quiz_submit_results_text));
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextQuestionCard.setVisibility(View.VISIBLE);
        nextQuestion.setEnabled(true);
    }
}
