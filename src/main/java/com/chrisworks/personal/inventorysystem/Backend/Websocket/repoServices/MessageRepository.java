package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
