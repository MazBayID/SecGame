# CYBER//TRACE

**CYBER//TRACE** adalah game edukasi Python bergaya **cyberpunk hacking simulator** dengan pendekatan story-driven RPG.

Pemain memulai perjalanan sebagai seseorang yang benar-benar belum pernah belajar coding. Melalui cerita, investigasi, pekerjaan underground, dan simulasi hacking dalam lingkungan virtual, pemain mempelajari Python dari tingkat nol hingga tingkat lanjut.

Game ini berpusat pada terminal interaktif, code editor, eksekusi Python sungguhan, misi berbasis cerita, serta sistem bantuan belajar yang membimbing pemain ketika menghadapi konsep atau kesulitan baru.

## Dokumen Utama

- **[GAMECONCEPT.md](GAMECONCEPT.md)** — konsep game, identitas desain, gameplay loop, sistem hacking simulator, kurikulum Python, sistem bantuan NLAS, misi, progresi, antarmuka, dan arah pengembangan.
- **[STORYLINE.md](STORYLINE.md)** — latar dunia NEXUS CITY, karakter utama, konflik HELIX CORPORATION, perjalanan cerita lima Act, lima belas chapter, misteri NEXUS, dan rancangan ending.
- **[ARCHITECTURE.md](ARCHITECTURE.md)** — rancangan arsitektur Android, fondasi build, kandidat runtime Python terisolasi, risiko keamanan, dan gerbang pengujian teknis.
- **[docs/PYTHON_RUNTIME_POC.md](docs/PYTHON_RUNTIME_POC.md)** — proof of concept runtime Python, harness Web Worker debug, hardening WebView, batas resource, dan matriks uji keamanan.
- **[PROMPT.md](PROMPT.md)** — arahan eksekusi proyek, tanggung jawab, prinsip desain, standar engineering, dan tahapan produksi.

## Identitas Game

- **Genre:** Story-driven Cyberpunk Hacking Simulator · Python Educational RPG
- **Fokus pendidikan:** Python dari pemula absolut hingga advanced
- **Gaya antarmuka:** layar dan teks, terminal futuristik, cyberpunk UI, serta animasi terminal
- **Interaksi utama:** menulis, menjalankan, menguji, dan memperbaiki kode Python
- **Cerita:** perjalanan mengungkap misteri jaringan NEXUS CITY
- **Combat:** tidak ada sistem combat
- **Hacking:** seluruh aktivitas berlangsung dalam lingkungan virtual yang dikendalikan game

## Build Android

Fondasi Android menggunakan Kotlin, Jetpack Compose, Android Gradle Plugin 9.4.0, Gradle 9.6.0, dan JDK 17.

Build debug secara lokal (dengan Android SDK dan Gradle yang sesuai tersedia):

```bash
bash tools/prepare_pyodide_core.sh
gradle --no-daemon assembleDebug
```

GitHub Actions menjalankan unit test otomatis saat kode aplikasi atau konfigurasi build berubah pada `main` maupun pull request. Build dan upload debug APK dijalankan secara manual pada milestone melalui `workflow_dispatch` dengan opsi `build_apk` diaktifkan. Artifact bernama `cyber-trace-debug` tersedia setelah job build berhasil.

### Status verifikasi

<badge color="success">CI berhasil</badge>

