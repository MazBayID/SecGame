# AEGIS-Net — STORY LINE

## Naskah Cerita dan Garis Besar Naratif

Dokumen ini memuat peristiwa, tokoh, konflik, misteri, dan perkembangan kisah AEGIS-Net. Dokumen ini adalah sumber naratif, bukan spesifikasi gameplay atau panduan implementasi.

**Aturan kontinuitas:** `GAME_CONCEPT.md` adalah sumber kebenaran untuk identitas produk, aturan game, dan keputusan teknis. Jika naskah ini bertentangan dengannya, naskah ini yang harus diperbaiki. Aturan baru yang disetujui menggantikan aturan lama yang bertentangan; jangan mempertahankan dua versi yang saling meniadakan.

---

## 1. Identitas Cerita

- **Judul:** AEGIS-Net
- **Genre:** thriller intelijen sinematik dengan drama karakter.
- **Latar:** dunia fiksi yang terinspirasi Asia Tenggara; markas A.E.G.I.S. berada di Manila, Filipina.
- **Sudut pandang:** orang pertama. Pemain mengalami peristiwa dari sudut pandangnya sendiri.
- **Nuansa:** misterius, membumi, tegang, penuh rasa ingin tahu, dan sesekali hangat melalui hubungan antarkarakter.

AEGIS-Net bercerita tentang seseorang biasa yang perlahan memasuki dunia intelijen. Ia tidak menjadi penting karena kekuatan fisik atau kemampuan luar biasa sejak awal. Ia berkembang karena belajar memperhatikan hal-hal kecil, memahami teknologi, mempertanyakan informasi, dan berani mengakui ketika sesuatu belum diketahui.

Informasi dapat menjadi petunjuk, perlindungan, senjata, atau jebakan. Sebuah catatan mungkin benar, tetapi belum tentu lengkap. Dua laporan dapat sama-sama terlihat masuk akal, tetapi saling bertentangan. Kebenaran tidak selalu hadir sebagai jawaban yang langsung terlihat.

## 2. Awal Segalanya

Pada suatu hari yang tampaknya biasa, pemain membuka Campaign. Layar menjadi hitam. Setelah jeda, sebuah notifikasi **Pesan Aman — A.E.G.I.S. / Recruitment Office** muncul.

Tidak ada nama misterius atau ancaman. Hanya pesan resmi dari organisasi yang belum pernah dikenal pemain.

Pesan itu menyampaikan:

> “Seseorang telah memberikan perintah kepada A.E.G.I.S. untuk merekrutmu.”

Pesan tersebut tidak menyebut siapa orang itu, mengapa pemain dipilih, atau apa yang membuatnya layak direkrut. A.E.G.I.S. memperkenalkan dirinya secara singkat dan mengundang pemain membuka tautan **BUKA RANGKAIAN MISI**.

Pemain harus mengetuk tautan tersebut. Identitas orang yang memerintahkan perekrutan tetap dirahasiakan.

## 3. First Contact — Pertemuan Pertama

### 3.1 Pesan dari Freya

Ketika misi pertama aktif, notifikasi kedua muncul di atas layar misi. Pengirimnya **Freya Knox**. Notifikasi ini terpisah dari pesan Recruitment Office dan harus dibuka oleh pemain; percakapan tidak terbuka otomatis.

Freya memperkenalkan diri sebagai mentor yang akan menemani pemain selama penugasan. Ia tahu pemain belum terbiasa dengan dunia A.E.G.I.S., tetapi tidak memperlakukan ketidaktahuan itu sebagai kelemahan.

Freya tidak mengungkap siapa yang memerintahkan perekrutan dan tidak mengaku memiliki hubungan dengan keputusan tersebut. Ia bersikap profesional pada awal pertemuan. Sisi santai, humor, dan sifat jahilnya muncul perlahan seiring hubungan mereka berkembang.

### 3.2 NEXUS

