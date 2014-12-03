# README #
Eksperimen terkait textual entailment

### requirement ###
1. Java 1.6+
2. Mysql

### Build ###
1.
Download tambahan lib di: [tbd]
Ekstrak di direktori /lib
Isinya:
stanford-corenlp-3.3.1-models.jar
stanford-parser-3.3.1-models.jar
MySqlConnector .... jar

(lib lain yg lebih kecil sudah ada di repo)

Tambahakan semua lib ini di add, untuk model harus dimasukkan sebagai kelas, bukan anotasi (kalau diinerllji)
PENTING: Tambah lib-nya, jangan sampai ketinggalan, walaupun file jar sudah ada di /lib, tapi kalau belum ditambah, berarti belum bisa digunakan. 

2. Ekstrak db di direktori db/

3. Cek /resources/conf/db.properties, pindahkan ke direktori output classpath (/out atu /dist)
sesuakan username password yg sesuai

3. Build --> jar



### Menjalankan ##
3. Copy jar
4. Copy /resources  di tempat yang sama dengan jar
5. Jalankan ... [tbd]

### Deskripsi DB ###
Terdapat 3 tabel:

utama (RTE)
disc_h
disc_t

tabel disc_t dan h digunakan untuk menampung semua variasi t dan h


Tip untuk mengosongkan tabel utama, query:
update rte3
		set
		max_rasio_subj_kata=null,max_rasio_verb_kata=null,max_rasio_obj_kata=null,
		id_disc_h=null,id_disc_t=null,jeniscocok = null, id_disc_h_tfidf=null,
		id_disc_t_tfidf=null,similar_tfidf=null,t_tfidf=null,h_tfidf=null,
		similar_tfidf_langsung=null,
		id_disc_h_svo_tfidf=null,id_disc_t_svo_tfidf=null,
		similar_svo_tfidf=null,similar_avg_svo_tfidf=null,bobot_jenis_cocok=null,
		id_skor_avg_svo_diskrit = null,
		t_gram_structure=null,
		t_type_dependency=null,
		h_gram_structure=null,
		h_type_dependency=null;

Alternatif proses:

Jalankan TextualEntailment.proses(): yg menjalankan semua dari ujung sampai akhir.
Atau secara bertahap:
- Memisahkan T dan H di tabel utama ke tabel disc_h dan disc_t
-



### Kontak ###

Yudi Wibisono
yudi@upi.edu

=======