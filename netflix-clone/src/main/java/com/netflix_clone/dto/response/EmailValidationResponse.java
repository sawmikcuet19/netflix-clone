package com.netflix_clone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailValidationResponse {
    private boolean exists;
    public boolean available;
}
