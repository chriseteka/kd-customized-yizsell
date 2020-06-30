package com.chrisworks.personal.inventorysystem.Backend.Websocket.models;

import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.enums.ONLINE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Chris_Eteka
 * @since 4/21/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "UserMiniProfiles")
public class UserMiniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    private String email;
    private String fullName;
    private String description;
    private ONLINE_STATUS status;
    private long businessId;

    public com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.UserMiniProfile toDTO(){
        return new com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.UserMiniProfile(this.getId(),
            this.getEmail(), this.getFullName(), this.getDescription(), this.getStatus(), this.getBusinessId());
    }
}
