//package com.app.gerencia.config;
//
//import com.app.gerencia.entities.Role;
//import com.app.gerencia.entities.User;
//import com.app.gerencia.repository.RoleRepository;
//import com.app.gerencia.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.text.SimpleDateFormat;
//import java.util.Set;
//
//@Configuration
//public class AdminUserConfig implements CommandLineRunner {
//    private RoleRepository roleRepository;
//    private UserRepository userRepository;
//    private BCryptPasswordEncoder passwordEncoder;
//
//    public AdminUserConfig(RoleRepository roleRepository,
//                           UserRepository userRepository,
//                           BCryptPasswordEncoder passwordEncoder) {
//        this.roleRepository = roleRepository;
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//
//        var roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
//
//        var userAdmin = userRepository.findByCpf("052.305.042-93");
//
//        userAdmin.ifPresentOrElse(
//                (user) -> {
//                    System.out.println("Admin já existe");
//                },
//                () -> {
//                    try {
//                        var user = new User();
//                        user.setCpf("111.111.111-11");
//                        user.setName("Administrador");
//                        user.setDateBirth(new SimpleDateFormat("dd-MM-yyyy").parse("20-06-2006"));
//                        user.setEmail("adm@gmail.com");
//                        user.setPhoneNumber("(11) 11111-1111");
//                        user.setPassword(passwordEncoder.encode("gerencia@adm"));
//                        user.setRoles(Set.of(roleAdmin));
//                        userRepository.save(user);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                );
//    }
//}
