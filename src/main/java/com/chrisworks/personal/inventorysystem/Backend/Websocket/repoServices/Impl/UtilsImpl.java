package com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.Impl;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.Message;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.MessageDto;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.ONLINE_STATUS;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.MessageRepository;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.MessageUtils;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserMiniProfileRepository;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
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
    public boolean verifyEmail(String... args){

        if (args.length == 0) return false;

        for (String email: args){
            if (profileRepository.findDistinctByEmail(email) == null)
                return false;
        }

        return true;
    }

    @Override
    @Transactional
    public UserMiniProfile createAccount() {

        ACCOUNT_TYPE accountType = AuthenticatedUserDetails.getAccount_type();
        String userEmail = AuthenticatedUserDetails.getUserFullName();

        UserMiniProfile existingUser = profileRepository.findDistinctByEmail(userEmail);
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
            profile.setBusinessId(seller.getWarehouse().getBusinessOwner().getBusinessOwnerId());

            seller.setIsRegisteredForChat(true);
            if (sellerRepository.save(seller) == null) return null;
        }

        if (profile.getEmail().isEmpty() || profile.getBusinessId() < 0) return null;
        return profileRepository.save(profile);
    }

    @Override
    public List<UserMiniProfile> fetchUsers(){

        String userEmail = AuthenticatedUserDetails.getUserFullName();
        UserMiniProfile user = profileRepository.findDistinctByEmail(userEmail);

        if (user == null) return Collections.emptyList();

        return profileRepository.findAllByBusinessId(user.getBusinessId())
                .stream().filter(a -> !a.getEmail().equalsIgnoreCase(userEmail))
                .collect(Collectors.toList());
    }

    @Override
    public boolean turnUserActiveOrInactive(String status){

        ONLINE_STATUS online_status = ONLINE_STATUS.valueOf(status.toUpperCase());

        String email = AuthenticatedUserDetails.getUserFullName();
        UserMiniProfile user = profileRepository.findDistinctByEmail(email);

        if (user == null) return false;
        user.setStatus(online_status);

        return profileRepository.save(user) != null;
    }

    private Message fromDTOtoMessage(MessageDto messageDto){

        if (messageDto == null) return null;

        String from = messageDto.getFromEmail();
        String to = messageDto.getToEmail();

        UserMiniProfile userFrom = profileRepository.findDistinctByEmail(from);
        UserMiniProfile userTo = profileRepository.findDistinctByEmail(to);

        if (userFrom == null || userTo == null) return null;

        return new Message(userFrom, userTo, messageDto.getBody(), messageDto.getAttachment());
    }
}
