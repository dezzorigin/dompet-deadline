package dompetdeadline.view;

import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.service.DompetService;
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

/** Tab tugas: tabel dengan total biaya per tugas, pencarian, tambah, ubah, hapus. */
public class TugasPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final MainFrame frame;
    private final DompetService service = new DompetService();
    private final TugasTableModel model = new TugasTableModel();
    private final JTable tabel = new JTable(model);
    private final JTextField kolomCari = new JTextField(20);

    private List<Tugas> semuaTugas = new ArrayList<>();
    private List<Transaksi> semuaTransaksi = new ArrayList<>();

    public TugasPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(8, 8));

        JPanel atas = new JPanel(new BorderLayout(8, 0));
        atas.add(new JLabel("Cari:"), BorderLayout.WEST);
        atas.add(kolomCari, BorderLayout.CENTER);
        kolomCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saring();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saring();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saring();
            }
        });
        add(atas, BorderLayout.NORTH);

        TableRowSorter<TugasTableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
        tabel.setRowSorter(sorter);
        tabel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        JPanel bawah = new JPanel();
        JButton tambah = new JButton("Tambah Tugas");
        tambah.addActionListener(e -> tambah());
        JButton ubah = new JButton("Ubah");
        ubah.addActionListener(e -> ubah());
        JButton hapus = new JButton("Hapus");
        hapus.addActionListener(e -> hapus());
        JButton selesai = new JButton("Tandai Selesai");
        selesai.addActionListener(e -> tandaiSelesai());
        bawah.add(tambah);
        bawah.add(ubah);
        bawah.add(hapus);
        bawah.add(selesai);
        add(bawah, BorderLayout.SOUTH);
    }

    /** Tampilkan daftar tugas terbaru di tabel. */
    public void tampilkan(List<Tugas> tugas, List<Transaksi> transaksi) {
        this.semuaTugas = tugas;
        this.semuaTransaksi = transaksi;
        saring();
    }

    private void saring() {
        String kata = kolomCari.getText().trim().toLowerCase();
        List<Tugas> hasil = new ArrayList<>();
        for (Tugas t : semuaTugas) {
            if (kata.isEmpty()
                    || t.getJudul().toLowerCase().contains(kata)
                    || t.getMataKuliah().toLowerCase().contains(kata)) {
                hasil.add(t);
            }
        }
        model.setData(hasil, semuaTransaksi);
    }

    private void tambah() {
        FormTugasDialog dialog = new FormTugasDialog(frame, null);
        dialog.setVisible(true);
        if (dialog.isDisimpan()) {
            try {
                frame.getTugasRepo().simpan(dialog.getHasil());
                JOptionPane.showMessageDialog(this, "Tugas berhasil disimpan.",
                        "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                frame.refreshAll();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(),
                        "Galat", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ubah() {
        Tugas lama = tugasTerpilih();
        if (lama == null) {
            JOptionPane.showMessageDialog(this, "Pilih dulu tugas yang mau diubah.",
                    "Belum ada yang dipilih", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FormTugasDialog dialog = new FormTugasDialog(frame, lama);
        dialog.setVisible(true);
        if (dialog.isDisimpan()) {
            Tugas baru = dialog.getHasil();
            baru.setId(lama.getId());
            baru.setSelesai(lama.isSelesai());
            try {
                frame.getTugasRepo().ubah(baru);
                JOptionPane.showMessageDialog(this, "Tugas berhasil diubah.",
                        "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                frame.refreshAll();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal mengubah: " + e.getMessage(),
                        "Galat", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void hapus() {
        Tugas terpilih = tugasTerpilih();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih dulu tugas yang mau dihapus.",
                    "Belum ada yang dipilih", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int jawab = JOptionPane.showConfirmDialog(this,
                "Hapus tugas \"" + terpilih.getJudul() + "\"?",
                "Hapus Tugas", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            frame.getTugasRepo().hapus(terpilih.getId());
            JOptionPane.showMessageDialog(this, "Tugas berhasil dihapus.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            frame.refreshAll();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal menghapus. Mungkin tugas ini masih dipakai transaksi.",
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tandaiSelesai() {
        Tugas terpilih = tugasTerpilih();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih dulu tugas yang mau ditandai.",
                    "Belum ada yang dipilih", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean baru = !terpilih.isSelesai();
            frame.getTugasRepo().tandaiSelesai(terpilih.getId(), baru);
            JOptionPane.showMessageDialog(this,
                    baru ? "Tugas ditandai selesai." : "Tugas dikembalikan menjadi aktif.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            frame.refreshAll();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah status: " + e.getMessage(),
                    "Galat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Tugas tugasTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            return null;
        }
        return model.getData().get(tabel.convertRowIndexToModel(baris));
    }

    /** Model tabel tugas, hanya untuk tampilan. */
    private class TugasTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
        private final String[] kolom = {"Judul", "Mata Kuliah", "Tenggat", "Prioritas", "Status", "Total Biaya"};
        private List<Tugas> data = new ArrayList<>();
        private List<Transaksi> transaksi = new ArrayList<>();

        void setData(List<Tugas> data, List<Transaksi> transaksi) {
            this.data = data;
            this.transaksi = transaksi;
            fireTableDataChanged();
        }

        List<Tugas> getData() {
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
            Tugas t = data.get(row);
            switch (column) {
                case 0:
                    return t.getJudul();
                case 1:
                    return t.getMataKuliah();
                case 2:
                    return t.getTenggat().format(TANGGAL);
                case 3:
                    return t.getPrioritas().getLabel();
                case 4:
                    return t.isSelesai() ? "Selesai" : "Aktif";
                case 5:
                    return FormatUang.format(service.totalBiayaTugas(t.getId(), transaksi));
                default:
                    return "";
            }
        }
    }
}
