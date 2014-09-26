package com.yt.reader.model;

public class Bookmark {
	private long id;

	private long bookId;

	private long location;

	private String description;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Bookmark [id=" + id + ", bookId=" + bookId + ", location="
				+ location + ", description=" + description + "]";
	}

}
