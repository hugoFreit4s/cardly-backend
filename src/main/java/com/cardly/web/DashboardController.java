package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.CardService;
import com.cardly.service.DeckService;
import com.cardly.service.StudyReviewService;
import com.cardly.web.dto.DashboardChartsResponse;
import com.cardly.web.dto.DashboardPieSliceResponse;
import com.cardly.web.dto.DashboardResponse;
import com.cardly.web.dto.DashboardSubjectStackResponse;
import com.cardly.web.dto.StudyCalendarResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DeckService deckService;
	private final CardService cardService;
	private final StudyReviewService studyReviewService;

	public DashboardController(DeckService deckService, CardService cardService, StudyReviewService studyReviewService) {
		this.deckService = deckService;
		this.cardService = cardService;
		this.studyReviewService = studyReviewService;
	}

	@GetMapping
	public DashboardResponse getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
		Integer userId = principal.getId();
		return new DashboardResponse(
				deckService.countSubjects(userId),
				cardService.countCards(userId),
				cardService.countDueCards(userId),
				cardService.countAnsweredToday(userId)
		);
	}

	@GetMapping("/charts")
	public DashboardChartsResponse getDashboardCharts(@AuthenticationPrincipal UserPrincipal principal) {
		Integer userId = principal.getId();
		long dueCards = cardService.countDueCardsOnly(userId);
		long scheduledCards = cardService.countWaitingCards(userId);
		long unscheduledCards = cardService.countUnscheduledCards(userId);
		var pie = java.util.List.of(
				new DashboardPieSliceResponse("due", "Vencidos", dueCards),
				new DashboardPieSliceResponse("scheduled", "Agendados", scheduledCards),
				new DashboardPieSliceResponse("unscheduled", "Sem agendamento", unscheduledCards)
		);
		java.util.List<DashboardSubjectStackResponse> stackedBySubject = deckService.summarizeSubjects(userId);
		return new DashboardChartsResponse(pie, stackedBySubject);
	}

	@GetMapping("/calendar")
	public StudyCalendarResponse getStudyCalendar(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(defaultValue = "120") int limit,
			@RequestParam(required = false) Integer timezoneOffsetMinutes) {
		return new StudyCalendarResponse(studyReviewService.listStudyDays(principal.getId(), limit, timezoneOffsetMinutes));
	}
}
