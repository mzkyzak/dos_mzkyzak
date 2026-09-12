# 🔥 DOS ATTACK BY:MZKYZAK v5.0 — Framework

**MZKYZAK Cyber Engine** adalah Serangan cyber attack/ujicoba testing dengan ekosistem audit keamanan dan stress testing terlengkap untuk Android. dan untuk serangan siber yang membuat situs web atau server menjadi down atau tidak bisa diakses oleh pengguna, dan untuk fitur = Menyatukan kekuatan framework **MHDDoS Python** dengan efisiensi **Java Native Apex Engine** untuk system utama agar android bisa berfungsi dengan dos.

---

## 🏗️ ARSITEKTUR HYBRID-CORE (DUAL-PATH)
Sistem ini menggunakan logika yang memastikan audit tidak pernah terhenti:
- **Master Path (Python)**: Mengeksekusi `start.py` asli jika interpreter tersedia (Termux/Rooted). Mendukung proxy chaining penuh dan pembuatan paket mentah.
- **Apex Path (Java Native)**: Fallback otomatis jika Python tidak terdeteksi gw sebagai Ditulis dalam Java murni untuk kecepatan eksekusi maksimal dengan latensi rendah di layer sistem Android.

---

## 🧬 MODUL UTAMA FRAMEWORK
Semua modul berjalan secara sinkron untuk memastikan target audit mencapai batas maksimalnya:

### 1. Attack Engine (AttackService)
- **Vektor**: Layer 7 (HTTP/S) & Layer 4 (UDP/TCP/SYN).
- **Fitur**: Injeksi header dinamis, rotasi payload, dan manajemen backpressure.

### 2. Local Saturation (LocalSaturationService)
- **Fungsi**: Verifikasi uplink infrastruktur melalui pengujian NAT gateway.
- **Durasi**: Dikonfigurasi untuk misi 10 jam berkelanjutan.

### 3. Hardware Stressor (LocalStressService)
- **Fungsi**: Audit termal hardware melalui loopback 127.0.0.1.
- **Logika**: Memaksa CPU dan RAM ke titik didih untuk menguji ketahanan fisik perangkat/server target.

### 4. Persistent Mission (NetworkStressService)
- **Fungsi**: Menjaga koneksi tetap aktif dengan metode START_STICKY.
- **WAKE_LOCK**: Memastikan radio Wi-Fi/Data tidak pernah tidur.

---

## 🔥 DAFTAR METODE LENGKAP (57+ METHODS)

| Kategori | Metode (Vektor) | Deskripsi Audit |
| :--- | :--- | :--- |
| **Layer 7 (Bypass)** | `CFB`, `CFBUAM`, `DGB`, `AVB`, `GSB` | Bypass Cloudflare, DDoS-Guard, ArvanCloud, & Google Shield. |
| **Layer 7 (Flood)** | `GET`, `POST`, `STRESS`, `BOMB`, `PPS` | Saturasi request HTTP dengan payload tinggi (BPS) atau frekuensi tinggi (PPS). |
| **Layer 7 (Special)** | `XMLRPC`, `APACHE`, `STOMP`, `RHEX` | Eksploitasi pingback, range-header, dan random HEX challenge. |
| **Layer 7 (Slow)** | `SLOW`, `DOWNLOADER` | Slowloris audit untuk menghabiskan slot koneksi server secara perlahan. |
| **Layer 4 (Saturation)** | `UDP`, `OVH-UDP`, `FIVEM`, `VSE` | Saturasi bandwidth brutal pada game server dan infrastruktur OVH. |
| **Layer 4 (Table)** | `TCP`, `SYN`, `CPS`, `CONNECTION` | Menguras tabel koneksi firewall dan load balancer. |
| **Layer 4 (Amp)** | `DNS`, `NTP`, `MEM`, `ARD`, `RDP` | Simulasi serangan amplifikasi menggunakan reflektor publik. |

---

## 🛡️ FITUR KEAMANAN & PERSISTENSI (STEALTH MODE)
Didesain agar "Aplikasi Tidak Bisa Mati":
- **Foreground Priority**: Dihindari secara paksa dari OOM (Out of Memory) Killer Android.
- **Radio Lock (WAKE_LOCK)**: Sinyal data tetap dipaksa aktif 100% meskipun HP dalam saku atau layar mati.
- **Auto-Restart Logic**: Jika Android mencoba menutup aplikasi, layanan akan hidup kembali secara instan.
- **Header Entropy Pool**: Menggunakan 1000+ User-Agent dan spoofing IP (`X-Forwarded-For`) untuk memvalidasi kebijakan origin filter.

---

## 📊 REAL-TIME TELEMETRY DASHBOARD
Monitoring performa audit yang klinis dan akurat:
- **📡 Paket**: Hitungan akumulatif paket yang berhasil dikirim ke kabel jaringan.
- **⚡ Speed**: Kecepatan PPS (Packets Per Second) yang sedang dikirim.
- **🕒 Mission Timer**: Jam digital yang melacak durasi aktif audit secara real-time.
- **🌡️ Status WAF**: Monitoring real-time kode status 510/515 dengan **DROP Detection**.

---

## 🛠️ CARA MENJALANKAN MISI
1. Isi **Target URL/IP** pada kolom yang tersedia.
2. Pilih **Metode Audit** (Gunakan `BYPASS` atau `DGB` untuk website dengan perlindungan).
3. Atur **Thread** (Disarankan 1000-1500 untuk stabilitas terbaik).
4. Klik **🌀 MISI MHDDoS** untuk memulai saturasi hibrida.

---
**[💀] MZKYZAK — ATTACK VERIFIED [💀]**
> *PERINGATAN KHUSUS: INI Gunakan hanya untuk edukasi cyber security hanya (support testing keamanan terhadap serangan stres network dan flood brutal. khusus test) dan untuk cyber attack membuat sebuah situs web, server, atau layanan online menjadi  pembanjiran sehingga web menjadi down sehingga tidak bisa diakses oleh pengguna yang sah.) Segala bentuk penyalahgunaan adalah tanggung jawab penuh pengguna.*
> *JIka lo memakai aplikasi ini secara sembarangan, Segala bentuk penyalahgunaan adalah tanggung jawab penuh pengguna.*
