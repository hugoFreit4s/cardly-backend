package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.NotificationService;
import com.cardly.web.dto.NotificationIdsRequest;
import com.cardly.web.dto.NotificationResponse;
import com.cardly.web.dto.NotificationSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public List<NotificationResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
		return notificationService.listForUser(principal.getId());
	}

	@GetMapping("/summary")
	public NotificationSummaryResponse summary(@AuthenticationPrincipal UserPrincipal principal) {
		return notificationService.summaryForUser(principal.getId());
	}

	@PostMapping("/read-all")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void readAll(@AuthenticationPrincipal UserPrincipal principal) {
		notificationService.markAllRead(principal.getId());
	}

	@PostMapping("/mark-read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markRead(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody NotificationIdsRequest request) {
		notificationService.markRead(principal.getId(), request.ids());
	}

	@PostMapping("/delete")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody NotificationIdsRequest request) {
		notificationService.deleteNotifications(principal.getId(), request.ids());
	}
}
