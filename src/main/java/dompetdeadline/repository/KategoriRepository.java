package dompetdeadline.repository;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Kategori;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Akses data untuk tabel kategori. Semua query memakai PreparedStatement. */
public class KategoriRepository {

    public List<Kategori> semua() throws SQLException {
        List<Kategori> hasil = new ArrayList<>();
        try (Connection c = Koneksi.buka();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nama, jenis FROM kategori ORDER BY jenis, nama")) {
            while (rs.next()) {
                hasil.add(baca(rs));
            }
        }
        return hasil;
    }

    public Kategori cariById(long id) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, nama, jenis FROM kategori WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return baca(rs);
                }
            }
        }
        return null;
    }

    /** Cari kategori berdasarkan nama dan jenis, untuk keperluan impor CSV. */
    public Kategori cariByNamaDanJenis(String nama, Jenis jenis) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, nama, jenis FROM kategori WHERE nama = ? AND jenis = ?")) {
            ps.setString(1, nama);
            ps.setString(2, jenis.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return baca(rs);
                }
            }
        }
        return null;
    }

    public Kategori simpan(Kategori kategori) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO kategori (nama, jenis) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kategori.getNama());
            ps.setString(2, kategori.getJenis().name());
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                if (kunci.next()) {
                    kategori.setId(kunci.getLong(1));
                }
            }
        }
        return kategori;
    }

    public void ubah(Kategori kategori) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE kategori SET nama = ?, jenis = ? WHERE id = ?")) {
            ps.setString(1, kategori.getNama());
            ps.setString(2, kategori.getJenis().name());
            ps.setLong(3, kategori.getId());
            ps.executeUpdate();
        }
    }

    public void hapus(long id) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement("DELETE FROM kategori WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Isi kategori awal kalau tabel masih kosong, supaya aplikasi langsung bisa dipakai. */
    public void isiAwalKalauKosong() throws SQLException {
        if (!semua().isEmpty()) {
            return;
        }
        simpan(new Kategori("Uang Saku", Jenis.PEMASUKAN));
        simpan(new Kategori("Gaji Paruh Waktu", Jenis.PEMASUKAN));
        simpan(new Kategori("Makan", Jenis.PENGELUARAN));
        simpan(new Kategori("Transport", Jenis.PENGELUARAN));
        simpan(new Kategori("Cetak dan ATK", Jenis.PENGELUARAN));
        simpan(new Kategori("Buku", Jenis.PENGELUARAN));
        simpan(new Kategori("Lainnya", Jenis.PENGELUARAN));
    }

    private Kategori baca(ResultSet rs) throws SQLException {
        return new Kategori(
                rs.getLong("id"),
                rs.getString("nama"),
                Jenis.valueOf(rs.getString("jenis")));
    }
}
