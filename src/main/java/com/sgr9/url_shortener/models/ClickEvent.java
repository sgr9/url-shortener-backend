package com.sgr9.url_shortener.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDateTime clickDate;

    @ManyToOne
    @JoinColumn(name = "url_mapping_id")
    private UrlMapping urlMapping;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getClickDate() {
        return clickDate;
    }

    public void setClickDate(LocalDateTime clickDate) {
        this.clickDate = clickDate;
    }

    public UrlMapping getUrlMapping() {
        return urlMapping;
    }

    public void setUrlMapping(UrlMapping urlMapping) {
        this.urlMapping = urlMapping;
    }
}
