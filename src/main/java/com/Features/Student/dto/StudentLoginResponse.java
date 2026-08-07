package com.Features.Student.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentLoginResponse {

    private String  token;
    private String  tokenType;

    private UUID    studentId;
    private String  firstName;
    private String  lastName;
    private String  email;

    /** True once the student has logged in from their phone at least once */
    private boolean phoneRegistered;

    /**
     * True when THIS login caused the device to be registered for the first time.
     * Frontend uses this to show a one-time "Phone registered successfully" message.
     */
    private boolean deviceJustRegistered;

    /**
     * True when THIS login was from a DIFFERENT device than the one registered.
     * Frontend can show: "New device detected. Admin has been notified."
     */
    private boolean deviceChanged;

    private String  role;
}
