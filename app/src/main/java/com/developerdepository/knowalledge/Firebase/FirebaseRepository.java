package com.developerdepository.knowalledge.Firebase;

import com.developerdepository.knowalledge.Model.QuizListModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class FirebaseRepository {

    private OnFirestoreTaskComplete onFirestoreTaskComplete;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private Query categoryRef = firebaseFirestore.collection("Quiz Category").whereEqualTo("visibility", "public");

    public FirebaseRepository(OnFirestoreTaskComplete onFirestoreTaskComplete) {
        this.onFirestoreTaskComplete = onFirestoreTaskComplete;
    }

    public void getQuizCategoryData() {
        categoryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onFirestoreTaskComplete.quizListDataAdded(task.getResult().toObjects(QuizListModel.class));
            } else {
                onFirestoreTaskComplete.onError(task.getException());
            }
        });
    }

    public interface OnFirestoreTaskComplete {
        void quizListDataAdded(List<QuizListModel> quizListModelList);

        void onError(Exception e);
    }
}
