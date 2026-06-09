package com.cardly.web.dto;

import java.util.List;

public record DashboardChartsResponse(
		List<DashboardPieSliceResponse> pie,
		List<DashboardSubjectStackResponse> stackedBySubject
) {
}
