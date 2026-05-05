package cn.chunana.simblog17api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应包装器，替代 Spring Data {@link Page} 直接序列化到 Redis。
 * <p>所有泛型参数 T 必须实现 {@link Serializable}。</p>
 */
@Schema(description = "分页响应")
public record PageResponse<T extends Serializable>(

        @Schema(description = "当前页数据列表")
        List<T> content,

        @Schema(description = "总元素数量", example = "100")
        long totalElements,

        @Schema(description = "总页数", example = "10")
        int totalPages,

        @Schema(description = "当前页码（0-based）", example = "0")
        int pageNumber,

        @Schema(description = "每页大小", example = "10")
        int pageSize

) implements Serializable {

    /**
     * 从 Spring Data Page 对象构造
     */
    public static <T extends Serializable> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}

