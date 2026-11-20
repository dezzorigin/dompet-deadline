package dompetdeadline.model;

/** Satu kategori transaksi, misalnya "Makan" atau "Uang Saku". */
public class Kategori {
    private long id;
    private String nama;
    private Jenis jenis;

    public Kategori(long id, String nama, Jenis jenis) {
        this.id = id;
        this.nama = nama;
        this.jenis = jenis;
    }

    public Kategori(String nama, Jenis jenis) {
        this(0, nama, jenis);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Jenis getJenis() {
        return jenis;
    }

    public void setJenis(Jenis jenis) {
        this.jenis = jenis;
    }

    @Override
    public String toString() {
        return nama + " (" + jenis.getLabel() + ")";
    }
}
