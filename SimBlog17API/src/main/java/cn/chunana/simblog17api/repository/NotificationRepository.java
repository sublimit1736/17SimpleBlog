package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, Integer status);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}

