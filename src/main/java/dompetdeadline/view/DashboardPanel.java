package dompetdeadline.view;

import dompetdeadline.model.Transaksi;
import dompetdeadline.model.Tugas;
import dompetdeadline.service.DompetService;
import dompetdeadline.service.FormatUang;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Tab dashboard: sisa uang bulan ini, tenggat terdekat, dan biaya tugas aktif. */
public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final DompetService service = new DompetService();
    private final JLabel labelSisaUang = new JLabel("-");
    private final JLabel labelJumlahTugas = new JLabel("-");
    private final JLabel labelBiayaAktif = new JLabel("-");
    private final JTextArea areaTenggat = new JTextArea();

    public DashboardPanel() {
        setLayout(new GridLayout(1, 3, 12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buatKartu("Sisa Uang Bulan Ini", labelSisaUang));
        add(buatKartu("Tugas Aktif", labelJumlahTugas, labelBiayaAktif));

        JPanel kartuTenggat = new JPanel(new BorderLayout());
        kartuTenggat.setBorder(BorderFactory.createTitledBorder("3 Tenggat Terdekat"));
        areaTenggat.setEditable(false);
        areaTenggat.setLineWrap(true);
        areaTenggat.setWrapStyleWord(true);
        areaTenggat.setBackground(kartuTenggat.getBackground());
        kartuTenggat.add(areaTenggat, BorderLayout.CENTER);
        add(kartuTenggat);
    }

    private JPanel buatKartu(String judul, JLabel... isi) {
        JPanel kartu = new JPanel(new BorderLayout());
        kartu.setBorder(BorderFactory.createTitledBorder(judul));
        JPanel tengah = new JPanel(new GridLayout(isi.length, 1, 0, 6));
        for (JLabel label : isi) {
            label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
            tengah.add(label);
        }
        kartu.add(tengah, BorderLayout.NORTH);
        return kartu;
    }

    /** Isi ulang dashboard dari data terbaru. */
    public void tampilkan(List<Transaksi> transaksi, List<Tugas> tugas) {
        YearMonth bulanIni = YearMonth.now();
        long sisa = service.hitungSisaUang(transaksi, bulanIni);
        labelSisaUang.setText(FormatUang.format(sisa));

        long aktif = tugas.stream().filter(t -> !t.isSelesai()).count();
        labelJumlahTugas.setText(aktif + " tugas belum selesai");
        long biayaAktif = service.totalBiayaTugasAktif(tugas, transaksi);
        labelBiayaAktif.setText("Total biaya: " + FormatUang.format(biayaAktif));

        List<Tugas> dekat = service.tigaTenggatTerdekat(tugas, LocalDate.now());
        if (dekat.isEmpty()) {
            areaTenggat.setText("Tidak ada tenggat yang akan datang.");
        } else {
            StringBuilder teks = new StringBuilder();
            for (Tugas t : dekat) {
                teks.append("- ").append(t.getJudul())
                        .append(" (").append(t.getMataKuliah()).append(")\n  ")
                        .append(t.getTenggat().format(TANGGAL))
                        .append(" - ").append(t.getPrioritas().getLabel()).append("\n");
            }
            areaTenggat.setText(teks.toString());
        }
    }
}
