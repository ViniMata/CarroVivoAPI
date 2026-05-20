package com.carrovivo.api.dealer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerDTO {
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Double servicePrice;
}