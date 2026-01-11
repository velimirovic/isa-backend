package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.core.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

//Asinhrono slanje mailova
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    //Cita iz application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${app.base-url}")
    private String baseUrl;

    //Asinhrono slanje aktivacionog maila
    @Async
    public void sendActivationEmail(String toEmail, String username, String token) throws MailException {
        System.out.println("Slanje aktivacionog emaila na: " + toEmail);

        String subject = "Jutjubic - Aktivacija naloga";
        String activationLink = baseUrl + "/auth/activate?token=" + token;

        String body = String.format(
                "Postovani %s,\n\n" +
                        "Hvala sto ste se registrovali na Jutjubic!\n\n" +
                        "Molimo vas da aktivirate svoj nalog klikom na sledeći link:\n" +
                        "%s\n\n" +
                        "Link vazi 24 sata.\n\n" +
                        "Ukoliko niste vi kreirali ovaj nalog, ignorišite ovu poruku.\n\n" +
                        "Srdacan pozdrav,\n" +
                        "Jutjubic Tim (Dragana, Petar & Marko)",
                username,
                activationLink
        );

        sendEmail(toEmail, subject, body);
        System.out.println("Email uspesno poslat!");
    }

    //Posalji mail
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setFrom(fromEmail);
        mail.setSubject(subject);
        mail.setText(body);
        javaMailSender.send(mail);
    }
}