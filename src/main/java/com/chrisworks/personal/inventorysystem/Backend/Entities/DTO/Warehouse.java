package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/26/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Warehouse implements Serializable {

    private Long warehouseId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String warehouseName;
    private String warehouseAddress;
    private String createdBy;

    public Warehouse(Long warehouseId, Date createdDate, Date createdTime, Date updateDate,
                     String warehouseName, String warehouseAddress, String createdBy) {
        this.warehouseId = warehouseId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.warehouseName = warehouseName;
        this.warehouseAddress = warehouseAddress;
        this.createdBy = createdBy;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse fromDTO(){
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse warehouseFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse();
        warehouseFromDTO.setWarehouseId(this.warehouseId);
        warehouseFromDTO.setCreatedDate(this.getCreatedDate());
        warehouseFromDTO.setCreatedTime(this.getCreatedTime());
        warehouseFromDTO.setUpdateDate(this.getUpdateDate());
        warehouseFromDTO.setWarehouseName(this.getWarehouseName());
        warehouseFromDTO.setWarehouseAddress(this.getWarehouseAddress());
        warehouseFromDTO.setCreatedBy(this.getCreatedBy());

        return warehouseFromDTO;
    }
}
