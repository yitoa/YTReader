package com.yt.reader.format.pdf;

/**
 * 用于从底层返回搜索结果。
 * 
 * @author lsj
 * 
 */
public class SearchItem {
	private int page;// 页码
	private String description;// 搜索结果描述
	private float left1, top1, right1, bottom1;// 用于高亮显示的矩形坐标
	private float left2, top2, right2, bottom2;// 用于高亮显示的矩形坐标，如果搜索结果显示在两行

	public SearchItem(int page, String description, float left1, float top1,
			float right1, float bottom1, float left2, float top2, float right2,
			float bottom2) {
		this.page = page;
		this.description = description;
		this.left1 = left1;
		this.top1 = top1;
		this.right1 = right1;
		this.bottom1 = bottom1;
		this.left2 = left2;
		this.top2 = top2;
		this.right2 = right2;
		this.bottom2 = bottom2;
	}

	public int getPage() {
		return page;
	}

	public String getDescription() {
		return description;
	}

	public float getLeft1() {
		return left1;
	}

	public float getTop1() {
		return top1;
	}

	public float getRight1() {
		return right1;
	}

	public float getBottom1() {
		return bottom1;
	}

	public float getLeft2() {
		return left2;
	}

	public float getTop2() {
		return top2;
	}

	public float getRight2() {
		return right2;
	}

	public float getBottom2() {
		return bottom2;
	}

	@Override
	public String toString() {
		return "SearchItem [page=" + page + ", left1=" + left1 + ", top1="
				+ top1 + ", right1=" + right1 + ", bottom1=" + bottom1
				+ ", left2=" + left2 + ", top2=" + top2 + ", right2=" + right2
				+ ", bottom2=" + bottom2 + ", description=" + description + "]";
	}

}
