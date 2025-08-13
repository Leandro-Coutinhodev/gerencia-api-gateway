package com.app.gerencia.services;

import com.app.gerencia.entities.User;
import com.app.gerencia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public String save(User user){

        userRepository.save(user);
        return "Usuários registrado com sucesso";
    }

    public String update(User user, Long id){

        user.setId(id);
        userRepository.save(user);
        return "usuário atualizado com sucesso";
    }

    public User findById(Long id){

        return userRepository.findById(id).get();
    }

    public List<User> getAll(){

        return userRepository.findAll();
    }

    public String delete(Long id){

        userRepository.deleteById(id);
        return "Usuário excluído com sucesso";
    }
}
