package com.code4ro.legalconsultation.service.api;

import java.math.BigInteger;
import java.util.UUID;

import com.code4ro.legalconsultation.model.dto.CommentDto;
import com.code4ro.legalconsultation.model.persistence.VoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDto update(UUID nodeId, UUID id, CommentDto commentDto);
    CommentDto create(UUID nodeId, CommentDto commentDto);
    void delete(UUID id);
    Page<CommentDto> findAll(UUID nodeId, Pageable pageable);
    BigInteger count(UUID nodeId);
    CommentDto addVote(UUID id, VoteType voteType);
}
