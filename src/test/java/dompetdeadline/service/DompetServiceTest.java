package dompetdeadline.service;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Kategori;
import dompetdeadline.model.Prioritas;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DompetServiceTest {

    private final DompetService service = new DompetService();

    private static final Kategori UANG_SAKU = new Kategori(1, "Uang Saku", Jenis.PEMASUKAN);
    private static final Kategori MAKAN = new Kategori(2, "Makan", Jenis.PENGELUARAN);
    private static final Kategori CETAK = new Kategori(3, "Cetak dan ATK", Jenis.PENGELUARAN);

    @Test
    void sisaUangHanyaMenghitungBulanYangDiminta() {
        List<Transaksi> semua = List.of(
                new Transaksi(LocalDate.of(2026, 11, 5), 1500000, UANG_SAKU, null, ""),
                new Transaksi(LocalDate.of(2026, 11, 6), 200000, MAKAN, null, ""),
                new Transaksi(LocalDate.of(2026, 10, 20), 900000, UANG_SAKU, null, ""),
                new Transaksi(LocalDate.of(2026, 10, 21), 800000, MAKAN, null, ""));
        assertEquals(1300000, service.hitungSisaUang(semua, YearMonth.of(2026, 11)));
        assertEquals(100000, service.hitungSisaUang(semua, YearMonth.of(2026, 10)));
    }

    @Test
    void totalBiayaTugasHanyaPengeluaranYangTerkait() {
        Tugas laporan = new Tugas(10, "Laporan PBO", "PBO", LocalDate.of(2026, 11, 28),
                Prioritas.TINGGI);
        Tugas lain = new Tugas(11, "Makalah Agama", "Agama", LocalDate.of(2026, 11, 29),
                Prioritas.SEDANG);
        List<Transaksi> semua = List.of(
                new Transaksi(LocalDate.of(2026, 11, 5), 25000, CETAK, laporan, "cetak laporan"),
                new Transaksi(LocalDate.of(2026, 11, 6), 15000, CETAK, laporan, "jilid"),
                new Transaksi(LocalDate.of(2026, 11, 7), 50000, MAKAN, laporan, "makan"),
                new Transaksi(LocalDate.of(2026, 11, 8), 100000, CETAK, lain, "cetak makalah"),
                new Transaksi(LocalDate.of(2026, 11, 9), 200000, UANG_SAKU, laporan, "bukan biaya"));
        // Hanya 25000 + 15000 + 50000 yang dihitung; pemasukan dan tugas lain tidak.
        assertEquals(90000, service.totalBiayaTugas(10, semua));
        assertEquals(100000, service.totalBiayaTugas(11, semua));
    }

    @Test
    void tugasMendesakHanyaYangTenggatnyaDalam24Jam() {
        LocalDate hariIni = LocalDate.of(2026, 11, 21);
        Tugas hariIniTugas = new Tugas(1, "Kuis", "Logika",
                LocalDate.of(2026, 11, 21), Prioritas.TINGGI);
        Tugas besok = new Tugas(2, "Tugas", "Basdat",
                LocalDate.of(2026, 11, 22), Prioritas.SEDANG);
        Tugas lusa = new Tugas(3, "Laporan", "PBO",
                LocalDate.of(2026, 11, 23), Prioritas.RENDAH);
        Tugas sudahSelesai = new Tugas(4, "Makalah", "Agama",
                LocalDate.of(2026, 11, 21), Prioritas.TINGGI, true);
        List<Tugas> hasil = service.tugasMendesak(
                List.of(hariIniTugas, besok, lusa, sudahSelesai), hariIni);
        assertEquals(2, hasil.size());
        assertTrue(hasil.contains(hariIniTugas));
        assertTrue(hasil.contains(besok));
    }

    @Test
    void tigaTenggatTerdekatMaksimalTigaDanTerurut() {
        LocalDate hariIni = LocalDate.of(2026, 11, 21);
        List<Tugas> semua = List.of(
                new Tugas(1, "A", "PBO", LocalDate.of(2026, 11, 30), Prioritas.RENDAH),
                new Tugas(2, "B", "Basdat", LocalDate.of(2026, 11, 22), Prioritas.TINGGI),
                new Tugas(3, "C", "Logika", LocalDate.of(2026, 11, 25), Prioritas.SEDANG),
                new Tugas(4, "D", "Agama", LocalDate.of(2026, 11, 24), Prioritas.SEDANG),
                new Tugas(5, "E", "PBO", LocalDate.of(2026, 11, 23), Prioritas.TINGGI, true));
        List<Tugas> hasil = service.tigaTenggatTerdekat(semua, hariIni);
        assertEquals(3, hasil.size());
        assertEquals("B", hasil.get(0).getJudul());
        assertEquals("D", hasil.get(1).getJudul());
        assertEquals("C", hasil.get(2).getJudul());
    }

    @Test
    void pengeluaranPerKategoriHanyaBulanItuDanTerurutTerbesar() {
        List<Transaksi> semua = List.of(
                new Transaksi(LocalDate.of(2026, 11, 5), 300000, MAKAN, null, ""),
                new Transaksi(LocalDate.of(2026, 11, 6), 50000, CETAK, null, ""),
                new Transaksi(LocalDate.of(2026, 11, 7), 120000, MAKAN, null, ""),
                new Transaksi(LocalDate.of(2026, 11, 8), 1000000, UANG_SAKU, null, ""),
                new Transaksi(LocalDate.of(2026, 10, 5), 999000, MAKAN, null, ""));
        Map<String, Long> hasil = service.pengeluaranPerKategori(semua, YearMonth.of(2026, 11));
        assertEquals(2, hasil.size());
        assertEquals(420000, hasil.get("Makan"));
        assertEquals(50000, hasil.get("Cetak dan ATK"));
        // Urutan dari yang terbesar.
        assertEquals("Makan", hasil.keySet().iterator().next());
    }

    @Test
    void totalBiayaTugasAktifTidakMenghitungTugasSelesai() {
        Tugas aktif = new Tugas(10, "Laporan", "PBO", LocalDate.of(2026, 11, 28),
                Prioritas.TINGGI);
        Tugas selesai = new Tugas(11, "Makalah", "Agama", LocalDate.of(2026, 11, 29),
                Prioritas.SEDANG, true);
        List<Transaksi> semua = List.of(
                new Transaksi(LocalDate.of(2026, 11, 5), 25000, CETAK, aktif, ""),
                new Transaksi(LocalDate.of(2026, 11, 6), 80000, CETAK, selesai, ""));
        assertEquals(25000, service.totalBiayaTugasAktif(List.of(aktif, selesai), semua));
    }
}