Freya memperkenalkan sistem internal bernama **NEXUS**, sistem AI komunikasi-intelijen yang menyediakan peringatan, informasi sistem, referensi, dan dukungan teknis sekunder.

Freya memberikan tautan **NEXUS / KANAL SISTEM**. Kontak NEXUS baru muncul di inbox setelah pemain mengetuk tautan itu.

NEXUS berbicara secara terukur, informatif, dan terkadang terlalu literal. NEXUS bukan manusia, bukan pengganti Freya, dan tidak menyelesaikan misi untuk pemain. Freya tetap menjadi mentor utama yang mengenal perjalanan dan perkembangan pemain.

### 3.3 Belajar Sebelum Bertugas

Pemain mulai mengenal lingkungan kerja A.E.G.I.S.: terminal, direktori, berkas, program, dan istilah teknis. Freya mengajar dari nol, menjelaskan tujuan dan makna setiap konsep sebelum meminta pemain menggunakannya.

Pemain mempelajari terminal, lokasi kerja, folder, path, cara menemukan dan membaca berkas, lalu dasar Python. Program diperkenalkan sebagai rangkaian instruksi yang dijalankan komputer. Pemain mulai dengan nilai, teks, input, output, dan memahami respons sistem.

Kesalahan seperti tanda kutip yang tertinggal atau nama variabel yang keliru adalah bagian wajar dari proses belajar. Freya mengajak pemain menelusuri penyebabnya tanpa mempermalukan pemain. NEXUS dapat membantu menunjukkan diagnostik dan informasi teknis, tetapi tidak mengambil alih proses belajar.

### 3.4 Pintu Menuju A.E.G.I.S.

Freya mengarahkan pemain ke intranet internal A.E.G.I.S. untuk mengakses informasi organisasi dan penugasan. Naskah awal menggunakan ID personel fiktif `RECRUIT-014` dan kode akses `FIRSTCONTACT`; penggunaan kredensial ini dalam build harus mengikuti konten dan urutan unlock yang ditetapkan oleh game.

Pemain melihat catatan operasi, arsip, laporan, dan informasi dengan tingkat kewenangan berbeda. Pada tahap ini aksesnya masih terbatas. Ia sedang dinilai dan belum mengetahui alasan sebenarnya dirinya dipilih.

## 4. Campaign 01 — The Recruitment
**Chapter 001 — First Contact**

Tema kampanye ini adalah **kepercayaan harus diperoleh**. Perjalanan pemain dimulai sebagai orang biasa yang memasuki A.E.G.I.S. dan berkembang menjadi Recruit.

Chapter ini terdiri dari 14 story beats dan delapan misi teknis yang membentuk satu investigasi berkelanjutan. Pembelajaran terminal dan Python harus memiliki tujuan yang masuk akal di dalam cerita; bukan latihan terpisah yang ditempelkan pada narasi.

### 4.1 Delapan Misi

1. **Sinyal Pertama (First Signal)** — Pemain menerima fragmen pesan dan mempelajari variabel serta `print()`.
2. **Verifikasi Identitas (Identity Check)** — Pemain menyimpan dan menampilkan informasi menggunakan variabel bernama, lalu memahami keluaran program.
3. **Audit Jejak Sistem (System Trace Audit)** — Pemain membaca log, menggunakan `for` dan `if`, serta menghitung entri `ERROR`.
4. **Inventaris Rekaman (Record Inventory)** — Pemain memeriksa jumlah rekaman menggunakan `len()`.
5. **Penelusuran Referensi (Reference Trace)** — Pemain menggunakan `enumerate()`, penyaringan, dan indeks sumber untuk menemukan referensi tertentu.
6. **Perbandingan Dua Sumber (Two-Source Comparison)** — Pemain menggunakan indexing list untuk membandingkan dua catatan.
7. **Normalisasi Waktu (Time Normalization)** — Pemain menggunakan `int()`, indexing, dan menghitung selisih bertanda.
8. **Validasi Selisih (Delta Validation)** — Pemain menggunakan `abs()` untuk memperoleh selisih absolut dan memvalidasi hasil.

