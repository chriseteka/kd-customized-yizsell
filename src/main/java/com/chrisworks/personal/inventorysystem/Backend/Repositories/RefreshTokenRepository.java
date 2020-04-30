package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Chris_Eteka
 * @since 4/30/2020
 * @email chriseteka@gmail.com
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findDistinctByToken(String token);
    RefreshToken findDistinctByUserEmail(String token);
}
