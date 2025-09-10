package com.medscheduler.service;

import com.medscheduler.entity.Medicine;
import com.medscheduler.repository.MedicineRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class MedicineScheduler {

    private final MedicineRepository medicineRepository;
    private final EmailService emailService;

    public MedicineScheduler(MedicineRepository medicineRepository, EmailService emailService) {
        this.medicineRepository = medicineRepository;
        this.emailService = emailService;
    }

    // ✅ Runs every minute and checks for matches
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")
    public void checkReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // current time (HH:mm)
        System.out.println("⏰ Checking reminders at " + now);

        medicineRepository.findAll().forEach(m -> {
            // Morning reminder
            if (m.getMorningTime() != null && m.getMorningTime().equals(now)) {
                sendReminder(m, "Morning");
            }

            // Afternoon reminder
            if (m.getAfternoonTime() != null && m.getAfternoonTime().equals(now)) {
                sendReminder(m, "Afternoon");
            }

            // Night reminder
            if (m.getNightTime() != null && m.getNightTime().equals(now)) {
                sendReminder(m, "Night");
            }
        });
    }

    private void sendReminder(Medicine m, String timeLabel) {
        String reminderMsg = "Reminder: Take your medicine " + m.getName() +
                " (" + m.getQuantity() + " left) at " + timeLabel + ".";
        emailService.sendMail(
                m.getFamily().getEmail(),
                "Medicine Reminder - " + timeLabel,
                reminderMsg
        );
        System.out.println("📧 Sent " + timeLabel + " reminder to " + m.getFamily().getEmail());

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
}
