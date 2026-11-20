package dompetdeadline;

import dompetdeadline.repository.KategoriRepository;
import dompetdeadline.view.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/** Titik masuk aplikasi. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Kalau gagal, biarkan tampilan bawaan Java, aplikasi tetap jalan.
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Siapkan kategori awal supaya aplikasi langsung bisa dipakai.
                new KategoriRepository().isiAwalKalauKosong();
            } catch (SQLException e) {
                System.err.println("Gagal menyiapkan kategori awal: " + e.getMessage());
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
