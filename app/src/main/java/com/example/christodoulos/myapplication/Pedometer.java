package com.example.christodoulos.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;



public class Pedometer extends AppCompatActivity {
    private TextView textViewX,textViewY,textViewZ,textThreshold,textViewSteps,textViewDistance,textViewUserActivity;
    //private Button buttonReset, buttonStart, buttonStop;
    private Button buttonResults;
    private SeekBar seekBar;
    public SensorManager mSensorManager;
    private int steps;
    private boolean stepCount, startFlag, stopFlag, stepCountFlag;
    private float threshold;
    private List accX,accY,accZ,filX,filY,filZ,dotProd,filDot1,filDot2, g;
    private LineGraphSeries<DataPoint> series1,series2,series3;
    private GraphView graph;
    private int i=0;
    private Chronometer clock1;
    private Toast toastTime, toastSteps, toastDistance;
    private ImageButton timeButton, stepsButton, distanceButton;
    private long lastPause;
    private static final int REQUEST_ID_READ_PERMISSION = 100;
    private static final int REQUEST_ID_WRITE_PERMISSION = 200;
    private FileWriter writer;
    private ArrayList<Float> valuesX ;
    /*private List<ActivityTransition> transitions;
    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent transitionPendingIntent;
    private Context mContext;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);

        /*mContext = this;
        activityRecognitionClient = ActivityRecognition.getClient(mContext);

        Intent intent = new Intent(this, TransitionIntentService.class);
        transitionPendingIntent = PendingIntent.getService(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/

        textViewX = (TextView)findViewById(R.id.textViewX);
        textViewY = (TextView)findViewById(R.id.textViewY);
        textViewZ = (TextView)findViewById(R.id.textViewZ);

        textViewSteps = (TextView)findViewById(R.id.textSteps);
        textViewDistance = (TextView)findViewById(R.id.textDistance);
        textViewUserActivity = (TextView)findViewById(R.id.textViewUserActivity);
        //textThreshold = (TextView)findViewById(R.id.textThreshold);

        //buttonReset = (Button)findViewById(R.id.buttonReset);
        //buttonStart = (Button)findViewById(R.id.buttonStart);
        //buttonStop = (Button)findViewById(R.id.buttonStop);

        //seekBar = (SeekBar)findViewById(R.id.seekBar);
        buttonResults = (Button) findViewById(R.id.buttonResults);

        valuesX = new ArrayList<>();





        accX = new ArrayList();
        accY = new ArrayList();
        accZ = new ArrayList();
        filX = new ArrayList();
        filY = new ArrayList();
        filZ = new ArrayList();
        dotProd = new ArrayList();
        filDot1 = new ArrayList();
        filDot2 = new ArrayList();
        g = new ArrayList();

        accX.add(0.0f);accX.add(0.0f);
        accY.add(0.0f);accY.add(0.0f);
        accZ.add(0.0f);accZ.add(0.0f);
        filX.add(0.0f);filX.add(0.0f);filX.add(0.0f);
        filY.add(0.0f);filY.add(0.0f);filY.add(0.0f);
        filZ.add(0.0f);filZ.add(0.0f);filZ.add(0.0f);
        dotProd.add(0.0f);dotProd.add(0.0f);dotProd.add(0.0f);
        filDot1.add(0.0f);filDot1.add(0.0f);filDot1.add(0.0f);
        filDot2.add(0.0f);filDot2.add(0.0f);filDot2.add(0.0f);
        g.add(0.0f);g.add(0.0f);

        steps = 0;
        stepCount = true;

        graph = (GraphView) findViewById(R.id.graph);
        series1 = new LineGraphSeries<DataPoint>();
        series2 = new LineGraphSeries<DataPoint>();
        series3 = new LineGraphSeries<DataPoint>();
        graph.addSeries(series1);
        graph.addSeries(series2);
        graph.addSeries(series3);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-15);
        viewport.setMaxY(15);
        viewport.setScrollable(true);
        series1.setColor(0xffff0000);
        series2.setColor(0xff00ff00);
        series3.setColor(0xff0000ff);
        series1.setTitle("X-axis");
        series2.setTitle("Y-axis");
        series3.setTitle("Z-axis");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        clock1 = (Chronometer) findViewById(R.id.clock1);
        clock1.start();
        startFlag = false;
        stopFlag = true;
        stepCountFlag = true;

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        toastTime = Toast.makeText(context, "Elapsed Time", duration);
        toastSteps = Toast.makeText(context, "Steps Taken", duration);
        toastDistance = Toast.makeText(context, "Approximate Distance (km)", duration);



        SensorActivity();
    }

    public void SensorActivity() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(sensorEventListener,  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
    }

    public SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override

        public void onSensorChanged(SensorEvent event) {
            Sensor mySensor = event.sensor;
            i++;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {



                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                valuesX.add(x);

                series1.appendData(new DataPoint(i, x), false, 1000);
                series2.appendData(new DataPoint(i, y), false, 1000);
                series3.appendData(new DataPoint(i, z), false, 1000);

                //float g = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
                accX.add(x);
                accY.add(y);
                accZ.add(z);
                g.add((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH));

                filX.add(1f * (((float) (accX.get(accX.size() - 1))) * 0.000086384997973502f + ((float) (accX.get(accX.size() - 2))) * 0.000172769995947004f + ((float) (accX.get(accX.size() - 3))) * 0.000086384997973502f - ((float) (filX.get(filX.size() - 2))) * (-1.979133761292768f) - ((float) (filX.get(filX.size() - 3))) * 0.979521463540373f));
                filY.add(1f * (((float) (accY.get(accX.size() - 1))) * 0.000086384997973502f + ((float) (accY.get(accX.size() - 2))) * 0.000172769995947004f + ((float) (accY.get(accX.size() - 3))) * 0.000086384997973502f - ((float) (filY.get(filY.size() - 2))) * (-1.979133761292768f) - ((float) (filY.get(filY.size() - 3))) * 0.979521463540373f));
                filZ.add(1f * (((float) (accZ.get(accX.size() - 1))) * 0.000086384997973502f + ((float) (accZ.get(accX.size() - 2))) * 0.000172769995947004f + ((float) (accZ.get(accX.size() - 3))) * 0.000086384997973502f - ((float) (filZ.get(filZ.size() - 2))) * (-1.979133761292768f) - ((float) (filZ.get(filZ.size() - 3))) * 0.979521463540373f));

                dotProd.add((float) (accX.get(accX.size() - 1)) * (float) (filX.get(filX.size() - 1)) + (float) (accY.get(accY.size() - 1)) * (float) (filY.get(filY.size() - 1)) + (float) (accZ.get(accZ.size() - 1)) * (float) (filZ.get(filZ.size() - 1)));

                filDot1.add(1f * (((float) (dotProd.get(dotProd.size() - 1))) * 0.095465967120306f + ((float) (dotProd.get(dotProd.size() - 2))) * (-0.172688631608676f) + ((float) (dotProd.get(dotProd.size() - 3))) * 0.095465967120306f - ((float) (filDot1.get(filDot1.size() - 2))) * (-1.80898117793047f) - ((float) (filDot1.get(filDot1.size() - 3))) * 0.827224480562408f));
                filDot2.add(1f * (((float) (filDot1.get(filDot1.size() - 1))) * 0.953986986993339f + ((float) (filDot1.get(filDot1.size() - 2))) * (-1.907503180919730f) + ((float) (filDot1.get(filDot1.size() - 3))) * 0.953986986993339f - ((float) (filDot2.get(filDot2.size() - 2))) * (-1.905384612118461f) - ((float) (filDot2.get(filDot2.size() - 3))) * 0.910092542787947f));

                if ((float) (g.get(g.size() - 1)) >= 1.5f && (float) (g.get(g.size() - 2)) < 1.5f && stepCount && stepCountFlag) {
                    steps++;
                    stepCount = false;
                    textViewSteps.setText(String.valueOf(steps));
                }
                if ((float) g.get(g.size() - 1) < 1f && (float) accY.get(accY.size() - 2) >= 1f) {
                    stepCount = true;
                }
                //currentG = g;

                /*if ((float)filDot2.get(filDot2.size()-1)>=100000 && (float)filDot2.get(filDot2.size()-2)<100000 && stepCount){
                    steps++;
                    stepCount=false;
                    textViewSteps.setText(String.valueOf(steps));
                }
                if ((float)filDot2.get(filDot2.size()-1)<0f && (float)filDot2.get(filDot2.size()-2)>=0f){
                    stepCount=true;
                }*/

