package com.restohub.adminapi.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SubscriptionStatus {
    DRAFT("DRAFT"),
    PENDING("PENDING"),
    ACTIVATED("ACTIVATED"),
    EXPIRED("EXPIRED"),
    CANCELLED("CANCELLED");

    private final String value;

    SubscriptionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SubscriptionStatus fromValue(String value) {
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown subscription status: " + value);
    }

    @Converter(autoApply = true)
    public static class SubscriptionStatusConverter implements AttributeConverter<SubscriptionStatus, String> {
        @Override
        public String convertToDatabaseColumn(SubscriptionStatus status) {
            if (status == null) {
                return null;
            }
            return status.getValue();
        }

        @Override
        public SubscriptionStatus convertToEntityAttribute(String value) {
            if (value == null) {
                return null;
            }
            return SubscriptionStatus.fromValue(value);
        }
    }
}

