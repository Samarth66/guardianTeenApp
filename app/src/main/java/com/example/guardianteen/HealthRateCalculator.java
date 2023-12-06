package com.example.guardianteen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


public class HealthRateCalculator extends AppCompatActivity {
    Button sendAlertButton;
    SensorManager sensorManager;
    private boolean isMeasuring = false;
    Sensor accelerometer;
    private int heartRate = 0; // default value
    private int respiratoryR = 0; // default value
    private ArrayList<Double> accelValuesX = new ArrayList<Double>();
    private ArrayList<Double> accelValuesY = new ArrayList<Double>();
    private ArrayList<Double> accelValuesZ = new ArrayList<Double>();
    VideoView videoView;
    TextView RespText;
    String childId;
    TextView HeartText;
    int REQUEST_CODE_VIDEO_CAPTURE=2607;
    long startTime = 0; // Added variable for tracking start time of data capture
    private static final int CAMERA_REQUEST_CODE = 100;
    // Check if the app has permission to use the camera
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    // Request camera permission from the user
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can start the camera activity here
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_health_rate_calculator);
        sendAlertButton = findViewById(R.id.btnSendData);
        videoView = findViewById(R.id.videoView);
        HeartText = (TextView) findViewById(R.id.heartRateText);
        RespText = (TextView) findViewById(R.id.respRateText);
        RespText.setText("Respiratory Rate....");
        HeartText.setText("Heart Rate....");
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Button measRespRateButton = findViewById(R.id.btnMeasureRespRate);
        Intent intent = getIntent();
        childId = intent.getStringExtra("childId");
        measRespRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                measRespRate();
            }
        });

        sendAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace 'childId' with the actual value of childId


                // Create JSON data for alert
                JSONObject alertData = new JSONObject();
                try {
                    alertData.put("cid", childId);
                    alertData.put("type", "Health Checkup");
                    int heartRatee = HealthDataRepository.getInstance().getHeartRate();
                    int respiratoryRatee = HealthDataRepository.getInstance().getRespiratoryRate();

                    String description = String.format("Your child's HeartRate is %d bpm and Respiratory Rate is %d breaths/min", heartRatee, respiratoryRatee);
                    alertData.put("description", description);

                    alertData.put("time", System.currentTimeMillis());
                    alertData.put("location", "your_location_here"); // Replace with actual location data
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Show a toast with the location information (for testing)

                System.out.println("AlertData: " + childId);
                // Call the sendAlert method with alertData
                sendAlert(alertData);

                // You can add any additional logic here
            }
        });



    }

    private void sendAlert(JSONObject alertData) {
        String url = "https://guardianteenbackend.onrender.com/create";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, alertData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Alert sent successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Failed to send alert", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }


    public void measRespRate() {
        if(!isMeasuring) {
            isMeasuring = true;
            // Clear any existing accelerometer data
            accelValuesX.clear();
            accelValuesY.clear();
            accelValuesZ.clear();
            RespText.setText("Calulating. Please wait!");
            // Register the sensor event listener to start data collection
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            // Reset the startTime
            startTime = System.currentTimeMillis();
            // Schedule a task to stop after 45 seconds
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isMeasuring) {
                        isMeasuring = false;
                        sensorManager.unregisterListener(sensorEventListener);
                        int respiratoryRate = callRespiratoryCalculator();
                        respiratoryR=respiratoryRate;
                        int rate=respiratoryR;
                        HealthDataRepository.getInstance().setRespiratoryRate(rate);
                        RespText.setText("Respiratory Rate: " + respiratoryRate);
                    }
                }
            }, 45000); // 45 seconds
        }
        else {
            // Unregister the sensor listener if we are already measuring
            isMeasuring = false;
            sensorManager.unregisterListener(sensorEventListener);
            int respiratoryRate = callRespiratoryCalculator();
            HealthDataRepository.getInstance().setRespiratoryRate(respiratoryRate);// Optional: You can remove this line if you don't want an immediate calculation.
            RespText.setText("Measurement Stopped. Resp Rate:" + respiratoryRate); // Optional: Modify this text as needed
        }
    }
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime < 45000 && isMeasuring)) {
                accelValuesX.add((double) event.values[0]);
                accelValuesY.add((double) event.values[1]);
                accelValuesZ.add((double) event.values[2]);
                Log.d("Values are", accelValuesX+" -"+accelValuesY+" -"+accelValuesZ);
            } else if (isMeasuring) {
                sensorManager.unregisterListener(this);
                int respiratoryRate = callRespiratoryCalculator();
                RespText.setText("Respiratory Rate: " + respiratoryRate);
                startTime = 0;  // Reset startTime for the next measurement
                isMeasuring = false; // Reset the isMeasuring flag
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Do nothing
        }
    };
    private int callRespiratoryCalculator() {
        // Log size of arrays for debugging
        Log.d("Debug", "Size of accelValuesX: " + accelValuesX.size());
        Log.d("Debug", "Size of accelValuesY: " + accelValuesY.size());
        Log.d("Debug", "Size of accelValuesZ: " + accelValuesZ.size());

        double previousValue = 10f;
        double currentValue;
        int k = 0;
        int size = Math.min(Math.min(accelValuesX.size(), accelValuesY.size()), accelValuesZ.size());


        for (int i = 11; i <size; i++) {
            currentValue = (double) Math.sqrt(
                    Math.pow(accelValuesZ.get(i), 2) +
                            Math.pow(accelValuesX.get(i), 2) +
                            Math.pow(accelValuesY.get(i), 2)
            );

            if (Math.abs(previousValue - currentValue) > 0.15) {
                k++;
            }

            previousValue = currentValue;

        }

        double ret = (double)k / 45.0;
        return (int) (ret * 30);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister accelerometer listener
        sensorManager.unregisterListener(sensorEventListener);
    }
    public void recordHeartRate(View view) {
        if (checkCameraPermission()) {
            videoView.setVisibility(View.VISIBLE); // Show VideoView
            HeartText.setText("Calculating. Please Wait!");

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_VIDEO_CAPTURE);
            } else {
                Toast.makeText(this, "No camera app found!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();

            // Hide VideoView after recording
            videoView.setVisibility(View.GONE);

            String videoPath = convertMediaUriToPath(videoUri, getContentResolver());
            new SlowTask().execute(videoPath);
        }
    }
    public String convertMediaUriToPath(Uri uri, ContentResolver contentResolver) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(uri, proj, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }
    public class SlowTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d("Bitch", "SlowTask: doInBackground started");
            Bitmap m_bitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            ArrayList<Bitmap> frameList = new ArrayList<>();
            try {
                retriever.setDataSource(params[0]);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
                int aduration = Integer.parseInt(duration);
                int i = 10;
                while (i < aduration) {
                    Bitmap bitmap = retriever.getFrameAtIndex(i);
                    frameList.add(bitmap);
                    i += 5;
                }
            } catch (Exception m_e) {
                m_e.printStackTrace();
                return null;
                // Handle the exception if necessary
            } finally {
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                long redBucket = 0;
                long pixelCount = 0;
                List<Long> a = new ArrayList<>();
                for (Bitmap i : frameList) {
                    redBucket = 0;
                    for (int y = 550; y < 650; y++) {
                        for (int x = 550; x < 650; x++) {
                            int c = i.getPixel(x, y);
                            pixelCount++;
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c);
                        }
                    }
                    a.add(redBucket);
                }
                List<Long> b = new ArrayList<>();
                for (int i = 0; i < a.size() - 5; i++) {
                    long temp = (a.get(i) + a.get(i + 1) + a.get(i + 2) + a.get(i + 3) + a.get(i + 4)) / 4;
                    b.add(temp);
                }
                long x = b.get(0);
                int count = 0;
                for (int i = 1; i < b.size() - 1; i++) {
                    long p = b.get(i);
                    if ((p - x) > 3500) {
                        count++;
                    }
                    x = b.get(i);
                }
                int rate = (int) ((count / 45.0) * 60);
                return Integer.toString(rate / 2);
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                int heartRate = Integer.parseInt(result);
                HealthDataRepository.getInstance().setHeartRate(heartRate);
                HeartText.setText("Heart Rate: " + result);
            } else {
                HeartText.setText("Calculation failed!");
            }

        }





    }
}