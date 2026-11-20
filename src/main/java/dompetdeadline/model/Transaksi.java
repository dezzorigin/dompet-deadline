package dompetdeadline.model;

import java.time.LocalDate;

/**
 * Satu transaksi pemasukan atau pengeluaran.
 * Jumlah disimpan sebagai long dalam rupiah, bukan double,
 * supaya tidak ada galat pembulatan.
 */
public class Transaksi {
    private long id;
    private LocalDate tanggal;
    private long jumlah;
    private Kategori kategori;
    private Tugas tugas; // boleh kosong, artinya tidak terkait tugas apa pun
    private String catatan;

    public Transaksi(long id, LocalDate tanggal, long jumlah, Kategori kategori,
                     Tugas tugas, String catatan) {
        this.id = id;
        this.tanggal = tanggal;
        this.jumlah = jumlah;
        this.kategori = kategori;
        this.tugas = tugas;
        this.catatan = catatan;
    }

    public Transaksi(LocalDate tanggal, long jumlah, Kategori kategori,
                     Tugas tugas, String catatan) {
        this(0, tanggal, jumlah, kategori, tugas, catatan);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public long getJumlah() {
        return jumlah;
    }

    public void setJumlah(long jumlah) {
        this.jumlah = jumlah;
    }

    public Kategori getKategori() {
        return kategori;
    }

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
    }

    public Tugas getTugas() {
        return tugas;
    }

    public void setTugas(Tugas tugas) {
        this.tugas = tugas;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}
