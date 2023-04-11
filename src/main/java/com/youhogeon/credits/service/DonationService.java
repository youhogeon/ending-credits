package com.youhogeon.credits.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.youhogeon.credits.entity.Donation;
import com.youhogeon.credits.repository.DonationRepsitory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DonationService {

    private final DonationRepsitory donationRepsitory;
    private final ConfigService configService;

    public void save(Donation d) {
        donationRepsitory.save(d);
    }

    public List<Donation> getDonations(Donation.Type type) {
        return donationRepsitory.findByType(type, configService.getLastCredits());
    }

    public List<Donation> getDonations(Donation.Type type, Donation.Platform platform) {
        return donationRepsitory.findByTypeAndPlatform(type, platform, configService.getLastCredits());
    }

    public Long expectedCount(LocalDateTime from) {
        return donationRepsitory.count(from);
    }

}
