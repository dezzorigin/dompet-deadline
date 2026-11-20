package dompetdeadline.service;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logika hitung-hitungan aplikasi. Kelas ini tidak tahu soal database
 * maupun tampilan, jadi gampang diuji.
 */
public class DompetService {

    /** Sisa uang pada bulan tertentu: total pemasukan dikurangi total pengeluaran. */
    public long hitungSisaUang(List<Transaksi> semua, YearMonth bulan) {
        long masuk = 0;
        long keluar = 0;
        for (Transaksi t : semua) {
            if (!YearMonth.from(t.getTanggal()).equals(bulan)) {
                continue;
            }
            if (t.getKategori().getJenis() == Jenis.PEMASUKAN) {
                masuk += t.getJumlah();
            } else {
                keluar += t.getJumlah();
            }
        }
        return masuk - keluar;
    }

    /** Total pengeluaran yang dikaitkan ke satu tugas. Pemasukan tidak dihitung. */
    public long totalBiayaTugas(long tugasId, List<Transaksi> semua) {
        long total = 0;
        for (Transaksi t : semua) {
            if (t.getTugas() != null && t.getTugas().getId() == tugasId
                    && t.getKategori().getJenis() == Jenis.PENGELUARAN) {
                total += t.getJumlah();
            }
        }
        return total;
    }

    /** Total biaya semua tugas yang masih aktif (belum selesai). */
    public long totalBiayaTugasAktif(List<Tugas> semuaTugas, List<Transaksi> semuaTransaksi) {
        long total = 0;
        for (Tugas tugas : semuaTugas) {
            if (!tugas.isSelesai()) {
                total += totalBiayaTugas(tugas.getId(), semuaTransaksi);
            }
        }
        return total;
    }

    /** Tiga tenggat terdekat yang belum selesai, diurutkan dari yang paling dekat. */
    public List<Tugas> tigaTenggatTerdekat(List<Tugas> semua, LocalDate hariIni) {
        List<Tugas> aktif = new ArrayList<>();
        for (Tugas tugas : semua) {
            if (!tugas.isSelesai() && !tugas.getTenggat().isBefore(hariIni)) {
                aktif.add(tugas);
            }
        }
        aktif.sort(Comparator.comparing(Tugas::getTenggat));
        if (aktif.size() > 3) {
            return aktif.subList(0, 3);
        }
        return aktif;
    }

    /**
     * Tugas yang mendesak: belum selesai dan tenggatnya hari ini atau besok,
     * alias jatuh dalam 24 jam ke depan.
     */
    public List<Tugas> tugasMendesak(List<Tugas> semua, LocalDate hariIni) {
        List<Tugas> mendesak = new ArrayList<>();
        LocalDate batas = hariIni.plusDays(1);
        for (Tugas tugas : semua) {
            if (tugas.isSelesai()) {
                continue;
            }
            if (!tugas.getTenggat().isBefore(hariIni)
                    && !tugas.getTenggat().isAfter(batas)) {
                mendesak.add(tugas);
            }
        }
        mendesak.sort(Comparator.comparing(Tugas::getTenggat));
        return mendesak;
    }

    /** Pengeluaran per kategori pada bulan tertentu, diurutkan dari yang terbesar. */
    public Map<String, Long> pengeluaranPerKategori(List<Transaksi> semua, YearMonth bulan) {
        Map<String, Long> jumlah = new LinkedHashMap<>();
        for (Transaksi t : semua) {
            if (!YearMonth.from(t.getTanggal()).equals(bulan)) {
                continue;
            }
            if (t.getKategori().getJenis() != Jenis.PENGELUARAN) {
                continue;
            }
            String nama = t.getKategori().getNama();
            jumlah.put(nama, jumlah.getOrDefault(nama, 0L) + t.getJumlah());
        }
        List<Map.Entry<String, Long>> urut = new ArrayList<>(jumlah.entrySet());
        urut.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        Map<String, Long> hasil = new LinkedHashMap<>();
        for (Map.Entry<String, Long> e : urut) {
            hasil.put(e.getKey(), e.getValue());
        }
        return hasil;
    }
}