                //previousG=g

                buttonResults.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSensorManager.unregisterListener(sensorEventListener,  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                        startActivity(new Intent(Pedometer.this, OutputDisplayActivity.class).putExtra("valuesX",valuesX));
                        // TODO Auto-generated method stub

                    }
                });



                textViewX.setText(String.valueOf(x));
                textViewY.setText(String.valueOf(y));
                textViewZ.setText(String.valueOf(z));
                DecimalFormat f = new DecimalFormat("#0.00");
                textViewDistance.setText(String.valueOf(f.format(steps * 0.0007)));
                //writeFileOnInternalStorage("walk_200_steps.txt","x=" + String.valueOf(x) + "\n" +
                        //"y=" + String.valueOf(y) + "\n" + "z=" + String.valueOf(z) + "\n");
                generateNoteOnSD("Drastiriotita", "x="+Float.toString(x)+"\n"+"y="+Float.toString(y)+"\n"+"z="+Float.toString(z)+"\n");
            }
        }
        public void writeFileOnInternalStorage(String sFileName, String sBody){
            try {
                FileOutputStream fOut = openFileOutput(sFileName,MODE_PRIVATE);
                fOut.write(sBody.getBytes());
                fOut.close();
                Toast.makeText(getBaseContext(),"file saved",Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {

                e.printStackTrace();
            }
        }

        public void generateNoteOnSD(String sFileName, String sBody){
            try{
                File root = new File(Environment.getExternalStorageDirectory(), "Notes");

                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, sFileName);
                FileWriter writer = new FileWriter(gpxfile,true);
                writer.append(sBody);
                writer.flush();
                writer.close();
                Toast.makeText(getBaseContext(), "file saved", Toast.LENGTH_SHORT).show();
            }
            catch(IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void onResume(){
        super.onResume();
        //mSensorManager.registerListener(sensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onPause() {
        super.onPause();

    }

    public void stopChronometer(View view){
        if (stopFlag) {
            lastPause = SystemClock.elapsedRealtime();
            clock1.stop();
            startFlag = true;
            stopFlag = false;
            stepCountFlag = false;
            mSensorManager.unregisterListener(sensorEventListener,  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        }
    }

    public void startChronometer(View view){
        if (startFlag) {
            startFlag = false;
            stopFlag = true;
            clock1.setBase(clock1.getBase() + SystemClock.elapsedRealtime() - lastPause);
            clock1.start();
            stepCountFlag = true;
            mSensorManager.registerListener(sensorEventListener,  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void resetSteps(View view){
        steps = 0;
        textViewSteps.setText(String.valueOf(steps));
        clock1.setBase(SystemClock.elapsedRealtime());
        clock1.start();
        startFlag = false;
        stopFlag = true;
        stepCountFlag = true;
        mSensorManager.registerListener(sensorEventListener,  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    /*public void showResults(View view){

                startActivity(new Intent(Pedometer.this, OutputDisplayActivity.class).putExtra("name","john"));


    }*/
    public void showTimeMessage(View view){
        toastTime.show();
    }
    public void showStepsMessage(View view){
        toastSteps.show();
    }
    public void showDistanceMessage(View view){
        toastDistance.show();
    }




}
