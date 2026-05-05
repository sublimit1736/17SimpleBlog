package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameContaining(String username);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    @Query(value = "SELECT * FROM users u WHERE u.username ~* :pattern",
           countQuery = "SELECT COUNT(*) FROM users u WHERE u.username ~* :pattern",
           nativeQuery = true)
    Page<User> findByUsernameRegex(@Param("pattern") String pattern, Pageable pageable);
}


