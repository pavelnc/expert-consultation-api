package com.code4ro.legalconsultation.service;

import java.util.Optional;
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
import com.code4ro.legalconsultation.service.api.DocumentNodeService;
import com.code4ro.legalconsultation.service.api.MapperService;
import com.code4ro.legalconsultation.service.impl.CommentServiceImpl;
import com.code4ro.legalconsultation.util.CommentFactory;
import com.code4ro.legalconsultation.util.DocumentNodeFactory;
import com.code4ro.legalconsultation.util.RandomObjectFiller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;
	@Mock
	private MapperService mapperService;
	@Mock
	private CurrentUserService currentUserService;
	@Mock
	private DocumentNodeService documentNodeService;
	@InjectMocks
	private CommentServiceImpl commentService;

	@Mock
	private CommentVoteRepository commentVoteRepository;

	@Captor
	private ArgumentCaptor<Comment> commentArgumentCaptor;
	@Captor
	private ArgumentCaptor<CommentVote> commentVoteArgumentCaptor;

	private final DocumentNodeFactory documentNodeFactory = new DocumentNodeFactory();
	private final CommentFactory commentFactory = new CommentFactory();
	private ApplicationUser currentUser;

	@Before
	public void before() {
		currentUser = RandomObjectFiller.createAndFill(ApplicationUser.class);
		currentUser.getUser().setRole(UserRole.ADMIN);
		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
	}

	@Test
	public void create() {
		final UUID id = UUID.randomUUID();
		final CommentDto commentDto = RandomObjectFiller.createAndFill(CommentDto.class);
		final DocumentNode documentNode = documentNodeFactory.create();
		final Comment comment = new Comment();
		when(mapperService.map(commentDto, Comment.class)).thenReturn(comment);
		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
		when(documentNodeService.getEntity(any())).thenReturn(documentNode);

		commentService.create(id, commentDto);

		verify(commentRepository).save(comment);
		assertThat(comment.getOwner()).isEqualTo(currentUser);
		assertThat(comment.getDocumentNode()).isEqualTo(documentNode);
	}

	@Test
	public void update() {
		final UUID id = UUID.randomUUID();
		final CommentDto commentDto = RandomObjectFiller.createAndFill(CommentDto.class);
		final Comment comment = new Comment();
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

		commentService.update(UUID.randomUUID(), id, commentDto);

		verify(commentRepository).save(commentArgumentCaptor.capture());
		assertThat(commentArgumentCaptor.getValue().getText()).isEqualTo(commentDto.getText());
		assertThat(commentArgumentCaptor.getValue().isAllowVotes()).isEqualTo(commentDto.isAllowVotes());
	}

	@Test(expected = LegalValidationException.class)
	public void updateUnauthorizedUser() {
		final UUID id = UUID.randomUUID();
		final CommentDto commentDto = new CommentDto();
		currentUser.getUser().setRole(UserRole.CONTRIBUTOR);
		final Comment comment = commentFactory.createEntity();
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

		commentService.update(UUID.randomUUID(), id, commentDto);
	}

	@Test
	public void delete() {
		final UUID id = UUID.randomUUID();
		final Comment comment = new Comment();
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

		commentService.delete(id);

		verify(commentRepository).delete(comment);
	}

	@Test(expected = LegalValidationException.class)
	public void deleteUnauthorizedUser() {
		final UUID id = UUID.randomUUID();
		currentUser.getUser().setRole(UserRole.CONTRIBUTOR);
		final Comment comment = commentFactory.createEntity();
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

		commentService.delete(id);
	}

	@Test
	public void findAll() {
		final UUID nodeId = UUID.randomUUID();
		final Pageable pageable = mock(Pageable.class);

		commentService.findAll(nodeId, pageable);

		verify(commentRepository).findByDocumentNodeId(nodeId, pageable);
	}

	@Test(expected = EntityNotFoundException.class)
	public void addVoteCommentNotFoundFail() {
		// given
		final UUID id = UUID.randomUUID();

		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
		when(commentRepository.findById(id)).thenReturn(Optional.empty());
		// when
		 commentService.addVote(id, VoteType.UP);
		// then



	}

	@Test(expected = LegalValidationException.class)
	public void addVoteClosed() {
		final UUID id = UUID.randomUUID();
		final Comment comment = new Comment();
		comment.setAllowVotes(false);
		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
		when(commentVoteRepository.findByCommentAndOwner(comment, currentUser)).thenReturn(Optional.empty());
		// when
		commentService.addVote(id, VoteType.UP);
	}

	@Test
	public void addMissingVote() {
		// given
		final UUID id = UUID.randomUUID();
		final Comment comment = new Comment();
		comment.setAllowVotes(true);
		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
		when(commentVoteRepository.findByCommentAndOwner(comment, currentUser)).thenReturn(Optional.empty());
		// when
		commentService.addVote(id, VoteType.UP);
		// then
		verify(commentVoteRepository).save(commentVoteArgumentCaptor.capture());
		assertThat(commentVoteArgumentCaptor.getValue().getVoteType()).isEqualTo(VoteType.UP);
	}

	@Test
	public void updateMissingVote() {
		// given
		final UUID id = UUID.randomUUID();
		final Comment comment = new Comment();
		comment.setAllowVotes(true);
		when(currentUserService.getCurrentUser()).thenReturn(currentUser);
		when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
		final CommentVote commentVote = new CommentVote();
		when(commentVoteRepository.findByCommentAndOwner(comment, currentUser)).thenReturn(Optional.of(commentVote));
		// when
		commentService.addVote(id, VoteType.UP);
		// then
		verify(commentVoteRepository).save(commentVote);
		verify(commentVoteRepository).save(commentVoteArgumentCaptor.capture());
		assertThat(commentVoteArgumentCaptor.getValue().getVoteType()).isEqualTo(VoteType.UP);
	}
}
