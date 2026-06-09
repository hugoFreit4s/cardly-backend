package com.cardly.web;

import com.cardly.domain.UserRoleENUM;
import com.cardly.security.UserPrincipal;
import com.cardly.service.CardService;
import com.cardly.service.DeckService;
import com.cardly.service.StudyReviewService;
import com.cardly.web.dto.DashboardChartsResponse;
import com.cardly.web.dto.DashboardSubjectStackResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

	@Mock
	private DeckService deckService;

	@Mock
	private CardService cardService;

	@Mock
	private StudyReviewService studyReviewService;

	@InjectMocks
	private DashboardController dashboardController;

	@Test
	void getDashboardChartsReturnsPieAndStackedData() {
		UserPrincipal principal = new UserPrincipal(10, "user@cardly.com", UserRoleENUM.USER);
		when(cardService.countDueCardsOnly(10)).thenReturn(4L);
		when(cardService.countWaitingCards(10)).thenReturn(7L);
		when(cardService.countUnscheduledCards(10)).thenReturn(3L);
		when(deckService.summarizeSubjects(10)).thenReturn(List.of(
				new DashboardSubjectStackResponse("Matemática", 8, 3, 4, 1),
				new DashboardSubjectStackResponse("História", 6, 1, 2, 3)
		));

		DashboardChartsResponse response = dashboardController.getDashboardCharts(principal);

		assertThat(response.pie()).hasSize(3);
		assertThat(response.pie()).extracting(slice -> slice.key() + ":" + slice.value())
				.containsExactly("due:4", "scheduled:7", "unscheduled:3");
		assertThat(response.stackedBySubject()).hasSize(2);
		assertThat(response.stackedBySubject().getFirst().subject()).isEqualTo("Matemática");
	}
}
