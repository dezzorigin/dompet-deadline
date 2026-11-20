package dompetdeadline.repository;

import dompetdeadline.model.Prioritas;
import dompetdeadline.model.Tugas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Akses data untuk tabel tugas. Semua query memakai PreparedStatement. */
public class TugasRepository {

    public List<Tugas> semua() throws SQLException {
        List<Tugas> hasil = new ArrayList<>();
        try (Connection c = Koneksi.buka();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, judul, mata_kuliah, tenggat, prioritas, selesai "
                             + "FROM tugas ORDER BY tenggat, judul")) {
            while (rs.next()) {
                hasil.add(baca(rs));
            }
        }
        return hasil;
    }

    public Tugas cariById(long id) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, judul, mata_kuliah, tenggat, prioritas, selesai "
                             + "FROM tugas WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return baca(rs);
                }
            }
        }
        return null;
    }

    /** Cari tugas berdasarkan judul, untuk keperluan impor CSV. */
    public Tugas cariByJudul(String judul) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, judul, mata_kuliah, tenggat, prioritas, selesai "
                             + "FROM tugas WHERE judul = ?")) {
            ps.setString(1, judul);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return baca(rs);
                }
            }
        }
        return null;
    }

    public Tugas simpan(Tugas tugas) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO tugas (judul, mata_kuliah, tenggat, prioritas, selesai) "
                             + "VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            isiParameter(ps, tugas);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                if (kunci.next()) {
                    tugas.setId(kunci.getLong(1));
                }
            }
        }
        return tugas;
    }

    public void ubah(Tugas tugas) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE tugas SET judul = ?, mata_kuliah = ?, tenggat = ?, "
                             + "prioritas = ?, selesai = ? WHERE id = ?")) {
            isiParameter(ps, tugas);
            ps.setLong(6, tugas.getId());
            ps.executeUpdate();
        }
    }

    public void hapus(long id) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tugas WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void tandaiSelesai(long id, boolean selesai) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE tugas SET selesai = ? WHERE id = ?")) {
            ps.setInt(1, selesai ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void isiParameter(PreparedStatement ps, Tugas tugas) throws SQLException {
        ps.setString(1, tugas.getJudul());
        ps.setString(2, tugas.getMataKuliah());
        ps.setString(3, tugas.getTenggat().toString());
        ps.setString(4, tugas.getPrioritas().name());
        ps.setInt(5, tugas.isSelesai() ? 1 : 0);
    }

    private Tugas baca(ResultSet rs) throws SQLException {
        return new Tugas(
                rs.getLong("id"),
                rs.getString("judul"),
                rs.getString("mata_kuliah"),
                LocalDate.parse(rs.getString("tenggat")),
                Prioritas.valueOf(rs.getString("prioritas")),
                rs.getInt("selesai") == 1);
    }
}
