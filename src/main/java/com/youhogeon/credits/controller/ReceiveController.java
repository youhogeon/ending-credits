package com.youhogeon.credits.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.youhogeon.credits.entity.Donation;
import com.youhogeon.credits.receiver.MessageHandler;
import com.youhogeon.credits.receiver.Receiver;
import com.youhogeon.credits.service.DonationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiveController implements ApplicationRunner {

    @Qualifier("twip")
    private final Receiver twipReceiver;

    @Qualifier("toonation")
    private final Receiver toonationReceiver;

    private final DonationService donationService;

    private void onDonate(Donation donation) {
        donationService.save(donation);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MessageHandler<Donation> handler = (Donation donation) -> {
            log.info(donation.toString());

            onDonate(donation);
        };

        twipReceiver.subscribeDonation(handler);
        toonationReceiver.subscribeDonation(handler);
    }

}
