package com.carrovivo.api.dealer.service;

import com.carrovivo.api.dealer.dto.DealerDTO;
import com.carrovivo.api.dealer.model.Dealer;
import com.carrovivo.api.dealer.repository.DealerRepository;
import com.carrovivo.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository repository;

    public List<DealerDTO> findNearby(String city) {
        return repository.findByNameContainingIgnoreCase(city)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<DealerDTO> getPrices(Long dealerId) {
        Dealer dealer = repository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Concessionária não encontrada"));
        return List.of(toDTO(dealer));
    }

    public List<DealerDTO> getSchedule(Long dealerId) {
        Dealer dealer = repository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Concessionária não encontrada"));
        return List.of(toDTO(dealer));
    }

    private DealerDTO toDTO(Dealer d) {
        return DealerDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .address(d.getAddress())
                .phone(d.getPhone())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .distanceKm(d.getDistanceKm())
                .servicePrice(d.getServicePrice())
                .createdAt(d.getCreatedAt())
                .build();
    }
}