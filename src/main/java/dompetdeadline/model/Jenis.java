package dompetdeadline.model;

/** Jenis kategori: pemasukan atau pengeluaran. */
public enum Jenis {
    PEMASUKAN("Pemasukan"),
    PENGELUARAN("Pengeluaran");

    private final String label;

    Jenis(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
