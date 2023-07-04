package com.example.baymaxzilla;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static BluetoothDevice hc06;
    public static BluetoothSocket bluetoothSocket;
    public static OutputStream outputStream;
    private TextView tvgiroscopiox, tvgiroscopioy, tvgiroscopioz, tvindicaciones;
    private float sensitivity = 5.0f;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private int lastDirection = RELEASED;

    // Variables para la comunicación Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private static final float THRESHOLD = 1.5f;
    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int RELEASED = 4;

    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String DEVICE_ADDRESS = "00:00:00:00:00:00"; // Coloca la dirección MAC de tu dispositivo Bluetooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvgiroscopiox = findViewById(R.id.tvgiroscopiox);
        tvgiroscopioy = findViewById(R.id.tvgiroscopioy);
        tvgiroscopioz = findViewById(R.id.tvgiroscopioz);
        tvindicaciones = findViewById(R.id.tvindicaciones);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Inicializar la comunicación Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no es compatible con Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            String gyroscopeValuesx = "x = " + x * sensitivity;
            String gyroscopeValuesy = "y = " + y * sensitivity;
            String gyroscopeValuesz = "z = " + z * sensitivity;

            tvgiroscopiox.setText(gyroscopeValuesx);
            tvgiroscopioy.setText(gyroscopeValuesy);
            tvgiroscopioz.setText(gyroscopeValuesz);

            // Obtener la dirección basada en los valores del giroscopio
            int direction = getDirectionFromGyroscope(x, y, z);

            // Verificar si la dirección ha cambiado desde la última vez
            if (lastDirection != direction) {
                switch (direction) {
                    case UP:
                        tvindicaciones.setText("Arriba");
                        enviarMsjBt((byte) 52);
                        break;
                    case DOWN:
                        tvgiroscopiox.setText("Abajo");
                        enviarMsjBt((byte) 51);
                        break;
                    case LEFT:
                        tvgiroscopiox.setText("Derecha");
                        enviarMsjBt((byte) 50);
                        break;
                    case RIGHT:
                        tvgiroscopiox.setText("Izquierda");
                        enviarMsjBt((byte) 49);
                        break;
                    case RELEASED:
                        tvgiroscopiox.setText("Detenido");
                        enviarMsjBt((byte) 53);
                        break;
                }
                lastDirection = direction;
            }
        }
    }
    private int getDirectionFromGyroscope(float x, float y, float z) {
        if (x > 1.0f) {
            tvindicaciones.setText("Derecha");
            enviarMsjBt((byte) 50);
        } else if (x < -1.0f) {
            tvindicaciones.setText("Izquierda");
            enviarMsjBt((byte) 49);
        } else if (y > 1.0f) {
            tvindicaciones.setText("Adelante");
            enviarMsjBt((byte) 52);
        } else if (y < -1.0f) {
            tvindicaciones.setText("Abajo");
            enviarMsjBt((byte) 51);
        } else if() {
            tvindicaciones.setText("Detenido");
            enviarMsjBt((byte) 53);
        }
        return lastDirection;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void enviarMsjBt(byte controlValue) {
        if (outputStream != null) {
            try {
                outputStream.write(controlValue);
            } catch (IOException e) {
                Toast.makeText(this, "Error al enviar mensaje por Bluetooth: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
