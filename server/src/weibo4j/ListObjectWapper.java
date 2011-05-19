/*
 * ListObjectWapper.java created on 2010-7-28 上午08:48:35 by bwl (Liu Daoru)
 */

package weibo4j;

import java.io.Serializable;
import java.util.List;

/**
 * 对ListObject对象列表进行的包装，以支持cursor相关信息传递
 * @author liudaoru - daoru at sina.com.cn
 */
public class ListObjectWapper implements Serializable {

	private static final long serialVersionUID = -3119168701303920284L;

	/**
	 * list对象列表
	 */
	private List<ListObject> listObjects;

	/**
	 * 向前翻页的cursor
	 */
	private long previousCursor;

	/**
	 * 向后翻页的cursor
	 */
	private long nextCursor;

	public ListObjectWapper(List<ListObject> listObjects, long previousCursor, long nextCursor) {
		this.listObjects = listObjects;
		this.previousCursor = previousCursor;
		this.nextCursor = nextCursor;
	}

	public List<ListObject> getListObjects() {
		return listObjects;
	}

	public void setListObjects(List<ListObject> listObjects) {
		this.listObjects = listObjects;
	}

	public long getPreviousCursor() {
		return previousCursor;
	}

	public void setPreviousCursor(long previousCursor) {
		this.previousCursor = previousCursor;
	}

	public long getNextCursor() {
		return nextCursor;
	}

	public void setNextCursor(long nextCursor) {
		this.nextCursor = nextCursor;
	}

}
