package com.mzkyzak.ddospro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private EditText etTarget, etThreads, etDuration, etMethod;
    private Button btnStart, btnStop, btnSaturate, btnStressHp, btnProfStress, btnMHDDoS;
    private TextView tvStatus, tvMethod, tvStats, tvTimer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private long startTime = 0;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        etTarget = findViewById(R.id.etTarget);
        etThreads = findViewById(R.id.etThreads);
        etDuration = findViewById(R.id.etDuration);
        etMethod = findViewById(R.id.etMethod);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnSaturate = findViewById(R.id.btnSaturate);
        btnStressHp = findViewById(R.id.btnStressHp);
        btnProfStress = findViewById(R.id.btnProfStress);
        btnMHDDoS = findViewById(R.id.btnMHDDoS);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvMethod = findViewById(R.id.tvMethod);
        tvStats = findViewById(R.id.tvStats);

        etTarget.setText("https://target-audit.internal");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        btnStart.setOnClickListener(v -> startMission(AttackService.class));
        btnStop.setOnClickListener(v -> stopMission());
        btnSaturate.setOnClickListener(v -> startMission(LocalSaturationService.class));
        btnStressHp.setOnClickListener(v -> startMission(LocalStressService.class));
        btnProfStress.setOnClickListener(v -> toggleProfStress());
        btnMHDDoS.setOnClickListener(v -> toggleMHDDoS());
    }

    private void startMission(Class<?> serviceClass) {
        if (isRunning) return;
        
        String target = etTarget.getText().toString().trim();
        int threads = Integer.parseInt(etThreads.getText().toString());
        int duration = Integer.parseInt(etDuration.getText().toString());

        Intent intent = new Intent(this, serviceClass);
        intent.putExtra("target", target);
        intent.putExtra("threads", threads);
        intent.putExtra("duration", duration);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        isRunning = true;
        startTime = System.currentTimeMillis();
        tvStatus.setText("🟢 RUNNING");
        tvStatus.setTextColor(0xFF00FF00);
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        if (vibrator != null && Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        handler.postDelayed(updateStats, 1000);
    }

    private void stopMission() {
        stopService(new Intent(this, AttackService.class));
        stopService(new Intent(this, LocalSaturationService.class));
        stopService(new Intent(this, LocalStressService.class));
        stopService(new Intent(this, NetworkStressService.class));
        stopService(new Intent(this, MHDDoSBridgeService.class));
        
        isRunning = false;
        startTime = 0;
        btnProfStress.setText("🛡️ PERSISTENT MISSION (10H)");
        btnMHDDoS.setText("🌀 MISI MHDDoS (EXTERNAL)");
        tvStatus.setText("🔴 STANDBY");
        tvStatus.setTextColor(0xFFFF0000);
        tvTimer.setText("00:00:00");
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        handler.removeCallbacks(updateStats);

        if (vibrator != null && Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void toggleProfStress() {
        if (isRunning && startTime > 0) {
            stopMission();
        } else {
            Intent intent = new Intent(this, NetworkStressService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            isRunning = true;
            startTime = System.currentTimeMillis();
            btnProfStress.setText("⛔ STOP MISSION");
            tvStatus.setText("🟢 MISSION PERSISTENT");
            tvStatus.setTextColor(0xFF00FF00);
            handler.postDelayed(updateStats, 1000);
        }
    }

    private void toggleMHDDoS() {
        if (isRunning && startTime > 0) {
            stopMission();
        } else {
            String target = etTarget.getText().toString().trim();
            String method = etMethod.getText().toString().trim().toUpperCase();
            int threads = 1000;
            try { threads = Integer.parseInt(etThreads.getText().toString()); } catch (Exception e) {}
            int duration = 3600;
            try { duration = Integer.parseInt(etDuration.getText().toString()); } catch (Exception e) {}

            Intent intent = new Intent(this, MHDDoSBridgeService.class);
            intent.putExtra("target", target);
            intent.putExtra("threads", threads);
            intent.putExtra("duration", duration);
            intent.putExtra("method", method);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            isRunning = true;
            startTime = System.currentTimeMillis();
            btnMHDDoS.setText("⛔ STOP MHDDoS");
            tvStatus.setText("🟢 MHDDoS [" + method + "] ACTIVE");
            tvStatus.setTextColor(0xFF8A2BE2);
            handler.postDelayed(updateStats, 1000);
        }
    }

    private final Runnable updateStats = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            
            // Clock Logic
            long elapsed = System.currentTimeMillis() - startTime;
            int h = (int)(elapsed / 3600000);
            int m = (int)((elapsed % 3600000) / 60000);
            int s = (int)((elapsed % 60000) / 1000);
            tvTimer.setText(String.format("%02d:%02d:%02d", h, m, s));
            // Adaptive stats based on which service is running
            long pCount = AttackService.packetCount.get() + 
                          LocalSaturationService.packetCount.get() + 
                          LocalStressService.packetCount.get() +
                          NetworkStressService.packetCount.get() +
                          MHDDoSBridgeService.packetCount.get();
            long pSpeed = AttackService.packetPerSecond.get() + 
                          LocalSaturationService.packetPerSecond.get() + 
                          LocalStressService.packetPerSecond.get() +
                          NetworkStressService.packetPerSecond.get() +
                          MHDDoSBridgeService.packetPerSecond.get();
                          
            tvStats.setText("📡 Paket: " + pCount);
            int code = AttackService.lastResponseCode != 0 ? AttackService.lastResponseCode : NativeMHDDoS.lastResponseCode;
            long latency = NativeMHDDoS.lastLatency.get();
            tvMethod.setText("⚡ Speed: " + pSpeed + "/s | Resp: " + (code != 0 ? code : "---") + " | Lat: " + (latency == -1 ? "DROP" : latency + "ms"));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMission();
    }
}