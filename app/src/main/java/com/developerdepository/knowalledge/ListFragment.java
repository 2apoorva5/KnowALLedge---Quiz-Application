package com.developerdepository.knowalledge;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerdepository.knowalledge.Adapter.QuizListAdapter;
import com.developerdepository.knowalledge.Model.QuizListViewModel;
import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.wang.avi.AVLoadingIndicatorView;

import maes.tech.intentanim.CustomIntent;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements QuizListAdapter.OnQuizListItemClicked {

    private NavController navController;

    private ImageButton optionsMenu;
    private RecyclerView listView;
    private AVLoadingIndicatorView listProgress;

    private QuizListViewModel quizListViewModel;
    private QuizListAdapter adapter;

    private Animation fadeInAnim;
    private Animation fadeOutAnim;

    private PowerMenu powerMenu;

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
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
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

        optionsMenu.setOnClickListener(v -> {
            powerMenu = new PowerMenu.Builder(getContext())
                    .addItem(new PowerMenuItem("Privacy Policy", false))
                    .setCircularEffect(CircularEffect.BODY)
                    .setMenuRadius(8f)
                    .setMenuShadow(8f)
                    .setTextColor(getActivity().getResources().getColor(android.R.color.black))
                    .setTextSize(16)
                    .setTextGravity(Gravity.CENTER)
                    .setSelectedTextColor(getActivity().getResources().getColor(android.R.color.black))
                    .setMenuColor(Color.WHITE)
                    .setSelectedMenuColor(getActivity().getResources().getColor(R.color.colorPrimary))
                    .setOnMenuItemClickListener(onMenuItemClickListener)
                    .setTextTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                    .build();

            powerMenu.showAsAnchorLeftBottom(optionsMenu);
        });
    }

    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            powerMenu.setSelectedPosition(position);
            if (powerMenu.getSelectedPosition() == 0) {
                powerMenu.dismiss();
                startActivity(new Intent(getContext(), WebViewActivity.class));
                CustomIntent.customType(getContext(), "bottom-to-up");
            }
        }
    };

    private void initViews(View view) {
        optionsMenu = view.findViewById(R.id.options_menu);
        listView = view.findViewById(R.id.list_view);
        listProgress = view.findViewById(R.id.list_progress);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), quizListModels -> {
            adapter.setQuizListModels(quizListModels);
            adapter.notifyDataSetChanged();
            listProgress.hide();
            listView.startAnimation(fadeInAnim);
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
