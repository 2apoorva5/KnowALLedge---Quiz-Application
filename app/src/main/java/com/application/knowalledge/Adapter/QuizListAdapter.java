package com.application.knowalledge.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.application.knowalledge.Model.QuizListModel;
import com.application.knowalledge.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {

    private List<QuizListModel> quizListModels;
    private OnQuizListItemClicked onQuizListItemClicked;

    public QuizListAdapter(OnQuizListItemClicked onQuizListItemClicked) {
        this.onQuizListItemClicked = onQuizListItemClicked;
    }

    public void setQuizListModels(List<QuizListModel> quizListModels) {
        this.quizListModels = quizListModels;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        holder.categoryTitle.setText(quizListModels.get(position).getName());

        String imgUrl = quizListModels.get(position).getImage();

        Glide.with(holder.itemView.getContext())
                .load(imgUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.categoryImg);

        String description = quizListModels.get(position).getDesc();
        if (description.length() > 150) {
            description = description.substring(0, 150);
        }

        holder.categoryDescription.setText(description + "...");
        holder.categoryDifficulty.setText(quizListModels.get(position).getLevel());
    }

    @Override
    public int getItemCount() {
        if (quizListModels == null) {
            return 0;
        } else {
            return quizListModels.size();
        }
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView categoryImg;
        private TextView categoryTitle, categoryDescription, categoryDifficulty;
        private CardView viewQuizCard;
        private ConstraintLayout viewQuiz;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryImg = itemView.findViewById(R.id.category_img);
            categoryTitle = itemView.findViewById(R.id.category_title);
            categoryDescription = itemView.findViewById(R.id.category_description);
            categoryDifficulty = itemView.findViewById(R.id.category_difficulty);
            viewQuizCard = itemView.findViewById(R.id.view_quiz_card);
            viewQuiz = itemView.findViewById(R.id.view_quiz);

            viewQuiz.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onQuizListItemClicked.onItemClicked(getAdapterPosition());
        }
    }

    public interface OnQuizListItemClicked {
        void onItemClicked(int position);
    }
}
