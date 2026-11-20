package dompetdeadline.model;

/** Prioritas tugas: tinggi, sedang, atau rendah. */
public enum Prioritas {
    TINGGI("Tinggi"),
    SEDANG("Sedang"),
    RENDAH("Rendah");

    private final String label;

    Prioritas(String label) {
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
