package com.code4ro.legalconsultation.service.impl;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import com.code4ro.legalconsultation.common.exceptions.LegalValidationException;
import com.code4ro.legalconsultation.config.security.CurrentUserService;
import com.code4ro.legalconsultation.model.dto.CommentDto;
import com.code4ro.legalconsultation.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.model.persistence.Comment;
import com.code4ro.legalconsultation.model.persistence.CommentVote;
import com.code4ro.legalconsultation.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.model.persistence.UserRole;
import com.code4ro.legalconsultation.model.persistence.VoteType;
import com.code4ro.legalconsultation.repository.CommentRepository;
import com.code4ro.legalconsultation.repository.CommentVoteRepository;
import com.code4ro.legalconsultation.service.api.CommentService;
import com.code4ro.legalconsultation.service.api.DocumentNodeService;
import com.code4ro.legalconsultation.service.api.MapperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {
	private final CommentRepository commentRepository;
	private final CurrentUserService currentUserService;
	private final DocumentNodeService documentNodeService;
	private final MapperService mapperService;
	private final CommentVoteRepository commentVoteRepository;

	public CommentServiceImpl(CommentRepository commentRepository,
							  CurrentUserService currentUserService,
							  DocumentNodeService documentNodeService,
							  MapperService mapperService,
							  final CommentVoteRepository commentVoteRepository) {
		this.commentRepository = commentRepository;
		this.currentUserService = currentUserService;
		this.documentNodeService = documentNodeService;
		this.mapperService = mapperService;
		this.commentVoteRepository = commentVoteRepository;
	}

	@Transactional
	@Override
	public CommentDto addVote(final UUID id, final VoteType voteType) {
		final ApplicationUser currentUser = currentUserService.getCurrentUser();
		Comment comment = commentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		if (!comment.isAllowVotes()) {
			throw new LegalValidationException("vote.closed", HttpStatus.BAD_REQUEST);
		}
		final CommentVote commentVote = commentVoteRepository.findByCommentAndOwner(comment, currentUser).orElseGet(() -> createCommentVote(currentUser, comment));
		commentVote.setVoteType(voteType);
		commentVoteRepository.save(commentVote);
		return mapperService.map(comment, CommentDto.class);
	}

	private CommentVote createCommentVote(final ApplicationUser currentUser, final Comment comment) {
		final CommentVote commentVote = new CommentVote();
		commentVote.setOwner(currentUser);
		commentVote.setComment(comment);
		return commentVote;
	}

	@Transactional
	@Override
	public CommentDto update(UUID nodeId, final UUID id, final CommentDto commentDto) {
		Comment comment = commentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		checkIfAuthorized(comment);

		comment.setAllowVotes(commentDto.isAllowVotes());
		comment.setText(commentDto.getText());
		comment = commentRepository.save(comment);

		return mapperService.map(comment, CommentDto.class);
	}

	@Transactional
	@Override
	public CommentDto create(UUID nodeId, final CommentDto commentDto) {
		final DocumentNode node = documentNodeService.getEntity(nodeId);

		final ApplicationUser currentUser = currentUserService.getCurrentUser();

		Comment comment = mapperService.map(commentDto, Comment.class);
		comment.setDocumentNode(node);
		comment.setOwner(currentUser);
		comment = commentRepository.save(comment);

		return mapperService.map(comment, CommentDto.class);
	}

	@Transactional
	@Override
	public void delete(final UUID id) {
		final Comment comment = commentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		checkIfAuthorized(comment);

		commentRepository.delete(comment);
	}

	@Transactional(readOnly = true)
	@Override
	public Page<CommentDto> findAll(final UUID documentNodeId, final Pageable pageable) {
		final Page<Comment> userPage = commentRepository.findByDocumentNodeId(documentNodeId, pageable);
		return mapperService.mapPage(userPage, CommentDto.class);
	}

	@Transactional(readOnly = true)
	@Override
	public BigInteger count(UUID nodeId) {
		return commentRepository.countByDocumentNodeId(nodeId);
	}

	private void checkIfAuthorized(Comment comment) {
		final ApplicationUser owner = comment.getOwner();
		final ApplicationUser currentUser = currentUserService.getCurrentUser();
		if (currentUser.getUser().getRole() != UserRole.ADMIN && !Objects.equals(currentUser.getId(), owner.getId())) {
			throw new LegalValidationException("comment.Unauthorized.user", HttpStatus.BAD_REQUEST);
		}
	}
}
