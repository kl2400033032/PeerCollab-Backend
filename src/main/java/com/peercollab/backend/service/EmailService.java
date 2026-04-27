package com.peercollab.backend.service;

import com.peercollab.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:no-reply@peercollab.local}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationEmail(User user) {
        sendEmail(user.getEmail(), "Welcome to PeerCollab", "Hi " + user.getName() + ", your PeerCollab account is ready.");
    }

    public void sendReviewReceivedEmail(User user, String projectTitle) {
        sendEmail(user.getEmail(), "New review received", "Your project \"" + projectTitle + "\" has received new peer feedback.");
    }

    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            LOGGER.info("Email disabled. Would have sent '{}' to {}", subject, to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
