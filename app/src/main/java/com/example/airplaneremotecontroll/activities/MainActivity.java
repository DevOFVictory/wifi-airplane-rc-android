package com.example.airplaneremotecontroll.activities;

import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.airplaneremotecontroll.R;
import com.example.airplaneremotecontroll.objects.SpeedometerView;
import com.example.airplaneremotecontroll.utils.ConnectionUtil;
import com.example.airplaneremotecontroll.utils.MovementUtil;
import com.example.airplaneremotecontroll.utils.OtherUtils;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static MainActivity instance;
    private static Sensor mySensor;
    private static SensorManager sensorManager;
    SpeedometerView speedometer;
    public static boolean stopped = true;
    SeekBar speedBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        System.out.println("App started");
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            initSpeedBar();
            initSensors();
            startClock();
            initStopButton();
            initSpeedometer();

        }else {
            Button connect_button = findViewById(R.id.button_connect);
            connect_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Reconnecting...");
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {

                                ConnectionUtil.connect();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            });

            Button disconnect_button = findViewById(R.id.button_disconnect);
            disconnect_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionUtil.disconnect();
                }
            });

            Button send_debug_button = findViewById(R.id.button_send_debug);
            EditText input_debug_packet = findViewById(R.id.input_debug_packet);


            send_debug_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!ConnectionUtil.socket.isClosed() && ConnectionUtil.socket.isConnected()) {

                        Thread thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {

                                    ConnectionUtil.sendMessage(input_debug_packet.getText().toString());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();

                    }else {
                        OtherUtils.popupMessage("Not connected", "The application client is not connected to the server.");
                    }

                }
            });

            SeekBar seekBar_packet_delay = findViewById(R.id.seekBar_packet_delay);
            EditText label_packet_delay = findViewById(R.id.input_packet_delay);

            seekBar_packet_delay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    label_packet_delay.setText(seekBar_packet_delay.getProgress()+"");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });

            CheckBox checkBox_default = findViewById(R.id.checkBox_default);
            EditText input_ip = findViewById(R.id.text_input_ip);
            EditText input_port = findViewById(R.id.text_input_port);
            checkBox_default.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    input_ip.setEnabled(!checkBox_default.isChecked());
                    input_port.setEnabled(!checkBox_default.isChecked());
                    input_ip.setTextColor(checkBox_default.isChecked() ? Color.parseColor("#808080") : Color.WHITE);
                    input_port.setTextColor(checkBox_default.isChecked() ? Color.parseColor("#808080") : Color.WHITE);



                }
            });

        }

        if (savedInstanceState == null) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ConnectionUtil.connect();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("==================================================================" + newConfig.orientation);

    }

    private void initSpeedometer() {
        speedometer = findViewById(R.id.speedometer);
        speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setMaxSpeed(100);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(0);

        speedometer.addColoredRange(0, 50, Color.GREEN);
        speedometer.addColoredRange(50, 75, Color.YELLOW);
        speedometer.addColoredRange(75, 100, Color.RED);


    }

    private void initStopButton() {
        ImageButton stopButton = findViewById(R.id.button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Emergency Stop!");
                stopped = true;
                MovementUtil.height = 0;
                MovementUtil.site = 0;
                MovementUtil.speed = 0;
                speedBar.setProgress(0);

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            String message = MovementUtil.speed + "," + MovementUtil.site + "," + MovementUtil.height + "\n";
                            ConnectionUtil.sendMessage(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();


                OtherUtils.popupMessage("Emergency Stop", "All network traffic was stopped and motors turned off!");
            }
        });

    }

    private void startClock() {
        final TextView timeLabel = findViewById(R.id.label_time);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    timeLabel.setText(OtherUtils.getFormattedTime());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        thread.start();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    private void initSpeedBar() {
        speedBar = findViewById(R.id.seekbar_speed);
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MovementUtil.speed = progress;
                System.out.println(MovementUtil.speed);
                speedometer.setSpeed(progress);
                if (fromUser)
                    stopped = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    public void initSensors() {
        sensorManager = (SensorManager) MainActivity.getInstance().getSystemService(MainActivity.getInstance().SENSOR_SERVICE);

        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //System.out.println(event.values[0] + " / " + event.values[1] + " / " + event.values[2]);

        MovementUtil.site = OtherUtils.reRangeNumber(event.values[1]);
        MovementUtil.height = OtherUtils.reRangeNumber(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}