- Android project berhasil dikompilasi pada GitHub Actions.
- Dua puluh sembilan unit test telah ditambahkan: tiga untuk kebijakan akses lesson, enam untuk kontrak API eksekusi Python, enam untuk validasi kurikulum serta lesson pertama, enam untuk evaluasi output latihan, dan delapan untuk aturan progres serta mastery lesson. Workflow #43274182700 telah selesai dengan sukses untuk seluruh unit test, pemeriksaan sintaks, dan pemeriksaan invariant keamanan POC.
- Debug APK pernah berhasil dibuat dan diunggah pada baseline build sebelumnya. Workflow milestone yang sekarang juga menyiapkan Pyodide 314.0.7 sebelum APK dibuat; jalur packaging baru ini masih perlu diverifikasi lewat `workflow_dispatch`.
- Workflow build/test baseline yang berhasil: [GitHub Actions run #43261479549](https://github.com/MazBayID/SecGame/actions/runs/43261479549). Workflow baru memisahkan unit test otomatis dari build APK milestone. Run #43276717762 memverifikasi 29 unit test, sintaks JavaScript worker, skrip persiapan, invariant runtime POC termasuk peringatan batas memori, serta kompilasi debug source untuk probe `isolatedProcess` dan pembacaan asset APK; seluruh validasi berhasil. APK tidak dibuat karena run ini bukan dispatch milestone. Smoke test startup menjalankan Python sungguhan untuk aritmetika, JSON, datetime, dan SQLite in-memory; host hanya menerima status ready jika Python 3.14.x dan seluruh capability terkonfirmasi. Probe service terisolasi belum diuji pada perangkat fisik.
- Instalasi, peluncuran, dan pengujian pada perangkat Android fisik **belum diverifikasi**.
- Kontrak API runtime Python tersedia, mencakup batas ukuran source, field resource, workspace virtual, status eksekusi, diagnostik terstruktur, dan pembatalan asinkron. Evaluator latihan dapat memeriksa output dari hasil interpreter sungguhan. Kontrak dan evaluator belum dihubungkan ke interpreter produksi; eksekusi Python sungguhan saat ini hanya ada di harness debug.
- Langkah latihan pertama menyediakan editor draft yang dapat diubah. Tombol Run tetap nonaktif sampai runtime Python terisolasi tersedia; draft ini belum disimpan dan belum dieksekusi. Model progres lesson kini mengatur langkah yang sudah dilihat, jumlah percobaan, status latihan lulus, dan syarat penyelesaian lesson.
- Runtime Python untuk lesson **belum diintegrasikan**. Harness debug kini menguji lifecycle Web Worker dan menyediakan panel eksekusi Python sungguhan melalui Pyodide 314.0.7. Runtime core diunduh saat persiapan build, diverifikasi dengan SHA-256, lalu dimasukkan ke debug APK; file biner tidak disimpan di Git. Activity POC hanya mengizinkan runtime berjalan jika WebView menggunakan renderer multi-process yang terpisah dan tersandbox dari proses aplikasi; jika tidak, runtime diblokir. Panel POC membatasi source 16 KiB, output gabungan 16 KiB, dan waktu eksekusi 3 detik melalui terminasi worker dari host. Python juga diberi namespace JavaScript kosong dan CSP menolak nested worker serta `unsafe-eval`; startup memeriksa jalur escape Function-constructor dan menjalankan smoke test Python sungguhan untuk aritmetika, JSON, datetime, serta SQLite in-memory sebelum panel menerima status runtime siap. Namun build Pyodide 314.0.7 mungkin memerlukan dynamic JavaScript execution sehingga CSP ketat dapat menggagalkan inisialisasi. Jangan melonggarkan CSP tanpa evaluasi keamanan baru. Belum ada batas memori yang dapat dikonfigurasi dan pengujian perangkat fisik belum dilakukan. CPython/Chaquopy dalam service proses terpisah tetap menjadi pembanding; varian `android:isolatedProcess="true"` masih harus dibuktikan karena contoh upstream Chaquopy hanya memakai `android:process` biasa. Belum ada kandidat sandbox produksi yang disetujui. Lesson pertama masih belum terhubung ke runtime tersebut.

Setelah memasang debug APK, tersedia dua entry terpisah di app drawer: **CYBER//TRACE Runtime POC** untuk uji Pyodide/Web Worker dan **CYBER//TRACE Isolated Process POC** untuk memeriksa UID terisolasi dan akses service ke asset yang dikemas di dalam APK. POC kedua hanya menguji batas proses Android dan akses resource APK; belum menjalankan Python/Chaquopy. Detail prosedur dan batas pengujiannya ada di [docs/PYTHON_RUNTIME_POC.md](docs/PYTHON_RUNTIME_POC.md).

Hasil CI yang berhasil bukan bukti bahwa seluruh game telah selesai atau bahwa sandbox Python sudah aman. Status implementasi setiap fitur harus tetap dibedakan dari target desainnya.

## Prinsip Utama

Pemain datang karena tertarik pada dunia cyberpunk dan ceritanya, menjalankan misi untuk mengungkap misteri, dan secara bertahap memperoleh kemampuan Python yang nyata.

**The player enters for the story, continues for the missions, and leaves with real Python skills.**

---

*Dokumen konsep dan storyline merupakan fondasi desain. Detail implementasi dan produksi dikembangkan berdasarkan dokumen arsitektur, pengujian, dan hasil validasi teknis.*
