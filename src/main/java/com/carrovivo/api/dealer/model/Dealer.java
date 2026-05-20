package com.carrovivo.api.dealer.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dealer {
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Double servicePrice;
}