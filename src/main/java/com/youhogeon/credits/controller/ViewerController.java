package com.youhogeon.credits.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.youhogeon.credits.service.ConfigService;
import com.youhogeon.credits.service.DonationService;
import com.youhogeon.credits.entity.Donation;
import com.youhogeon.credits.receiver.Receiver;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ViewerController {

    private final DonationService donationService;
    private final ConfigService configService;

    @Qualifier("twip")
    private final Receiver twipReceiver;

    @Qualifier("toonation")
    private final Receiver toonationReceiver;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("lastCredits", configService.getLastCreditsForHtml());
        model.addAttribute("header", configService.getHeader());
        model.addAttribute("footer", configService.getFooter());
        model.addAttribute("toonationKey", configService.getToonationKey());
        model.addAttribute("twipKey", configService.getTwipKey());

        model.addAttribute("toonationActive", toonationReceiver.isActive());
        model.addAttribute("twipActive", twipReceiver.isActive());

        
        return "index";
    }

    @GetMapping("/credits")
    public String credits(Model model) {
        model.addAttribute("toonation", donationService.getDonations(Donation.Type.DONATION, Donation.Platform.TOONATION));
        model.addAttribute("twip", donationService.getDonations(Donation.Type.DONATION, Donation.Platform.TWIP));
        model.addAttribute("bits", donationService.getDonations(Donation.Type.BITS));
        model.addAttribute("subscription", donationService.getDonations(Donation.Type.SUBSCRIPTION));
        model.addAttribute("raid", donationService.getDonations(Donation.Type.RAID));
        model.addAttribute("follow", donationService.getDonations(Donation.Type.FOLLOW));

        model.addAttribute("header", configService.getHeader());
        model.addAttribute("footer", configService.getFooter());

        configService.setLastCredits(LocalDateTime.now());

        return "credits";
    }

}
