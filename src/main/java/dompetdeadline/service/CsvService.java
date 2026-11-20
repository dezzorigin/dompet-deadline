package dompetdeadline.service;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Kategori;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.repository.KategoriRepository;
import dompetdeadline.repository.TugasRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Ekspor dan impor transaksi dalam format CSV. */
public class CsvService {

    private static final String[] KOLOM = {
            "tanggal", "kategori", "jenis", "jumlah", "tugas", "catatan"
    };

    /** Tulis semua transaksi ke berkas CSV. */
    public void ekspor(List<Transaksi> semua, File tujuan) throws IOException {
        try (BufferedWriter tulis = new BufferedWriter(new FileWriter(tujuan))) {
            tulis.write(String.join(",", KOLOM));
            tulis.newLine();
            for (Transaksi t : semua) {
                String[] baris = {
                        t.getTanggal().toString(),
                        t.getKategori().getNama(),
                        t.getKategori().getJenis().name(),
                        String.valueOf(t.getJumlah()),
                        t.getTugas() == null ? "" : t.getTugas().getJudul(),
                        t.getCatatan() == null ? "" : t.getCatatan()
                };
                tulis.write(gabungBaris(baris));
                tulis.newLine();
            }
        }
    }

    /**
     * Baca berkas CSV menjadi daftar transaksi. Kategori yang belum ada dibuat
     * baru, tugas dicocokkan berdasarkan judul, kalau tidak ketemu dibiarkan kosong.
     */
    public List<Transaksi> impor(File sumber, KategoriRepository kategoriRepo,
                                 TugasRepository tugasRepo) throws IOException, SQLException {
        List<Transaksi> hasil = new ArrayList<>();
        try (BufferedReader baca = new BufferedReader(new FileReader(sumber))) {
            String baris = baca.readLine(); // lewati baris judul
            int nomorBaris = 1;
            while ((baris = baca.readLine()) != null) {
                nomorBaris++;
                if (baris.isBlank()) {
                    continue;
                }
                String[] kolom = pecahBaris(baris);
                if (kolom.length < 6) {
                    throw new IOException("Baris " + nomorBaris
                            + " tidak lengkap, harus ada 6 kolom.");
                }
                LocalDate tanggal;
                try {
                    tanggal = LocalDate.parse(kolom[0].trim());
                } catch (DateTimeParseException e) {
                    throw new IOException("Baris " + nomorBaris
                            + ": tanggal harus format yyyy-MM-dd, contoh 2026-11-21.");
                }
                Jenis jenis;
                try {
                    jenis = Jenis.valueOf(kolom[2].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IOException("Baris " + nomorBaris
                            + ": jenis harus PEMASUKAN atau PENGELUARAN.");
                }
                long jumlah;
                try {
                    jumlah = Long.parseLong(kolom[3].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Baris " + nomorBaris
                            + ": jumlah harus berupa angka.");
                }
                Kategori kategori = kategoriRepo.cariByNamaDanJenis(kolom[1].trim(), jenis);
                if (kategori == null) {
                    kategori = kategoriRepo.simpan(new Kategori(kolom[1].trim(), jenis));
                }
                Tugas tugas = null;
                if (!kolom[4].trim().isEmpty()) {
                    tugas = tugasRepo.cariByJudul(kolom[4].trim());
                }
                hasil.add(new Transaksi(tanggal, jumlah, kategori, tugas, kolom[5]));
            }
        }
        return hasil;
    }

    /** Gabungkan kolom menjadi satu baris CSV, beri tanda kutip kalau perlu. */
    static String gabungBaris(String[] kolom) {
        StringBuilder hasil = new StringBuilder();
        for (int i = 0; i < kolom.length; i++) {
            if (i > 0) {
                hasil.append(',');
            }
            String nilai = kolom[i] == null ? "" : kolom[i];
            if (nilai.contains(",") || nilai.contains("\"") || nilai.contains("\n")) {
                hasil.append('"');
                hasil.append(nilai.replace("\"", "\"\""));
                hasil.append('"');
            } else {
                hasil.append(nilai);
            }
        }
        return hasil.toString();
    }

    /** Pecah satu baris CSV menjadi kolom, dengan dukungan tanda kutip. */
    static String[] pecahBaris(String baris) {
        List<String> kolom = new ArrayList<>();
        StringBuilder saatIni = new StringBuilder();
        boolean diDalamKutip = false;
        for (int i = 0; i < baris.length(); i++) {
            char c = baris.charAt(i);
            if (diDalamKutip) {
                if (c == '"') {
                    if (i + 1 < baris.length() && baris.charAt(i + 1) == '"') {
                        saatIni.append('"');
                        i++;
                    } else {
                        diDalamKutip = false;
                    }
                } else {
                    saatIni.append(c);
                }
            } else if (c == '"') {
                diDalamKutip = true;
            } else if (c == ',') {
                kolom.add(saatIni.toString());
                saatIni.setLength(0);
            } else {
                saatIni.append(c);
            }
        }
        kolom.add(saatIni.toString());
        return kolom.toArray(new String[0]);
    }
}
