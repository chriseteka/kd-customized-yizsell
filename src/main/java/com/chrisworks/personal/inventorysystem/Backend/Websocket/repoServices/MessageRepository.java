package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByTo(UserMiniProfile messagesTo);

    List<Message> findAllByFrom(UserMiniProfile messagesFrom);

    List<Message> findAllByFromAndTo(UserMiniProfile from, UserMiniProfile to);
}
