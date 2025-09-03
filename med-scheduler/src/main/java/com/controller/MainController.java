package com.medscheduler.controller;

import com.medscheduler.entity.Family;
import com.medscheduler.entity.Medicine;
import com.medscheduler.repository.FamilyRepository;
import com.medscheduler.repository.MedicineRepository;
import com.medscheduler.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("families", familyRepository.findAll());
        model.addAttribute("medicines", medicineRepository.findAll());
        return "index";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String tablet,
            @RequestParam int quantity,
            @RequestParam(required = false) String[] times
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
        med.setSchedule(times != null && times.length > 0 ? String.join(",", times) : "");
        med.setFamily(family);
        medicineRepository.save(med);

        return "redirect:/";
    }

    // ✅ Fetch medicines by family (for restock UI dropdown)
    @GetMapping("/medicines/{familyId}")
    @ResponseBody
    public List<Medicine> getMedicinesByFamily(@PathVariable Long familyId) {
        Family family = familyRepository.findById(familyId).orElse(null);
        if (family != null) {
            return medicineRepository.findByFamily(family);
        }
        return List.of();
    }

    // ✅ Restock medicine (adds quantity instead of replacing)
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

    // ✅ Test mail endpoint
    @GetMapping("/test-mail")
    @ResponseBody
    public String testMail() {
        emailService.sendMail(
                "vamsisunny51@gmail.com", // change to your test email
                "Test Mail",
                "This is a test message from MedScheduler"
        );
        return "Email sent (check your inbox)";
    }
}
