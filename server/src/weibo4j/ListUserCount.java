/*
 * ListUserCount.java created on 2010-8-19 下午02:16:15 by bwl (Liu Daoru)
 */

package weibo4j;

import java.io.Serializable;

import org.w3c.dom.Element;

import weibo4j.http.Response;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

/**
 * List用户统计对象
 * 
 * @author		bwl (刘道儒)
 */
public class ListUserCount extends WeiboResponse implements Serializable {

	/**
	 * rand serial id
	 */
	private static final long serialVersionUID = 2638697034012299545L;

	/**
	 * 用户创建的List数
	 */
	private int listCount;

	/**
	 * 用户订阅的List数
	 */
	private int subscriberCount;

	/**
	 * 用户被添加为List成员的次数
	 */
	private int listedCount;

	/**
	 * 将JSON返回结果反序列化为ListUserCount对象的构造方法
	 * @param uid		用户ID
	 * @param json		结果json对象
	 * @throws WeiboException
	 * @throws JSONException
	 */
	public ListUserCount(JSONObject json) throws WeiboException, JSONException {
		this.listCount = json.getInt("lists");
		this.subscriberCount = json.getInt("subscriptions");
		this.listedCount = json.getInt("listed");
	}

	/**
	 * 将XML返回结果反序列化为ListUserCount对象的构造方法
	 * @param uid		用户ID
	 * @param res		结果XML对象
	 * @throws WeiboException
	 */
	public ListUserCount(Response res) throws WeiboException {
		Element elem = res.asDocument().getDocumentElement();
		ensureRootNodeNameIs("count", elem);
		this.listCount = getChildInt("lists", elem);
		this.subscriberCount = getChildInt("subscriptions", elem);
		this.listedCount = getChildInt("listed", elem);
	}

	@Override
	public int hashCode() {
		return (int) (listCount * 100 + subscriberCount * 10 + listedCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		return obj instanceof ListUserCount && ((ListUserCount) obj).hashCode() == this.hashCode();
	}

	public int getListCount() {
		return listCount;
	}

	public void setListCount(int listCount) {
		this.listCount = listCount;
	}

	public int getSubscriberCount() {
		return subscriberCount;
	}

	public void setSubscriberCount(int subscriberCount) {
		this.subscriberCount = subscriberCount;
	}

	public int getListedCount() {
		return listedCount;
	}

	public void setListedCount(int listedCount) {
		this.listedCount = listedCount;
	}

	@Override
	public String toString() {
		return "ListUserCount{listCount=" + listCount + ", subscriberCount=" + subscriberCount + ", listedCount="
				+ listedCount + '}';
	}

}
