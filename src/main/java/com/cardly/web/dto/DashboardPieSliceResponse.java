package com.cardly.web.dto;

public record DashboardPieSliceResponse(
		String key,
		String label,
		long value
) {
}