Misi pertama dan kedua membangun kemampuan dasar dan menjadi bagian dari milestone verifikasi perekrutan. Rank **Recruit** diberikan pada milestone perekrutan yang lebih awal tersebut. Misi berikutnya memperluas investigasi di dalam chapter dan tidak memindahkan atau mengulang peristiwa pemberian rank.

### 4.2 Fragmen Pesan

Sebuah fragmen bertuliskan **ACCESS GRANTED** tersedia dalam berkas `message_fragment.txt`. Freya mengirimkannya sebagai lampiran; salinan yang sama juga tersedia di File Repository intranet. Kedua rujukan menunjuk pada satu konten bukti yang sama, bukan dua file bukti yang berbeda.

Pemain menyimpan atau mengakses berkas melalui ruang kerja misi, membacanya melalui Terminal, lalu mengolah isinya dengan program sederhana. Freya menjelaskan tujuan tugas dan memberi petunjuk bertahap, tetapi pemain tetap harus mencoba sendiri.

### 4.3 Jejak yang Tidak Cocok

Dalam peninjauan rutin terhadap transmisi pelatihan, ditemukan dua catatan penerimaan untuk transmisi yang sama. Isi dan referensinya cocok, tetapi waktu pencatatannya berbeda beberapa detik.

Perbedaan itu belum membuktikan manipulasi. Kemungkinan awal meliputi keterlambatan antrean, perbedaan mekanisme pencatatan, atau jam sistem yang tidak sepenuhnya sinkron.

Pemain diminta memeriksa log peristiwa yang memuat informasi, peringatan, dan sejumlah kesalahan. Ia juga menelusuri daftar referensi rekaman dan membandingkan sumber A.E.G.I.S. dengan arsip pembanding. Bukti asli tidak boleh diubah.

### 4.4 Referensi RX-14

Satu referensi mulai menarik perhatian: **RX-14**. Referensi ini muncul dalam daftar penerimaan dari dua sumber berbeda.

Setelah menelusuri daftar dan membandingkan catatan, pemain menemukan:
- A.E.G.I.S. mencatat penerimaan RX-14 pada pukul **09:41:12**.
- ARCHIVE mencatat referensi yang sama pada pukul **09:41:16**.
- Selisih waktu yang teramati adalah **empat detik**.

Angka tersebut dapat dihitung dan diverifikasi. Penyebabnya belum diketahui. Keterlambatan antrean, sinkronisasi waktu, perbedaan mekanisme pencatatan, atau faktor lain tetap berupa kemungkinan, bukan fakta yang telah dibuktikan.

Dalam laporan, pemain harus memisahkan pengamatan dari dugaan. Tidak boleh ada tuduhan manipulasi atau penetapan pelaku tanpa bukti yang cukup.

### 4.5 Laporan Awal

A.E.G.I.S. menerima laporan yang mencatat selisih empat detik sebagai fakta teramati, sementara penyebabnya tetap belum terverifikasi.

Freya mengakui perkembangan pemain: dari seseorang yang belum mengenal terminal menjadi seseorang yang mampu memeriksa berkas, memahami data, dan menyampaikan kesimpulan secara bertanggung jawab.

Chapter berakhir dengan pertanyaan yang belum terjawab. Belum ada tersangka, belum ada bukti konspirasi, dan belum ada dasar untuk menyatakan bahwa seseorang telah mengubah data. RX-14 tetap menjadi petunjuk kecil yang mungkin terhubung dengan kasus lain di masa depan.

## 5. Freya Knox — Mentor dan Rahasia

Freya adalah tokoh manusia yang paling dekat dengan perjalanan pemain. Ia menyaksikan kebingungan, kesalahan, pemahaman baru, dan perkembangan kepercayaan diri pemain.

Ia kompeten dan profesional saat keadaan menuntut, tetapi juga percaya diri, ekspresif, usil, dan humoris. Candaan tidak boleh mempermalukan pemain atau meremehkan korban dan bahaya nyata dalam cerita.

