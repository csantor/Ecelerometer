package com.example.christodoulos.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class OutputDisplayActivity extends Pedometer {

    private TextView textViewDisplay;
    private Button buttonReturn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_display);
        textViewDisplay = (TextView)findViewById(R.id.textViewDisplay);
        //Bundle extras = getIntent().getExtras();
        ArrayList<Float> list = ( ArrayList<Float>) getIntent().getSerializableExtra("valuesX");
        //textViewDisplay.setText(String.valueOf(list));
    }
}
