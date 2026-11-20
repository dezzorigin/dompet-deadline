package dompetdeadline.service;

import java.text.NumberFormat;
import java.util.Locale;

/** Format rupiah dan membaca kembali angka yang diketik pengguna. */
public class FormatUang {

    private static final NumberFormat FORMAT =
            NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    private FormatUang() {
    }

    /** Contoh: 1500000 menjadi "Rp1.500.000". */
    public static String format(long jumlah) {
        return FORMAT.format(jumlah).replace(",00", "");
    }

    /**
     * Membaca angka dari isian pengguna. Titik dan koma sebagai pemisah ribuan
     * diabaikan, jadi "1.500.000" dan "1500000" sama-sama terbaca 1500000.
     */
    public static long baca(String teks) {
        if (teks == null) {
            throw new IllegalArgumentException("Jumlah tidak boleh kosong.");
        }
        String bersih = teks.trim().replace(".", "").replace(",", "").replace("Rp", "").trim();
        if (bersih.isEmpty()) {
            throw new IllegalArgumentException("Jumlah tidak boleh kosong.");
        }
        try {
            long nilai = Long.parseLong(bersih);
            if (nilai <= 0) {
                throw new IllegalArgumentException("Jumlah harus lebih dari 0.");
            }
            return nilai;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Jumlah harus berupa angka, contoh: 50000 atau 1.500.000.");
        }
    }
}
