package com.code4ro.legalconsultation.controller;

import com.code4ro.legalconsultation.common.controller.AbstractControllerIntegrationTest;
import com.code4ro.legalconsultation.model.dto.CommentDto;
import com.code4ro.legalconsultation.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.model.persistence.VoteType;
import com.code4ro.legalconsultation.repository.CommentRepository;
import com.code4ro.legalconsultation.service.api.CommentService;
import com.code4ro.legalconsultation.util.CommentFactory;
import com.code4ro.legalconsultation.util.DocumentNodeFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableJpaAuditing
public class CommentControllerIntegrationTest extends AbstractControllerIntegrationTest {

	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentFactory commentFactory;
	@Autowired
	private DocumentNodeFactory documentNodeFactory;

	@Before
	public void before() {
		persistMockedUser();
	}

	@Test
	@WithMockUser
	public void create() throws Exception {
		final DocumentNode node = documentNodeFactory.save();
		final CommentDto commentDto = commentFactory.create();

		mvc.perform(post(endpoint("/api/documentnodes/", node.getId(), "/comments"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(commentDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.text").value(commentDto.getText()))
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(status().isOk());

		assertThat(commentRepository.count()).isEqualTo(1);
	}

	@Test
	@WithMockUser
	public void addVote() throws Exception {
		final DocumentNode node = documentNodeFactory.save();
		final CommentDto commentDto = commentService.create(node.getId(), commentFactory.create());

		mvc.perform(post(endpoint("/api/documentnodes/", node.getId(), "/comments/vote", commentDto.getId(), VoteType.UP))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.text").value(commentDto.getText()))
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.upVotes").value("1"))
				.andExpect(status().isOk());

		assertThat(commentRepository.count()).isEqualTo(1);
	}


	@Test
	@WithMockUser
	@Transactional
	public void update() throws Exception {
		final DocumentNode node = documentNodeFactory.save();
		final CommentDto commentDto = commentService.create(node.getId(), commentFactory.create());
		final String newText = "new text";
		commentDto.setText(newText);

		mvc.perform(put(endpoint("/api/documentnodes/", node.getId(), "/comments/", commentDto.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(commentDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.text").value(newText))
				.andExpect(status().isOk());

		assertThat(commentRepository.getOne(commentDto.getId()).getText()).isEqualTo(newText);
	}

	@Test
	@WithMockUser
	@Transactional
	public void deleteComment() throws Exception {
		final DocumentNode node = documentNodeFactory.save();
		final CommentDto commentDto = commentService.create(node.getId(), commentFactory.create());

		mvc.perform(delete(endpoint("/api/documentnodes/", node.getId(), "/comments/", commentDto.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(commentDto))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		assertThat(commentRepository.count()).isEqualTo(0);
	}

	@Test
	@WithMockUser
	@Transactional
	public void findAll() throws Exception {
		final DocumentNode node = documentNodeFactory.save();
		commentService.create(node.getId(), commentFactory.create());
		commentService.create(node.getId(), commentFactory.create());
		commentService.create(node.getId(), commentFactory.create());

		mvc.perform(get(endpoint("/api/documentnodes/", node.getId(), "/comments?page=0"))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.content.size()").value(2))
				.andExpect(status().isOk());

		mvc.perform(get(endpoint("/api/documentnodes/", node.getId(), "/comments?page=1"))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.content.size()").value(1))
				.andExpect(status().isOk());
	}
}
