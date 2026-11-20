package dompetdeadline.service;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Kategori;
import dompetdeadline.model.Prioritas;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.repository.KategoriRepository;
import dompetdeadline.repository.TugasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvServiceTest {

    private final CsvService csv = new CsvService();

    @Test
    void eksporLaluImporKembaliDatanyaSama(@TempDir Path folder) throws Exception {
        // Arahkan database ke folder sementara supaya tidak mengganggu data asli.
        String homeLama = System.getProperty("user.home");
        System.setProperty("user.home", folder.toString());
        try {
            KategoriRepository kategoriRepo = new KategoriRepository();
            TugasRepository tugasRepo = new TugasRepository();
            Kategori makan = kategoriRepo.simpan(new Kategori("Makan", Jenis.PENGELUARAN));
            Kategori saku = kategoriRepo.simpan(new Kategori("Uang Saku", Jenis.PEMASUKAN));
            Tugas laporan = tugasRepo.simpan(new Tugas("Laporan PBO", "PBO",
                    LocalDate.of(2026, 11, 28), Prioritas.TINGGI));
            List<Transaksi> awal = List.of(
                    new Transaksi(LocalDate.of(2026, 11, 5), 25000, makan, laporan, "cetak, jilid"),
                    new Transaksi(LocalDate.of(2026, 11, 6), 1500000, saku, null, "uang saku"));

            File berkas = folder.resolve("transaksi.csv").toFile();
            csv.ekspor(awal, berkas);
            String isi = Files.readString(berkas.toPath());
            assertTrue(isi.startsWith("tanggal,kategori,jenis,jumlah,tugas,catatan"));
            assertTrue(isi.contains("\"cetak, jilid\""));

            // Hapus database lalu impor ulang ke database kosong.
            // Tugasnya dibuat ulang dulu supaya impor bisa mengaitkannya lagi.
            Files.deleteIfExists(folder.resolve(".dompet-deadline/dompet-deadline.db"));
            tugasRepo.simpan(new Tugas("Laporan PBO", "PBO",
                    LocalDate.of(2026, 11, 28), Prioritas.TINGGI));
            List<Transaksi> hasil = csv.impor(berkas, kategoriRepo, tugasRepo);
            assertEquals(2, hasil.size());
            assertEquals(25000, hasil.get(0).getJumlah());
            assertEquals("Laporan PBO", hasil.get(0).getTugas().getJudul());
            assertEquals("cetak, jilid", hasil.get(0).getCatatan());
            assertEquals(1500000, hasil.get(1).getJumlah());
            assertEquals(null, hasil.get(1).getTugas());
        } finally {
            System.setProperty("user.home", homeLama);
        }
    }

    @Test
    void imporTanggalSalahMemberiPesanYangJelas(@TempDir Path folder) throws Exception {
        String homeLama = System.getProperty("user.home");
        System.setProperty("user.home", folder.toString());
        try {
            File berkas = folder.resolve("rusak.csv").toFile();
            Files.writeString(berkas.toPath(),
                    "tanggal,kategori,jenis,jumlah,tugas,catatan\n"
                            + "21-11-2026,Makan,PENGELUARAN,25000,,cetak\n");
            IOException galat = assertThrows(IOException.class, () ->
                    csv.impor(berkas, new KategoriRepository(), new TugasRepository()));
            assertTrue(galat.getMessage().contains("tanggal"));
        } finally {
            System.setProperty("user.home", homeLama);
        }
    }

    @Test
    void gabungDanPecahBarisMenanganiKomaDanKutip() {
        String[] kolom = {"2026-11-05", "Makan", "PENGELUARAN", "25000", "", "cetak, \"jilid\""};
        String baris = CsvService.gabungBaris(kolom);
        String[] kembali = CsvService.pecahBaris(baris);
        assertEquals(6, kembali.length);
        assertEquals("cetak, \"jilid\"", kembali[5]);
    }
}
