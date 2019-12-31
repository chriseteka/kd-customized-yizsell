package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.VerificationToken;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findDistinctByBusinessOwner(BusinessOwner businessOwner);
}
