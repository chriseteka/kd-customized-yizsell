package com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/WS")
public class UserController {

    private final UserUtils userUtils;

    @GetMapping(path = "/register")
    public ResponseEntity<?> registerUser(){

        UserMiniProfile account = userUtils.createAccount();
        if (account == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(account);
    }

    @GetMapping(path = "/users")
    public ResponseEntity<?> fetchAllUsers(){

        return ResponseEntity.ok(userUtils.fetchUsers());
    }

    @PutMapping(path = "/updateStatus")
    public ResponseEntity<?> updateOnlineStatus(@RequestParam String status){
        return ResponseEntity.ok(userUtils.turnUserActiveOrInactive(status));
    }

    @GetMapping(path = "/messages/to")
    public ResponseEntity<?> fetchMessagesTo(@RequestParam String email){
        return ResponseEntity.ok(userUtils.fetchAllMessagesTo(email));
    }

    @GetMapping(path = "/messages/from")
    public ResponseEntity<?> fetchMessagesFrom(@RequestParam String email){
        return ResponseEntity.ok(userUtils.fetchAllMessagesFrom(email));
    }

    @GetMapping(path = "/recent/messages")
    public ResponseEntity<?> fetchRecentMessages(@RequestParam int page){
        return ResponseEntity.ok(userUtils.fetchRecentMessages(page));
    }
}
