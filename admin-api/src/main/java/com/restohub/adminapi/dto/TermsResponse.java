package com.restohub.adminapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsResponse {
    private String terms;
    
    public TermsResponse(String terms) {
        this.terms = terms;
    }
}

