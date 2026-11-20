package dompetdeadline.view;

import dompetdeadline.model.Jenis;
import dompetdeadline.model.Kategori;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.service.FormatUang;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Dialog isi transaksi, dipakai untuk tambah dan ubah. */
public class FormTransaksiDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField kolomTanggal = new JTextField(12);
    private final JTextField kolomJumlah = new JTextField(12);
    private final JComboBox<Kategori> pilihKategori = new JComboBox<>();
    private final JComboBox<Tugas> pilihTugas = new JComboBox<>();
    private final JTextField kolomCatatan = new JTextField(20);
    private final JButton tombolSimpan = new JButton("Simpan");

    private boolean disimpan = false;
    private Transaksi hasil;

    public FormTransaksiDialog(JFrame induk, Transaksi lama,
                               List<Kategori> kategori, List<Tugas> tugas) {
        super(induk, lama == null ? "Tambah Transaksi" : "Ubah Transaksi", true);
        setLayout(new BorderLayout(8, 8));

        for (Kategori k : kategori) {
            pilihKategori.addItem(k);
        }
        pilihTugas.addItem(null); // pilihan "tidak ada tugas terkait"
        for (Tugas t : tugas) {
            pilihTugas.addItem(t);
        }
        pilihTugas.setRenderer((daftar, nilai, index, dipilih, fokus) -> {
            JLabel label = new JLabel(nilai == null ? "(tidak ada tugas)" : nilai.toString());
            if (dipilih) {
                label.setOpaque(true);
                label.setBackground(daftar.getSelectionBackground());
                label.setForeground(daftar.getSelectionForeground());
            }
            return label;
        });

        JPanel isi = new JPanel(new GridLayout(0, 2, 8, 8));
        isi.add(new JLabel("Tanggal (yyyy-MM-dd):"));
        isi.add(kolomTanggal);
        isi.add(new JLabel("Jumlah (Rp):"));
        isi.add(kolomJumlah);
        isi.add(new JLabel("Kategori:"));
        isi.add(pilihKategori);
        isi.add(new JLabel("Tugas terkait (boleh kosong):"));
        isi.add(pilihTugas);
        isi.add(new JLabel("Catatan:"));
        isi.add(kolomCatatan);
        add(isi, BorderLayout.CENTER);

        JPanel bawah = new JPanel();
        JButton batal = new JButton("Batal");
        batal.addActionListener(e -> dispose());
        tombolSimpan.addActionListener(e -> simpan());
        bawah.add(tombolSimpan);
        bawah.add(batal);
        add(bawah, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(tombolSimpan);

        if (lama == null) {
            kolomTanggal.setText(LocalDate.now().toString());
        } else {
            kolomTanggal.setText(lama.getTanggal().toString());
            kolomJumlah.setText(String.valueOf(lama.getJumlah()));
            for (Kategori k : kategori) {
                if (k.getId() == lama.getKategori().getId()) {
                    pilihKategori.setSelectedItem(k);
                    break;
                }
            }
            if (lama.getTugas() != null) {
                for (Tugas t : tugas) {
                    if (t.getId() == lama.getTugas().getId()) {
                        pilihTugas.setSelectedItem(t);
                        break;
                    }
                }
            }
            kolomCatatan.setText(lama.getCatatan());
        }

        pack();
        setLocationRelativeTo(induk);
    }

    private void simpan() {
        LocalDate tanggal;
        try {
            tanggal = LocalDate.parse(kolomTanggal.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Tanggal tidak valid. Tulis dengan format yyyy-MM-dd, contoh 2026-11-21.",
                    "Tanggal salah", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long jumlah;
        try {
            jumlah = FormatUang.baca(kolomJumlah.getText());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Jumlah salah", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Kategori kategori = (Kategori) pilihKategori.getSelectedItem();
        if (kategori == null) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu kategorinya. Kalau belum ada, tambahkan lewat menu Kategori.",
                    "Kategori kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Tugas tugas = (Tugas) pilihTugas.getSelectedItem();
        String catatan = kolomCatatan.getText().trim();
        hasil = new Transaksi(tanggal, jumlah, kategori, tugas, catatan);
        disimpan = true;
        dispose();
    }

    public boolean isDisimpan() {
        return disimpan;
    }

    public Transaksi getHasil() {
        return hasil;
    }
}
