package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.dto.response.UserAccessResponse;
import cn.chunana.simblog17api.entities.User;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserAccessService {
    Optional<User> login(String username, String password);

    Optional<User> register(String username, String password);

    Optional<User> findById(Long userId);

    Optional<User> updateUsername(Long userId, String newUsername);

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    PageResponse<UserAccessResponse> searchUsersByUsername(String keyword, Pageable pageable);

    PageResponse<UserAccessResponse> searchUsersByUsernameRegex(String pattern, Pageable pageable);
}
