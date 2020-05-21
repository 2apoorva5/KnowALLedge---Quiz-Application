package com.application.knowalledge;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.application.knowalledge.Adapter.QuizListAdapter;
import com.application.knowalledge.Model.QuizListModel;
import com.application.knowalledge.Model.QuizListViewModel;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements QuizListAdapter.OnQuizListItemClicked {

    private NavController navController;

    private RecyclerView listView;
    private AVLoadingIndicatorView listProgress;

    private QuizListViewModel quizListViewModel;
    private QuizListAdapter adapter;

    private Animation fadeInAnim;
    private Animation fadeOutAnim;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting StatusBar Color
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.backgroundSecondary));

        initViews(view);

        navController = Navigation.findNavController(view);

        adapter = new QuizListAdapter(this);

        listProgress.show();

        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setHasFixedSize(true);
        listView.setAdapter(adapter);

        fadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_animation);
        fadeOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_animation);
    }

    private void initViews(View view) {
        listView = view.findViewById(R.id.list_view);
        listProgress = view.findViewById(R.id.list_progress);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                adapter.setQuizListModels(quizListModels);
                adapter.notifyDataSetChanged();
                listProgress.hide();
                listView.startAnimation(fadeInAnim);
            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        //Navigate to DetailsFragment
        ListFragmentDirections.ActionListFragmentToDetailsFragment action = ListFragmentDirections.actionListFragmentToDetailsFragment();
        action.setPosition(position);
        navController.navigate(action);
    }
}
