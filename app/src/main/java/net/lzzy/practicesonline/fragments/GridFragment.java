package net.lzzy.practicesonline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.models.view.QuestionResult;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzzy_gxy on 2019/5/13.
 * Description:
 */
public class GridFragment extends BaseFragment {
    private static final String ARG_RESULTS = "argResults";
    private List<QuestionResult> results;
    private OnResultSwitchListener listener;

    public static GridFragment newInstance(List<QuestionResult> results){
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_RESULTS, (ArrayList<? extends Parcelable>) results);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            results = getArguments().getParcelableArrayList(ARG_RESULTS);
        }
    }

    @Override
    protected void populate() {
        GridView gv = find(R.id.fragment_grid_gv);
        GenericAdapter<QuestionResult> adapter =
                new GenericAdapter<QuestionResult>(getContext(),R.layout.result_item,results) {
            @Override
            public void populate(ViewHolder holder, QuestionResult result) {
                int order = getPosition(result) + 1;
                TextView tv = holder.getView(R.id.result_item_tv);
                tv.setText(String.valueOf(order));
                int bg = result.isRight() ? R.drawable.right_style : R.drawable.wrong_style;
                tv.setBackgroundResource(bg);
            }

            @Override
            public boolean persistInsert(QuestionResult questionResult) {
                return false;
            }

            @Override
            public boolean persistDelete(QuestionResult questionResult) {
                return false;
            }
        };
        gv.setAdapter(adapter);
        gv.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null){
                listener.goBack(position);
            }
        });
        find(R.id.fragment_grid_tv_go).setOnClickListener(v -> {
            if (listener != null){
                listener.gotoChart();
            }
        });
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_grid;
    }

    @Override
    public void search(String kw) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultSwitchListener){
            listener = (OnResultSwitchListener) context;
        } else {
            throw new ClassCastException(context.toString() + "必须实现OnResultSwitchListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnResultSwitchListener {
        /**
         * 返回题目视图查看题目
         * @param position 具体位置
         */
        void goBack(int position);

        /**
         * 查看图表
         */
        void gotoChart();
    }
}
