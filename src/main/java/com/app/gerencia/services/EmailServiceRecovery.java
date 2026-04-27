package com.app.gerencia.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceRecovery {

    @Autowired
    private JavaMailSender mailSender;

    public void sendRecoveryEmail(String to, String link) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Recuperação de senha — GerencIA");
        msg.setText("Clique no link para redefinir sua senha:\n\n" + link);
        mailSender.send(msg);
    }
}
