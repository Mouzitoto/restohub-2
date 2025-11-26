package com.restohub.adminapi.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum PaymentStatus {
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    PENDING("PENDING");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + value);
    }

    @Converter(autoApply = true)
    public static class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {
        @Override
        public String convertToDatabaseColumn(PaymentStatus status) {
            if (status == null) {
                return null;
            }
            return status.getValue();
        }

        @Override
        public PaymentStatus convertToEntityAttribute(String value) {
            if (value == null) {
                return null;
            }
            return PaymentStatus.fromValue(value);
        }
    }
}

