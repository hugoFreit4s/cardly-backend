package com.cardly.service;

import com.cardly.repository.DeckRepository;
import com.cardly.web.dto.CardResponse;
import com.cardly.web.dto.RevisionDeckResponse;
import com.cardly.web.dto.RevisionsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RevisionService {

	private final DeckRepository deckRepository;
	private final CardService cardService;

	public RevisionService(DeckRepository deckRepository, CardService cardService) {
		this.deckRepository = deckRepository;
		this.cardService = cardService;
	}

	@Transactional(readOnly = true)
	public RevisionsResponse listRevisions(Integer userId) {
		var decks = deckRepository.findByUser_IdAndDeletedAtIsNullOrderByPositionAscIdAsc(userId);
		List<RevisionDeckResponse> result = new ArrayList<>();
		for (var deck : decks) {
			List<CardResponse> cards = cardService.listScheduledCardsForDeck(userId, deck.getId());
			if (cards.isEmpty()) {
				continue;
			}
			long waiting = cards.stream().filter(c -> c.dueAt() != null && c.dueAt().isAfter(java.time.Instant.now())).count();
			long ready = cards.stream().filter(c -> c.dueAt() != null && !c.dueAt().isAfter(java.time.Instant.now())).count();
			result.add(new RevisionDeckResponse(
					deck.getId(),
					deck.getName(),
					deck.getSubject(),
					cards.size(),
					(int) waiting,
					(int) ready,
					cards));
		}
		return new RevisionsResponse(result);
	}
}
