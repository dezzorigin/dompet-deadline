package dompetdeadline.view;

import dompetdeadline.model.Kategori;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.service.FormatUang;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Tab transaksi: tabel yang bisa diurutkan, pencarian, tambah, ubah, hapus. */
public class TransaksiPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final MainFrame frame;
    private final TransaksiTableModel model = new TransaksiTableModel();
    private final JTable tabel = new JTable(model);
    private final JTextField kolomCari = new JTextField(20);

    private List<Kategori> kategori = new ArrayList<>();
    private List<Tugas> tugas = new ArrayList<>();

    public TransaksiPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(8, 8));

        JPanel atas = new JPanel(new BorderLayout(8, 0));
        atas.add(new JLabel("Cari:"), BorderLayout.WEST);
        atas.add(kolomCari, BorderLayout.CENTER);
        kolomCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                cari();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                cari();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                cari();
            }
        });
        add(atas, BorderLayout.NORTH);

        TableRowSorter<TransaksiTableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        tabel.setRowSorter(sorter);
        tabel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        JPanel bawah = new JPanel();
        JButton tambah = new JButton("Tambah Transaksi");
        tambah.addActionListener(e -> tambah());
        JButton ubah = new JButton("Ubah");
        ubah.addActionListener(e -> ubah());
        JButton hapus = new JButton("Hapus");
        hapus.addActionListener(e -> hapus());
        bawah.add(tambah);
        bawah.add(ubah);
        bawah.add(hapus);
        add(bawah, BorderLayout.SOUTH);
    }

    /** Tampilkan daftar transaksi terbaru di tabel. */
    public void tampilkan(List<Transaksi> transaksi, List<Kategori> kategori, List<Tugas> tugas) {
        this.kategori = kategori;
        this.tugas = tugas;
        model.setData(transaksi);
    }

    private void cari() {
        String kata = kolomCari.getText().trim();
        if (kata.isEmpty()) {
            frame.refreshAll();
            return;
        }
        try {
            model.setData(frame.getTransaksiRepo().cari(kata));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencari: " + e.getMessage(),
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tambah() {
        FormTransaksiDialog dialog = new FormTransaksiDialog(frame, null, kategori, tugas);
        dialog.setVisible(true);
        if (dialog.isDisimpan()) {
            simpan(dialog.getHasil());
        }
    }

    private void ubah() {
        Transaksi lama = transaksiTerpilih();
        if (lama == null) {
            JOptionPane.showMessageDialog(this, "Pilih dulu transaksi yang mau diubah.",
                    "Belum ada yang dipilih", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FormTransaksiDialog dialog = new FormTransaksiDialog(frame, lama, kategori, tugas);
        dialog.setVisible(true);
        if (dialog.isDisimpan()) {
            Transaksi baru = dialog.getHasil();
            baru.setId(lama.getId());
            try {
                frame.getTransaksiRepo().ubah(baru);
                JOptionPane.showMessageDialog(this, "Transaksi berhasil diubah.",
                        "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                frame.refreshAll();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal mengubah: " + e.getMessage(),
                        "Galat", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void hapus() {
        Transaksi terpilih = transaksiTerpilih();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih dulu transaksi yang mau dihapus.",
                    "Belum ada yang dipilih", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int jawab = JOptionPane.showConfirmDialog(this,
                "Hapus transaksi " + FormatUang.format(terpilih.getJumlah())
                        + " (" + terpilih.getKategori().getNama() + ")?",
                "Hapus Transaksi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            frame.getTransaksiRepo().hapus(terpilih.getId());
            JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            frame.refreshAll();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus: " + e.getMessage(),
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpan(Transaksi transaksi) {
        try {
            frame.getTransaksiRepo().simpan(transaksi);
            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            frame.refreshAll();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(),
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Transaksi transaksiTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        return model.getData().get(tabel.convertRowIndexToModel(baris));
    }

    /** Model tabel transaksi, hanya untuk tampilan. */
    private static class TransaksiTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
        private final String[] kolom = {"Tanggal", "Kategori", "Jenis", "Jumlah", "Tugas", "Catatan"};
        private List<Transaksi> data = new ArrayList<>();

        void setData(List<Transaksi> data) {
            this.data = data;
            fireTableDataChanged();
        }

        List<Transaksi> getData() {
            return data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return kolom.length;
        }

        @Override
        public String getColumnName(int column) {
            return kolom[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Transaksi t = data.get(row);
            switch (column) {
                case 0:
                    return t.getTanggal().format(TANGGAL);
                case 1:
                    return t.getKategori().getNama();
                case 2:
                    return t.getKategori().getJenis().getLabel();
                case 3:
                    return FormatUang.format(t.getJumlah());
                case 4:
                    return t.getTugas() == null ? "-" : t.getTugas().getJudul();
                case 5:
                    return t.getCatatan();
                default:
                    return "";
            }
        }
    }
}
