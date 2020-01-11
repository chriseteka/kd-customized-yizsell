package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.SummaryReports;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryReportsRepository extends JpaRepository<SummaryReports, Long> {
}
