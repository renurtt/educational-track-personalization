package com.nurtdinov.educationaltrackpersonalization.security.service;

import java.util.HashSet;
import java.util.Set;

import com.nurtdinov.educationaltrackpersonalization.entity.User;
import com.nurtdinov.educationaltrackpersonalization.repository.UserRepository;
import com.nurtdinov.educationaltrackpersonalization.security.ApplicationUserRole;
import com.nurtdinov.educationaltrackpersonalization.security.dao.ApplicationUserDAO;
import com.nurtdinov.educationaltrackpersonalization.security.dto.RegistrationRequest;
import com.nurtdinov.educationaltrackpersonalization.security.exception.UserAlreadyRegisteredException;
import com.nurtdinov.educationaltrackpersonalization.security.model.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationUserService implements UserDetailsService {

    private final ApplicationUserDAO userDetailsDAO;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDetailsDAO.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found", username)));
    }

    public boolean isUserPresent(String username) {
        return userDetailsDAO.findById(username).isPresent();
    }

    public void addUser(ApplicationUser user) {
        userDetailsDAO.save(user);
    }

    public void register(RegistrationRequest registrationRequest, Set<ApplicationUserRole> userRoleSet) {
        if (isUserPresent(registrationRequest.getUsername())) {
            throw new UserAlreadyRegisteredException(registrationRequest.getUsername());
        }

        User user = new User(registrationRequest.getUsername());
        user.setExternalId(generateUniqueRandom());
        userRepository.save(user);

        doRegister(registrationRequest, userRoleSet);
    }

    public void doRegister(RegistrationRequest registrationRequest, Set<ApplicationUserRole> userRoleSet) {
        ApplicationUser applicationUser = new ApplicationUser(registrationRequest.getUsername(), null,
                passwordEncoder.encode(registrationRequest.getPassword()),
                userRoleSet,
                true, true, true, true);
        addUser(applicationUser);
    }

    private Long generateUniqueRandom() {
        while (true) {
            Long id = RandomUtils.nextLong();
            if (userRepository.findUserByExternalId(id) == null) {
                return id;
            }
        }
    }

    public void register(RegistrationRequest registrationRequest, ApplicationUserRole role) {
        Set<ApplicationUserRole> userRoleSet = new HashSet<>();
        userRoleSet.add(role);
        register(registrationRequest, userRoleSet);
    }
}
