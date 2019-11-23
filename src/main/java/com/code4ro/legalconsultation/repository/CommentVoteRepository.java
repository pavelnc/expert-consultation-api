package com.code4ro.legalconsultation.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.code4ro.legalconsultation.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.model.persistence.Comment;
import com.code4ro.legalconsultation.model.persistence.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, UUID> {
	Optional<CommentVote> findByCommentAndOwner(final Comment comment, final ApplicationUser owner);

	List<CommentVote> findByComment(final Comment comment);
}
