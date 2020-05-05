package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.Impl;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.RecentMessages;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.MESSAGE_FLOW;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.MessageStatus;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.ONLINE_STATUS;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.MessageRepository;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.MessageUtils;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserMiniProfileRepository;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UtilsImpl implements UserUtils, MessageUtils {

    private final MessageRepository messageRepository;
    private final UserMiniProfileRepository profileRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final SellerRepository sellerRepository;

    @Override
    public Message persistMessage(MessageDto messageDto){

        return messageRepository.save(fromDTOtoMessage(messageDto));
    }

    @Override
    public MessageDto fromMessageToDTO(Message message){

        if (message == null) return null;
        return new MessageDto(message.getFrom().getEmail(), message.getTo().getEmail(),
                message.getBody(), message.getAttachment(), message.getDateSent(),
                message.getTimeSent(), message.getStatus());
    }

    @Override
    public List<MessageDto> fetchAllMessagesTo(String email) {

        if (email == null || email.isEmpty()) return Collections.emptyList();

        UserMiniProfile miniProfile = fetchUserByEmail(email);
        if(miniProfile == null) return Collections.emptyList();

        List<Message> messagesTo = messageRepository.findAllByTo(miniProfile);

        return messagesTo.stream().map(this::fromMessageToDTO).collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> fetchAllMessagesFrom(String email) {

        if (email == null || email.isEmpty()) return Collections.emptyList();

        UserMiniProfile miniProfile = fetchUserByEmail(email);
        if(miniProfile == null) return Collections.emptyList();

        List<Message> messagesFrom = messageRepository.findAllByFrom(miniProfile);

        return messagesFrom.stream().map(this::fromMessageToDTO).collect(Collectors.toList());
    }

    @Override
    public boolean verifyEmail(List<String> emails){

        if (emails.size() == 0) return false;

        for (String email: emails){
            if (fetchUserByEmail(email) == null)
                return false;
        }

        return true;
    }

    @Override
    public List<MessageDto> notifyWarehouseAttendants(Warehouse warehouse, String notice) {

        if (warehouse == null || notice == null || notice.isEmpty()) return Collections.emptyList();

        return sellerRepository.findAllByWarehouse(warehouse)
                .stream()
                .map(attendant ->
                    prepareMessageDTO(attendant.getSellerEmail(), notice)
                ).collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> notifyUser(String notice, String... emails) {

        if (emails == null || emails.length <= 0 || notice == null || notice.isEmpty())
            return null;

        return Arrays.stream(emails)
                .map(email -> prepareMessageDTO(email, notice))
                .collect(Collectors.toList());
    }

    @Override
    public String getBusinessManagerMail() {

        Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

        if (seller == null) return null;

        return seller.getCreatedBy();
    }

    @Override
    @Transactional
    public UserMiniProfile createAccount() {

        ACCOUNT_TYPE accountType = AuthenticatedUserDetails.getAccount_type();
        String userEmail = AuthenticatedUserDetails.getUserFullName();

        UserMiniProfile existingUser = fetchUserByEmail(userEmail);
        if (existingUser != null) return existingUser;

        if (accountType == null) return null;
        UserMiniProfile profile = new UserMiniProfile();

        if (accountType.equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            BusinessOwner businessOwner = businessOwnerRepository.findDistinctByBusinessOwnerEmail(userEmail);
            if (businessOwner == null) return null;

            String description = "Business Owner to: " + businessOwner.getBusinessName();
            profile.setEmail(userEmail);
            profile.setDescription(description);
            profile.setStatus(ONLINE_STATUS.ONLINE);
            profile.setBusinessId(businessOwner.getBusinessOwnerId());
            profile.setFullName(businessOwner.getBusinessOwnerFullName());

            businessOwner.setIsRegisteredForChat(true);
            if (businessOwnerRepository.save(businessOwner) == null) return null;
        }else {

            Seller seller = sellerRepository.findDistinctBySellerEmail(userEmail);
            if (seller == null) return null;

            String description = accountType.toString();
            profile.setEmail(userEmail);
            profile.setDescription(description);
            profile.setStatus(ONLINE_STATUS.ONLINE);
            profile.setFullName(seller.getSellerFullName());
            if (accountType.equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
                profile.setBusinessId(seller.getWarehouse().getBusinessOwner().getBusinessOwnerId());
            else profile.setBusinessId(seller.getShop().getBusinessOwner().getBusinessOwnerId());

            seller.setIsRegisteredForChat(true);
            if (sellerRepository.save(seller) == null) return null;
        }

        if (profile.getEmail().isEmpty() || profile.getBusinessId() < 0) return null;
        return profileRepository.save(profile);
    }

    @Override
    public List<UserMiniProfile> fetchUsers(){

        String userEmail = AuthenticatedUserDetails.getUserFullName();
        UserMiniProfile user = fetchUserByEmail(userEmail);

        if (user == null) return Collections.emptyList();

        return profileRepository.findAllByBusinessId(user.getBusinessId())
                .stream().filter(a -> !a.getEmail().equalsIgnoreCase(userEmail))
                .collect(Collectors.toList());
    }

    @Override
    public boolean turnUserActiveOrInactive(String status){

        ONLINE_STATUS online_status = ONLINE_STATUS.valueOf(status.toUpperCase());

        String email = AuthenticatedUserDetails.getUserFullName();
        UserMiniProfile user = fetchUserByEmail(email);

        if (user == null) return false;
        user.setStatus(online_status);

        return profileRepository.save(user) != null;
    }

    @Override
    public Map<String, List<RecentMessages>> fetchRecentMessages(int page) {

        String authUserEmail = AuthenticatedUserDetails.getUserFullName();
        UserMiniProfile authUser = fetchUserByEmail(authUserEmail);
        if (authUser == null) return Collections.emptyMap();

        List<UserMiniProfile> authUserColleagues = fetchUsers();
        if (authUserColleagues.isEmpty()) return Collections.emptyMap();

        List<String> authUserColleaguesMails = authUserColleagues.stream()
                .map(UserMiniProfile::getEmail).collect(Collectors.toList());

        List<Message> authUserOutwardMessages = new ArrayList<>();
        List<Message> authUserInwardMessages = new ArrayList<>();

        authUserColleagues
            .forEach(colleague -> {
                authUserOutwardMessages.addAll(messageRepository.findAllByFromAndTo(authUser, colleague));
                authUserInwardMessages.addAll(messageRepository.findAllByFromAndTo(colleague, authUser));
            });
        if (authUserOutwardMessages.isEmpty() && authUserInwardMessages.isEmpty()) return Collections.emptyMap();

        Map<String, List<RecentMessages>> recentMessages = new HashMap<>();
        List<RecentMessages> messages = new ArrayList<>();

        authUserColleaguesMails
            .forEach(colleagueMail -> {

                messages.addAll(authUserOutwardMessages.stream()
                        .filter(message -> message.getTo().getEmail().equalsIgnoreCase(colleagueMail))
                        .map(this::fromMessageToRecentTo).collect(Collectors.toList()));
                messages.addAll(authUserInwardMessages.stream()
                        .filter(message -> message.getFrom().getEmail().equalsIgnoreCase(colleagueMail))
                        .map(this::fromMessageToRecentFrom).collect(Collectors.toList()));
                //Sorting by date and then by time and then return the first 20 items
                messages.sort(Comparator.comparing(RecentMessages::getDateSent));
                messages.sort(Comparator.comparing(RecentMessages::getTimeSent));

                recentMessages.put(colleagueMail, messages.subList(page - 1, 20 * page));
            });

        return recentMessages;
    }

    private Message fromDTOtoMessage(MessageDto messageDto){

        if (messageDto == null) return null;

        String from = messageDto.getFromEmail();
        String to = messageDto.getToEmail();

        UserMiniProfile userFrom = fetchUserByEmail(from);
        UserMiniProfile userTo = fetchUserByEmail(to);

        if (userFrom == null || userTo == null) return null;

        return new Message(userFrom, userTo, messageDto.getBody(), messageDto.getAttachment());
    }

    private UserMiniProfile fetchUserByEmail(String email) {
        return profileRepository.findDistinctByEmail(email);
    }

    private MessageDto prepareMessageDTO(String email, String notice){

        MessageDto messageDto = new MessageDto();
        messageDto.setBody(notice);
        messageDto.setToEmail(email);
        messageDto.setStatus(MessageStatus.SENT);
        messageDto.setFromEmail(AuthenticatedUserDetails.getUserFullName());

        return messageDto;

    }

    private RecentMessages fromMessageToRecentTo(Message message){
        return new RecentMessages(message.getBody(), message.getAttachment(),
                message.getDateSent(), message.getTimeSent(), MESSAGE_FLOW.TO);
    }

    private RecentMessages fromMessageToRecentFrom(Message message){
        return new RecentMessages(message.getBody(), message.getAttachment(),
                message.getDateSent(), message.getTimeSent(), MESSAGE_FLOW.FROM);
    }
}
