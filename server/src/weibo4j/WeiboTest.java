package weibo4j;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import weibo4j.http.ImageItem;
import weibo4j.org.json.JSONException;

public class WeiboTest {

	Weibo weibo=new Weibo("2057sky@sina.cn","s172721");


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetToken() {
		System.setProperty("weibo4j.oauth.consumerKey", Weibo.CONSUMER_KEY);
		System.setProperty("weibo4j.oauth.consumerSecret", Weibo.CONSUMER_SECRET);


		String consumerKey = Configuration.getOAuthConsumerKey(Weibo.CONSUMER_KEY);
		String consumerSecret = Configuration.getOAuthConsumerSecret(Weibo.CONSUMER_SECRET);

		assertNotNull("consumerKey is null", consumerKey); 
		assertNotNull("consumerSecret is null", consumerSecret);
		assertTrue(consumerKey.length()!=0&&consumerSecret.length()!=0);
		assertTrue(consumerKey!=null&&consumerSecret!=null);
	}




	@Test
	public void testGetSearchBaseURL() {
		assertEquals("http://api.t.sina.com.cn/", weibo.getSearchBaseURL());
	}


	@Test
	public void testGetOAuthRequestToken() throws WeiboException {
		assertNotNull(weibo.getOAuthRequestToken());
	}



	@Test
	public void testGetTimeline() throws WeiboException {
		assertNotNull(weibo.getPublicTimeline());
		assertNotNull(weibo.getHomeTimeline());
		assertNotNull(weibo.getHomeTimeline(new Paging(1,200)));
		assertNotNull(weibo.getFriendsTimeline());
		assertNotNull(weibo.getFriendsTimeline(new Paging(1,30)));
		assertNotNull(weibo.getUserTimeline("1377583044"));
	}

	@Test
	public void testGetRateLimitStatus() throws WeiboException {
		assertNotNull(weibo.getRateLimitStatus());
	}



	@Test
	public void testGetMentions() throws WeiboException {
		assertNotNull(weibo.getMentions());
		assertNotNull(weibo.getMentions(new Paging(1,30)));
	}




	@Test
	public void testShowStatus() throws WeiboException {
		long id = 4052331047L;
		assertNotNull(weibo.showStatus(id));
	}


	@Test
	public void testUpdateStatusString() throws WeiboException {
		String status="A year without rain";
		assertNotNull(weibo.updateStatus(status));
	}

	@Test
	public void testUpdateComment() throws WeiboException {
		String comment="Baby I like it";
		String id = "4052331047";
		assertNotNull(weibo.updateComment(comment, id, null));
	}

	@Test
	public void testUploadStatusStringImageItem() throws Exception {
		byte[] content= readFileImage("c:/1.jpg");
		ImageItem pic=new ImageItem("pic",content);
		String url1=java.net.URLEncoder.encode("Nice ","UTF-8");
		assertNotNull(weibo.uploadStatus(url1, pic));
	}
	public static byte[] readFileImage(String filename)throws IOException{
		BufferedInputStream bufferedInputStream=new BufferedInputStream(
				new FileInputStream(filename));
		int len =bufferedInputStream.available();
		byte[] bytes=new byte[len];
		int r=bufferedInputStream.read(bytes);
		if(len !=r){
			bytes=null;
			throw new IOException("???");
		}
		bufferedInputStream.close();
		return bytes;
	}




	@Test
	public void testUpdateStatusStringDoubleDouble() throws WeiboException, JSONException, UnsupportedEncodingException {

		String status="time to say goodbye";
		double latitude=39.9289;
		double longitude=116.3883;
		assertNotNull(weibo.updateStatus(status, latitude, longitude));
	}

	@Test
	public void testUpdateStringLong() throws WeiboException {
		String status="???";
		long inReplyToStatusId=4052331047L;
		assertNotNull(weibo.update(status, inReplyToStatusId));
	}


	@Test
	public void testUpdateStatusStringLongDoubleDouble() throws WeiboException {
		String status="这是不是在伊拉克转发的？";
		long inReplyToStatusId=4052331047L;
		double latitude=33.14;
		double longitude=44.22;
		assertNotNull(weibo.updateStatus(status, inReplyToStatusId, latitude, longitude));
	}


	@Test
	public void testDestroyStatus() throws WeiboException, InterruptedException {
		Status status=weibo.update("test 111111");
		Thread.sleep(1000);
		assertNotNull(weibo.destroyStatus(status.getId()));
	}

	@Test
	public void testDestroyComment() throws WeiboException, InterruptedException {
		Status status=weibo.updateStatus("HAHA207");
		String id=status.getId()+"";
		Comment comment=weibo.updateComment("heihei207", id, null);
		Thread.sleep(1000);
		assertNotNull(weibo.destroyComment(comment.getId()));
	}




	@Test
	public void testRetweetStatus() throws WeiboException {
		long id = 4052331047L;
		assertNotNull(weibo.retweetStatus(id));
	}



