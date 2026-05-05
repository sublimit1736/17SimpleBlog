package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.response.UserAccessResponse;
import cn.chunana.simblog17api.entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAccessMapperTest {

    @Test
    void toNewUserShouldMapInputFields() {
        LocalDateTime createTime = LocalDateTime.of(2026, 4, 11, 10, 0);

        User user = UserAccessMapper.toNewUser("alice", "$2a$10$encoded", createTime);

        assertEquals("alice", user.getUsername());
        assertEquals("$2a$10$encoded", user.getPassword());
        assertEquals(createTime, user.getCreateTime());
    }

    @Test
    void toUserAccessResponseShouldMapVisibleFieldsOnly() {
        LocalDateTime createTime = LocalDateTime.of(2026, 4, 11, 10, 0);
        User          user       = new User(1L, "alice", null, "$2a$10$encoded", User.UserRole.USER, createTime);

        UserAccessResponse response = UserAccessMapper.toUserAccessResponse(user);

        assertEquals(1L, response.id());
        assertEquals("alice", response.username());
        assertEquals(createTime, response.createTime());
    }
}

