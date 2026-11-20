package dompetdeadline.repository;

import dompetdeadline.model.Kategori;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Akses data untuk tabel transaksi. Semua query memakai PreparedStatement. */
public class TransaksiRepository {

    public List<Transaksi> semua() throws SQLException {
        List<Transaksi> hasil = new ArrayList<>();
        String sql = "SELECT t.id, t.tanggal, t.jumlah, t.kategori_id, t.tugas_id, t.catatan, "
                + "k.nama AS nama_kategori, k.jenis AS jenis_kategori, "
                + "g.judul AS judul_tugas, g.mata_kuliah, g.tenggat, g.prioritas, g.selesai "
                + "FROM transaksi t "
                + "JOIN kategori k ON k.id = t.kategori_id "
                + "LEFT JOIN tugas g ON g.id = t.tugas_id "
                + "ORDER BY t.tanggal DESC, t.id DESC";
        try (Connection c = Koneksi.buka();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                hasil.add(baca(rs));
            }
        }
        return hasil;
    }

    /** Cari transaksi berdasarkan catatan atau nama kategori, untuk kotak pencarian. */
    public List<Transaksi> cari(String kataKunci) throws SQLException {
        List<Transaksi> hasil = new ArrayList<>();
        String sql = "SELECT t.id, t.tanggal, t.jumlah, t.kategori_id, t.tugas_id, t.catatan, "
                + "k.nama AS nama_kategori, k.jenis AS jenis_kategori, "
                + "g.judul AS judul_tugas, g.mata_kuliah, g.tenggat, g.prioritas, g.selesai "
                + "FROM transaksi t "
                + "JOIN kategori k ON k.id = t.kategori_id "
                + "LEFT JOIN tugas g ON g.id = t.tugas_id "
                + "WHERE t.catatan LIKE ? OR k.nama LIKE ? OR g.judul LIKE ? "
                + "ORDER BY t.tanggal DESC, t.id DESC";
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String pola = "%" + kataKunci + "%";
            ps.setString(1, pola);
            ps.setString(2, pola);
            ps.setString(3, pola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(baca(rs));
                }
            }
        }
        return hasil;
    }

    public Transaksi simpan(Transaksi transaksi) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO transaksi (tanggal, jumlah, kategori_id, tugas_id, catatan) "
                             + "VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            isiParameter(ps, transaksi);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                if (kunci.next()) {
                    transaksi.setId(kunci.getLong(1));
                }
            }
        }
        return transaksi;
    }

    public void ubah(Transaksi transaksi) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE transaksi SET tanggal = ?, jumlah = ?, kategori_id = ?, "
                             + "tugas_id = ?, catatan = ? WHERE id = ?")) {
            isiParameter(ps, transaksi);
            ps.setLong(6, transaksi.getId());
            ps.executeUpdate();
        }
    }

    public void hapus(long id) throws SQLException {
        try (Connection c = Koneksi.buka();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transaksi WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void isiParameter(PreparedStatement ps, Transaksi transaksi) throws SQLException {
        ps.setString(1, transaksi.getTanggal().toString());
        ps.setLong(2, transaksi.getJumlah());
        ps.setLong(3, transaksi.getKategori().getId());
        if (transaksi.getTugas() == null) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setLong(4, transaksi.getTugas().getId());
        }
        ps.setString(5, transaksi.getCatatan());
    }

    private Transaksi baca(ResultSet rs) throws SQLException {
        Kategori kategori = new Kategori(
                rs.getLong("kategori_id"),
                rs.getString("nama_kategori"),
                dompetdeadline.model.Jenis.valueOf(rs.getString("jenis_kategori")));
        Tugas tugas = null;
        String judulTugas = rs.getString("judul_tugas");
        if (judulTugas != null) {
            tugas = new Tugas(
                    rs.getLong("tugas_id"),
                    judulTugas,
                    rs.getString("mata_kuliah"),
                    LocalDate.parse(rs.getString("tenggat")),
                    dompetdeadline.model.Prioritas.valueOf(rs.getString("prioritas")),
                    rs.getInt("selesai") == 1);
        }
        return new Transaksi(
                rs.getLong("id"),
                LocalDate.parse(rs.getString("tanggal")),
                rs.getLong("jumlah"),
                kategori,
                tugas,
                rs.getString("catatan"));
    }
}
