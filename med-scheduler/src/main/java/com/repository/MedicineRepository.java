package com.medscheduler.repository;

import com.medscheduler.entity.Family;
import com.medscheduler.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // ✅ Fetch medicines by family
    List<Medicine> findByFamily(Family family);
}
