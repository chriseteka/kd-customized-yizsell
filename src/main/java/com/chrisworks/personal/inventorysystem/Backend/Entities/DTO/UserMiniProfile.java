package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.ONLINE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Chris_Eteka
 * @since 6/30/2020
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
public class UserMiniProfile implements Serializable {

    private long id;
    private String email;
    private String fullName;
    private String description;
    private ONLINE_STATUS status;
    private long businessId;

    public com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile fromDTO(){
        return new com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile(this.getId(),
            this.getEmail(), this.getFullName(), this.getDescription(), this.getStatus(), this.businessId);
    }
}
