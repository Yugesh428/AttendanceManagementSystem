package com.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Central email utility.
 * All emails are sent asynchronously so they never block the HTTP response.
 *
 * To enable async, add @EnableAsync to your main application class or a config class.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.name:Attendance Management System}")
    private String appName;

    /**
     * Send student login credentials after enrollment.
     *
     * @param toEmail   student's email address
     * @param fullName  student's full name (for greeting)
     * @param username  login username (same as email)
     * @param password  plain-text generated password (shown once)
     */
    @Async
    public void sendStudentCredentials(String toEmail, String fullName,
                                       String username, String password) {
        String subject = appName + " — Your Login Credentials";
        String body = buildCredentialsEmail(fullName, username, password);
        sendEmail(toEmail, subject, body);
    }

    /**
     * Notify admin that a student logged in from a new/different device.
     *
     * @param adminEmail  admin's email
     * @param studentName student's full name
     * @param studentEmail student's email
     * @param oldDeviceId previously registered device fingerprint
     * @param newDeviceId new device fingerprint
     */
    @Async
    public void sendDeviceChangeAlert(String adminEmail, String studentName,
                                      String studentEmail, String oldDeviceId,
                                      String newDeviceId) {
        String subject = appName + " — Device Change Alert: " + studentName;
        String body = String.format("""
                Device Change Alert
                -------------------
                Student  : %s (%s)
                Old Device ID : %s
                New Device ID : %s
                
                This student logged in from a different device.
                If this was authorised, no action is needed.
                If suspicious, reset the device binding from the admin panel.
                
                — %s
                """, studentName, studentEmail, oldDeviceId, newDeviceId, appName);
        sendEmail(adminEmail, subject, body);
    }

    /**
     * Send enrollment confirmation to student when they are placed in a semester/section.
     */
    @Async
    public void sendEnrollmentConfirmation(String toEmail, String fullName,
                                           String semesterName, String sectionName,
                                           String subjectList) {
        String subject = appName + " — Enrollment Confirmation";
        String body = String.format("""
                Dear %s,
                
                You have been successfully enrolled. Here are your details:
                
                    Semester : %s
                    Section  : %s
                    Subjects : %s
                
                Log in from your mobile phone to complete device registration and start marking attendance.
                
                — %s
                """, fullName, semesterName, sectionName, subjectList, appName);
        sendEmail(toEmail, subject, body);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildCredentialsEmail(String fullName, String username, String password) {
        return String.format("""
                Dear %s,
                
                Welcome to %s!
                
                Your student account has been created. Use the credentials below to log in:
                
                    Username : %s
                    Password : %s
                
                Please log in from your mobile phone first to register your device.
                After that you can access the dashboard from any browser.
                
                IMPORTANT: Change your password after your first login.
                
                If you did not expect this email, please contact your institution administrator.
                
                — %s
                """, fullName, appName, username, password, appName);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[EMAIL] Sent to='{}' subject='{}'", to, subject);
        } catch (MailException ex) {
            // Log and swallow — email failure must never fail the main transaction
            log.error("[EMAIL] Failed to send to='{}': {}", to, ex.getMessage());
        }
    }
}
