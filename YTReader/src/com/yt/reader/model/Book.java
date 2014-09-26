package com.yt.reader.model;

import java.util.Date;
import android.graphics.Bitmap;
import com.yt.reader.utils.DateUtils;

public class Book {
	private long id;

	private String name;

	private String realName;

	private String author;

	private String size;

	private String fileType;

	private long currentLocation;

	private long totalPage;

	private long lastModifyTime;

	private long lastReadingTime;

	private long addedTime;

	private String path;

	private Bitmap bitmap;
	
	private String coverPath;

	private boolean isDRM;// TODO

	// add
	private String language;

	public Book() {

	}

	public Book(Book b) {
		id = b.id;
		name = b.name;
		realName = b.realName;
		author = b.author;
		size = b.size;
		fileType = b.fileType;
		currentLocation = b.currentLocation;
		totalPage = b.totalPage;
		lastModifyTime = b.lastModifyTime;
		lastReadingTime = b.lastReadingTime;
		addedTime = b.addedTime;
		path = b.path;
		bitmap = b.bitmap;
		coverPath = b.coverPath;
		isDRM = b.isDRM;
		language = b.language;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(long currentLocation) {
		this.currentLocation = currentLocation;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public long getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public long getLastReadingTime() {
		return lastReadingTime;
	}

	public void setLastReadingTime(long lastReadingTime) {
		this.lastReadingTime = lastReadingTime;
	}

	public long getAddedTime() {
		return addedTime;
	}

	public void setAddedTime(long addedTime) {
		this.addedTime = addedTime;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getCoverPath() {
		return coverPath;
	}

	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
	}
	public boolean isDRM() {
		return isDRM;
	}

	public void setDRM(boolean isDRM) {
		this.isDRM = isDRM;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * 返回是否有页码。
	 * 
	 * @return true:页码；false:百分比。
	 */
	public boolean hasPageNumber() {
		return fileType.equals("PDF");
	}

	@Override
	public String toString() {
		return "Book [id="
				+ id
				+ ", name="
				+ name
				+ ", realName="
				+ realName
				+ ", author="
				+ author
				+ ", size="
				+ size
				+ ", fileType="
				+ fileType
				+ ", currentLocation="
				+ currentLocation
				+ ", totalPage="
				+ totalPage
				+ ", lastModifyTime="
				+ DateUtils.dateToString(new Date(lastModifyTime),
						"yyyy-MM-dd hh:mm:ss")
				+ ", lastReadingTime="
				+ DateUtils.dateToString(new Date(lastReadingTime),
						"yyyy-MM-dd hh:mm:ss")
				+ ", addedTime="
				+ DateUtils.dateToString(new Date(addedTime),
						"yyyy-MM-dd hh:mm:ss") + ", path=" + path + ", isDRM="
				+ isDRM + ", language=" + language + "]";
	}

	// add
	public boolean pathNameEquals(Book item) {
		return eq(name, item.name) && eq(path, item.path);
	}

	public static boolean eq(String s1, String s2) {
		if (s1 == null)
			return s2 == null;
		return s1.equals(s2);
	}

}
