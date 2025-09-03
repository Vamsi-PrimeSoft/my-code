package com.medscheduler.service;

import com.medscheduler.entity.Medicine;
import com.medscheduler.repository.MedicineRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MedicineScheduler {

    private final MedicineRepository medicineRepository;
    private final EmailService emailService;

    public MedicineScheduler(MedicineRepository medicineRepository, EmailService emailService) {
        this.medicineRepository = medicineRepository;
        this.emailService = emailService;
    }

    // 🔔 Morning Reminder at 8 AM IST
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    public void morningReminder() {
        sendReminder("Morning", "8 AM");
    }

    // 🔔 Afternoon Reminder at 2 PM IST
    @Scheduled(cron = "0 02 14 * * *", zone = "Asia/Kolkata")
    public void afternoonReminder() {
        sendReminder("Afternoon", "2 PM");
    }

    // 🔔 Night Reminder at 8 PM IST
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Kolkata")
    public void nightReminder() {
        sendReminder("Night", "8 PM");
    }

    private void sendReminder(String time, String actualTime) {
        medicineRepository.findAll().forEach(m -> {
            if (m.getSchedule() != null && m.getSchedule().contains(time)) {

                // Reminder mail
                String reminderMsg = "Reminder: Take your medicine " + m.getName() +
                        " (" + m.getQuantity() + " left) at " + actualTime + ".";
                emailService.sendMail(
                        m.getFamily().getEmail(),
                        "Medicine Reminder - " + time,
                        reminderMsg
                );
                System.out.println("📧 Sent " + time + " reminder to " + m.getFamily().getEmail());

                // Reduce medicine quantity by 1
                if (m.getQuantity() > 0) {
                    m.setQuantity(m.getQuantity() - 1);
                    medicineRepository.save(m);
                    System.out.println("💊 Updated quantity of " + m.getName() + " to " + m.getQuantity());
                }

                // Low stock alert (if quantity < 5 after decrement)
                if (m.getQuantity() < 5) {
                    String lowStockMsg = "⚠️ Low Stock Alert: Only " + m.getQuantity() +
                            " tablets of " + m.getName() + " left. Please restock soon.";
                    emailService.sendMail(
                            m.getFamily().getEmail(),
                            "Low Stock Alert - " + m.getName(),
                            lowStockMsg
                    );
                    System.out.println("⚠️ Sent low stock alert to " + m.getFamily().getEmail());
                }
            }
        });
    }
}
