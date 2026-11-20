package dompetdeadline.view;

import dompetdeadline.model.Kategori;
import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.repository.KategoriRepository;
import dompetdeadline.repository.TransaksiRepository;
import dompetdeadline.repository.TugasRepository;
import dompetdeadline.service.CsvService;
import dompetdeadline.service.PengingatService;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Jendela utama: empat tab (Dashboard, Transaksi, Tugas, Grafik) dan menu. */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final KategoriRepository kategoriRepo = new KategoriRepository();
    private final TugasRepository tugasRepo = new TugasRepository();
    private final TransaksiRepository transaksiRepo = new TransaksiRepository();
    private final CsvService csvService = new CsvService();

    private final DashboardPanel dashboardPanel;
    private final TransaksiPanel transaksiPanel;
    private final TugasPanel tugasPanel;
    private final GrafikPanel grafikPanel;
    private final PengingatService pengingat;

    public MainFrame() {
        super("Dompet Deadline");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        dashboardPanel = new DashboardPanel();
        transaksiPanel = new TransaksiPanel(this);
        tugasPanel = new TugasPanel(this);
        grafikPanel = new GrafikPanel();

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Dashboard", dashboardPanel);
        tab.addTab("Transaksi", transaksiPanel);
        tab.addTab("Tugas", tugasPanel);
        tab.addTab("Grafik", grafikPanel);
        tab.addChangeListener(e -> refreshAll());
        add(tab);

        setJMenuBar(buatMenu());

        // Pengingat tenggat mulai jalan, memeriksa setiap 10 menit.
        pengingat = new PengingatService(this, () -> {
            try {
                return tugasRepo.semua();
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        });
        pengingat.mulai();

        refreshAll();
    }

    private JMenuBar buatMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem ekspor = new JMenuItem("Ekspor CSV...");
        ekspor.addActionListener(e -> eksporCsv());
        JMenuItem impor = new JMenuItem("Impor CSV...");
        impor.addActionListener(e -> imporCsv());
        JMenuItem keluar = new JMenuItem("Keluar");
        keluar.addActionListener(e -> {
            pengingat.berhenti();
            dispose();
        });
        menuFile.add(ekspor);
        menuFile.add(impor);
        menuFile.addSeparator();
        menuFile.add(keluar);

        JMenu menuKategori = new JMenu("Kategori");
        JMenuItem kelola = new JMenuItem("Kelola Kategori...");
        kelola.addActionListener(e -> {
            KategoriDialog dialog = new KategoriDialog(this);
            dialog.setVisible(true);
            refreshAll();
        });
        menuKategori.add(kelola);

        JMenu menuBantuan = new JMenu("Bantuan");
        JMenuItem tentang = new JMenuItem("Tentang");
        tentang.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Dompet Deadline 1.0\n\nAplikasi untuk mengatur uang dan tenggat tugas kuliah.\n"
                        + "Dibuat untuk Tugas 03 Praktikum PBO Kelas A.",
                "Tentang", JOptionPane.INFORMATION_MESSAGE));
        menuBantuan.add(tentang);

        menuBar.add(menuFile);
        menuBar.add(menuKategori);
        menuBar.add(menuBantuan);
        return menuBar;
    }

    /**
     * Muat ulang semua data dari database lewat SwingWorker supaya jendela
     * tidak membeku saat membaca database.
     */
    public void refreshAll() {
        new SwingWorker<DataSemua, Void>() {
            @Override
            protected DataSemua doInBackground() throws Exception {
                DataSemua data = new DataSemua();
                data.kategori = kategoriRepo.semua();
                data.tugas = tugasRepo.semua();
                data.transaksi = transaksiRepo.semua();
                return data;
            }

            @Override
            protected void done() {
                try {
                    DataSemua data = get();
                    dashboardPanel.tampilkan(data.transaksi, data.tugas);
                    transaksiPanel.tampilkan(data.transaksi, data.kategori, data.tugas);
                    tugasPanel.tampilkan(data.tugas, data.transaksi);
                    grafikPanel.tampilkan(data.transaksi);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Gagal memuat data: " + e.getMessage(),
                            "Galat", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void eksporCsv() {
        JFileChooser pilih = new JFileChooser();
        pilih.setDialogTitle("Simpan CSV");
        pilih.setFileFilter(new FileNameExtensionFilter("Berkas CSV", "csv"));
        pilih.setSelectedFile(new File("transaksi-dompet-deadline.csv"));
        if (pilih.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File tujuan = pilih.getSelectedFile();
        if (!tujuan.getName().toLowerCase().endsWith(".csv")) {
            tujuan = new File(tujuan.getAbsolutePath() + ".csv");
        }
        final File berkas = tujuan;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                csvService.ekspor(transaksiRepo.semua(), berkas);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Transaksi berhasil diekspor ke:\n" + berkas.getAbsolutePath(),
                            "Ekspor CSV", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Gagal mengekspor CSV: " + e.getMessage(),
                            "Galat", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void imporCsv() {
        JFileChooser pilih = new JFileChooser();
        pilih.setDialogTitle("Pilih berkas CSV");
        pilih.setFileFilter(new FileNameExtensionFilter("Berkas CSV", "csv"));
        if (pilih.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File sumber = pilih.getSelectedFile();
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                List<Transaksi> hasil = csvService.impor(sumber, kategoriRepo, tugasRepo);
                for (Transaksi t : hasil) {
                    transaksiRepo.simpan(t);
                }
                return hasil.size();
            }

            @Override
            protected void done() {
                try {
                    int jumlah = get();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            jumlah + " transaksi berhasil diimpor.",
                            "Impor CSV", JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                } catch (Exception e) {
                    String pesan = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Gagal mengimpor CSV: " + pesan,
                            "Galat", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public KategoriRepository getKategoriRepo() {
        return kategoriRepo;
    }

    public TugasRepository getTugasRepo() {
        return tugasRepo;
    }

    public TransaksiRepository getTransaksiRepo() {
        return transaksiRepo;
    }

    /** Wadah kecil untuk hasil baca database. */
    private static class DataSemua {

    private static final long serialVersionUID = 1L;
        List<Kategori> kategori;
        List<Tugas> tugas;
        List<Transaksi> transaksi;
    }
}
