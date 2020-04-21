package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMiniProfileRepository extends JpaRepository<UserMiniProfile, Long> {

    UserMiniProfile findDistinctByEmail(String email);

    List<UserMiniProfile> findAllByBusinessId(long businessId);
}
