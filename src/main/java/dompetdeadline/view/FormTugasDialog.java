package dompetdeadline.view;

import dompetdeadline.model.Prioritas;
import dompetdeadline.model.Tugas;

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

/** Dialog isi tugas, dipakai untuk tambah dan ubah. */
public class FormTugasDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField kolomJudul = new JTextField(20);
    private final JTextField kolomMataKuliah = new JTextField(20);
    private final JTextField kolomTenggat = new JTextField(12);
    private final JComboBox<Prioritas> pilihPrioritas = new JComboBox<>(Prioritas.values());
    private final JButton tombolSimpan = new JButton("Simpan");

    private boolean disimpan = false;
    private Tugas hasil;

    public FormTugasDialog(JFrame induk, Tugas lama) {
        super(induk, lama == null ? "Tambah Tugas" : "Ubah Tugas", true);
        setLayout(new BorderLayout(8, 8));

        JPanel isi = new JPanel(new GridLayout(0, 2, 8, 8));
        isi.add(new JLabel("Judul tugas:"));
        isi.add(kolomJudul);
        isi.add(new JLabel("Mata kuliah:"));
        isi.add(kolomMataKuliah);
        isi.add(new JLabel("Tenggat (yyyy-MM-dd):"));
        isi.add(kolomTenggat);
        isi.add(new JLabel("Prioritas:"));
        isi.add(pilihPrioritas);
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
            kolomTenggat.setText(LocalDate.now().plusDays(7).toString());
        } else {
            kolomJudul.setText(lama.getJudul());
            kolomMataKuliah.setText(lama.getMataKuliah());
            kolomTenggat.setText(lama.getTenggat().toString());
            pilihPrioritas.setSelectedItem(lama.getPrioritas());
        }

        pack();
        setLocationRelativeTo(induk);
    }

    private void simpan() {
        String judul = kolomJudul.getText().trim();
        if (judul.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul tugas tidak boleh kosong.",
                    "Judul kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String mataKuliah = kolomMataKuliah.getText().trim();
        if (mataKuliah.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mata kuliah tidak boleh kosong.",
                    "Mata kuliah kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate tenggat;
        try {
            tenggat = LocalDate.parse(kolomTenggat.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Tenggat tidak valid. Tulis dengan format yyyy-MM-dd, contoh 2026-11-28.",
                    "Tenggat salah", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Prioritas prioritas = (Prioritas) pilihPrioritas.getSelectedItem();
        hasil = new Tugas(judul, mataKuliah, tenggat, prioritas);
        disimpan = true;
        dispose();
    }

    public boolean isDisimpan() {
        return disimpan;
    }

    public Tugas getHasil() {
        return hasil;
    }
}
