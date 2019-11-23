package com.code4ro.legalconsultation.model.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name="comments")
@Getter
@Setter
public class Comment extends BaseEntity {
    @Column(name = "text")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_node_id")
    private DocumentNode documentNode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private ApplicationUser owner;

    @Column(name = "last_edit_date", nullable=false)
    @Temporal(TemporalType.DATE)
    @LastModifiedDate
    private Date lastEditDateTime;

    private boolean allowVotes;
}
