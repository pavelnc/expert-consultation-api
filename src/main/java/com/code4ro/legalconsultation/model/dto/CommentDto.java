package com.code4ro.legalconsultation.model.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto extends BaseEntityDto {
	private String text;
	private Date lastEditDateTime;
	private long upVotes;
	private long downVotes;
	private boolean allowVotes;
}