Freya menyimpan alasan pribadi mengapa ia memilih pemain. **Dialah orang yang pertama kali memerintahkan A.E.G.I.S. untuk merekrut pemain.** Rahasia ini tidak boleh dibocorkan melalui dialog awal, petunjuk yang terlalu jelas, atau pengakuan Freya sebelum momen pengungkapan yang sengaja ditulis.

Pengungkapan tersebut harus memiliki bobot emosional dan makna bagi hubungan mereka.

## 6. Misteri Besar — Pola di Balik Kasus

Kejanggalan RX-14 bukan bukti konspirasi. Kasus-kasus berikutnya dapat memperlihatkan pola yang mengingatkan pemain pada perbedaan kecil tersebut: laporan keuangan yang tidak cocok dengan dokumen sumber, jalur komunikasi yang tampak tidak aktif tetapi meninggalkan jejak, atau catatan dengan hubungan waktu yang aneh.

Awalnya, kasus-kasus itu tampak berdiri sendiri. Seiring perjalanan, pemain mungkin menemukan kemiripan pada jejak teknis dan cara informasi disajikan. Ada kemungkinan seseorang memanfaatkan celah di antara sistem, organisasi, dan asumsi manusia untuk memengaruhi informasi yang dipercaya.

Namun pola bukan bukti akhir. Setiap hubungan harus ditemukan dan diuji. Tidak ada basis data ajaib, sistem mahatahu, atau tokoh yang langsung menjelaskan seluruh misteri.

Pihak di balik pola, motif, jumlah orang yang terlibat, dan tujuan akhirnya tetap belum diketahui sampai cerita terkait dirancang dan dibuktikan.

## 7. Perjalanan Kampanye

### Campaign 01 — THE RECRUITMENT

- **Tema:** Kepercayaan harus diperoleh.
- **Perjalanan:** orang biasa menjadi Recruit.
- **Pertanyaan:** Mengapa A.E.G.I.S. memilih pemain, dan apa arti kejanggalan pertama yang ditemukan?

Pemain mengenal Freya dan NEXUS, mempelajari lingkungan kerja, menjalani verifikasi perekrutan, lalu menelusuri ketidaksesuaian kecil dalam catatan transmisi. Chapter berakhir dengan laporan RX-14 yang berhati-hati dan rahasia Freya yang masih tersimpan.

### Campaign 02 — THE SILENT NETWORK

- **Tema:** Hubungan tidak selalu terlihat.
- **Perjalanan:** Recruit mulai dipercaya menangani analisis yang lebih luas.
- **Pertanyaan:** Apakah anomali yang ditemukan selama ini benar-benar berdiri sendiri?

Pemain bekerja dengan anggota dari berbagai divisi A.E.G.I.S. Catatan, garis waktu, dan jejak infrastruktur fiktif memperlihatkan hubungan yang sebelumnya tidak tampak. Sebagian anggota menganggapnya kebetulan; sebagian lain khawatir ada sesuatu yang disembunyikan.

Pemain harus mempertanyakan informasi dari luar sekaligus asumsi yang tumbuh di dalam organisasi.

### Campaign 03 — THE GLASS ARCHIVE

- **Tema:** Informasi dapat melindungi manusia, tetapi juga dapat mengendalikan mereka.
- **Perjalanan:** operative berpengalaman dengan tanggung jawab lebih besar.
- **Pertanyaan:** Siapa yang mengendalikan catatan tentang apa yang terjadi, dan siapa yang diuntungkan ketika catatan itu dipercaya?

Pemain menemukan bukti yang mengubah pemahamannya terhadap beberapa peristiwa sebelumnya. Kesaksian yang dipercaya ternyata tidak lengkap, dan detail yang dahulu dianggap tidak penting mulai memiliki arti baru.

Pemain menghadapi pertanyaan tentang tanggung jawab, keterbukaan, dan batas kewenangan. Tidak semua informasi dapat disebarkan begitu saja, tetapi menyembunyikan kebenaran juga memiliki harga.

