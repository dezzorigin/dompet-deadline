package dompetdeadline.view;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Kategori;
import dompetdeadline.repository.KategoriRepository;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.List;

/** Dialog untuk menambah dan menghapus kategori. */
public class KategoriDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JFrame induk;
    private final KategoriRepository repo = new KategoriRepository();
    private final DefaultListModel<Kategori> model = new DefaultListModel<>();
    private final JTextField kolomNama = new JTextField(16);
    private final JComboBox<Jenis> pilihJenis = new JComboBox<>(Jenis.values());

    public KategoriDialog(JFrame induk) {
        super(induk, "Kelola Kategori", true);
        this.induk = induk;
        setLayout(new BorderLayout(8, 8));

        JList<Kategori> daftar = new JList<>(model);
        add(new JScrollPane(daftar), BorderLayout.CENTER);

        JPanel isi = new JPanel();
        isi.add(new JLabel("Nama:"));
        isi.add(kolomNama);
        isi.add(new JLabel("Jenis:"));
        isi.add(pilihJenis);
        JButton tambah = new JButton("Tambah");
        tambah.addActionListener(e -> tambahKategori());
        isi.add(tambah);
        add(isi, BorderLayout.NORTH);

        JPanel bawah = new JPanel();
        JButton hapus = new JButton("Hapus Kategori Terpilih");
        hapus.addActionListener(e -> {
            Kategori terpilih = daftar.getSelectedValue();
            if (terpilih == null) {
                JOptionPane.showMessageDialog(this, "Pilih dulu kategori yang mau dihapus.",
                        "Belum ada yang dipilih", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int jawab = JOptionPane.showConfirmDialog(this,
                    "Hapus kategori \"" + terpilih.getNama() + "\"?",
                    "Hapus Kategori", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (jawab != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                repo.hapus(terpilih.getId());
                muat();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Kategori tidak bisa dihapus karena masih dipakai transaksi.",
                        "Galat", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton tutup = new JButton("Tutup");
        tutup.addActionListener(e -> dispose());
        bawah.add(hapus);
        bawah.add(tutup);
        add(bawah, BorderLayout.SOUTH);

        muat();
        setSize(480, 320);
        setLocationRelativeTo(induk);
    }

    private void tambahKategori() {
        String nama = kolomNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kategori tidak boleh kosong.",
                    "Nama kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Jenis jenis = (Jenis) pilihJenis.getSelectedItem();
        try {
            repo.simpan(new Kategori(nama, jenis));
            kolomNama.setText("");
            muat();
            JOptionPane.showMessageDialog(this, "Kategori berhasil ditambahkan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah kategori: " + e.getMessage(),
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void muat() {
        try {
            List<Kategori> semua = repo.semua();
            model.clear();
            for (Kategori k : semua) {
                model.addElement(k);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(induk, "Gagal memuat kategori: " + e.getMessage(),
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }
}
