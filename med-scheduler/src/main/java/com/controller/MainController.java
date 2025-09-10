package com.medscheduler.controller;

import com.medscheduler.entity.Family;
import com.medscheduler.entity.Medicine;
import com.medscheduler.repository.FamilyRepository;
import com.medscheduler.repository.MedicineRepository;
import com.medscheduler.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@Controller
public class MainController {

    private final FamilyRepository familyRepository;
    private final MedicineRepository medicineRepository;
    private final EmailService emailService;

    public MainController(FamilyRepository familyRepository,
                          MedicineRepository medicineRepository,
                          EmailService emailService) {
        this.familyRepository = familyRepository;
        this.medicineRepository = medicineRepository;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("members", familyRepository.findAll());
        model.addAttribute("medicines", medicineRepository.findAll());
        return "index";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String tablet,
            @RequestParam int quantity,
            @RequestParam(required = false) String morningTime,
            @RequestParam(required = false) String afternoonTime,
            @RequestParam(required = false) String nightTime
    ) {
        // Save family
        Family family = new Family();
        family.setName(name);
        family.setEmail(email);
        familyRepository.save(family);

        // Save medicine
        Medicine med = new Medicine();
        med.setName(tablet);
        med.setQuantity(quantity);

        // ✅ Store time safely
        med.setMorningTime((morningTime != null && !morningTime.isBlank()) ? LocalTime.parse(morningTime) : null);
        med.setAfternoonTime((afternoonTime != null && !afternoonTime.isBlank()) ? LocalTime.parse(afternoonTime) : null);
        med.setNightTime((nightTime != null && !nightTime.isBlank()) ? LocalTime.parse(nightTime) : null);

        med.setFamily(family);
        medicineRepository.save(med);

        return "redirect:/";
    }

    @PostMapping("/update")
    public String update(
            @RequestParam Long id,
            @RequestParam String name,         // medicine name
            @RequestParam int quantity,
            @RequestParam(required = false) String familyName,
            @RequestParam(required = false) String familyEmail,
            @RequestParam(required = false) String morningTime,
            @RequestParam(required = false) String afternoonTime,
            @RequestParam(required = false) String nightTime
    ) {
        Medicine med = medicineRepository.findById(id).orElse(null);
        if (med != null) {
            // ✅ Update medicine fields
            med.setName(name);
            med.setQuantity(quantity);

            med.setMorningTime((morningTime != null && !morningTime.isBlank()) ? LocalTime.parse(morningTime) : null);
            med.setAfternoonTime((afternoonTime != null && !afternoonTime.isBlank()) ? LocalTime.parse(afternoonTime) : null);
            med.setNightTime((nightTime != null && !nightTime.isBlank()) ? LocalTime.parse(nightTime) : null);

            // ✅ Update family only if provided
            Family family = med.getFamily();
            if (family != null) {
                if (familyName != null && !familyName.isBlank()) {
                    family.setName(familyName);
                }
                if (familyEmail != null && !familyEmail.isBlank()) {
                    family.setEmail(familyEmail);
                }
                familyRepository.save(family);
            }

            medicineRepository.save(med);
        }
        return "redirect:/";
    }

    @GetMapping("/medicines/{familyId}")
    @ResponseBody
    public List<Medicine> getMedicinesByFamily(@PathVariable Long familyId) {
        return familyRepository.findById(familyId)
                .map(medicineRepository::findByFamily)
                .orElse(List.of());
    }

    @PostMapping("/restock")
    public String restock(
            @RequestParam Long medicineId,
            @RequestParam int addQty
    ) {
        Medicine med = medicineRepository.findById(medicineId).orElse(null);
        if (med != null) {
            int oldQty = med.getQuantity();
            med.setQuantity(oldQty + addQty);
            medicineRepository.save(med);

            // 📧 Send restock notification
            String message = "Hello " + med.getFamily().getName() + ",\n\n" +
                    "✅ Your medicine **" + med.getName() + "** has been restocked.\n" +
                    "Previous Quantity: " + oldQty + "\n" +
                    "Added: " + addQty + "\n" +
                    "New Quantity: " + med.getQuantity() + "\n\n" +
                    "Stay healthy!\n- MedScheduler";

            emailService.sendMail(
                    med.getFamily().getEmail(),
                    "Medicine Restocked - " + med.getName(),
                    message
            );
        }
        return "redirect:/";
    }

    // ✅ NEW: Delete a family and all its medicines
    @PostMapping("/delete-family")
    public String deleteFamily(@RequestParam Long familyId) {
        familyRepository.findById(familyId).ifPresent(family -> {
            List<Medicine> medicines = medicineRepository.findByFamily(family);
            medicineRepository.deleteAll(medicines);
            familyRepository.delete(family);
        });
        return "redirect:/";
    }

    @GetMapping("/test-mail")
    @ResponseBody
    public String testMail() {
        emailService.sendMail(
                "vamsisunny51@gmail.com",
                "Test Mail",
                "This is a test message from MedScheduler"
        );
        return "Email sent (check your inbox)";
    }
}
