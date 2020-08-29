package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 8/1/2020
 * @email chriseteka@gmail.com
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @Column(name = "planName")
    private String planName;

    @Column(name = "numberOfStaff")
    private int numberOfStaff;

    @Column(name = "numberOfWarehouses")
    private int numberOfWarehouses;

    @Column(name = "numberOfShops")
    private int numberOfShops;

    @JsonManagedReference
    @OneToMany(mappedBy = "plan", targetEntity = BusinessOwner.class, cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<BusinessOwner> businesses = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plan)) return false;

        Plan plan = (Plan) o;

        if (getNumberOfStaff() != plan.getNumberOfStaff()) return false;
        if (getNumberOfWarehouses() != plan.getNumberOfWarehouses()) return false;
        if (getNumberOfShops() != plan.getNumberOfShops()) return false;
        if (getPlanId() != null ? !getPlanId().equals(plan.getPlanId()) : plan.getPlanId() != null) return false;
        if (getCreatedDate() != null ? !getCreatedDate().equals(plan.getCreatedDate()) : plan.getCreatedDate() != null)
            return false;
        if (getCreatedTime() != null ? !getCreatedTime().equals(plan.getCreatedTime()) : plan.getCreatedTime() != null)
            return false;
        if (getUpdateDate() != null ? !getUpdateDate().equals(plan.getUpdateDate()) : plan.getUpdateDate() != null)
            return false;
        return getPlanName() != null ? getPlanName().equals(plan.getPlanName()) : plan.getPlanName() == null;
    }

    @Override
    public int hashCode() {
        int result = getPlanId() != null ? getPlanId().hashCode() : 0;
        result = 31 * result + (getCreatedDate() != null ? getCreatedDate().hashCode() : 0);
        result = 31 * result + (getCreatedTime() != null ? getCreatedTime().hashCode() : 0);
        result = 31 * result + (getUpdateDate() != null ? getUpdateDate().hashCode() : 0);
        result = 31 * result + (getPlanName() != null ? getPlanName().hashCode() : 0);
        result = 31 * result + getNumberOfStaff();
        result = 31 * result + getNumberOfWarehouses();
        result = 31 * result + getNumberOfShops();
        return result;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "planId=" + planId +
                ", createdDate=" + createdDate +
                ", createdTime=" + createdTime +
                ", updateDate=" + updateDate +
                ", planName='" + planName + '\'' +
                ", numberOfStaff=" + numberOfStaff +
                ", numberOfWarehouses=" + numberOfWarehouses +
                ", numberOfShops=" + numberOfShops +
                '}';
    }

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }
}
