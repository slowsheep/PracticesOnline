package net.lzzy.practicesonline.activities;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.fragments.ChartFragment;
import net.lzzy.practicesonline.fragments.GridFragment;
import net.lzzy.practicesonline.models.view.QuestionResult;

import java.util.List;

/**
 * @author lzzy_gxy on 2019/5/13.
 * Description:
 */
public class ResultActivity extends BaseActivity implements GridFragment.OnResultSwitchListener,
        ChartFragment.OnResultSwitchListener {

    public static final String EXTRA_POSITION = "extraPosition";
    public static final String EXTRA_VIEW_FAVORITE = "extraViewFavorite";
    private List<QuestionResult> results;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_result;
    }

    @Override
    protected int getContainerId() {
        return R.id.activity_result_container;
    }

    @Override
    protected Fragment createFragment() {
        results = getIntent().getParcelableArrayListExtra(QuestionActivity.EXTRA_RESULT);
        return GridFragment.newInstance(results);
    }

    @Override
    public void goBack(int position) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION,position);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void gotoChart() {
        getManager().beginTransaction()
                .replace(getContainerId(), ChartFragment.newInstance(results)).commit();
    }

    @Override
    public void gotoGrid() {
        getManager().beginTransaction()
                .replace(getContainerId(), GridFragment.newInstance(results)).commit();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("选择返回路径")
                .setNeutralButton("查看收藏",(dialog, which) -> goBackFavorite())
                .setNegativeButton("章节列表",(dialog, which) -> goBackMain())
                .setPositiveButton("返回题目",(dialog, which) -> goBack(-1))
                .show();
    }

    private void goBackMain() {
        startActivity(new Intent(this,PracticesActivity.class));
        finish();
    }

    private void goBackFavorite() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_VIEW_FAVORITE,true);
        intent.putExtra(EXTRA_POSITION,0);
        setResult(RESULT_OK,intent);
        finish();
    }
}
