# 🔥 SentinelFlow: Hybrid-Core Audit Framework (v5.0)

SentinelFlow adalah platform audit jaringan hibrida yang menggabungkan fleksibilitas **MHDDoS Python** dengan kecepatan **Native Java Apex Engine**. Framework ini dirancang untuk stabilitas maksimal di ekosistem Android.

## 🏗️ Arsitektur Framework

1.  **Python Master Framework (`start.py`)**:
    *   Berperan sebagai "Otak" utama serangan.
    *   Menggunakan basis kode MHDDoS asli dengan 57+ metode audit.
    *   Dijalankan jika lingkungan Python (Termux/Local) tersedia di perangkat.

2.  **Java System Logic (Apex Engine)**:
    *   Mengelola antarmuka pengguna (UI) dan telemetri real-time.
    *   Berperan sebagai mesin performa tinggi (Native Fallback).
    *   Menangani logika bypass WAF (Cloudflare/DGB) secara langsung di layer sistem Android untuk menghindari overhead interpreter.

## 🚀 Fitur Unggulan v5.0

*   **Dual-Core Execution**: Sistem otomatis mendeteksi ketersediaan Python. Jika tidak ditemukan, Java Apex Engine v10.5 akan mengambil alih fungsi audit dengan logika yang sama persis.
*   **WAF Intelligent Solver**: Penanganan otomatis kode HTTP 510 dan 515. Framework akan melakukan handshake ulang secara dinamis untuk menembus proteksi DDoS-Guard dan Cloudflare.
*   **Zero-Crash Persistence**: Logika threading yang dioptimalkan (Capped 1500-2000 thread) untuk memastikan audit berjalan hingga 10 jam tanpa dihentikan oleh kernel Android.
*   **Full Parity Mapping**: Seluruh metode Python (`SYN`, `UDP`, `BYPASS`, `CFB`, `DGB`, `XMLRPC`, dll) telah dipetakan ke dalam logika Java murni.

## 📊 Dashboard Telemetri

*   **Mission Clock**: Menghitung durasi audit secara presisi.
*   **Packet Counter**: Total paket yang berhasil di-injeksi ke jaringan.
*   **Latency & DROP Tracker**: Jika target memutus koneksi, dashboard akan menampilkan status **DROP** secara real-time.

## 🛠️ Cara Kerja Sistem

1.  **Input**: User memasukkan Target, Metode, dan Thread.
2.  **Bridge**: `MHDDoSBridgeService` mencoba memanggil Python interpreter.
3.  **Handoff**: Jika gagal, `NativeMHDDoS.java` dieksekusi dengan prioritas thread maksimal (`MAX_PRIORITY`).
4.  **Audit**: Paket dikirim dengan header sidik jari browser modern (`sec-ch-ua`) untuk meminimalisir deteksi.

---
**[💀] MZKYZAK — HYBRID AUDIT VERIFIED [💀]**
