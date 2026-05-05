package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.dto.response.UserAccessResponse;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.mapper.UserAccessMapper;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.UserAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
@RequiredArgsConstructor
public class UserAccessServiceImpl implements UserAccessService {
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return Optional.empty();
        }

        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username))
                                              .orElse(Optional.empty());

        return userOptional.filter(user -> credentialsMatch(password, user.getPassword()));
    }

    @Override
    public Optional<User> register(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return Optional.empty();
        }

        Optional<User> existing = Optional.ofNullable(userRepository.findByUsername(username))
                                          .orElse(Optional.empty());
        if (existing.isPresent()) {
            return Optional.empty();
        }

        User newUser = UserAccessMapper.toNewUser(username,
                                                  passwordEncoder.encode(password),
                                                  LocalDateTime.now());
        return Optional.of(userRepository.save(newUser));
    }

    @Override
    public Optional<User> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> updateUsername(Long userId, String newUsername) {
        if (userId == null || isBlank(newUsername)) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> sameNameUser = Optional.ofNullable(userRepository.findByUsername(newUsername))
                                              .orElse(Optional.empty());
        if (sameNameUser.isPresent() && !sameNameUser.get().getId().equals(userId)) {
            return Optional.empty();
        }

        User user = userOpt.get();
        user.setUsername(newUsername);
        return Optional.of(userRepository.save(user));
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || isBlank(oldPassword) || isBlank(newPassword)) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    public PageResponse<UserAccessResponse> searchUsersByUsername(String keyword, Pageable pageable) {
        return PageResponse.from(
                userRepository.findByUsernameContainingIgnoreCase(keyword, pageable)
                              .map(UserAccessMapper::toUserAccessResponse));
    }

    @Override
    public PageResponse<UserAccessResponse> searchUsersByUsernameRegex(String pattern, Pageable pageable) {
        validateRegex(pattern);
        return PageResponse.from(
                userRepository.findByUsernameRegex(pattern, pageable)
                              .map(UserAccessMapper::toUserAccessResponse));
    }

    private boolean credentialsMatch(String provided, String stored) {
        if (isBlank(stored) || isBlank(provided)) {
            return false;
        }
        return passwordEncoder.matches(provided, stored);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateRegex(String pattern) {
        if (isBlank(pattern)) {
            throw new IllegalArgumentException("regex pattern must not be blank");
        }
        if (pattern.length() > 256) {
            throw new IllegalArgumentException("regex pattern length must be at most 256");
        }
        try {
            Pattern.compile(pattern);
        }
        catch (PatternSyntaxException exception) {
            throw new IllegalArgumentException("invalid regex pattern");
        }
    }
}
