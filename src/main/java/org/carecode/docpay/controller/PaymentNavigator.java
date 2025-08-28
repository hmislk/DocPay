package org.carecode.docpay.controller;

import org.carecode.docpay.model.DoctorPayment;

public interface PaymentNavigator {
    void goToPrint(DoctorPayment payment);
}

