package weibo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import weibo4j.http.Response;
import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;
/**
 * 
 * @author haidong
 *
 */

public class Tag extends WeiboResponse implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String value;

	public Tag(Response res,Element elem) throws WeiboException{
		ensureRootNodeNameIs("tag", elem);
		id = getChildText("id",elem);
		value = getChildText("value",elem);
	
	
	}
	public Tag(Response res ,Element elem,Weibo weibo,String str) throws WeiboException{
		ensureRootNodeNameIs("tagid", elem);
		id=elem.getNodeName();
		value=elem.getTextContent();
	}
	public Tag(Response res,Element elem,Weibo weibo) throws WeiboException{
		ensureRootNodeNameIs("tagid", elem);
		id=elem.getNodeName();
		value=elem.getTextContent();
	}

	

	public Tag(JSONObject json)throws WeiboException,JSONException{
		if(json.getString("id").isEmpty()){
			Iterator i=json.keys();
			for(;i.hasNext();){
				id=(String) i.next();
				value=json.getString(id);
			} 
		}
		else{
			id=json.getString("id");
			value=json.getString("value");
		}


	}

	public static List<Tag> constructTags(Response res,Weibo weibo) throws WeiboException {
		Document doc = res.asDocument();
		if (isRootNodeNilClasses(doc)) {
			return new ArrayList<Tag>(0);
		} else {
			try {
				
				ensureRootNodeNameIs("tags", doc);
				NodeList list = doc.getDocumentElement().getElementsByTagName(
				"tag");
				int size = list.getLength();
				List<Tag> tags = new ArrayList<Tag>(size);
				for (int i = 0; i < size; i++) {
					tags.add(new Tag(res, (Element) list.item(i)));
				}
				return tags;
			} catch (WeiboException te) {
				ensureRootNodeNameIs("nil-classes", doc);
				return new ArrayList<Tag>(0);
			}
		}
	}
    public static List<Tag> createTags(Response res,Weibo weibo) throws WeiboException{
		Document doc=res.asDocument();
		if(isRootNodeNilClasses(doc)){
			return new ArrayList<Tag>(0);
		}else{
			try{
				ensureRootNodeNameIs("tagids",doc);
				NodeList list = doc.getDocumentElement().getElementsByTagName(
				"tagid");
				int size = list.getLength();
				List<Tag> tags = new ArrayList<Tag>(size);
				for (int i = 0; i < size; i++) {
					tags.add(new Tag(res, (Element) list.item(i),null));
				}
				return tags;
			} catch (WeiboException te) {
				ensureRootNodeNameIs("nil-classes", doc);
				return new ArrayList<Tag>(0);
			}
			
		}
    
    	
    }

    public static List<Tag> destroyTags(Response res,Weibo weibo) throws WeiboException{
		Document doc=res.asDocument();
		if(isRootNodeNilClasses(doc)){
			return new ArrayList<Tag>(0);
		}else{
			try{
				ensureRootNodeNameIs("tags",doc);
				NodeList list = doc.getDocumentElement().getElementsByTagName(
				"tagid");
				int size = list.getLength();
				List<Tag> tags = new ArrayList<Tag>(size);
				for (int i = 0; i < size; i++) {
					tags.add(new Tag(res, (Element)list.item(i),null,null));
				}
				return tags;
			} catch (WeiboException te) {
				ensureRootNodeNameIs("nil-classes", doc);
				return new ArrayList<Tag>(0);
			}
			
		}
    
    	
    }



	static List<Tag> constructTags(Response res) throws WeiboException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<Tag>tags  = new ArrayList<Tag>(size);
			for (int i = 0; i < size; i++) {
				tags.add(new Tag(list.getJSONObject(i)));
			}
			return tags;
		} catch (JSONException jsone) {
			throw new WeiboException(jsone);
		} catch (WeiboException te) {
			throw te;
		}
	}
	 public int hashCode() {
	        return Integer.parseInt(id);
	    }
        
	    @Override
	    public boolean equals(Object obj) {
	        if (null == obj) {
	            return false;
	        }
	        if (this == obj) {
	            return true;
	        }
	        return obj instanceof Tag && ((Tag) obj).id == this.id;
	    }

	 
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}



	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public String toString() {

		return "tags{ " +id +
		"," +value+
		'}';

	}


}
