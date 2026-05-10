package cn.chunana.simblog17api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    // Common
    SUCCESS(0, "Internal Success"),
    UNEXPECTED_ERROR(1, "Internal Error"),
    INVALID_REQUEST(2, "Invalid Request"),
    RESOURCE_NOT_FOUND(3, "Resource Not Found"),
    ACCESS_DENIED(4, "Access Denied"),
    UNAUTHORIZED(5, "Unauthorized"),
    TOO_MANY_REQUESTS(6, "Too Many Requests"),

    // UserLogin
    LOGIN_FAILED(1001, "Wrong Username or Password"),

    // UserRegister
    USER_ALREADY_EXISTS(2001, "User Already Exists"),
    USER_NOT_FOUND(2002, "User Not Found"),
    OLD_PASSWORD_INCORRECT(2003, "Old Password Incorrect"),

    // Articles
    ARTICLE_NOT_FOUND(3001, "Article Not Found"),

    // Comments
    COMMENT_NOT_FOUND(4001, "Comment Not Found"),

    // MetaConfig
    OWNER_TOKEN_INVALID(5001, "Owner Token Invalid");

    private final int    code;
    private final String message;
}
