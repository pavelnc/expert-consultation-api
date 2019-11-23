package com.code4ro.legalconsultation.controller;

import java.util.UUID;

import com.code4ro.legalconsultation.model.dto.CommentDto;
import com.code4ro.legalconsultation.model.persistence.VoteType;
import com.code4ro.legalconsultation.service.api.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/documentnodes/{nodeId}/comments")
@AllArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<CommentDto> create(@PathVariable final UUID nodeId,
											 @RequestBody final CommentDto commentDto) {
		return ResponseEntity.ok(commentService.create(nodeId, commentDto));
	}

	@PostMapping("/vote/{id}/{voteType}")
	public ResponseEntity<CommentDto> addVote(@PathVariable final UUID nodeId,
											  @PathVariable final UUID id,
											  @PathVariable final VoteType voteType) {
		return ResponseEntity.ok(commentService.addVote(id, voteType));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CommentDto> update(@PathVariable final UUID nodeId,
											 @PathVariable final UUID id,
											 @RequestBody final CommentDto commentDto) {
		return ResponseEntity.ok(commentService.update(nodeId, id, commentDto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable final UUID nodeId,
									   @PathVariable final UUID id) {
		commentService.delete(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<Page<CommentDto>> findAll(@PathVariable UUID nodeId,
													final Pageable pageable) {
		return ResponseEntity.ok(commentService.findAll(nodeId, pageable));
	}
}
