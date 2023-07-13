package com.example.tenkinoko;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String thServerUrl = "http://101.42.226.88:5000/Weather/Current?deviceName=device1";
    private LinearLayout btnSwitchSendor;
    private LinearLayout btnRainStatus;
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
        btnSwitchSendor = findViewById(R.id.main_btn_switch_sensor);
        btnSwitchSendor.setOnClickListener(v -> switchSensorBtnClick());

        btnRainStatus = findViewById(R.id.main_btn_rain_status);
        btnRainStatus.setOnClickListener(v -> updateTemperatureAndHumidity());

        textTemperature = findViewById(R.id.text_temperature);
        btnTemperature = findViewById(R.id.main_btn_temperature);
        btnTemperature.setOnClickListener(v -> updateTemperatureAndHumidity());

        textHumidity = findViewById(R.id.text_humidity);
        btnHumidity = findViewById(R.id.main_btn_humidity);
        btnHumidity.setOnClickListener(v -> updateTemperatureAndHumidity());
    }

    private void switchSensorBtnClick() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_change_lock_mode, null);
        final Spinner spinner = alertLayout.findViewById(R.id.lock_mode_spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.sensor_name, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // 构造对话框
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.str_main_text_sensor_title);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton(R.string.str_dialog_cancel, (dialog, which) -> dialog.cancel());
        alert.setPositiveButton(R.string.str_dialog_confirm, (dialog, which) -> {
            String data = spinner.getSelectedItem().toString();
            int length = data.length();
            int sensorId = (int)data.toCharArray()[length - 1] - (int)'0';
            switchSensor(sensorId);
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void switchSensor(int id) {
        Toast.makeText(this, String.format("%d", id), Toast.LENGTH_LONG).show();
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
        // TODO DEBUG
        new Thread(() -> {
            try {
                Response response = client.newCall(thRequest).execute();
                String responseData = response.body().string();
                Gson gson = new Gson();
                THJsonData thJsonData = gson.fromJson(responseData, THJsonData.class);
                double t = thJsonData.temp;
                double h = thJsonData.humidity;
                updateTHView(t, h);
                // TODO update rain status
                // TODO update line chart
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