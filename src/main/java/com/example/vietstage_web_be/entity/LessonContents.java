package com.example.vietstage_web_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "lesson_contents")
public class LessonContents {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "title")
    private String title;

    @Column(name = "content_url", nullable = false)
    private String contentUrl;

    @Column(name = "display_order")
    private Long displayOrder;

    @Column(name = "created_at")
    private Date createdAt;

}
