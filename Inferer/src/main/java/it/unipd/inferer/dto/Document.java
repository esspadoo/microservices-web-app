package it.unipd.inferer.dto;

import java.util.Objects;

public record Document(String url, String title, String main_content, String topic) {
    public Document(String url, String title, String main_content, String topic){
        Objects.requireNonNull(url);
        this.url = url;
        Objects.requireNonNull(title);
        this.title = title;
        this.main_content = main_content;
        this.topic = topic;

    }
}
