package edu.upi.cs.yudiwbs.rte;

public class TextualEntailment {
	
	public void proses() {
		/*
		prosesDBSimWordnetYW dari awal (data hanya mengandung T dan H)
		sampai dengan siap diproses weka 
		
		//terlebih dulu kosongkan tabel disc:
		
		!INGAT HEIDISQL mengcrop ouput, klik SHOW ALL kalau mau melihat
		
		==========> inisialiasi (mengosongkan) //kosongkan info di tabel rte (disc_h dan disc_t)
		delete from disc_h_rte3_ver1_test_gold;
		delete from disc_t_rte3_ver1_test_gold;		
		ALTER TABLE disc_h_rte3_ver1_test_gold AUTO_INCREMENT=1;
		ALTER TABLE disc_t_rte3_ver1_test_gold AUTO_INCREMENT=1;
		
		
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
		=======
		
		
		
		setelah *selesai* untuk diproses di weka
		
		select 
			id_internal,max_rasio_subj_kata,max_rasio_verb_kata,
			max_rasio_obj_kata,jeniscocok,isentail
		from
		rte3_ver1
		
		
		//versi yang lebih lengkap, setelah diproses TFIDFNYA
		
		select 
			id_internal,max_rasio_subj_kata,max_rasio_verb_kata,
			max_rasio_obj_kata,jeniscocok,bobot_jenis_cocok, similar_tfidf,
			similar_svo_tfidf,similar_avg_svo_tfidf,similar_tfidf_langsung,isentail
		from
		rte3_ver1_test_gold

		
		//hanya tfidf langsung dan LSA
	
	select 
	id_internal,similar_tfidf_langsung,skorLSA,rasioKataHT,isentail
	from
	rte3_ver1_coba4
		
		//urutan EkstrakDiscourse:
		//   pisah kalimat (stanford sentence split) --> SubKalimat (Sbar) 
		//                   --> kalimat sejajar (cc and) --> PP --> Pasif
		// prosesDBSimWordnetYW ekstrak fitur
		// hati2 jangan sampai dipanggil dua kali (setiap pemanggilan menambah rec di tabel disc)
		// prosesDBSimWordnetYW HypoText harus dipanggil setelah selesai tiap tahap
		// nantinya ini jadi satu prosesDBSimWordnetYW
		 * 
		 * 
		 * 
		
			 * 
		 * 
		 * 
		 * 
		 * 
		*/
		
		
		//22 sept: TESTING GAK PAKE KALIMAT PASIFNYA !!
		
		
		String namaTabelUtama = "rte3_ver1_coba2";
		String namaTabelDiscH = "disc_h_rte3_ver1_coba2";
		String namaTabelDiscT = "disc_t_rte3_ver1_coba2";
		
		
		//pisah kalimat dengan sentence extractor stanford (kalau ada titik)
		System.out.println();
		System.out.println("Proses Ekstrak Kalimat");
		EkstrakDiscourseKalimat edk = new EkstrakDiscourseKalimat();
		edk.proses(namaTabelUtama, namaTabelDiscT, namaTabelDiscH);
		
		
		//buat syntax tree 
		System.out.println();
		System.out.println("Proses parsetree");
		ParsingHypoText pht = new ParsingHypoText();
    	pht.prosesDiscourseT(namaTabelDiscT);
    	pht.prosesDiscourseH(namaTabelDiscH);
	
    	
    	//split subkalimat (SBAR
    	System.out.println();
		System.out.println("Proses subkalimat");
		EkstrakDiscourseSubKalimat ED = new EkstrakDiscourseSubKalimat();
		ED.prosesDiscourse(namaTabelDiscT,namaTabelDiscH);
		
		
		//isi lagi syntax tree-nya
		System.out.println();
		System.out.println("Proses parsetree");
		
		
		//ParsingHypoText pht = new ParsingHypoText();
		pht.prosesDiscourseT(namaTabelDiscT);
    	pht.prosesDiscourseH(namaTabelDiscH);
    	
    	//prosesDBSimWordnetYW kalimat sejajar
    	System.out.println();
    	System.out.println("Proses kalimat sejajar");
		EkstrakDiscourseKalimatSejajar eds = new EkstrakDiscourseKalimatSejajar();
		eds.prosesDiscourseSejajar(namaTabelDiscT,namaTabelDiscH);
    	
		//isi lagi syntax tree-nya
		System.out.println();
		System.out.println("Proses parsetree");
		pht.prosesDiscourseT(namaTabelDiscT);
    	pht.prosesDiscourseH(namaTabelDiscH);
    	
    	
        //prosesDBSimWordnetYW PP (kalimat dalam PP dipisah)
    	System.out.println();
    	System.out.println("Proses PP");
        EkstrakDiscoursePP pp = new EkstrakDiscoursePP();
 	   	pp.prosesKalimatdalamPP(namaTabelDiscT,namaTabelDiscH);
 	   	
 	   	//buat syntax tree 
 	   	System.out.println();
 	   	System.out.println("Proses parsetree");
 	 	pht.prosesDiscourseT(namaTabelDiscT);
 	    pht.prosesDiscourseH(namaTabelDiscH);
 	    
 	   
 	    AmbilSubject af = new AmbilSubject();
			
		//kalimat pasif
		//------------
		//DIDISABLE DULU!!
		/*
		 * 
		//cari subj untuk kalimat pasif
		System.out.println();
 	    System.out.println("Proses subject");
    	AmbilSubject af = new AmbilSubject();
		af.prosesDiscT(namaTabelDiscT);
		af.prosesDiscH(namaTabelDiscH);
		
		
		System.out.println();
		System.out.println("Proses pasif");
		EkstrakDiscoursePasif ed= new EkstrakDiscoursePasif();
		ed.prosesDisc(namaTabelDiscT, namaTabelDiscH);
		
		
		//isi lagi parsetree
		System.out.println();
		System.out.println("Proses parsetree");
 	 	pht.prosesDiscourseT(namaTabelDiscT);
 	    pht.prosesDiscourseH(namaTabelDiscH);
		*/
 	    
 	    //ambil VO lengkap
 	    System.out.println();
 	    System.out.println("Proses subject-verb-obj");
 	    af.prosesDiscT(namaTabelDiscT);
		af.prosesDiscH(namaTabelDiscH);
        AmbilVerbObj av = new AmbilVerbObj();
		av.prosesDiscT(namaTabelDiscT);
        av.prosesDiscH(namaTabelDiscH);
        
        //selanjutnya prosesDBSimWordnetYW fitur
        System.out.println();
 	    System.out.println("Proses ekstrak fitur");
		EkstrakFitur ef = new EkstrakFitur();
		ef.prosesDiscourses(namaTabelUtama,namaTabelDiscT,namaTabelDiscH);
		
		System.out.println("selesai semua, selanjutnya lihat class EkstrakFitur untuk tfidf ");
		
		//jalankan query berikut untuk ngisi bobot_jenis_cocok
		/*
		 
		update rte3_ver1_test_gold
		set bobot_jenis_cocok = 3
		where jeniscocok = 'svo';
		
		update rte3_ver1_test_gold
		set bobot_jenis_cocok = 2
		where jeniscocok = '-';
		
		update rte3_ver1_test_gold
		set bobot_jenis_cocok = 1
		where jeniscocok = 'so';
		
		update rte3_ver1_test_gold
		set bobot_jenis_cocok = 0
		where bobot_jenis_cocok is null;
		  
		  
		 * 
		 */
		
		
		//hasilnya save as sebagai delimited, lalu bisa dijadikan arff
	}

	public void debugProses() {

	}
	
	public static void main(String[] args) {
		TextualEntailment ps = new TextualEntailment();
		//ps.prosesDBSimWordnetYW();
		//ps.debugProses();
		System.out.println("----> serius, semua prosesDBSimWordnetYW selesai ...  :-) ");
	}
	
}
