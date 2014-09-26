
package com.yt.reader.model;

public class Chapter {
    private long id;

    private long bookId;

    private long location;

    private String title;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public long getLocation() {
        return location;
    }

    public void setLocation(long location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "Chapter [id=" + id + ", bookId=" + bookId + ", location=" + location + ", title="
                + title +  "]";
    }

}
