package com.chrisworks.personal.inventorysystem.Backend.Configurations.Interceptors.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class URIData {

    private STATUS status = STATUS.PENDING;
    private int responseStatus = 0;
    private LocalDateTime expirationTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0));
}
