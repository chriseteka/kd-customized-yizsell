package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.RecentMessages;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;

import java.util.List;
import java.util.Map;

public interface UserUtils {

    UserMiniProfile createAccount();

    List<UserMiniProfile> fetchUsers();

    List<MessageDto> fetchAllMessagesTo(String email);

    List<MessageDto> fetchAllMessagesFrom(String email);

    boolean turnUserActiveOrInactive(String status);

    Map<String, List<MessageDto>> fetchRecentMessages(int page);
}
