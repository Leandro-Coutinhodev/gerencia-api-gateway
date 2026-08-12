package com.app.gerencia.services;

import com.app.gerencia.entities.PasswordRecovery;
import com.app.gerencia.entities.User;
import com.app.gerencia.repository.PasswordRecoveryRepository;
import com.app.gerencia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordRecoveryRepository recoveryRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailServiceRecovery emailServiceRecovery;
    @Value("${link.host}")
    private String host;

    public PasswordRecoveryService(UserRepository userRepository,
                                   PasswordRecoveryRepository recoveryRepository,
                                   BCryptPasswordEncoder passwordEncoder,
                                   EmailServiceRecovery emailServiceRecovery) {
        this.userRepository = userRepository;
        this.recoveryRepository = recoveryRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailServiceRecovery = emailServiceRecovery;
    }


    public void requestRecovery(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return; // não revela se o email existe
        }

        User user = userOpt.get();

        PasswordRecovery recovery = new PasswordRecovery();
        recovery.setUser(user);

        recoveryRepository.save(recovery);


        String link = host + "/restaurar-senha?token=" + recovery.getToken();
        emailServiceRecovery.sendRecoveryEmail(user.getEmail(), link);


    }


    public void resetPassword(String token, String newPassword) {
        PasswordRecovery recovery = recoveryRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (recovery.isExpired()) {
            throw new RuntimeException("Token expirado");
        }

        User user = recovery.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        recoveryRepository.delete(recovery); // invalida o token
    }
}