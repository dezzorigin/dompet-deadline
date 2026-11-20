package dompetdeadline.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Mengurus koneksi SQLite. Berkas database disimpan di folder pengguna,
 * tepatnya di ~/.dompet-deadline/dompet-deadline.db, bukan di folder kode,
 * supaya data tetap ada walaupun folder proyek dipindah atau dihapus.
 */
public class Koneksi {

    private static final String NAMA_FOLDER = ".dompet-deadline";
    private static final String NAMA_FILE = "dompet-deadline.db";

    private Koneksi() {
    }

    /** Lokasi berkas database di folder pengguna. */
    public static File lokasiDatabase() {
        String home = System.getProperty("user.home");
        return new File(new File(home, NAMA_FOLDER), NAMA_FILE);
    }

    /** Buka koneksi baru, sekaligus membuat tabel kalau belum ada. */
    public static Connection buka() throws SQLException {
        File berkas = lokasiDatabase();
        File folder = berkas.getParentFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Connection koneksi = DriverManager.getConnection("jdbc:sqlite:" + berkas.getAbsolutePath());
        buatTabelKalauBelumAda(koneksi);
        return koneksi;
    }

    private static void buatTabelKalauBelumAda(Connection koneksi) throws SQLException {
        try (Statement st = koneksi.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("CREATE TABLE IF NOT EXISTS kategori ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nama TEXT NOT NULL, "
                    + "jenis TEXT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS tugas ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "judul TEXT NOT NULL, "
                    + "mata_kuliah TEXT NOT NULL, "
                    + "tenggat TEXT NOT NULL, "
                    + "prioritas TEXT NOT NULL, "
                    + "selesai INTEGER NOT NULL DEFAULT 0)");
            st.execute("CREATE TABLE IF NOT EXISTS transaksi ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "tanggal TEXT NOT NULL, "
                    + "jumlah INTEGER NOT NULL, "
                    + "kategori_id INTEGER NOT NULL REFERENCES kategori(id), "
                    + "tugas_id INTEGER REFERENCES tugas(id), "
                    + "catatan TEXT NOT NULL DEFAULT '')");
        }
    }
}
