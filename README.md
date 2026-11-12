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

GitHub Actions menjalankan unit test dan membuat debug APK pada push ke `main` maupun pull request. Jika workflow berhasil, APK tersedia sebagai artifact bernama `cyber-trace-debug`.

### Status verifikasi

<badge color="success">CI berhasil</badge>

- Android project berhasil dikompilasi pada GitHub Actions.
- Tiga unit test kebijakan pembukaan lesson telah berhasil dijalankan pada baseline CI sebelumnya. Enam test tambahan untuk kontrak API eksekusi Python kini telah ditambahkan; hasil CI yang menjalankan seluruh sembilan test menunggu workflow commit terbaru.
- Debug APK berhasil dibuat dan diunggah sebagai artifact.
- Workflow terbaru yang memverifikasi commit dokumentasi dan test: [GitHub Actions run #43259419839](https://github.com/MazBayID/SecGame/actions/runs/43259419839).
- Instalasi, peluncuran, dan pengujian pada perangkat Android fisik **belum diverifikasi**.
- Kontrak API runtime Python kini tersedia, mencakup batas ukuran source, batas resource, workspace virtual, status eksekusi, diagnostik terstruktur, dan pembatalan asinkron. Ini **belum menjalankan Python** dan bukan sandbox.
- Runtime Python **belum diintegrasikan**. Pemilihan runtime menunggu proof of concept yang membuktikan isolasi dan batas sumber daya.

Hasil CI yang berhasil bukan bukti bahwa seluruh game telah selesai atau bahwa sandbox Python sudah aman. Status implementasi setiap fitur harus tetap dibedakan dari target desainnya.

## Prinsip Utama

Pemain datang karena tertarik pada dunia cyberpunk dan ceritanya, menjalankan misi untuk mengungkap misteri, dan secara bertahap memperoleh kemampuan Python yang nyata.

**The player enters for the story, continues for the missions, and leaves with real Python skills.**

---

*Dokumen konsep dan storyline merupakan fondasi desain. Detail implementasi dan produksi dikembangkan berdasarkan dokumen arsitektur, pengujian, dan hasil validasi teknis.*