### Ruang untuk Kampanye Berikutnya

Kisah berikutnya dapat mengeksplorasi ketegangan internal A.E.G.I.S., operasi lintas negara, harga kerahasiaan institusi, serta perubahan tanggung jawab pemain. Arah tersebut belum menjadi canon sampai dirancang dan disetujui.

## 8. Tokoh dan Hubungan

### Freya Knox

Mentor manusia utama. Freya menghubungkan kehidupan lama pemain dengan dunia A.E.G.I.S. Ia mengajar dari nol, tegas ketika dibutuhkan, dan menyimpan rahasia tentang perintah perekrutan.

### NEXUS

Sistem AI komunikasi-intelijen A.E.G.I.S. NEXUS memberikan informasi, peringatan, referensi, dan dukungan teknis sekunder. Ia bukan manusia dan tidak menggantikan kedekatan maupun penilaian manusia yang diberikan Freya.

### Para Spesialis A.E.G.I.S.

Pemain kelak dapat bertemu anggota dari divisi Intelligence, Cyber Ops, Investigation, Technical, Field Ops, dan Command. Mereka membawa keahlian, sudut pandang, serta keterbatasan masing-masing. Nama dan latar belakang spesialis lain ditentukan saat kisah mereka dikembangkan.

## 9. Prinsip Naratif dan Kontinuitas

- Setiap informasi penting memiliki asal-usul yang masuk akal di dunia cerita.
- Tokoh hanya mengetahui sesuatu jika mereka telah menerima atau menemukan informasi tersebut.
- Fakta, dugaan, informasi yang diperdebatkan, dan hal yang belum terverifikasi harus tetap dibedakan.
- Pengungkapan baru boleh mengubah pemahaman terhadap peristiwa lama, tetapi tidak boleh membatalkan fakta yang sudah ditetapkan tanpa alasan cerita yang kuat.
- Misteri berkembang melalui banyak petunjuk dan sudut pandang, bukan penjelasan instan.
- Kesalahan dan ketidaktahuan pemain pada awal perjalanan adalah bagian wajar dari pertumbuhan.
- Pada versi awal, pilihan dialog pemain menggambarkan nada atau respons sesaat; pilihan tersebut tidak mengubah hubungan, akses, atau cabang utama cerita.
- Rahasia bahwa Freya memulai perintah perekrutan tetap tersembunyi sampai momen pengungkapan yang sengaja ditulis.
- Kasus, organisasi, perusahaan, individu, dan tuduhan utama dalam cerita adalah fiktif.
- Urutan peristiwa, milestone rank, jumlah misi, dan mekanik harus konsisten dengan `GAME_CONCEPT.md`.

## 10. Nada dan Arah Penceritaan

Cerita dimulai dari ruang yang kecil dan personal, lalu perlahan melebar menuju persoalan yang lebih besar. Hubungan pemain dengan Freya menjadi jangkar emosional ketika misteri intelijen semakin rumit.

Ketegangan tidak harus selalu hadir melalui ancaman besar. Pesan yang terlambat, catatan yang tidak cocok, jawaban yang terlalu cepat, atau keheningan seseorang dapat menjadi sumber rasa ingin tahu.

Karakter harus terasa seperti manusia dengan pandangan dan keterbatasan masing-masing. A.E.G.I.S. tidak selalu benar, dan pihak yang berseberangan dengannya tidak harus digambarkan sebagai kejahatan tanpa alasan.

Pemain tetap menjadi pusat cerita: ia mengamati, bertanya, mempelajari hal baru, menemukan hubungan, dan perlahan menentukan seperti apa seorang operative yang dapat dipercaya.

---

## Inti Cerita

**AEGIS-Net adalah kisah seseorang biasa yang memasuki dunia intelijen, belajar memahami teknologi dan manusia, lalu menemukan bahwa kebenaran tidak selalu sama dengan apa yang tercatat.**