	@Test
	public void testShowUser() throws WeiboException {
		assertNotNull(weibo.showUser("1377583044"));
	}

	@Test
	public void testGetHotUsers() throws WeiboException {
		String category="ent";
		assertNotNull(weibo.getHotUsers(category));
	}





	@Test
	public void testGetStatuses() throws WeiboException {
		assertNotNull(weibo.getFollowersStatuses());
		assertNotNull(weibo.getFollowersStatuses(new Paging(1,10)));
		assertNotNull(weibo.getFriendsStatuses());
		assertNotNull(weibo.getFriendsStatuses(new Paging(1,50)));
	}






	@Test
	public void testDirectMessage() throws WeiboException, InterruptedException {
		assertNotNull(weibo.getDirectMessages());
		assertNotNull(weibo.getDirectMessages(new Paging(1,10)));
		assertNotNull(weibo.getSentDirectMessages());
		assertNotNull(weibo.getSentDirectMessages(new Paging(1,10)));
		DirectMessage dm=weibo.sendDirectMessage("1869273855", "Love the way you lie!");
		assertTrue(dm!=null);
		Thread.sleep(1000);
		assertNotNull(weibo.destroyDirectMessage(dm.getId()));

	}




	@Test
	public void testFriendship() throws WeiboException, InterruptedException {
		assertNotNull(weibo.createFriendship("1646678371"));
		Thread.sleep(1000);
		assertNotNull(weibo.destroyFriendship("1646678371"));
		assertNotNull(weibo.existsFriendship("1377583044", "1646678371"));
	}





	@Test
	public void testFriendsIDs() throws WeiboException {
		assertNotNull(weibo.getFriendsIDs());
		assertNotNull(weibo.getFriendsIDs(new Paging(1,10)));
		assertNotNull(weibo.getFriendsIDs(1377583044));
		assertNotNull(weibo.getFriendsIDs("nicotine"));
	}

	@Test
	public void testFollowersIDs() throws WeiboException {
		assertNotNull(weibo.getFollowersIDs());
		assertNotNull(weibo.getFollowersIDs(new Paging(1,10)));
		assertNotNull(weibo.getFriendsIDs(1377583044));
		assertNotNull(weibo.getFriendsIDs("nicotine"));
	}



	@Test
	public void testVerifyCredentials() throws WeiboException {
		assertNotNull(weibo.verifyCredentials());
	}



	@Test
	public void testRateLimitStatus() throws WeiboException {
		assertNotNull(weibo.rateLimitStatus());
	}



	@Test
	public void testFavorites() throws WeiboException, InterruptedException {
		assertNotNull(weibo.getFavorites());
		assertNotNull(weibo.getFavorites(2));
		long id = 4052331047L;
		assertNotNull(weibo.createFavorite(id));
		Thread.sleep(1000);
		assertNotNull(weibo.destroyFavorite(id));
	}




	@Test
	public void testBlock() throws WeiboException, InterruptedException {
		assertNotNull(weibo.getBlockingUsers());
		assertNotNull(weibo.createBlock("1775590093"));
		Thread.sleep(1000);
		assertNotNull(weibo.existsBlock("1775590093"));
		Thread.sleep(1000);
		assertNotNull(weibo.destroyBlock("1775590093"));
		Thread.sleep(1000);
		assertNotNull(weibo.existsBlock("1775590093"));
		assertNotNull(weibo.getBlockingUsers());
		assertNotNull(weibo.getBlockingUsersIDs());

	}




	@Test
	public void testTest() throws WeiboException {
		assertNotNull(weibo.test());
		assertEquals(true, weibo.test());
	}



	@Test
	public void testGetComments() throws WeiboException {
		assertNotNull(weibo.getComments("4052331047"));
		assertNotNull(weibo.getCommentsTimeline());
		assertNotNull(weibo.getCommentsByMe());
		assertNotNull(weibo.getCommentsToMe());
	}



	@Test
	public void testGetUnread() throws WeiboException, JSONException {
		assertNotNull(weibo.getUnread());
	}

	@Test
	public void testRepost() throws WeiboException {
		assertNotNull("Error: repeated weibo text!",weibo.repost("4052331047", "repeated 呢妹!"));
	}

	@Test
	public void testReply() throws WeiboException {
		String sid="4052331047";
		List<Comment> comments=weibo.getComments(sid);

		String cid=comments.get(0).getId()+"";
		System.out.println(cid+"");
		String status="This is life,hold on 22tight~ahaya!";
		assertNotNull("Error: repeated weibo text!",weibo.reply(sid,cid,status));
	}

	@Test
	public void testShowFriendshipsString() throws WeiboException {
		assertNotNull(weibo.showFriendships("1775590093"));
		assertNotNull(weibo.showFriendships("1377583044", "1775590093"));
	}

	@Test
	public void testEndSession() throws WeiboException {
		assertNotNull(weibo.endSession());
	}


}
