package dompetdeadline.model;

import java.time.LocalDate;

/** Satu tugas kuliah: judul, mata kuliah, tenggat, prioritas, dan status selesai. */
public class Tugas {
    private long id;
    private String judul;
    private String mataKuliah;
    private LocalDate tenggat;
    private Prioritas prioritas;
    private boolean selesai;

    public Tugas(long id, String judul, String mataKuliah, LocalDate tenggat,
                 Prioritas prioritas, boolean selesai) {
        this.id = id;
        this.judul = judul;
        this.mataKuliah = mataKuliah;
        this.tenggat = tenggat;
        this.prioritas = prioritas;
        this.selesai = selesai;
    }

    public Tugas(String judul, String mataKuliah, LocalDate tenggat, Prioritas prioritas) {
        this(0, judul, mataKuliah, tenggat, prioritas, false);
    }

    public Tugas(long id, String judul, String mataKuliah, LocalDate tenggat,
                 Prioritas prioritas) {
        this(id, judul, mataKuliah, tenggat, prioritas, false);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getMataKuliah() {
        return mataKuliah;
    }

    public void setMataKuliah(String mataKuliah) {
        this.mataKuliah = mataKuliah;
    }

    public LocalDate getTenggat() {
        return tenggat;
    }

    public void setTenggat(LocalDate tenggat) {
        this.tenggat = tenggat;
    }

    public Prioritas getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(Prioritas prioritas) {
        this.prioritas = prioritas;
    }

    public boolean isSelesai() {
        return selesai;
    }

    public void setSelesai(boolean selesai) {
        this.selesai = selesai;
    }

    @Override
    public String toString() {
        return judul + " (" + mataKuliah + ")";
    }
}
