package net.lzzy.practicesonline.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.practicesonline.models.UserCookies;
import net.lzzy.practicesonline.models.view.QuestionResult;
import net.lzzy.practicesonline.models.view.WrongType;
import net.lzzy.practicesonline.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzzy_gxy on 2019/5/13.
 * Description:
 */
public class ChartFragment extends BaseFragment {
    private static final String ARG_RESULTS = "argResults";
    private static final String COLOR_GREEN = "#629755";
    private static final String COLOR_RED = "#D81B60";
    private static final String COLOR_PRIMARY = "#008577";
    private static final String COLOR_BROWN = "#00574B";
    public static final float MIN_DISTANCE = 10f;
    private List<QuestionResult> results;
    private OnResultSwitchListener listener;
    private PieChart pChart;
    private LineChart lChart;
    private BarChart bChart;
    private Chart[] charts;
    private View[] dots;
    private String[] titles = {"正确错误比例（单位%）", "题目阅读量统计", "题目错误类型统计"};
    private int rightCount = 0;
    private float touchX1;
    private int chartIndex = 0;

    public static ChartFragment newInstance(List<QuestionResult> results) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_RESULTS, (ArrayList<? extends Parcelable>) results);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            results = getArguments().getParcelableArrayList(ARG_RESULTS);
        }
        for (QuestionResult result : results) {
            if (result.isRight()) {
                rightCount++;
            }
        }
    }

    @Override
    protected void populate() {
        find(R.id.fragment_chart_tv_go).setOnClickListener(v -> {
            if (listener != null) {
                listener.gotoGrid();
            }
        });
        initCharts();
        configPieChart();
        displayPieChart();
        configBarLineChart(bChart);
        displayBarChart();
        configBarLineChart(lChart);
        displayLineChart();
        pChart.setVisibility(View.VISIBLE);
        View dot1 = find(R.id.fragment_chart_dot1);
        View dot2 = find(R.id.fragment_chart_dot2);
        View dot3 = find(R.id.fragment_chart_dot3);
        dots = new View[]{dot1, dot2, dot3};
        find(R.id.fragment_chart_container).setOnTouchListener(new ViewUtils.AbstractTouchListener() {
            @Override
            public boolean handleTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchX1 = event.getX();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float touchX2 = event.getX();
                    if (Math.abs(touchX2 - touchX1) > MIN_DISTANCE) {
                        if (touchX2 < touchX1) {
                            if (chartIndex < charts.length - 1) {
                                chartIndex++;
                            } else {
                                chartIndex = 0;
                            }
                        } else {
                            if (chartIndex > 0) {
                                chartIndex--;
                            } else {
                                chartIndex = charts.length - 1;
                            }
                        }
                        switchChart();
                    }
                }
                return true;
            }
        });
    }

    private void displayLineChart() {
        ValueFormatter xFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Q." + (int) value;
            }
        };
        lChart.getXAxis().setValueFormatter(xFormatter);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            int times = UserCookies.getInstance().getReadCount(results.get(i).getQuestionId().toString());
            entries.add(new Entry(i + 1, times));
        }
        LineDataSet dataSet = new LineDataSet(entries, "查看次数");
        dataSet.setColor(Color.parseColor(COLOR_RED));
        dataSet.setLineWidth(1f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor(COLOR_PRIMARY));
        dataSet.setValueTextSize(9f);
        LineData data = new LineData(dataSet);
        lChart.setData(data);
        lChart.invalidate();
    }

    private void displayBarChart() {
        ValueFormatter xFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return WrongType.getInstance((int) value).toString();
            }
        };
        bChart.getXAxis().setValueFormatter(xFormatter);
        int ok = 0, miss = 0, extra = 0, wrong = 0;
        for (QuestionResult result : results) {
            switch (result.getType()) {
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case RIGHT_OPTIONS:
                    ok++;
                    break;
                default:
                    break;
            }
        }
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, ok));
        entries.add(new BarEntry(1, miss));
        entries.add(new BarEntry(2, extra));
        entries.add(new BarEntry(3, wrong));
        BarDataSet dataSet = new BarDataSet(entries, "查看类型");
        dataSet.setColors(Color.parseColor(COLOR_PRIMARY), Color.parseColor(COLOR_RED),
                Color.parseColor(COLOR_GREEN), Color.parseColor(COLOR_BROWN));
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        BarData data = new BarData(dataSets);
        data.setBarWidth(0.8f);
        bChart.setData(data);
        bChart.invalidate();
    }

    private void configBarLineChart(BarLineChartBase chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(8f);
        xAxis.setGranularity(1f);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setLabelCount(8, false);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setTextSize(8f);
        yAxis.setGranularity(1f);
        yAxis.setAxisMinimum(0);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setPinchZoom(false);
    }

    private void switchChart() {
        for (int i = 0; i < charts.length; i++) {
            if (chartIndex == i) {
                charts[i].setVisibility(View.VISIBLE);
                dots[i].setBackgroundResource(R.drawable.dot_fill_style);
            } else {
                charts[i].setVisibility(View.GONE);
                dots[i].setBackgroundResource(R.drawable.dot_style);
            }
        }
    }

    private void displayPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(rightCount, "正确"));
        entries.add(new PieEntry(results.size() - rightCount, "错误"));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor(COLOR_GREEN));
        colors.add(Color.parseColor(COLOR_RED));
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        pChart.setData(data);
        pChart.invalidate();
    }

    private void configPieChart() {
        pChart.setUsePercentValues(true);
        pChart.setDrawHoleEnabled(false);
        pChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        pChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
    }

    private void initCharts() {
        pChart = find(R.id.fragment_chart_pie);
        lChart = find(R.id.fragment_chart_line);
        bChart = find(R.id.fragment_chart_bar);
        charts = new Chart[]{pChart, lChart, bChart};
        int i = 0;
        for (Chart chart : charts) {
            chart.setTouchEnabled(false);
            chart.setVisibility(View.GONE);
            Description desc = new Description();
            desc.setText(titles[i++]);
            chart.setDescription(desc);
            chart.setNoDataText("数据获取中……");
            chart.setExtraOffsets(5, 10, 5, 25);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chart;
    }

    @Override
    public void search(String kw) {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultSwitchListener) {
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
         * 查看表格
         */
        void gotoGrid();
    }
}
