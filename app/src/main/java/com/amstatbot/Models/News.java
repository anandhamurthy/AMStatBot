package com.amstatbot.Models;

public class News {
    String link, title, text;

    public News() {
    }

    public News(String link, String title, String text) {
        this.link = link;
        this.title = title;
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
