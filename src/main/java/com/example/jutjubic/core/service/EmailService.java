package com.example.jutjubic.core.service;

import org.springframework.mail.MailException;


public interface EmailService {

    void sendActivationEmail(String toEmail, String username, String token) throws MailException;
}