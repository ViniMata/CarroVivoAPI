package com.carrovivo.api.dealer.repository;

import com.carrovivo.api.dealer.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DealerRepository extends JpaRepository<Dealer, Long> {
    List<Dealer> findByNameContainingIgnoreCase(String city);
}
