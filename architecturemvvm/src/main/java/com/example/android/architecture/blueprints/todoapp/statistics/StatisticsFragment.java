package com.example.android.architecture.blueprints.todoapp.statistics;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.lll.architecturemvvm.R;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class StatisticsFragment extends Fragment {

    private static final String TAG = StatisticsFragment.class.getSimpleName();

    private TextView mStatisticsTV;

    @Nullable
    private StatisticsViewModel mViewModel;

    @Nullable
    private CompositeSubscription mSubscription;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        mStatisticsTV = (TextView) root.findViewById(R.id.statistics);

        mViewModel = StatisticsModule.createStatisticsViewModel(getContext());

        return root;
    }

    @Override
    public void onResume() {
        bindViewModel();
        super.onResume();
    }

    @Override
    public void onPause() {
        unbindViewModel();
        super.onPause();
    }

    private void bindViewModel() {
        Preconditions.checkNotNull(mViewModel);
        // using a CompositeSubscription to gather all the subscriptions
        // so all of them can be later unsubscribed together
        mSubscription = new CompositeSubscription();

        // The ViewModel holds an observable containing the state of the UI.
        // subscribe to the emissions of the UiModel
        // every time a new Ui Model is emitted, update the statistics
        mSubscription.add(mViewModel.getUiModel()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // onNext
                        this::updateStatistics,
                        // onError
                        throwable -> Log.e(TAG, "Error retrieving statistics text", throwable)));
    }

    private void unbindViewModel() {
        Preconditions.checkNotNull(mSubscription);
        // unsubscribing from all the subscriptions to ensure we don't have any memory leaks
        mSubscription.unsubscribe();
    }

    private void updateStatistics(@NonNull StatisticsUiModel statistics) {
        mStatisticsTV.setText(statistics.getText());
    }
}
