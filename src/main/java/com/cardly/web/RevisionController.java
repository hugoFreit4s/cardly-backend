package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.RevisionService;
import com.cardly.web.dto.RevisionsResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/revisions")
public class RevisionController {

	private final RevisionService revisionService;

	public RevisionController(RevisionService revisionService) {
		this.revisionService = revisionService;
	}

	@GetMapping
	public RevisionsResponse listRevisions(@AuthenticationPrincipal UserPrincipal principal) {
		return revisionService.listRevisions(principal.getId());
	}
}
