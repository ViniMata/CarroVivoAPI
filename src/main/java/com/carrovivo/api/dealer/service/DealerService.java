package com.carrovivo.api.dealer.service;

import com.carrovivo.api.dealer.dto.DealerDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DealerService {

    public List<DealerDTO> findNearby(String city) {
        return List.of(
                DealerDTO.builder()
                        .name("Ford Center " + city)
                        .address("Av. Paulista, 1000 - " + city)
                        .phone("(11) 3000-1000")
                        .latitude(-23.5613)
                        .longitude(-46.6558)
                        .distanceKm(2.4)
                        .servicePrice(350.00)
                        .build(),
                DealerDTO.builder()
                        .name("Ford Store " + city + " Sul")
                        .address("Rua das Flores, 500 - " + city)
                        .phone("(11) 3000-2000")
                        .latitude(-23.5900)
                        .longitude(-46.6800)
                        .distanceKm(5.1)
                        .servicePrice(320.00)
                        .build(),
                DealerDTO.builder()
                        .name("Ford Premium " + city)
                        .address("Av. Brasil, 2500 - " + city)
                        .phone("(11) 3000-3000")
                        .latitude(-23.5200)
                        .longitude(-46.6100)
                        .distanceKm(8.7)
                        .servicePrice(380.00)
                        .build()
        );
    }

    public List<DealerDTO> getPrices(Long dealerId) {
        return List.of(
                DealerDTO.builder()
                        .name("Troca de óleo")
                        .servicePrice(180.00)
                        .build(),
                DealerDTO.builder()
                        .name("Alinhamento e balanceamento")
                        .servicePrice(120.00)
                        .build(),
                DealerDTO.builder()
                        .name("Revisão completa")
                        .servicePrice(650.00)
                        .build()
        );
    }

    public List<DealerDTO> getSchedule(Long dealerId) {
        return List.of(
                DealerDTO.builder()
                        .name("Segunda-feira 09:00")
                        .servicePrice(null)
                        .build(),
                DealerDTO.builder()
                        .name("Terça-feira 14:00")
                        .servicePrice(null)
                        .build(),
                DealerDTO.builder()
                        .name("Quinta-feira 10:00")
                        .servicePrice(null)
                        .build()
        );
    }
}