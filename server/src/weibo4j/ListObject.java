package weibo4j;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import weibo4j.http.Response;
import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

/**
 * A data class representing list object.
 * @author liudaoru - daoru at sina.com.cn
 */
public class ListObject extends WeiboResponse implements java.io.Serializable {

	/**
	 * random serialId
	 */
	private static final long serialVersionUID = 4208232205515192208L;

	/**
	 * list id
	 */
	private long id;

	/**
	 * The name of list
	 */
	private String name;

	/**
	 * The full name of list
	 */
	private String fullName;

	/**
	 * The short name of list
	 */
	private String slug;

	/**
	 * The description of list
	 */
	private String description;

	/**
	 * listObject对应uri
	 */
	private String uri;

	/**
	 * follow此list的用户数
	 */
	private int subscriberCount;

	/**
	 * 此list中的用户数
	 */
	private int memberCount;

	/**
	 * 是否公开
	 */
	private String mode;

	/**
	 * ListObject创建者用户对象
	 */
	private User user;

	/*package*/ListObject(Response res, Weibo weibo) throws WeiboException {
		super(res);
		init(res, res.asDocument().getDocumentElement(), weibo);
	}

	/*package*/ListObject(Response res, Element elem, Weibo weibo) throws WeiboException {
		super(res);
		init(res, elem, weibo);
	}

	/*package*/ListObject(JSONObject json) throws WeiboException {
		try {
			id = json.getLong("id");
			name = json.getString("name");
			fullName = json.getString("full_name");
			slug = json.getString("slug");
			description = json.getString("description");
			//
			subscriberCount = json.getInt("subscriber_count");
			memberCount = json.getInt("member_count");
			uri = json.getString("uri");
			mode = json.getString("mode");
			//
			if (!json.isNull("user")) {
				user = new User(json.getJSONObject("user"));
			}
		} catch (JSONException jsone) {
			throw new WeiboException(jsone.getMessage() + ":" + json.toString(), jsone);
		}

	}

	private void init(Response res, Element elem, Weibo weibo) throws WeiboException {
		ensureRootNodeNameIs("list", elem);
		id = getChildLong("id", elem);
		name = getChildText("name", elem);
		fullName = getChildText("full_name", elem);
		slug = getChildText("slug", elem);
		description = getChildText("description", elem);
		//
		subscriberCount = getChildInt("subscriber_count", elem);
		memberCount = getChildInt("member_count", elem);
		uri = getChildText("uri", elem);
		mode = getChildText("mode", elem);
		//
		NodeList statuses = elem.getElementsByTagName("user");
		if (statuses.getLength() != 0) {
			Element userElem = (Element) statuses.item(0);
			user = new User(res, userElem, weibo);
		}
	}

	// getters & setters -----------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getSubscriberCount() {
		return subscriberCount;
	}

	public void setSubscriberCount(int subscriberCount) {
		this.subscriberCount = subscriberCount;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	// methods -----------------------------------------------------

	/**
	 * 将XML格式结果字符串反序列化为ListObjectWapper对象
	 * @param res		weibo4j http请求返回对象
	 * @param weibo		Weibo对象实例
	 * @return		如果出现错误，或者结果为空，则返回空的ListObjectWapper对象
	 * @throws WeiboException
	 */
	/*package*/static ListObjectWapper constructListObjects(Response res, Weibo weibo) throws WeiboException {
		Document doc = res.asDocument();
		if (isRootNodeNilClasses(doc)) {
			return new ListObjectWapper(new ArrayList<ListObject>(0), 0, 0);
		} else {
			try {
				ensureRootNodeNameIs("lists_list", doc);
				Element root = doc.getDocumentElement();
				NodeList list = root.getElementsByTagName("lists");
				int length = list.getLength();
				if (length == 0) {
					return new ListObjectWapper(new ArrayList<ListObject>(0), 0, 0);
				}
				// 
				Element listsRoot = (Element) list.item(0);
				list = listsRoot.getElementsByTagName("list");
				length = list.getLength();
				List<ListObject> lists = new ArrayList<ListObject>(length);
				for (int i = 0; i < length; i++) {
					Element status = (Element) list.item(i);
					lists.add(new ListObject(res, status, weibo));
				}
				//
				long previousCursor = getChildLong("previous_curosr", root);
				long nextCursor = getChildLong("next_curosr", root);
				if (nextCursor == -1) { // 兼容不同标签名称
					nextCursor = getChildLong("nextCurosr", root);
				}
				return new ListObjectWapper(lists, previousCursor, nextCursor);
			} catch (WeiboException te) {
				if (isRootNodeNilClasses(doc)) {
					return new ListObjectWapper(new ArrayList<ListObject>(0), 0, 0);
				} else {
					throw te;
				}
			}
		}
	}

	/**
	 * 将JSON格式结果字符串反序列化为ListObjectWapper对象
	 * @param res		weibo4j http请求返回对象
	 * @return		如果出现错误，或者结果为空，则返回空的ListObjectWapper对象
	 * @throws WeiboException
	 */
	/*package*/static ListObjectWapper constructListObjects(Response res) throws WeiboException {
		JSONObject jsonLists = res.asJSONObject(); //asJSONArray();
		try {
			JSONArray list = jsonLists.getJSONArray("lists");
			int size = list.length();
			List<ListObject> listObjects = new ArrayList<ListObject>(size);
			for (int i = 0; i < size; i++) {
				listObjects.add(new ListObject(list.getJSONObject(i)));
			}
			long previousCursor = jsonLists.getLong("previous_curosr");
			long nextCursor = jsonLists.getLong("next_cursor");
			if (nextCursor == -1) { // 兼容不同标签名称
				nextCursor = jsonLists.getLong("nextCursor");
			}
			return new ListObjectWapper(listObjects, previousCursor, nextCursor);
		} catch (JSONException jsone) {
			throw new WeiboException(jsone);
		}
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		return obj instanceof ListObject && ((ListObject) obj).id == this.id;
	}

	@Override
	public String toString() {
		return "ListObject{" + "id=" + id + ", name='" + name + '\'' + ", fullName='" + fullName + '\'' + ", slug='"
				+ slug + '\'' + ", description='" + description + '\'' + ", subscriberCount=" + subscriberCount
				+ ", memberCount=" + memberCount + ", mode='" + mode + "', uri='" + uri + '\'' + ", user='"
				+ user.toString() + "'}";
	}

}
