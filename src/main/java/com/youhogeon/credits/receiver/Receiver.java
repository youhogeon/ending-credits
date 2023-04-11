package com.youhogeon.credits.receiver;

import com.youhogeon.credits.entity.Donation;

public interface Receiver {
    
    boolean isActive();
    void subscribeDonation(MessageHandler<Donation> onNext);

}
