package dompetdeadline.view;

import dompetdeadline.model.Transaksi;
import dompetdeadline.service.DompetService;
import dompetdeadline.service.FormatUang;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tab grafik: pengeluaran per kategori pada bulan yang dipilih. */
public class GrafikPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DompetService service = new DompetService();
    private final JComboBox<YearMonth> pilihBulan = new JComboBox<>();
    private final PanelGrafik panelGrafik = new PanelGrafik();

    private List<Transaksi> transaksi = new ArrayList<>();

    public GrafikPanel() {
        setLayout(new BorderLayout(8, 8));
        DateTimeFormatter formatBulan = DateTimeFormatter.ofPattern("MMMM yyyy");
        pilihBulan.setRenderer((daftar, nilai, index, dipilih, fokus) -> {
            JLabel label = new JLabel(nilai == null ? "" : nilai.format(formatBulan));
            if (dipilih) {
                label.setOpaque(true);
                label.setBackground(daftar.getSelectionBackground());
                label.setForeground(daftar.getSelectionForeground());
            }
            return label;
        });

        YearMonth sekarang = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            pilihBulan.addItem(sekarang.minusMonths(i));
        }
        pilihBulan.setSelectedItem(sekarang);
        pilihBulan.addActionListener(e -> gambarUlang());

        JPanel atas = new JPanel();
        atas.add(new JLabel("Bulan:"));
        atas.add(pilihBulan);
        add(atas, BorderLayout.NORTH);
        add(panelGrafik, BorderLayout.CENTER);
    }

    /** Tampilkan grafik dari data terbaru. */
    public void tampilkan(List<Transaksi> transaksi) {
        this.transaksi = transaksi;
        gambarUlang();
    }

    private void gambarUlang() {
        YearMonth bulan = (YearMonth) pilihBulan.getSelectedItem();
        if (bulan == null) {
            return;
        }
        Map<String, Long> data = service.pengeluaranPerKategori(transaksi, bulan);
        panelGrafik.setData(data);
    }

    /** Panel yang menggambar sendiri grafik batang lewat paintComponent. */
    private static class PanelGrafik extends JPanel {

    private static final long serialVersionUID = 1L;
        private Map<String, Long> data = new LinkedHashMap<>();

        void setData(Map<String, Long> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int lebar = getWidth();
            int tinggi = getHeight();
            int kiri = 90;
            int bawah = 50;
            int atas = 30;
            int areaLebar = lebar - kiri - 20;
            int areaTinggi = tinggi - atas - bawah;

            FontMetrics fm = g.getFontMetrics();

            if (data.isEmpty()) {
                String pesan = "Belum ada pengeluaran pada bulan ini.";
                g.drawString(pesan, kiri, atas + 20);
                return;
            }

            long terbesar = 0;
            for (long nilai : data.values()) {
                if (nilai > terbesar) {
                    terbesar = nilai;
                }
            }

            // Garis sumbu dan label nilai di sumbu kiri.
            g.setColor(Color.DARK_GRAY);
            g.drawLine(kiri, atas, kiri, atas + areaTinggi);
            g.drawLine(kiri, atas + areaTinggi, kiri + areaLebar, atas + areaTinggi);
            int langkah = 4;
            for (int i = 0; i <= langkah; i++) {
                long nilai = terbesar * i / langkah;
                int y = atas + areaTinggi - (int) (areaTinggi * (double) i / langkah);
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(kiri, y, kiri + areaLebar, y);
                g.setColor(Color.DARK_GRAY);
                String label = FormatUang.format(nilai);
                g.drawString(label, kiri - fm.stringWidth(label) - 8, y + fm.getAscent() / 2);
            }

            // Batang dan label kategori.
            int jumlah = data.size();
            int lebarSlot = areaLebar / jumlah;
            int lebarBatang = Math.min(70, lebarSlot - 20);
            int index = 0;
            Color[] warna = {
                    new Color(41, 128, 185), new Color(39, 174, 96),
                    new Color(231, 76, 60), new Color(243, 156, 18),
                    new Color(142, 68, 173), new Color(22, 160, 133)
            };
            for (Map.Entry<String, Long> entri : data.entrySet()) {
                int x = kiri + index * lebarSlot + (lebarSlot - lebarBatang) / 2;
                int tinggiBatang = (int) (areaTinggi * (double) entri.getValue() / terbesar);
                int y = atas + areaTinggi - tinggiBatang;
                g.setColor(warna[index % warna.length]);
                g.fillRect(x, y, lebarBatang, tinggiBatang);
                g.setColor(Color.DARK_GRAY);
                String nilai = FormatUang.format(entri.getValue());
                g.drawString(nilai, x + (lebarBatang - fm.stringWidth(nilai)) / 2, y - 6);
                String nama = entri.getKey();
                if (fm.stringWidth(nama) > lebarSlot - 4) {
                    nama = nama.substring(0, Math.max(1, (lebarSlot - 10) / fm.charWidth('m'))) + ".";
                }
                g.drawString(nama, x + (lebarBatang - fm.stringWidth(nama)) / 2,
                        atas + areaTinggi + 20);
                index++;
            }
        }
    }
}
