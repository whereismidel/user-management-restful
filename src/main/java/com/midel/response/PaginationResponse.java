package com.midel.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PaginationResponse extends CustomResponse {

    private final Map<String, Long> pagination;
    private final Map<String, String> links;
    private final List<?> data;

    public PaginationResponse(HttpStatus status, long page, long size, long total, List<?> data, String nextLink, String prevLink) {
        super(status);

        this.pagination = Map.of(
                "page", page,
                "size", size,
                "total_elements", total
        );

        this.data = data;

        this.links = new LinkedHashMap<>();
        if (nextLink != null) {
            links.put("next", nextLink);
        }
        if (prevLink != null) {
            links.put("prev", prevLink);
        }
    }
}
