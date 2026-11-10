
package com.vti.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {

    public static <E, T> PageResponse<T> from(
            Page<E> result,
            Function<E, T> mapper
    ) {
        return new PageResponse<>(
                result.getContent()
                        .stream()
                        .map(mapper)
                        .toList(),

                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.isFirst(),
                result.isLast()
        );
    }
}
