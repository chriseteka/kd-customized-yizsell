package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.ONLINE_STATUS;

import java.util.List;

public interface UserUtils {

    UserMiniProfile createAccount();

    List<UserMiniProfile> fetchUsers();

    boolean turnUserActiveOrInactive(String status);
}
