package dompetdeadline.service;

import dompetdeadline.model.Tugas;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

/**
 * Mengingatkan tenggat yang sudah dekat. Timer memeriksa berkala, dan setiap
 * tugas hanya diingatkan sekali selama aplikasi berjalan, supaya tidak muncul
 * berulang terus menerus.
 */
public class PengingatService {

    private static final long JEDA_CEK_MENIT = 10;

    private final JFrame induk;
    private final DompetService service = new DompetService();
    private final Supplier<List<Tugas>> penyediaTugas;
    private final Set<Long> sudahDiingatkan = new HashSet<>();
    private TrayIcon ikonTray;
    private Timer timer;

    public PengingatService(JFrame induk, Supplier<List<Tugas>> penyediaTugas) {
        this.induk = induk;
        this.penyediaTugas = penyediaTugas;
    }

    /** Pasang ikon tray kalau didukung, lalu mulai pemeriksaan berkala. */
    public void mulai() {
        siapkanTray();
        cekSekarang();
        timer = new Timer("pengingat-tenggat", true);
        long jeda = JEDA_CEK_MENIT * 60 * 1000;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cekSekarang();
            }
        }, jeda, jeda);
    }

    public void berhenti() {
        if (timer != null) {
            timer.cancel();
        }
        if (ikonTray != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(ikonTray);
        }
    }

    /** Satu kali pemeriksaan: ingatkan semua tugas mendesak yang belum pernah diingatkan. */
    public void cekSekarang() {
        List<Tugas> semua = penyediaTugas.get();
        if (semua == null) {
            return;
        }
        List<Tugas> mendesak = service.tugasMendesak(semua, LocalDate.now());
        for (Tugas tugas : mendesak) {
            if (sudahDiingatkan.contains(tugas.getId())) {
                continue;
            }
            sudahDiingatkan.add(tugas.getId());
            String pesan = "Tugas \"" + tugas.getJudul() + "\" (" + tugas.getMataKuliah()
                    + ") tenggat " + tugas.getTenggat() + ".";
            tampilkan(pesan);
        }
    }

    private void tampilkan(String pesan) {
        if (ikonTray != null) {
            ikonTray.displayMessage("Dompet Deadline: tenggat dekat", pesan,
                    TrayIcon.MessageType.WARNING);
        } else {
            javax.swing.SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(induk, pesan,
                            "Dompet Deadline: tenggat dekat",
                            JOptionPane.WARNING_MESSAGE));
        }
    }

    private void siapkanTray() {
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            Image gambar = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            ikonTray = new TrayIcon(gambar, "Dompet Deadline");
            ikonTray.setImageAutoSize(true);
            SystemTray.getSystemTray().add(ikonTray);
        } catch (AWTException e) {
            ikonTray = null;
        }
    }
}
