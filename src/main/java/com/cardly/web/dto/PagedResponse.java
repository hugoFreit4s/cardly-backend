package com.cardly.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(
		List<T> content,
		long totalElements,
		int totalPages,
		int page,
		int size,
		boolean first,
		boolean last
) {
	public static <T> PagedResponse<T> from(Page<T> pageData) {
		return new PagedResponse<>(
				pageData.getContent(),
				pageData.getTotalElements(),
				pageData.getTotalPages(),
				pageData.getNumber(),
				pageData.getSize(),
				pageData.isFirst(),
				pageData.isLast()
		);
	}
}
