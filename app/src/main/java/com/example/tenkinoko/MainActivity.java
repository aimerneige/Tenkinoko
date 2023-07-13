package com.example.tenkinoko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private final String thServerUrl = "http://101.34.24.60:5000/getTH";
    private LinearLayout btnTemperature;
    private LinearLayout btnHumidity;
    private TextView textTemperature;
    private TextView textHumidity;
    private OkHttpClient client;
    private Request thRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initServices();
        initView();
        initDemoChart();
    }

    private void initServices() {
        // okhttp client
        client = new OkHttpClient();

        // okhttp request
        thRequest = new Request.Builder()
                .url(thServerUrl)
                .build();
    }

    private void initView() {
        textTemperature = findViewById(R.id.text_temperature);
        btnTemperature = findViewById(R.id.main_btn_temperature);
        btnTemperature.setOnClickListener(v -> updateTemperatureAndHumidity());

        textHumidity = findViewById(R.id.text_humidity);
        btnHumidity = findViewById(R.id.main_btn_humidity);
        btnHumidity.setOnClickListener(v -> updateTemperatureAndHumidity());
    }

    private void initDemoChart() {
        // 获取对 LineChart 视图的引用
        LineChart chart = findViewById(R.id.chart);
        // 表格数据
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4));
        entries.add(new Entry(1, 8));
        entries.add(new Entry(2, 6));
        entries.add(new Entry(3, 2));
        entries.add(new Entry(4, 7));
        // 创建一个 LineDataSet 对象来保存数据并设置图表的样式
        LineDataSet dataSet = new LineDataSet(entries, "Label"); // entries 是一个包含数据点的 List<Entry> 对象
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLUE);
        // 创建一个 LineData 对象并将 LineDataSet 添加到其中
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        // 调用 chart.invalidate() 方法以更新图表
        chart.invalidate();
    }

    private void updateTemperatureAndHumidity() {
        new Thread(() -> {
            try {
                Response response = client.newCall(thRequest).execute();
                String responseData = response.body().string();
                Gson gson = new Gson();
                THJsonData thJsonData = gson.fromJson(responseData, THJsonData.class);
                if (thJsonData.data.size() > 0) {
                    double t = thJsonData.data.get(0).T;
                    double h = thJsonData.data.get(0).H;
                    updateTHView(t, h);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTHView(double t, double h) {
        runOnUiThread(() -> {
            textTemperature.setText(t + "℃");
            textHumidity.setText(h + "%");
        });
    }
}