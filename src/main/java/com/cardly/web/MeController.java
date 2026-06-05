package com.cardly.web;

import com.cardly.domain.User;
import com.cardly.security.UserPrincipal;
import com.cardly.service.UserService;
import com.cardly.web.dto.MeResponse;
import com.cardly.web.dto.UpdateProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class MeController {

	private final UserService userService;

	public MeController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public MeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
		User user = userService.findActiveUserById(principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return new MeResponse(user.getPublicId(), user.getName(), user.getEmail(), user.getRole());
	}

	@PatchMapping("/me")
	public MeResponse updateMe(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestBody UpdateProfileRequest request) {
		User user = userService.updateOwnProfile(principal.getId(), request);
		return new MeResponse(user.getPublicId(), user.getName(), user.getEmail(), user.getRole());
	}

	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMyAccount(@AuthenticationPrincipal UserPrincipal principal) {
		userService.softDeleteActiveUser(principal.getId());
	}
}
