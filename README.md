# CYBER//TRACE

**CYBER//TRACE** adalah game edukasi Python bergaya **cyberpunk hacking simulator** dengan pendekatan story-driven RPG.

Pemain memulai perjalanan sebagai seseorang yang benar-benar belum pernah belajar coding. Melalui cerita, investigasi, pekerjaan underground, dan simulasi hacking dalam lingkungan virtual, pemain mempelajari Python dari tingkat nol hingga tingkat lanjut.

Game ini berpusat pada terminal interaktif, code editor, eksekusi Python sungguhan, misi berbasis cerita, serta sistem bantuan belajar yang membimbing pemain ketika menghadapi konsep atau kesulitan baru.

## Dokumen Utama

- **[GAMECONCEPT.md](GAMECONCEPT.md)** — konsep game, identitas desain, gameplay loop, sistem hacking simulator, kurikulum Python, sistem bantuan NLAS, misi, progresi, antarmuka, dan arah pengembangan.
- **[STORYLINE.md](STORYLINE.md)** — latar dunia NEXUS CITY, karakter utama, konflik HELIX CORPORATION, perjalanan cerita lima Act, lima belas chapter, misteri NEXUS, dan rancangan ending.
- **[ARCHITECTURE.md](ARCHITECTURE.md)** — rancangan arsitektur Android, fondasi build, kandidat runtime Python terisolasi, risiko keamanan, dan gerbang pengujian teknis.
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
gradle --no-daemon assembleDebug
```

GitHub Actions menjalankan unit test otomatis saat kode aplikasi atau konfigurasi build berubah pada `main` maupun pull request. Build dan upload debug APK dijalankan secara manual pada milestone melalui `workflow_dispatch` dengan opsi `build_apk` diaktifkan. Artifact bernama `cyber-trace-debug` tersedia setelah job build berhasil.

### Status verifikasi

<badge color="success">CI berhasil</badge>

- Android project berhasil dikompilasi pada GitHub Actions.
- Dua puluh satu unit test telah ditambahkan: tiga untuk kebijakan akses lesson, enam untuk kontrak API eksekusi Python, enam untuk validasi kurikulum serta lesson pertama, dan enam untuk evaluasi output latihan. Workflow #43264668199 untuk perubahan sebelumnya telah selesai dengan sukses; perubahan editor draft terbaru sedang menunggu CI.
- Debug APK berhasil dibuat dan diunggah sebagai artifact.
- Workflow build/test baseline yang berhasil: [GitHub Actions run #43261479549](https://github.com/MazBayID/SecGame/actions/runs/43261479549). Workflow baru memisahkan unit test otomatis dari build APK milestone.
- Instalasi, peluncuran, dan pengujian pada perangkat Android fisik **belum diverifikasi**.
- Kontrak API runtime Python kini tersedia, mencakup batas ukuran source, batas resource, workspace virtual, status eksekusi, diagnostik terstruktur, dan pembatalan asinkron. Evaluator latihan dapat memeriksa output dari hasil interpreter sungguhan. Ini **belum menjalankan Python** dan bukan sandbox.
- Langkah latihan pertama menyediakan editor draft yang dapat diubah. Tombol Run tetap nonaktif sampai runtime Python terisolasi tersedia; draft ini belum disimpan dan belum dieksekusi.
- Runtime Python **belum diintegrasikan**. Pemilihan runtime menunggu proof of concept yang membuktikan isolasi dan batas sumber daya. Lesson pertama memiliki navigasi langkah demi langkah dan editor draft, tetapi belum menjadi lesson yang dapat dieksekusi.

Hasil CI yang berhasil bukan bukti bahwa seluruh game telah selesai atau bahwa sandbox Python sudah aman. Status implementasi setiap fitur harus tetap dibedakan dari target desainnya.

## Prinsip Utama

Pemain datang karena tertarik pada dunia cyberpunk dan ceritanya, menjalankan misi untuk mengungkap misteri, dan secara bertahap memperoleh kemampuan Python yang nyata.

**The player enters for the story, continues for the missions, and leaves with real Python skills.**

---

*Dokumen konsep dan storyline merupakan fondasi desain. Detail implementasi dan produksi dikembangkan berdasarkan dokumen arsitektur, pengujian, dan hasil validasi teknis.*
