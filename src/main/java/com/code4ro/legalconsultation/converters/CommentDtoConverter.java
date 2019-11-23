package com.code4ro.legalconsultation.converters;

import java.util.Map;

import com.code4ro.legalconsultation.model.dto.CommentDto;
import com.code4ro.legalconsultation.model.persistence.Comment;
import com.code4ro.legalconsultation.model.persistence.VoteType;
import com.code4ro.legalconsultation.repository.CommentVoteRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;


import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Component
@AllArgsConstructor
public class CommentDtoConverter extends AbstractConverter<Comment, CommentDto> {

	private final CommentVoteRepository commentVoteRepository;


	@Override
	protected CommentDto convert(final Comment comment) {
		final CommentDto commentDto = new CommentDto();
		commentDto.setId(comment.getId());
		commentDto.setText(comment.getText());
		commentDto.setLastEditDateTime(comment.getLastEditDateTime());

		final Map<VoteType, Long> voteTypes = commentVoteRepository.findByComment(comment).stream().collect(groupingBy(commentVote -> commentVote.getVoteType(), counting()));
		commentDto.setDownVotes(voteTypes.getOrDefault(VoteType.DOWN, 0L));
		commentDto.setUpVotes(voteTypes.getOrDefault(VoteType.UP, 0L));
		return commentDto;
	}
}
