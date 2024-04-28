package com.midel.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@Getter
public class PaginationResponse extends CustomResponse {

    private final Map<String, Long> pagination;
    private final Map<String, String> links;
    private final Stream<?> data;

    public PaginationResponse(HttpStatus status, long offset, long limit, long total, Stream<?> data, String nextLink, String prevLink) {
        super(status);

        this.pagination = Map.of(
                "page", offset,
                "size", limit,
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
