package com.example.tenkinoko;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String _baseUrl = "http://101.42.226.88:5000";
    private LinearLayout btnSwitchSendor;
    private LinearLayout btnRainStatus;
    private LinearLayout btnTemperature;
    private LinearLayout btnHumidity;
    private TextView textRainStatus;
    private TextView textTemperature;
    private TextView textHumidity;
    private OkHttpClient _client;
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initServices();
        initView();
        initHistoryChart();
    }

    private void initServices() {
        // okhttp client
        _client = new OkHttpClient();

        this.updateCurrentWeather();
        this.updateHistoryWeather();
    }

    private void initView() {
        btnSwitchSendor = findViewById(R.id.main_btn_switch_sensor);
        btnSwitchSendor.setOnClickListener(v -> switchSensorBtnClick());

        textRainStatus = findViewById(R.id.text_rain_status);
        btnRainStatus = findViewById(R.id.main_btn_rain_status);
        btnRainStatus.setOnClickListener(v -> updateCurrentWeather());

        textTemperature = findViewById(R.id.text_temperature);
        btnTemperature = findViewById(R.id.main_btn_temperature);
        btnTemperature.setOnClickListener(v -> updateCurrentWeather());

        textHumidity = findViewById(R.id.text_humidity);
        btnHumidity = findViewById(R.id.main_btn_humidity);
        btnHumidity.setOnClickListener(v -> updateCurrentWeather());
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
            int sensorId = (int) data.toCharArray()[length - 1] - (int) '0';
            switchSensor(sensorId);
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @SuppressLint("DefaultLocale")
    private void switchSensor(int id) {
        this.deviceName = "device" + id;
    }

    private void initHistoryChart() {
        LineChart chart = findViewById(R.id.chart);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);

            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });
    }

    private void updateHistoryView(List<Entry> tempPoints) {
        // 获取对 LineChart 视图的引用
        LineChart chart = findViewById(R.id.chart);
        // 创建一个 LineDataSet 对象来保存数据并设置图表的样式
        LineDataSet tempData = new LineDataSet(tempPoints, "Temp");

        tempData.setColor(Color.RED);
        tempData.setValueTextColor(Color.BLUE);

        // 创建一个 LineData 对象并将 LineDataSet 添加到其中
        LineData tempLine = new LineData(tempData);

        chart.setData(tempLine);
        // 调用 chart.invalidate() 方法以更新图表
        chart.invalidate();
    }

    private void updateHistoryWeather() {
        HttpUrl.Builder urlBuilder
                = Objects.requireNonNull(HttpUrl.parse(_baseUrl + "/Weather/History")).newBuilder();
        urlBuilder.addQueryParameter("deviceName", this.deviceName);

        String url = urlBuilder.build().toString();

        Request historyRequest = new Request.Builder()
                .url(url)
                .build();

        Call call = _client.newCall(historyRequest);
        call.enqueue(new Callback() {
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseData = response.body().string();

                Gson gson = new Gson();

                ArrayList<WeatherJsonData> data = gson.fromJson(responseData, new TypeToken<List<WeatherJsonData>>() {
                }.getType());

                List<Entry> temp = data.stream()
                        .map(x -> new Entry(x.time.getTime(), (float) x.temp))
                        .collect(Collectors.toList());

                updateHistoryView(temp);
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // failed
                Log.e("Sensor Request", "request failed with " + e.getLocalizedMessage());
            }
        });
    }

    private void updateCurrentWeather() {
        HttpUrl.Builder urlBuilder
                = Objects.requireNonNull(HttpUrl.parse(_baseUrl + "/Weather/Current")).newBuilder();
        urlBuilder.addQueryParameter("deviceName", this.deviceName);

        String url = urlBuilder.build().toString();

        Request currentRequest = new Request.Builder()
                .url(url)
                .build();

        Call call = _client.newCall(currentRequest);
        call.enqueue(new Callback() {
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseData = response.body().string();

                Gson gson = new Gson();

                WeatherJsonData data = gson.fromJson(responseData, WeatherJsonData.class);

                double t = data.temp;
                double h = data.humidity;
                boolean r = data.rain > 5;

                updateWeatherView(t, h, r);
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // failed
                Log.e("Sensor Request", "request failed with " + e.getLocalizedMessage());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateWeatherView(double t, double h, boolean r) {
        runOnUiThread(() -> {
            textTemperature.setText(t + "℃");
            textHumidity.setText(h + "%");
            textRainStatus.setText(r ? "下雨" : "晴天");
        });
    }
}