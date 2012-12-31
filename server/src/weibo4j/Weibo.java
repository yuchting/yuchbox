/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package weibo4j;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import weibo4j.http.AccessToken;
import weibo4j.http.HttpClient;
import weibo4j.http.ImageItem;
import weibo4j.http.PostParameter;
import weibo4j.http.RequestToken;
import weibo4j.http.Response;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

/**
 * A java reporesentation of the <a href="http://open.t.sina.com.cn/wiki/">Weibo API</a>
 */
public class Weibo extends WeiboSupport implements java.io.Serializable {
	public static String CONSUMER_KEY = "";
	public static String CONSUMER_SECRET = "";
    private String baseURL = Configuration.getScheme() + "api.t.sina.com.cn/";
    private String searchBaseURL = Configuration.getScheme() + "api.t.sina.com.cn/";
    private static final long serialVersionUID = -1486360080128882436L;

    public Weibo() {
        super();
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        http.setRequestTokenURL(Configuration.getScheme() + "api.t.sina.com.cn/oauth/request_token");
        http.setAuthorizationURL(Configuration.getScheme() + "api.t.sina.com.cn/oauth/authorize");
        http.setAccessTokenURL(Configuration.getScheme() + "api.t.sina.com.cn/oauth/access_token");


    }

    /**
     * Sets token information
     * @param token
     * @param tokenSecret
     */
    public void setToken(String token, String tokenSecret) {
        http.setToken(token, tokenSecret);
    }

    public Weibo(String baseURL) {
        this();
        this.baseURL = baseURL;
    }

    public Weibo(String id, String password) {
        this();
        setUserId(id);
        setPassword(password);
    }

    public Weibo(String id, String password, String baseURL) {
        this();
        setUserId(id);
        setPassword(password);
        this.baseURL = baseURL;
    }

    /**
     * Sets the base URL
     *
     * @param baseURL String the base URL
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Returns the base URL
     *
     * @return the base URL
     */
    public String getBaseURL() {
        return this.baseURL;
    }

    /**
     * Sets the search base URL
     *
     * @param searchBaseURL the search base URL
     * @since Weibo4J 1.1220
     */
    public void setSearchBaseURL(String searchBaseURL) {
        this.searchBaseURL = searchBaseURL;
    }

    /**
     * Returns the search base url
     * @return search base url
     * @since Weibo4J 1.1220
     */
    public String getSearchBaseURL(){
        return this.searchBaseURL;
    }

    /**
     *
     * @param consumerKey OAuth consumer key
     * @param consumerSecret OAuth consumer secret
     * @since Weibo4J 1.1220
     */
    public synchronized void setOAuthConsumer(String consumerKey, String consumerSecret){
        this.http.setOAuthConsumer(consumerKey, consumerSecret);
    }

    /**
     * Retrieves a request token
     * @return generated request token.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 - 6.1.  Obtaining an Unauthorized Request Token</a>
     */
    public RequestToken getOAuthRequestToken() throws WeiboException {
        return http.getOAuthRequestToken();
    }

    public RequestToken getOAuthRequestToken(String callback_url) throws WeiboException {
        return http.getOauthRequestToken(callback_url);
    }

    /**
     * Retrieves an access token assosiated with the supplied request token.
     * @param requestToken the request token
     * @return access token associsted with the supplied request token.
     * @throws WeiboException when Weibo service or network is unavailable, or the user has not authorized
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Oauth/access_token">Oauth/access token </a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     * @since Weibo4J 1.1220
     */
    public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken) throws WeiboException {
        return http.getOAuthAccessToken(requestToken);
    }

    /**
     * Retrieves an access token assosiated with the supplied request token and sets userId.
     * @param requestToken the request token
     * @param pin pin
     * @return access token associsted with the supplied request token.
     * @throws WeiboException when Weibo service or network is unavailable, or the user has not authorized
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Oauth/access_token">Oauth/access token </a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     * @since  Weibo4J 1.1220
     */
    public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken, String pin) throws WeiboException {
        AccessToken accessToken = http.getOAuthAccessToken(requestToken, pin);
        setUserId(accessToken.getScreenName());
        return accessToken;
    }

    /**
     * Retrieves an access token assosiated with the supplied request token and sets userId.
     * @param token request token
     * @param tokenSecret request token secret
     * @return access token associsted with the supplied request token.
     * @throws WeiboException when Weibo service or network is unavailable, or the user has not authorized
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Oauth/access_token">Oauth/access token </a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     * @since  Weibo4J 1.1220
     */
    public synchronized AccessToken getOAuthAccessToken(String token, String tokenSecret) throws WeiboException {
        AccessToken accessToken = http.getOAuthAccessToken(token, tokenSecret);
        setUserId(accessToken.getScreenName());
        return accessToken;
    }

    /**
     * Retrieves an access token assosiated with the supplied request token.
     * @param token request token
     * @param tokenSecret request token secret
     * @param oauth_verifier oauth_verifier or pin
     * @return access token associsted with the supplied request token.
     * @throws WeiboException when Weibo service or network is unavailable, or the user has not authorized
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Oauth/access_token">Oauth/access token </a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     * @since  Weibo4J 1.1220
     */
    public synchronized AccessToken getOAuthAccessToken(String token
            , String tokenSecret, String oauth_verifier) throws WeiboException {
        return http.getOAuthAccessToken(token, tokenSecret, oauth_verifier);
    }

    public synchronized AccessToken getXAuthAccessToken(String userId,String passWord,String mode) throws WeiboException {
    	return http.getXAuthAccessToken(userId, passWord, mode);
    }

    /**
     * Sets the access token
     * @param accessToken accessToken
     * @since  Weibo4J 1.1220
     */
    public void setOAuthAccessToken(AccessToken accessToken){
        this.http.setOAuthAccessToken(accessToken);
    }

    /**
     * Sets the access token
     * @param token token
     * @param tokenSecret token secret
     * @since  Weibo4J 1.1220
     */
    public void setOAuthAccessToken(String token, String tokenSecret) {
        setOAuthAccessToken(new AccessToken(token, tokenSecret));
    }


    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */

    private Response get(String url, boolean authenticate) throws WeiboException {
        return get(url, null, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @param name1        the name of the first parameter
     * @param value1       the value of the first parameter
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */

    protected Response get(String url, String name1, String value1, boolean authenticate) throws WeiboException {
        return get(url, new PostParameter[]{new PostParameter(name1, value1)}, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param name1        the name of the first parameter
     * @param value1       the value of the first parameter
     * @param name2        the name of the second parameter
     * @param value2       the value of the second parameter
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */

    protected Response get(String url, String name1, String value1, String name2, String value2, boolean authenticate) throws WeiboException {
        return get(url, new PostParameter[]{new PostParameter(name1, value1), new PostParameter(name2, value2)}, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param params       the request parameters
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */
    protected Response get(String url, PostParameter[] params, boolean authenticate) throws WeiboException {
		if (url.indexOf("?") == -1) {
			url += "?source=" + CONSUMER_KEY;
		} else if (url.indexOf("source") == -1) {
			url += "&source=" + CONSUMER_KEY;
		}
    	if (null != params && params.length > 0) {
			url += "&" + HttpClient.encodeParameters(params);
		}
        return http.get(url, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param params       the request parameters
     * @param paging controls pagination
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */
    protected Response get(String url, PostParameter[] params, Paging paging, boolean authenticate) throws WeiboException {
        if (null != paging) {
            List<PostParameter> pagingParams = new ArrayList<PostParameter>(4);
            if (-1 != paging.getMaxId()) {
                pagingParams.add(new PostParameter("max_id", String.valueOf(paging.getMaxId())));
            }
            if (-1 != paging.getSinceId()) {
                pagingParams.add(new PostParameter("since_id", String.valueOf(paging.getSinceId())));
            }
            if (-1 != paging.getPage()) {
                pagingParams.add(new PostParameter("page", String.valueOf(paging.getPage())));
            }
            if (-1 != paging.getCount()) {
                if (-1 != url.indexOf("search")) {
                    // search api takes "rpp"
                    pagingParams.add(new PostParameter("rpp", String.valueOf(paging.getCount())));
                } else {
                    pagingParams.add(new PostParameter("count", String.valueOf(paging.getCount())));
                }
            }
            PostParameter[] newparams = null;
            PostParameter[] arrayPagingParams = pagingParams.toArray(new PostParameter[pagingParams.size()]);
            if (null != params) {
                newparams = new PostParameter[params.length + pagingParams.size()];
                System.arraycopy(params, 0, newparams, 0, params.length);
                System.arraycopy(arrayPagingParams, 0, newparams, params.length, pagingParams.size());
            } else {
                if (0 != arrayPagingParams.length) {
                    String encodedParams = HttpClient.encodeParameters(arrayPagingParams);
                    if (-1 != url.indexOf("?")) {
                        url += "&source=" + CONSUMER_KEY +
                        		"&" + encodedParams;
                    } else {
                        url += "?source=" + CONSUMER_KEY +
                        		"&" + encodedParams;
                    }
                }
            }
            return get(url, newparams, authenticate);
        } else {
            return get(url, params, authenticate);
        }
    }

    /**
     * Returns tweets that match a specified query.
     * <br>This method calls http://api.t.sina.com.cn/users/search.format
     * @param query - the search condition
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/search">users/search </a>
     */
    public QueryResult search(Query query) throws WeiboException {
        try{
        return new QueryResult(get(searchBaseURL + "search.json", query.asPostParameters(), false), this);
        }catch(WeiboException te){
            if(404 == te.getStatusCode()){
                return new QueryResult(query);
            }else{
                throw te;
            }
        }
    }
    /**
     * Search Statues (much)(the only condition combination of co-developer open)
     * @param q
     * @return a list of statuses
     * @throws WeiboException
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/search">Statuses/search </a>
     */
    public List<Status>search(String q)throws WeiboException{
    	return Status.constructStatuses(http.get(getBaseURL()+"statuses/search.json?source="+Weibo.CONSUMER_KEY
    			+"&q="+q,true));
    }

    /**
     * Returns the top ten topics that are currently trending on Weibo.  The response includes the time of the request, the name of each trend.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public Trends getTrends() throws WeiboException {
        return Trends.constructTrends(get(searchBaseURL + "trends.json", false));
    }

    /**
     * Returns the current top 10 trending topics on Weibo.  The response includes the time of the request, the name of each trending topic.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public Trends getCurrentTrends() throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL + "trends/current.json"
                , false)).get(0);
    }

    /**
     * Returns the current top 10 trending topics on Weibo.  The response includes the time of the request, the name of each trending topic.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public Trends getCurrentTrends(boolean excludeHashTags) throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL + "trends/current.json"
                + (excludeHashTags ? "?exclude=hashtags" : ""), false)).get(0);
    }


    /**
     * Returns the top 20 trending topics for each hour in a given day.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public List<Trends> getDailyTrends() throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL + "trends/daily.json", false));
    }

    /**
     * Returns the top 20 trending topics for each hour in a given day.
     * @param date Permits specifying a start date for the report.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public List<Trends> getDailyTrends(Date date, boolean excludeHashTags) throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL
                + "trends/daily.json?date=" + toDateStr(date)
                + (excludeHashTags ? "&exclude=hashtags" : ""), false));
    }

    private String toDateStr(Date date){
        if(null == date){
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Returns the top 30 trending topics for each day in a given week.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public List<Trends> getWeeklyTrends() throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL
                + "trends/weekly.json", false));
    }

    /**
     * Returns the top 30 trending topics for each day in a given week.
     * @param date Permits specifying a start date for the report.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     */
    public List<Trends> getWeeklyTrends(Date date, boolean excludeHashTags) throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL
                + "trends/weekly.json?date=" + toDateStr(date)
                + (excludeHashTags ? "&exclude=hashtags" : ""), false));
    }

    /* Status Methods */

    /**
     * Returns the 20 most recent statuses from non-protected users who have set a custom user icon.
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline.format
     *
     * @return list of statuses of the Public Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     */
    public List<Status> getPublicTimeline() throws
            WeiboException {
    	/*modify by sycheng edit with json */
        return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.json", true));

//      return Status.constructStatuses(get(getBaseURL() +"statuses/public_timeline.xml", true), this);
    }

    public RateLimitStatus getRateLimitStatus()throws
            WeiboException {
    	/*modify by sycheng edit with json */
        return new RateLimitStatus(get(getBaseURL() +
                "account/rate_limit_status.json", true),this);
    }

    /**
     * Returns only public statuses with an ID greater than (that is, more recent than) the specified ID.
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline.format
     *
     * @param sinceID returns only public statuses with an ID greater than (that is, more recent than) the specified ID
     * @return the 20 most recent statuses
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated use getPublicTimeline(long sinceID) instead
     */
    public List<Status> getPublicTimeline(int sinceID) throws
            WeiboException {
        return getPublicTimeline((long)sinceID);
    }
    /**
     * Returns only public statuses with an ID greater than (that is, more recent than) the specified ID.
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline.format
     *
     * @param sinceID returns only public statuses with an ID greater than (that is, more recent than) the specified ID
     * @return the 20 most recent statuses
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     */
    public List<Status> getPublicTimeline(long sinceID) throws
            WeiboException {
      /*  return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.xml", null, new Paging((long) sinceID)
                , false), this);*/
    	return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.json", null, new Paging((long) sinceID)
                , false));
    }

    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls http://api.t.sina.com.cn/statuses/home_timeline.format
     *
     * @return list of the home Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @since  Weibo4J 1.1220
     */
    public List<Status> getHomeTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.xml", true), this);
    }


    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls  http://api.t.sina.com.cn/statuses/home_timeline.format
     *
     * @param paging controls pagination
     * @return list of the home Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @since  Weibo4J 1.1220
     */
    public List<Status> getHomeTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", null, paging, true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.xml", null, paging, true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating1 user and that user's friends.
     * It's also possible to request another user's friends_timeline via the id parameter below.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml", true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimelineByPage(int page) throws
            WeiboException {
        return getFriendsTimeline(new Paging(page));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(int page) throws
            WeiboException {
        return getFriendsTimeline(new Paging(page));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param page    the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(long sinceId, int page) throws
            WeiboException {
        return getFriendsTimeline(new Paging(page).sinceId(sinceId));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the friends_timeline
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimelineByPage(String id, int page) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id, int page) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(long sinceId, String id, int page) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param paging controls pagination
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json",null, paging, true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml",null, paging, true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param paging controls pagination
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id, Paging paging) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }


    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(Date since) throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml",
                "since", format.format(since), true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     */
    public List<Status> getFriendsTimeline(long sinceId) throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml",
                "since_id", String.valueOf(sinceId), true), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id,Date since) throws WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }
    
    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id, long sinceId) throws WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public List<Status> getUserTimeline(String id, int count
            , Date since) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                "since", format.format(since), "count", String.valueOf(count), http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since  Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, int count,long sinceId) throws WeiboException {
        return getUserTimeline(id, new Paging(sinceId).count(count));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param paging controls pagenation
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     */
    public List<Status> getUserTimeline(String id, Paging paging)
            throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                null, paging, http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, Date since) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                "since", format.format(since), http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, int count) throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                "count", String.valueOf(count), http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public List<Status> getUserTimeline(int count, Date since) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml",
                "since", format.format(since), "count", String.valueOf(count), true), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param sinceId returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 1.1220
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(int count, long sinceId) throws WeiboException {
        return getUserTimeline(new Paging(sinceId).count(count));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the user_timeline
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     */
    public List<Status> getUserTimeline(String id) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".json", http.isAuthenticationEnabled()));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml", http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the user_timeline
     * @param sinceId returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 1.1220
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, long sinceId) throws WeiboException {
        return getUserTimeline(id, new Paging(sinceId));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     */
    public List<Status> getUserTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml"
                , true), this);*/
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param paging controls pagination
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 1.1220
     */
    public List<Status> getUserTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , null, paging, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml"
                , null, paging, true), this);*/
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param sinceId returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 1.1220
     * @deprecated Use getUserTimeline(Paging paging) instead
     */
    public List<Status> getUserTimeline(long sinceId) throws
            WeiboException {
        return getUserTimeline(new Paging(sinceId));
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     *
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getMentions() instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public List<Status> getReplies() throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml", true), this);
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public List<Status> getReplies(long sinceId) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "since_id", String.valueOf(sinceId), true), this);
    }

    /**
     * Returns the most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     *
     * @param page the number of page
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public List<Status> getRepliesByPage(int page) throws WeiboException {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "page", String.valueOf(page), true), this);
    }

    /**
     * Returns the most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     *
     * @param page the number of page
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public List<Status> getReplies(int page) throws WeiboException {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "page", String.valueOf(page), true), this);
    }

    /**
     * Returns the most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param page the number of page
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public List<Status> getReplies(long sinceId, int page) throws WeiboException {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "since_id", String.valueOf(sinceId),
                "page", String.valueOf(page), true), this);
    }

    /**
     * Returns the 20 most recent mentions (status containing @username) for the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/mentions.format
     *
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/mentions">Statuses/mentions </a>
     */
    public List<Status> getMentions() throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                null, true));
    }

    /**
     * Returns the 20 most recent mentions (status containing @username) for the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/mentions.format
     *
     * @param paging controls pagination
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/mentions">Statuses/mentions </a>
     */
    public List<Status> getMentions(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                null, paging, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.xml",
                null, paging, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user.
     *
     * @return the 20 most recent retweets posted by the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getRetweetedByMe() throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.xml",
                null, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user.
     * @param paging controls pagination
     * @return the 20 most recent retweets posted by the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getRetweetedByMe(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.xml",
                null, paging, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user's friends.
     * @return the 20 most recent retweets posted by the authenticating user's friends.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getRetweetedToMe() throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.xml",
                null, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user's friends.
     * @param paging controls pagination
     * @return the 20 most recent retweets posted by the authenticating user's friends.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getRetweetedToMe(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.json",
                null, paging, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.xml",
                null, paging, true), this);*/
    }

    /**
     * Returns the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @return the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getRetweetsOfMe() throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.xml",
                null, true), this);*/
    }

    /**
     * Returns the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @param paging controls pagination
     * @return the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getRetweetsOfMe(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.json",
                null, paging, true));
    	/* return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.xml",
                null, paging, true), this);*/
    }


    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use showStatus(long id) instead.
     */
    public Status show(int id) throws WeiboException {
        return showStatus((long)id);
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/show/id.format
     *
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/show">statuses/show </a>
     * @deprecated Use showStatus(long id) instead.
     */

    public Status show(long id) throws WeiboException {
        return new Status(get(getBaseURL() + "statuses/show/" + id + ".xml", false), this);
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/show/id.format
     *
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/show">statuses/show </a>
     */
    public Status showStatus(long id) throws WeiboException {
//        return new Status(get(getBaseURL() + "statuses/show/" + id + ".xml", false), this);
    	return new Status(get(getBaseURL() + "statuses/show/" + id + ".json", true));
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @deprecated Use updateStatus(String status) instead
     */
    public Status update(String status) throws WeiboException {
        return updateStatus(status);
    }

    /*modify by sycheng with json */
    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     */
    public Status updateStatus(String status) throws WeiboException{

    	return new Status(http.post(getBaseURL() + "statuses/update.json",
                new PostParameter[]{new PostParameter("status", status)}, true));
        /*return new Status(http.post(getBaseURL() + "statuses/update.xml",
                new PostParameter[]{new PostParameter("status", status)}, true), this);*/
    }

    /**
     *
     * @param comment the comment text
     * @param id status id
     * @param cid reply comment id, can be null
     * @return the comment object
     * @throws WeiboException
     */
    public Comment updateComment(String comment, String id, String cid) throws WeiboException {
        PostParameter[] params = null;
        if (cid == null)
        	params = new PostParameter[] {
    			new PostParameter("comment", comment),
    			new PostParameter("id", id)
    		};
        else
        	params = new PostParameter[] {
    			new PostParameter("comment", comment),
    			new PostParameter("cid", cid),
    			new PostParameter("id", id)
    		};
//    	return new Comment(http.post(getBaseURL() + "statuses/comment.xml", params, true), this);
        return new Comment(http.post(getBaseURL() + "statuses/comment.json", params, true));
    }
    /**
     * upload the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * The image suport.
     * <br>This method calls http://api.t.sina.com.cn/statuses/upload.format
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/upload">statuses/upload </a>
     */
    public Status uploadStatus(String status,ImageItem item) throws Exception {
    	return new Status(http.multPartURL(getBaseURL() + "statuses/upload.json",
                new PostParameter[]
                {
    				new PostParameter("status", URLEncoder.encode(status,"UTF-8")), 
    				new PostParameter("source", source)
    			},item, true));
        /*return new Status(http.multPartURL(getBaseURL() + "statuses/upload.xml",
                new PostParameter[]{new PostParameter("status", status), new PostParameter("source", source)},item, true), this);*/
    }
    
    public Status uploadStatus(String status,ImageItem item,double _lat,double _long) throws Exception {
    	return new Status(http.multPartURL(getBaseURL() + "statuses/upload.json",
                new PostParameter[]
                {
    				new PostParameter("status", URLEncoder.encode(status,"UTF-8")), 
    				new PostParameter("source", source),
    				new PostParameter("lat", Double.toString(_lat)),
    				new PostParameter("long", Double.toString(_long)),
    			},item, true));
        /*return new Status(http.multPartURL(getBaseURL() + "statuses/upload.xml",
                new PostParameter[]{new PostParameter("status", status), new PostParameter("source", source)},item, true), this);*/
    }

    public Status uploadStatus(String status,File file) throws Exception {
    	return new Status(http.multPartURL("pic",getBaseURL() + "statuses/upload.json",
                new PostParameter[]
                {
    				new PostParameter("status", URLEncoder.encode(status,"UTF-8")),
    				new PostParameter("source", source),
                },file, true));
        /*return new Status(http.multPartURL(getBaseURL() + "statuses/upload.xml",
                new PostParameter[]{new PostParameter("status", status), new PostParameter("source", source)},item, true), this);*/
    }
    
    public Status uploadStatus(String status,File file,double _lat,double _long) throws Exception {
    	return new Status(http.multPartURL("pic",getBaseURL() + "statuses/upload.json",
                new PostParameter[]
                {
    				new PostParameter("status", URLEncoder.encode(status,"UTF-8")),
    				new PostParameter("source", source),
    				new PostParameter("lat", Double.toString(_lat)),
    				new PostParameter("long", Double.toString(_long)),
                },file, true));
        /*return new Status(http.multPartURL(getBaseURL() + "statuses/upload.xml",
                new PostParameter[]{new PostParameter("status", status), new PostParameter("source", source)},item, true), this);*/
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status    the text of your status update
     * @param latitude  The location's latitude that this tweet refers to.
     * @param longitude The location's longitude that this tweet refers to.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 1.1220
     */
    public Status updateStatus(String status, double latitude, double longitude) throws WeiboException, JSONException {
       /* return new Status(http.post(getBaseURL() + "statuses/update.xml",
                new PostParameter[]{new PostParameter("status", status),
                        new PostParameter("lat", latitude),
                        new PostParameter("long", longitude)}, true), this);*/
    	return new Status(http.post(getBaseURL() + "statuses/update.json",
                new PostParameter[]{new PostParameter("status", status),
                        new PostParameter("lat", latitude),
                        new PostParameter("long", longitude)}, true));
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @deprecated Use updateStatus(String status, long inReplyToStatusId) instead
     */
    public Status update(String status, long inReplyToStatusId) throws WeiboException {
        return updateStatus(status, inReplyToStatusId);
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     */
    public Status updateStatus(String status, long inReplyToStatusId) throws WeiboException {
    	 return new Status(http.post(getBaseURL() + "statuses/update.json",
                 new PostParameter[]{new PostParameter("status", status), new PostParameter("in_reply_to_status_id", String.valueOf(inReplyToStatusId)), new PostParameter("source", source)}, true));
       /* return new Status(http.post(getBaseURL() + "statuses/update.xml",
                new PostParameter[]{new PostParameter("status", status), new PostParameter("in_reply_to_status_id", String.valueOf(inReplyToStatusId)), new PostParameter("source", source)}, true), this);*/
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @param latitude          The location's latitude that this tweet refers to.
     * @param longitude         The location's longitude that this tweet refers to.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 1.1220
     */
    public Status updateStatus(String status, long inReplyToStatusId
            , double latitude, double longitude) throws WeiboException {
        /*return new Status(http.post(getBaseURL() + "statuses/update.xml",
                new PostParameter[]{new PostParameter("status", status),
                        new PostParameter("lat", latitude),
                        new PostParameter("long", longitude),
                        new PostParameter("in_reply_to_status_id",
                                String.valueOf(inReplyToStatusId)),
                        new PostParameter("source", source)}, true), this);*/
    	return new Status(http.post(getBaseURL() + "statuses/update.json",
                new PostParameter[]{new PostParameter("status", status),
                        new PostParameter("lat", latitude),
                        new PostParameter("long", longitude),
                        new PostParameter("in_reply_to_status_id",
                                String.valueOf(inReplyToStatusId)),
                        new PostParameter("source", source)}, true));
    }

    /**
     * Destroys the status specified by the required ID parameter.  The authenticating user must be the author of the specified status.
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     *
     * @param statusId The ID of the status to destroy.
     * @return the deleted status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public Status destroyStatus(long statusId) throws WeiboException {
        /*return new Status(http.post(getBaseURL() + "statuses/destroy/" + statusId + ".xml",
                new PostParameter[0], true), this);*/
    	return new Status(http.post(getBaseURL() + "statuses/destroy/" + statusId + ".json",
                new PostParameter[0], true));
    }

    /**
     * Destroys the status specified by the required ID parameter.  The authenticating user must be the author of the specified status.
     * <br>This method calls http://api.t.sina.com.cn/statuses/comment_destroy/id.format
     *
     * @param statusId The ID of the status to destroy.
     * @return the deleted status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/comment_destroy">statuses/comment_destroy </a>
     */
    public Comment destroyComment(long commentId) throws WeiboException {
    	return new Comment(http.delete(getBaseURL() + "statuses/comment_destroy/" + commentId + ".json?source=" + CONSUMER_KEY,
                true));
    	/*return new Comment(http.delete(getBaseURL() + "statuses/comment_destroy/" + commentId + ".xml?source=" + CONSUMER_KEY,
                true), this);*/
    }

    /**
     * Batch Destroys the comments specified by the required ids parameter,Can destroy login user comments released to the others, may not be destroyed comment.
     *<br>This method calls http://api.t.sina.com.cn/statuses/comment/destroy_batch.format
     * @Ricky
     * @param The ids of the comments to destroy
     * @return
     * @throws WeiboException
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/comment/destroy_batch">statuses/comment/destroy batch</a> 
     */
    public List<Comment> destroyComments(String ids)throws WeiboException{
    	return Comment.constructComments(http.post(getBaseURL()+"statuses/comment/destroy_batch.json",
    			new PostParameter[]{new PostParameter("ids",ids)},true));
    }

    public List<Comment> destroyComments(String[] ids)throws WeiboException{
    	StringBuilder sb = new StringBuilder();
 	    for(String id : ids) {
 		   sb.append(id).append(',');
 	    }
 	    sb.deleteCharAt(sb.length() - 1);
    	return Comment.constructComments(http.post(getBaseURL()+"statuses/comment/destroy_batch.json",
    			new PostParameter[]{new PostParameter("ids",sb.toString())},true));
    }

    /**
     * Will the current logon users a new message not readings for 0. Can reset the count of categories: 1. Comment count, 2. @me count, 3. directmessage count 4.attention count
     * <br>This method calls http://api.t.sina.com.cn/statuses/reset_count.format
     * @param type
     * @return
     * @throws WeiboException
     * @Ricky
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reset_count">statuses/reset_count</a> 
     */
    public Status resetCount(int type)throws WeiboException{
    	return new Status(http.post(getBaseURL()+"statuses/reset_count.json",
    			new PostParameter[]{new PostParameter("type",type)},true));
    }


    /**
     * Retweets a tweet. Requires the id parameter of the tweet you are retweeting. Returns the original tweet with retweet details embedded.
     * @param statusId The ID of the status to retweet.
     * @return the retweeted status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public Status retweetStatus(long statusId) throws WeiboException {
        /*return new Status(http.post(getBaseURL() + "statuses/retweet/" + statusId + ".xml",
                new PostParameter[0], true), this);*/
    	return new Status(http.post(getBaseURL() + "statuses/retweet/" + statusId + ".json",
                new PostParameter[0], true));
    }

    /**
     * Returns up to 100 of the first retweets of a given tweet.
     * @param statusId The numerical ID of the tweet you want the retweets of.
     * @return the retweets of a given tweet
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<RetweetDetails> getRetweets(long statusId) throws WeiboException {
       /* return RetweetDetails.createRetweetDetails(get(getBaseURL()
                + "statuses/retweets/" + statusId + ".xml", true), this);*/
    	 return RetweetDetails.createRetweetDetails(get(getBaseURL()
                 + "statuses/retweets/" + statusId + ".json", true));
    }

    /**
     * Returns extended information of a given user, specified by ID or screen name as per the required id parameter below.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.
     * <br>This method calls http://api.t.sina.com.cn/users/show.format
     * @param id the ID or screen name of the user for whom to request the detail
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/show">users/show </a>
     * @deprecated use showUser(id) instead
     */
    public User getUserDetail(String id) throws WeiboException {
        return showUser(id);
    }

    /**
     * Returns extended information of a given user, specified by ID or screen name as per the required id parameter below.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.
     * <br>This method calls http://api.t.sina.com.cn/users/show.format
     *
     * @param id the or screen name of the user for whom to request the detail
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/show">users/show </a>
     * @since Weibo4J 1.1220
     */
    public User showUser(String screenName) throws WeiboException {
       /* return new User(get(getBaseURL() + "users/show/" + id + ".xml"
                , http.isAuthenticationEnabled()), this);*/
    	
    	return new User(get(getBaseURL() + "users/show.json",
    			new PostParameter[]{new PostParameter("screen_name", screenName)},
    			http.isAuthenticationEnabled()).asJSONObject());
    }
    
    public User showUser(long id) throws WeiboException {
        /* return new User(get(getBaseURL() + "users/show/" + id + ".xml"
                 , http.isAuthenticationEnabled()), this);*/
     	
     	return new User(get(getBaseURL() + "users/show/"+id+".json",
     			http.isAuthenticationEnabled()).asJSONObject());
     }

    /**
     * Returns System hot user
     * <br>This method calls http://api.t.sina.com.cn/users/hot.format
     * @Ricky
     * @param
     * @return User
     * @throws WeiboException
     * @see<a href="http://open.t.sina.com.cn/wiki/index.php/Users/hot">users/hot</a> 
     * @since Weibo4J 1.1220
     */
    public List<User> getHotUsers(String category) throws WeiboException{
    	return User.constructUsers(get(getBaseURL()+"users/hot.json","category",  category, true));
    }
    /**
     * Settings privacy information
     * @param comment (message&realname&geo&badge) 
     * @return User
     * @throws WeiboException
     * @see<a href="http://open.t.sina.com.cn/wiki/index.php/Account/update_privacy">Account/update privacy</a>
     * @since Weibo4J 1.1220
     */
    
    public User updatePrivacy(String comment) throws WeiboException{
        return new User(http.post(getBaseURL() + "account/update_privacy.json",
    	                new PostParameter[]{new PostParameter("comment", comment)}, true).asJSONObject());
    	     
    	    
    	     
    	    }
    /**
     * Obtain privacy information Settings
     * @return User
     * @throws WeiboException
     * @see<a href="http://open.t.sina.com.cn/wiki/index.php/Account/get_privacy">Account/get privacy</a>
     */
    public User getPrivacy()throws WeiboException{
        return  new User(http.post(getBaseURL()+"account/get_privacy.json",true).asJSONObject());
       }

    	 

    /* User Methods */

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatues() instead
     */
    public List<User> getFriends() throws WeiboException {
        return getFriendsStatuses();
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 1.1220
     */
    public List<User> getFriendsStatuses() throws WeiboException {
//        return User.constructUsers(get(getBaseURL() + "statuses/friends.xml", true), this);
    	return User.constructResult(get(getBaseURL() + "statuses/friends.json", true));
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param paging controls pagination
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated Use getFriendsStatuses(Paging paging) instead
     */
    public List<User> getFriends(Paging paging) throws WeiboException {
        return getFriendsStatuses(paging);
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param paging controls pagination
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public List<User> getFriendsStatuses(Paging paging) throws WeiboException {
        /*return User.constructUsers(get(getBaseURL() + "statuses/friends.xml", null,
                paging, true), this);*/
    	return User.constructUsers(get(getBaseURL() + "statuses/friends.json", null,
                paging, true));
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param page number of page
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFriendsStatuses(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public List<User> getFriends(int page) throws WeiboException {
        return getFriendsStatuses(new Paging(page));
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatuses(id) instead
     */
    public List<User> getFriends(String id) throws WeiboException {
        return getFriendsStatuses(id);
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 1.1220
     */
    public List<User> getFriendsStatuses(String id) throws WeiboException {
        /*return User.constructUsers(get(getBaseURL() + "statuses/friends/" + id + ".xml"
                , false), this);*/
    	return User.constructUsers(get(getBaseURL() + "statuses/friends/" + id + ".json"
                , false));
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @param paging controls pagination
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatuses(id,paging) instead
     */
    public List<User> getFriends(String id, Paging paging) throws WeiboException {
        return getFriendsStatuses(id, paging);
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @param paging controls pagination
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public List<User> getFriendsStatuses(String id, Paging paging) throws WeiboException {
       /* return User.constructUsers(get(getBaseURL() + "statuses/friends/" + id + ".xml"
                , null, paging, false), this);*/
    	return User.constructUsers(get(getBaseURL() + "statuses/friends/" + id + ".json"
                , false));
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id   the ID or screen name of the user for whom to request a list of friends
     * @param page the number of page
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFriendsStatuses(String id, Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public List<User> getFriends(String id, int page) throws WeiboException {
        return getFriendsStatuses(id, new Paging(page));
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatuses() instead
     */
    public List<User> getFollowers() throws WeiboException {
        return getFollowersStatuses();
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @since Weibo4J 1.1220
     */
    public List<User> getFollowersStatuses() throws WeiboException {
//        return User.constructUsers(get(getBaseURL() + "statuses/followers.xml", true), this);
    	return User.constructResult(get(getBaseURL() + "statuses/followers.json", true));
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatuses(paging)
     */
    public List<User> getFollowers(Paging paging) throws WeiboException {
        return getFollowersStatuses(paging);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public List<User> getFollowersStatuses(Paging paging) throws WeiboException {
        /*return User.constructUsers(get(getBaseURL() + "statuses/followers.xml", null
                , paging, true), this);*/
    	return User.constructUsers(get(getBaseURL() + "statuses/followers.json", null
                , paging, true));
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param page Retrieves the next 100 followers.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getFollowersStatuses(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public List<User> getFollowers(int page) throws WeiboException {
        return getFollowersStatuses(new Paging(page));
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id The ID or screen name of the user for whom to request a list of followers.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatuses(id) instead
     */
    public List<User> getFollowers(String id) throws WeiboException {
        return getFollowersStatuses(id);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id The ID or screen name of the user for whom to request a list of followers.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public List<User> getFollowersStatuses(String id) throws WeiboException {
//        return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id + ".xml", true), this);
    	 return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id + ".json", true));
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id   The ID or screen name of the user for whom to request a list of followers.
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatuses(id) instead
     */
    public List<User> getFollowers(String id, Paging paging) throws WeiboException {
        return getFollowersStatuses(id, paging);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id   The ID or screen name of the user for whom to request a list of followers.
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public List<User> getFollowersStatuses(String id, Paging paging) throws WeiboException {
        /*return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id +
                ".xml", null, paging, true), this);*/
    	return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id +
                ".json", null, paging, true));
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id   The ID or screen name of the user for whom to request a list of followers.
     * @param page Retrieves the next 100 followers.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getFollowersStatuses(String id, Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public List<User> getFollowers(String id, int page) throws WeiboException {
        return getFollowersStatuses(id, new Paging(page));
    }

    /**
     * Returns a list of the users currently featured on the site with their current statuses inline.
     *
     * @return List of User
     * @throws WeiboException when Weibo service or network is unavailable
     */
    public List<User> getFeatured() throws WeiboException {
//        return User.constructUsers(get(getBaseURL() + "statuses/featured.xml", true), this);
        return User.constructUsers(get(getBaseURL() + "statuses/featured.json", true));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public List<DirectMessage> getDirectMessages() throws WeiboException {

        return DirectMessage.constructDirectMessages(get(getBaseURL() + "direct_messages.json", true));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     *
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public List<DirectMessage> getDirectMessages(Paging paging) throws WeiboException {
        /*return DirectMessage.constructDirectMessages(get(getBaseURL()
                + "direct_messages.xml", null, paging, true), this);*/
        return DirectMessage.constructDirectMessages(get(getBaseURL()
                + "direct_messages.json", null, paging, true));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     *
     * @param page the number of page
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getDirectMessages(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public List<DirectMessage> getDirectMessagesByPage(int page) throws WeiboException {
        return getDirectMessages(new Paging(page));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     *
     * @param page    the number of page
     * @param sinceId Returns only direct messages with an ID greater than (that is, more recent than) the specified ID.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getDirectMessages(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public List<DirectMessage> getDirectMessages(int page
            , int sinceId) throws WeiboException {
        return getDirectMessages(new Paging(page).sinceId(sinceId));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     *
     * @param sinceId Returns only direct messages with an ID greater than (that is, more recent than) the specified ID.
     * @return list of direct messages
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getDirectMessages(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public List<DirectMessage> getDirectMessages(int sinceId) throws WeiboException {
        return getDirectMessages(new Paging((long)sinceId));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     *
     * @param since narrows the resulting list of direct messages to just those sent after the specified HTTP-formatted date
     * @return list of direct messages
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getDirectMessages(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public List<DirectMessage> getDirectMessages(Date since) throws
            WeiboException {
        return DirectMessage.constructDirectMessages(get(getBaseURL() +
                "direct_messages.xml", "since", format.format(since), true), this);
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">direct messages/sent </a>
     */
    public List<DirectMessage> getSentDirectMessages() throws
            WeiboException {
        /*return DirectMessage.constructDirectMessages(get(getBaseURL() +
                "direct_messages/sent.xml", new PostParameter[0], true), this);*/
    	 return DirectMessage.constructDirectMessages(get(getBaseURL() +
                 "direct_messages/sent.json", new PostParameter[0], true));
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     *
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">direct messages/sent </a>
     */
    public List<DirectMessage> getSentDirectMessages(Paging paging) throws
            WeiboException {
       /* return DirectMessage.constructDirectMessages(get(getBaseURL() +
                "direct_messages/sent.xml", new PostParameter[0],paging, true), this);*/
    	 return DirectMessage.constructDirectMessages(get(getBaseURL() +
                 "direct_messages/sent.json", new PostParameter[0],paging, true));
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     *
     * @param since narrows the resulting list of direct messages to just those sent after the specified HTTP-formatted date
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated using long sinceId is suggested.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">direct messages/sent </a>
     */
    public List<DirectMessage> getSentDirectMessages(Date since) throws
            WeiboException {
        return DirectMessage.constructDirectMessages(get(getBaseURL() +
                "direct_messages/sent.xml", "since", format.format(since), true), this);
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     *
     * @param sinceId returns only sent direct messages with an ID greater than (that is, more recent than) the specified ID
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getSentDirectMessages(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">direct messages/sent </a>
     */
    public List<DirectMessage> getSentDirectMessages(int sinceId) throws
            WeiboException {
        return getSentDirectMessages(new Paging((long)sinceId));
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     *
     * @param sinceId returns only sent direct messages with an ID greater than (that is, more recent than) the specified ID
     * @param page Retrieves the 20 next most recent direct messages.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use getSentDirectMessages(Paging paging) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">direct messages/sent </a>
     */
    public List<DirectMessage> getSentDirectMessages(int page
            , int sinceId) throws WeiboException {
        return getSentDirectMessages(new Paging(page, (long)sinceId));
    }

    /**
     * Sends a new direct message to the specified user from the authenticating user.  Requires both the user and text parameters below.
     * The text will be trimed if the length of the text is exceeding 140 characters.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/new.format
     *
     * @param id   the ID or screen name of the user to whom send the direct message
     * @param text String
     * @return DirectMessage
     * @throws WeiboException when Weibo service or network is unavailable
       @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/new">direct messages/sent </a>
     */
    public DirectMessage sendDirectMessage(String id,
                                                        String text) throws WeiboException {
        /*return new DirectMessage(http.post(getBaseURL() + "direct_messages/new.xml",
                new PostParameter[]{new PostParameter("user", id),
                        new PostParameter("text", text)}, true), this);*/
    	return new DirectMessage(http.post(getBaseURL() + "direct_messages/new.json",
                new PostParameter[]{new PostParameter("user_id", id),
                        new PostParameter("text", text),new PostParameter("source", source)}, true).asJSONObject());
    }


    /**
     * Destroys the direct message specified in the required ID parameter.  The authenticating user must be the recipient of the specified direct message.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/destroy/id.format
     *
     * @param id the ID of the direct message to destroy
     * @return the deleted direct message
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/destroy">Weibo API Wiki / Weibo REST API Method: direct_messages destroy</a>
     * @deprecated Use destroyDirectMessage(int id) instead
     */
    public DirectMessage deleteDirectMessage(int id) throws
            WeiboException {
        return destroyDirectMessage(id);
    }

    /**
     * Destroys the direct message specified in the required ID parameter.  The authenticating user must be the recipient of the specified direct message.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/destroy/id.format
     *
     * @param id the ID of the direct message to destroy
     * @return the deleted direct message
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/destroy">Weibo API Wiki / Weibo REST API Method: direct_messages destroy</a>
     * @since Weibo4J 1.1220
     */
    public DirectMessage destroyDirectMessage(int id) throws
            WeiboException {
        /*return new DirectMessage(http.post(getBaseURL() +
                "direct_messages/destroy/" + id + ".xml", new PostParameter[0], true), this);*/
    	return new DirectMessage(http.post(getBaseURL() +
                "direct_messages/destroy/" + id + ".json", new PostParameter[0], true).asJSONObject());
    }

    /**
     * Batch destroy the current login user direct messages. Abnormal, return HTTP400 mistake.
     * @param The ids of the direct messages to destroy
     * @return the deleted direct messages
     * @throws WeiboException
     * @Ricky
     * @see<a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/destroy_batch">direct_messages/destroy_batch</a>
     * @since Weibo4J 1.1220
     */
    public List<DirectMessage> destroyDirectMessages(String ids)throws WeiboException{
    	return DirectMessage.constructDirectMessages(http.post(getBaseURL()+"direct_messages/destroy_batch.json",
    			new PostParameter[]{new PostParameter("ids",ids)},true));
    }

    public List<DirectMessage> destroyDirectMessages(String[] ids)throws WeiboException{
    	 StringBuilder sb = new StringBuilder();
  	   	 for(String id : ids) {
  		   sb.append(id).append(',');
  	     }
  	     sb.deleteCharAt(sb.length() - 1);
    	return DirectMessage.constructDirectMessages(http.post(getBaseURL()+"direct_messages/destroy_batch.json",
    			new PostParameter[]{new PostParameter("ids",sb.toString())},true));
    }


    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID or screen name of the user to be befriended
     * @return the befriended user
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use createFriendship(String id) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */

    public User create(String id) throws WeiboException {
        return createFriendship(Long.parseLong(id));
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID of the user to be befriended
     * @return the befriended user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public User createFriendship(long id) throws WeiboException {
//        return new User(http.post(getBaseURL() + "friendships/create/" + id + ".xml", new PostParameter[0], true), this);
    	 return new User(http.post(getBaseURL() + "friendships/create/" + id + ".json", new PostParameter[0], true).asJSONObject());
    }
    
    /** yuchberry modified
     * follow the user by screen name 
     * @param screenName 
     * @return
     * @throws WeiboException
     */
    public User createFriendship(String screenName) throws WeiboException {
    	return new User(http.post(getBaseURL() + "friendships/create.json", new PostParameter[]{
    				new PostParameter("screen_name",screenName)}, true).asJSONObject());
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID or screen name of the user to be befriended
     * @param follow Enable notifications for the target user in addition to becoming friends.
     * @return the befriended user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public User createFriendship(String id, boolean follow) throws WeiboException {
        /*return new User(http.post(getBaseURL() + "friendships/create/" + id + ".xml"
                , new PostParameter[]{new PostParameter("follow"
                        , String.valueOf(follow))}, true)
                , this);*/
    	 return new User(http.post(getBaseURL() + "friendships/create/" + id + ".json"
                 , new PostParameter[]{new PostParameter("follow"
                         , String.valueOf(follow))}, true).asJSONObject()
                 );
    }

    /**
     * Discontinues friendship with the user specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use destroyFriendship(String id) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/destroy">friendships/destroy </a>
     */
    public User destroy(String id) throws WeiboException {
        return destroyFriendship(id);
    }

    /**
     * Discontinues friendship with the user specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/destroy">friendships/destroy </a>
     */
    public User destroyFriendship(String screenName) throws WeiboException {
//        return new User(http.post(getBaseURL() + "friendships/destroy/" + id + ".xml", new PostParameter[0], true), this);
    	return new User(http.post(getBaseURL() + "friendships/destroy.json", new PostParameter[]{
			new PostParameter("screen_name",screenName)}, true).asJSONObject());
    	
    }

    /**
     * Tests if a friendship exists between two users.
     *
     * @param userA The ID or screen_name of the first user to test friendship for.
     * @param userB The ID or screen_name of the second user to test friendship for.
     * @return if a friendship exists between two users.
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use friendshipExists(String userA, String userB)
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/exists">friendships/exists </a>
     */
    public boolean exists(String userA, String userB) throws WeiboException {
        return existsFriendship(userA, userB);
    }
    /**
     * Tests if a friendship exists between two users.
     *
     * @param userA The ID or screen_name of the first user to test friendship for.
     * @param userB The ID or screen_name of the second user to test friendship for.
     * @return if a friendship exists between two users.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/exists">friendships/exists </a>
     */
    public boolean existsFriendship(String userA, String userB) throws WeiboException {
        return -1 != get(getBaseURL() + "friendships/exists.json", "user_a", userA, "user_b", userB, true).
                asString().indexOf("true");
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * @return an array of numeric IDs for every user the authenticating user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs() throws WeiboException {
        return getFriendsIDs(-1l);
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * @param paging Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the authenticating user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @deprecated use getFriendsIDs(long cursor) instead
     */
    public IDs getFriendsIDs(Paging paging) throws WeiboException {
        return new IDs(get(getBaseURL() + "friends/ids.xml", null, paging, true));
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.<br>
     * all IDs are attempted to be returned, but large sets of IDs will likely fail with timeout errors.
     * @param userId Specfies the ID of the user for whom to return the friends list.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(long userId) throws WeiboException {
        return getFriendsIDs(userId, -1l);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * @param userId Specifies the ID of the user for whom to return the friends list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(long userId, long cursor) throws WeiboException {
        /*return new IDs(get(getBaseURL() + "friends/ids.xml?user_id=" + userId +
                "&cursor=" + cursor, true));*/
    	return new IDs(get(getBaseURL() + "friends/ids.json?user_id=" + userId +
                "&cursor=" + cursor, true),this);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(String screenName) throws WeiboException {
        return getFriendsIDs(screenName, -1l);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param paging Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(long userId, long cursor,long _count) throws WeiboException {
    	return new IDs(get(getBaseURL() + "friends/ids.json?user_id=" + userId +
                "&cursor=" + cursor + "&count=" + _count, true),this);
    }
    
    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param paging Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(long userId, Paging _page) throws WeiboException {
    	return new IDs(get(getBaseURL() + "friends/ids.json?user_id=" + userId +
                "&cursor=" + _page.getPage() + "&count=" + _page.getCount(), true),this);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(String screenName, long cursor) throws WeiboException {
       /* return new IDs(get(getBaseURL() + "friends/ids.xml?screen_name=" + screenName
                + "&cursor=" + cursor, true));*/
    	 return new IDs(get(getBaseURL() + "friends/ids.json?screen_name=" + screenName
                 + "&cursor=" + cursor, true),this);
    }
    
    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the specified user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public IDs getFriendsIDs(String screenName, Paging page) throws WeiboException {
       /* return new IDs(get(getBaseURL() + "friends/ids.xml?screen_name=" + screenName
                + "&cursor=" + cursor, true));*/
    	 return new IDs(get(getBaseURL() + "friends/ids.json?screen_name=" + screenName
                 + "&cursor=" + page.getPage(), true),this);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     */
    public IDs getFollowersIDs() throws WeiboException {
        return getFollowersIDs(-1l);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @param paging Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @deprecated use getFollowersIDs(long cursor) instead
     */
    public IDs getFollowersIDs(Paging paging) throws WeiboException {
        return new IDs(get(getBaseURL() + "followers/ids.xml", null, paging
                , true));
    }


    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @param userId Specfies the ID of the user for whom to return the followers list.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     */
    public IDs getFollowersIDs(long userId) throws WeiboException {
        return getFollowersIDs(userId, -1l,-1l);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @param userId Specfies the ID of the user for whom to return the followers list.
     * @param paging Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @deprecated use getFollowersIDs(int userId, long cursor) instead
     */
    public IDs getFollowersIDs(long userId, Paging paging) throws WeiboException {
        return new IDs(get(getBaseURL() + "followers/ids.xml?user_id=" + userId, null
                , paging, true));
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @param userId Specifies the ID of the user for whom to return the followers list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     */
    public IDs getFollowersIDs(long userId, long cursor,long count) throws WeiboException {
        return new IDs(get(getBaseURL() + "followers/ids.xml?user_id=" + userId
                + "&cursor=" + cursor + "&count=" + count, true));
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @param screenName Specfies the screen name of the user for whom to return the followers list.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     */
    public IDs getFollowersIDs(String screenName) throws WeiboException {
        return getFollowersIDs(screenName, -1l);
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @param screenName Specfies the screen name of the user for whom to return the followers list.
     * @param paging Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @deprecated use getFollowersIDs(String screenName, long cursor) instead
     */
    public IDs getFollowersIDs(String screenName, Paging paging) throws WeiboException {
        return new IDs(get(getBaseURL() + "followers/ids.xml?screen_name="
                + screenName, null, paging, true));
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * @param screenName Specfies the screen name of the user for whom to return the followers list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return The ID or screen_name of the user to retrieve the friends ID list for.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     */
    public IDs getFollowersIDs(String screenName, long cursor) throws WeiboException {
       /* return new IDs(get(getBaseURL() + "followers/ids.xml?screen_name="
                + screenName + "&cursor=" + cursor, true));*/
    	 return new IDs(get(getBaseURL() + "followers/ids.json?screen_name="
                 + screenName + "&cursor=" + cursor, true),this);
    }

    /**
     * Returns an HTTP 200 OK response code and a representation of the requesting user if authentication was successful; returns a 401 status code and an error message if not.  Use this method to test if supplied user credentials are valid.
     *
     * @return user
     * @since Weibo4J 1.1220
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Account/verify_credentials">account/verify_credentials </a>
     */
    public User verifyCredentials() throws WeiboException {
        /*return new User(get(getBaseURL() + "account/verify_credentials.xml"
                , true), this);*/
    	return new User(get(getBaseURL() + "account/verify_credentials.json"
                , true).asJSONObject());
    }

    /**
     * Updates the location
     *
     * @param location the current location of the user
     * @return the updated user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use updateProfile(String name, String email, String url, String location, String description) instead
     */
    public User updateLocation(String location) throws WeiboException {
        return new User(http.post(getBaseURL() + "account/update_location.xml", new PostParameter[]{new PostParameter("location", location)}, true), this);
    }

    /**
     * Sets values that users are able to set under the "Account" tab of their settings page. Only the parameters specified(non-null) will be updated.
     *
     * @param name Optional. Maximum of 20 characters.
     * @param email Optional. Maximum of 40 characters. Must be a valid email address.
     * @param url Optional. Maximum of 100 characters. Will be prepended with "http://" if not present.
     * @param location Optional. Maximum of 30 characters. The contents are not normalized or geocoded in any way.
     * @param description Optional. Maximum of 160 characters.
     * @return the updated user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Account/update_profile">account/update_profile </a>
     */
    public User updateProfile(String name, String email, String url
            , String location, String description) throws WeiboException {
        List<PostParameter> profile = new ArrayList<PostParameter>(5);
        addParameterToList(profile, "name", name);
        addParameterToList(profile, "email", email);
        addParameterToList(profile, "url", url);
        addParameterToList(profile, "location", location);
        addParameterToList(profile, "description", description);
        /*return new User(http.post(getBaseURL() + "account/update_profile.xml"
                , profile.toArray(new PostParameter[profile.size()]), true), this);*/
        return new User(http.post(getBaseURL() + "account/update_profile.json"
                , profile.toArray(new PostParameter[profile.size()]), true).asJSONObject());
    }

    /**
     * @param image
     * @return
     * @throws WeiboException
     */
    public User updateProfileImage(File image)throws WeiboException {
    	return new User(http.multPartURL("image",getBaseURL() + "account/update_profile_image.json",
                null,image, true).asJSONObject());
    }

    /**
     * Returns the remaining number of API requests available to the requesting user before the API limit is reached for the current hour. Calls to rate_limit_status do not count against the rate limit.  If authentication credentials are provided, the rate limit status for the authenticating user is returned.  Otherwise, the rate limit status for the requester's IP address is returned.<br>
     *
     * @return the rate limit status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Account/rate_limit_status">account/rate_limit_status </a>
     */
    public RateLimitStatus rateLimitStatus() throws WeiboException {
//        return new RateLimitStatus(http.get(getBaseURL() + "account/rate_limit_status.xml", null != getUserId() && null != getPassword()));
    	 return new RateLimitStatus(http.get(getBaseURL() + "account/rate_limit_status.json?" +
    	 		"source="+Weibo.CONSUMER_KEY, true),this);
    }

    public final static Device IM = new Device("im");
    public final static Device SMS = new Device("sms");
    public final static Device NONE = new Device("none");

    static class Device {
        final String DEVICE;

        public Device(String device) {
            DEVICE = device;
        }
    }

    /**
     * Sets which device Weibo delivers updates to for the authenticating user.  Sending none as the device parameter will disable IM or SMS updates.
     *
     * @param device new Delivery device. Must be one of: IM, SMS, NONE.
     * @return the updated user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public User updateDeliverlyDevice(Device device) throws WeiboException {
//        return new User(http.post(getBaseURL() + "account/update_delivery_device.xml", new PostParameter[]{new PostParameter("device", device.DEVICE)}, true), this);
    	return new User(http.post(getBaseURL() + "account/update_delivery_device.json", new PostParameter[]{new PostParameter("device", device.DEVICE)}, true).asJSONObject());
    }


    /**
     * Sets one or more hex values that control the color scheme of the authenticating user's profile page on sina.com.cn.  These values are also returned in the getUserDetail() method.
     * @param profileBackgroundColor optional, can be null
     * @param profileTextColor optional, can be null
     * @param profileLinkColor optional, can be null
     * @param profileSidebarFillColor optional, can be null
     * @param profileSidebarBorderColor optional, can be null
     * @return the updated user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public User updateProfileColors(
            String profileBackgroundColor,
            String profileTextColor,
            String profileLinkColor,
            String profileSidebarFillColor,
            String profileSidebarBorderColor)
            throws WeiboException {
        List<PostParameter> colors = new ArrayList<PostParameter>(5);
        addParameterToList(colors, "profile_background_color"
                , profileBackgroundColor);
        addParameterToList(colors, "profile_text_color"
                , profileTextColor);
        addParameterToList(colors, "profile_link_color"
                , profileLinkColor);
        addParameterToList(colors, "profile_sidebar_fill_color"
                , profileSidebarFillColor);
        addParameterToList(colors, "profile_sidebar_border_color"
                , profileSidebarBorderColor);
        /*return new User(http.post(getBaseURL() +
                "account/update_profile_colors.xml",
                colors.toArray(new PostParameter[colors.size()]), true), this);*/
        return new User(http.post(getBaseURL() +
                "account/update_profile_colors.json",
                colors.toArray(new PostParameter[colors.size()]), true).asJSONObject());
    }

    private void addParameterToList(List<PostParameter> colors,
                                      String paramName, String color) {
        if(null != color){
            colors.add(new PostParameter(paramName,color));
        }
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavorited() instead
     */
    public List<Status> favorites() throws WeiboException {
        return getFavorites();
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 1.1220
     */
    public List<Status> getFavorites() throws WeiboException {
//        return Status.constructStatuses(get(getBaseURL() + "favorites.xml", new PostParameter[0], true), this);
    	return Status.constructStatuses(get(getBaseURL() + "favorites.json", new PostParameter[0], true));
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * @param page the number of page
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavorites(int page) instead
     */
    public List<Status> favorites(int page) throws WeiboException {
        return getFavorites(page);
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param page the number of page
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 1.1220
     */
    public List<Status> getFavorites(int page) throws WeiboException {
//        return Status.constructStatuses(get(getBaseURL() + "favorites.xml", "page", String.valueOf(page), true), this);
    	return Status.constructStatuses(get(getBaseURL() + "favorites.json", "page", String.valueOf(page), true));
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavorites(String id) instead
     */
    public List<Status> favorites(String id) throws WeiboException {
        return getFavorites(id);
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 1.1220
     */
    public List<Status> getFavorites(String id) throws WeiboException {
//        return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".xml", new PostParameter[0], true), this);
    	 return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", new PostParameter[0], true));
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param id   the ID or screen name of the user for whom to request a list of favorite statuses
     * @param page the number of page
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFavorites(String id, int page) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     */
    public List<Status> favorites(String id, int page) throws WeiboException {
        return getFavorites(id, page);
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param id   the ID or screen name of the user for whom to request a list of favorite statuses
     * @param page the number of page
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     */
    public List<Status> getFavorites(String id, int page) throws WeiboException {
//        return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".xml", "page", String.valueOf(page), true), this);
    	return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", "page", String.valueOf(page), true));
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     *
     * @param id the ID of the status to favorite
     * @return Status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/create">favorites/create </a>
     */
    public Status createFavorite(long id) throws WeiboException {
//        return new Status(http.post(getBaseURL() + "favorites/create/" + id + ".xml", true), this);
    	return new Status(http.post(getBaseURL() + "favorites/create/" + id + ".json", true));
    }

    /**
     * Un-favorites the status specified in the ID parameter as the authenticating user.  Returns the un-favorited status in the requested format when successful.
     *
     * @param id the ID of the status to un-favorite
     * @return Status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/destroy">favorites/destroy </a>
     */
    public Status destroyFavorite(long id) throws WeiboException {
//        return new Status(http.post(getBaseURL() + "favorites/destroy/" + id + ".xml", true), this);
    	return new Status(http.post(getBaseURL() + "favorites/destroy/" + id + ".json", true));
    }

    /**
     * Batch destroy the current login user's collection. Abnormal, return HTTP400 mistake.
     * @param ids of the statuses to destroy
     * @return Status
     * @throws WeiboException
     * @Ricky
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/destroy_batch">favorites/destroy_batch</a>
     * @since Weibo4J 1.1220
     */
    public List<Status> destroyFavorites(String ids)throws WeiboException{
    	return Status.constructStatuses(http.post(getBaseURL()+"favorites/destroy_batch.json",
    			new PostParameter[]{new PostParameter("ids",ids)},true));
    }

    public List<Status> destroyFavorites(String[] ids)throws WeiboException{
    	 StringBuilder sb = new StringBuilder();
	  	 for(String id : ids) {
	  		 sb.append(id).append(',');
	  	 }
	  	 sb.deleteCharAt(sb.length() - 1);
    	return Status.constructStatuses(http.post(getBaseURL()+"favorites/destroy_batch.json",
    			new PostParameter[]{new PostParameter("ids",sb.toString())},true));
    }

    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     *
     * @param id String
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use enableNotification(String id) instead
     */
    public User follow(String id) throws WeiboException {
        return enableNotification(id);
    }

    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * @param id String
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public User enableNotification(String id) throws WeiboException {
//        return new User(http.post(getBaseURL() + "notifications/follow/" + id + ".xml", true), this);
    	return new User(http.post(getBaseURL() + "notifications/follow/" + id + ".json", true).asJSONObject());
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * @param id String
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use disableNotification(String id) instead
     */
    public User leave(String id) throws WeiboException {
        return disableNotification(id);
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * @param id String
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public User disableNotification(String id) throws WeiboException {
//        return new User(http.post(getBaseURL() + "notifications/leave/" + id + ".xml", true), this);
    	return new User(http.post(getBaseURL() + "notifications/leave/" + id + ".json", true).asJSONObject());
    }

    /* Block Methods */

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.  Returns the blocked user in the requested format when successful.
     * @param id the ID or screen_name of the user to block
     * @return the blocked user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use createBlock(String id) instead
     */
    public User block(String id) throws WeiboException {
        return new User(http.post(getBaseURL() + "blocks/create/" + id + ".xml", true), this);
    }

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.  Returns the blocked user in the requested format when successful.
     * @param id the ID or screen_name of the user to block
     * @return the blocked user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public User createBlock(String id) throws WeiboException {
//        return new User(http.post(getBaseURL() + "blocks/create/" + id + ".xml", true), this);
        return new User(http.post(getBaseURL() + "blocks/create.json",
    			new PostParameter[]{new PostParameter("id", id)}, true).asJSONObject());
    }


    /**
     * Un-blocks the user specified in the ID parameter as the authenticating user.  Returns the un-blocked user in the requested format when successful.
     * @param id the ID or screen_name of the user to block
     * @return the unblocked user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use destroyBlock(String id) instead
     */
    public User unblock(String id) throws WeiboException {
        return new User(http.post(getBaseURL() + "blocks/destroy/" + id + ".xml", true), this);
    }

    /**
     * Un-blocks the user specified in the ID parameter as the authenticating user.  Returns the un-blocked user in the requested format when successful.
     * @param id the ID or screen_name of the user to block
     * @return the unblocked user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public User destroyBlock(String id) throws WeiboException {
//        return new User(http.post(getBaseURL() + "blocks/destroy/" + id + ".xml", true), this);
    	return new User(http.post(getBaseURL() + "blocks/destroy.json",
    			new PostParameter[]{new PostParameter("id",id)}, true).asJSONObject());
    }


    /**
     * Tests if a friendship exists between two users.
     * @param id The ID or screen_name of the potentially blocked user.
     * @return  if the authenticating user is blocking a target user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public boolean existsBlock(String id) throws WeiboException {
        try{
            return -1 == get(getBaseURL() + "blocks/exists.json?user_id="+id, true).
                    asString().indexOf("<error>You are not blocking this user.</error>");
        }catch(WeiboException te){
            if(te.getStatusCode() == 404){
                return false;
            }
            throw te;
        }
    }

    /**
     * Returns a list of user objects that the authenticating user is blocking.
     * @return a list of user objects that the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<User> getBlockingUsers() throws
            WeiboException {
        /*return User.constructUsers(get(getBaseURL() +
                "blocks/blocking.xml", true), this);*/
    	return User.constructUsers(get(getBaseURL() +
                "blocks/blocking.json", true));
    }

    /**
     * Returns a list of user objects that the authenticating user is blocking.
     * @param page the number of page
     * @return a list of user objects that the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<User> getBlockingUsers(int page) throws
            WeiboException {
        /*return User.constructUsers(get(getBaseURL() +
                "blocks/blocking.xml?page=" + page, true), this);*/
    	return User.constructUsers(get(getBaseURL() +
                "blocks/blocking.json?page=" + page, true));
    }

    /**
     * Returns an array of numeric user ids the authenticating user is blocking.
     * @return Returns an array of numeric user ids the authenticating user is blocking.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public IDs getBlockingUsersIDs() throws WeiboException {
//        return new IDs(get(getBaseURL() + "blocks/blocking/ids.xml", true));
    	return new IDs(get(getBaseURL() + "blocks/blocking/ids.json", true),this);
    }

    /* Saved Searches Methods */
    /**
     * Returns the authenticated user's saved search queries.
     * @return Returns an array of numeric user ids the authenticating user is blocking.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<SavedSearch> getSavedSearches() throws WeiboException {
        return SavedSearch.constructSavedSearches(get(getBaseURL() + "saved_searches.json", true));
    }

    /**
     * Retrieve the data for a saved search owned by the authenticating user specified by the given id.
     * @param id The id of the saved search to be retrieved.
     * @return the data for a saved search
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public SavedSearch showSavedSearch(int id) throws WeiboException {
        return new SavedSearch(get(getBaseURL() + "saved_searches/show/" + id
                + ".json", true));
    }

    /**
     * Retrieve the data for a saved search owned by the authenticating user specified by the given id.
     * @return the data for a created saved search
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public SavedSearch createSavedSearch(String query) throws WeiboException {
        return new SavedSearch(http.post(getBaseURL() + "saved_searches/create.json"
                , new PostParameter[]{new PostParameter("query", query)}, true));
    }

    /**
     * Destroys a saved search for the authenticated user. The search specified by id must be owned by the authenticating user.
     * @param id The id of the saved search to be deleted.
     * @return the data for a destroyed saved search
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public SavedSearch destroySavedSearch(int id) throws WeiboException {
        return new SavedSearch(http.post(getBaseURL() + "saved_searches/destroy/" + id
                + ".json", true));
    }

	/**
	 * Obtain list interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject getList(String uid, String listId, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists/").append(listId).append(".xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Obtain the ListObject feed list
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public List<Status> getListStatuses(String uid, String listId, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists/").append(listId).append("/statuses.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		return Status.constructStatuses(http.httpRequest(url, null, auth, httpMethod), this);
	}

	/**
	 * Obtain created by the user interface ListObject list
	 * @param uid		User ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObjectWapper getUserLists(String uid, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return ListObject.constructListObjects(res, this);
	}

	/**
	 * Obtain users subscribed ListObject list interface
	 * @param uid		User ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObjectWapper getUserSubscriberLists(String uid, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists/subscriptions.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return ListObject.constructListObjects(res, this);
	}

	/**
	 * Retrieve the user interface has been added to the list ListObject
	 * @param uid		User ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObjectWapper getUserListedLists(String uid, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists/memberships.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return ListObject.constructListObjects(res, this);
	}

	/**
	 * Obtain user ListObject statistics 
	 * @param uid		UserID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return		ListUserCount
	 * @throws WeiboException
	 */
	public ListUserCount getListUserCount(String uid, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(String.valueOf(uid)).append("/lists").append("/counts.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return new ListUserCount(res);
	}

	/**
	 * Obtain ListObject member list
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public UserWapper getListMembers(String uid, String listId, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/members.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		return User.constructWapperUsers(http.httpRequest(url, null, auth, httpMethod), this);
	}

	/**
	 * Obtain ListObject subscribe user's list 
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public UserWapper getListSubscribers(String uid, String listId, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/subscribers.xml").append("?source=").append(CONSUMER_KEY);
		String httpMethod = "GET";
		String url = sb.toString();
		//
		return User.constructWapperUsers(http.httpRequest(url, null, auth, httpMethod), this);
	}

	/**
	 * Creat ListObject interface
	 * @param uid		User ID or screen_name
	 * @param name		The name of ListObject
	 * @param mode		The mode of ListObject, 'public' or 'private'
	 * @param description	The description of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject insertList(String uid, String name, boolean mode, String description, boolean auth)
			throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists.xml");
		List<PostParameter> postParams = new LinkedList<PostParameter>();
		postParams.add(new PostParameter("name", name));
		postParams.add(new PostParameter("description", description));
		postParams.add(new PostParameter("mode", mode ? "public" : "private"));
		postParams.add(new PostParameter("source", CONSUMER_KEY));
		PostParameter[] params = new PostParameter[postParams.size()];
		int index = 0;
		for (PostParameter postParam : postParams) {
			params[index++] = postParam;
		}
		String httpMethod = "POST";
		//
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, params, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Update list interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param name		The name of ListObject
	 * @param mode		The mode of ListObject, 'public' or 'private'
	 * @param description	The description of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject updateList(String uid, String listId, String name, boolean mode, String description,
			boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists/").append(listId).append(".xml");
		List<PostParameter> postParams = new LinkedList<PostParameter>();
		postParams.add(new PostParameter("name", name));
		postParams.add(new PostParameter("mode", mode ? "public" : "private"));
		postParams.add(new PostParameter("description", description));
		postParams.add(new PostParameter("source", CONSUMER_KEY));
		PostParameter[] params = new PostParameter[postParams.size()];
		int index = 0;
		for (PostParameter postParam : postParams) {
			params[index++] = postParam;
		}
		String httpMethod = "POST";
		//
		String url = sb.toString();
		//
		Response res = http.httpRequest(url, params, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Destroy list interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject removeList(String uid, String listId, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/lists/").append(listId).append(".xml").append("?source=").append(CONSUMER_KEY);
		String url = sb.toString();
		String httpMethod = "DELETE";
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Add list member interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param targetUid	Target user ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject addListMember(String uid, String listId, String targetUid, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/members.xml");
		String url = sb.toString();
		//
		List<PostParameter> postParams = new LinkedList<PostParameter>();
		postParams.add(new PostParameter("id", String.valueOf(targetUid)));
		postParams.add(new PostParameter("source", CONSUMER_KEY));
		PostParameter[] params = new PostParameter[postParams.size()];
		int index = 0;
		for (PostParameter postParam : postParams) {
			params[index++] = postParam;
		}
		String httpMethod = "POST";
		//
		Response res = http.httpRequest(url, params, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Destroy list menber interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param targetUid	Target user ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject removeListMember(String uid, String listId, String targetUid, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/members.xml").append("?source=").append(CONSUMER_KEY).append("&id=").append(String.valueOf(targetUid));
		String url = sb.toString();
		String httpMethod = "DELETE";
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Subscribe list interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject addListSubscriber(String uid, String listId, boolean auth) throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/subscribers.xml");
		String url = sb.toString();
		//
		List<PostParameter> postParams = new LinkedList<PostParameter>();
		postParams.add(new PostParameter("source", CONSUMER_KEY));
		PostParameter[] params = new PostParameter[postParams.size()];
		int index = 0;
		for (PostParameter postParam : postParams) {
			params[index++] = postParam;
		}
		String httpMethod = "POST";
		//
		Response res = http.httpRequest(url, params, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * Remove subscribe list interface
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public ListObject removeListSubscriber(String uid, String listId, boolean auth)
			throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/subscribers.xml").append("?source=").append(CONSUMER_KEY);
		String url = sb.toString();
		//
		String httpMethod = "DELETE";
		//
		Response res = http.httpRequest(url, null, auth, httpMethod);
		return new ListObject(res, this);
	}

	/**
	 * confirm list member
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param targetUid	Target user ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public boolean isListMember(String uid, String listId, String targetUid, boolean auth)
			throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/members/").append(targetUid)
				.append(".xml").append("?source=").append(CONSUMER_KEY);
		String url = sb.toString();
		//
		String httpMethod = "GET";
		//
		Document doc = http.httpRequest(url, null, auth, httpMethod).asDocument();
		Element root = doc.getDocumentElement();
		return "true".equals(root.getTextContent());
	}

	/**
	 * confirm subscription list
	 * @param uid		User ID or screen_name
	 * @param listId	The ID or slug of ListObject
	 * @param targetUid	Target user ID or screen_name
	 * @param auth		if true, the request will be sent with BASIC authentication header
	 * @return
	 * @throws WeiboException
	 */
	public boolean isListSubscriber(String uid, String listId, String targetUid, boolean auth)
			throws WeiboException {
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseURL()).append(uid).append("/").append(listId).append("/subscribers/").append(targetUid)
				.append(".xml").append("?source=").append(CONSUMER_KEY);
		String url = sb.toString();
		//
		String httpMethod = "GET";
		//
		Document doc = http.httpRequest(url, null, auth, httpMethod).asDocument();
		Element root = doc.getDocumentElement();
		return "true".equals(root.getTextContent());
	}

    /* Help Methods */
    /**
     * Returns the string "ok" in the requested format with a 200 OK HTTP status code.
     * @return true if the API is working
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public boolean test() throws WeiboException {
        return -1 != get(getBaseURL() + "help/test.json", false).
                asString().indexOf("ok");
    }

    /**
     * Returns extended information of the authenticated user.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.<br>
     * The call Weibo.getAuthenticatedUser() is equivalent to the call:<br>
     * weibo.getUserDetail(weibo.getUserId());
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     * @deprecated Use verifyCredentials() instead
     */
    public User getAuthenticatedUser() throws WeiboException {
        return new User(get(getBaseURL() + "account/verify_credentials.xml", true),this);
    }

    /**
     * @return the schedule
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.0.4
     * @deprecated this method is not supported by the Weibo API anymore
     */
    public String getDowntimeSchedule() throws WeiboException {
        throw new WeiboException(
                "this method is not supported by the Weibo API anymore"
                , new NoSuchMethodException("this method is not supported by the Weibo API anymore"));
    }


    private SimpleDateFormat format = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weibo weibo = (Weibo) o;

        if (!baseURL.equals(weibo.baseURL)) return false;
        if (!format.equals(weibo.format)) return false;
        if (!http.equals(weibo.http)) return false;
        if (!searchBaseURL.equals(weibo.searchBaseURL)) return false;
        if (!source.equals(weibo.source)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = http.hashCode();
        result = 31 * result + baseURL.hashCode();
        result = 31 * result + searchBaseURL.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + format.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Weibo{" +
                "http=" + http +
                ", baseURL='" + baseURL + '\'' +
                ", searchBaseURL='" + searchBaseURL + '\'' +
                ", source='" + source + '\'' +
                ", format=" + format +
                '}';
    }

    /**
     * Returns to the designated status's latest n comments
     * @param id specifies the ID of status
     * @return a list of comments objects
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Comment> getComments(String id) throws WeiboException {
    	return Comment.constructComments(get(getBaseURL() + "statuses/comments.json?id="+id, true));
    }

    /**
     * Returns in chronological order to send and receive the latest n the comments section
     * @return a list of comments objects
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Comment> getCommentsTimeline() throws WeiboException {
    	return Comment.constructComments(get(getBaseURL() + "statuses/comments_timeline.json", true));
    }

    /**
     * Returns the current user's comments
     * @return a list of comments objects
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Comment> getCommentsByMe() throws WeiboException {
    	return Comment.constructComments(get(getBaseURL() + "statuses/comments_by_me.json", true));
    }

    /**
     * return the current user's comment
     * @param _since_id Returns then weibos with an ID greater than (that is, more recent than) the specified ID 
     * @return
     * @throws WeiboException
     */
    public List<Comment> getCommentsToMe(long _since_id) throws WeiboException {
    	return Comment.constructComments(get(getBaseURL() + "statuses/comments_to_me.json?since_id="+_since_id, true));
    }
    
    /**
     * return the current user's comment 
     * @return
     * @throws WeiboException
     */
    public List<Comment> getCommentsToMe() throws WeiboException {
    	return Comment.constructComments(get(getBaseURL() + "statuses/comments_to_me.json", true));
    }

    /**
     * Returns the number of comments, the number of repost
     * @param ids ids a string, separated by commas
     * @return a list of counts objects
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Count> getCounts(String ids) throws WeiboException{
    	return Count.constructCounts(get(getBaseURL() + "statuses/counts.json?ids="+ids, true));
    }

    /**
     * Returns the number of the current user does not read messages
     * @return count objects
     * @throws WeiboException when Weibo service or network is unavailable
     * @throws JSONException
     * @since Weibo4J 1.1220
     */
    public Count getUnread() throws WeiboException, JSONException {
    	return new Count(get(getBaseURL() + "statuses/unread.json", true).asJSONObject());
    }

    /**
     * Return a status of repost
     * @param sid id specifies the ID of status
     * @param status message
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public Status repost(String sid,String status) throws WeiboException {
    	return new Status(http.post(getBaseURL() + "statuses/repost.json",
                new PostParameter[]{new PostParameter("id", sid),
									new PostParameter("status", status)}, true));
    }
    /**
     * Return statuses of repost by me.
     * @param id specifies the id of user
     * @return statuses
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public List<Status> getrepostbyme(String id)throws WeiboException {
    	return Status.constructStatuses(http.get(getBaseURL()+"statuses/repost_by_me.json"+"?source="+Weibo.CONSUMER_KEY+"&id="+id,true));
    }
    /**
     * Returns a original status of the latest n forwarding statuses. This interface is not an original status can query.
     * @param id specifies the id of original status.
     * @return a list of statuses object
     * @throws WeiboException
     * @since Weibo4J 1.1220
     */
    public List<Status>getreposttimeline(String id)throws WeiboException{
    	return Status.constructStatuses(http.get(getBaseURL()+"statuses/repost_timeline.json"+"?source="+Weibo.CONSUMER_KEY+"&id="+id,true));
    }
    
    /**
     * Return a status of reply
     * @param sid id specifies the ID of status
     * @param cid id specifies the ID of comment
     * @param comment a single comment
     * @return  a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1220
     */
    public Status reply(String sid,String cid,String comment) throws WeiboException {
    	return new Status(http.post(getBaseURL() + "statuses/reply.json",
                new PostParameter[]{new PostParameter("id", sid),
    								new PostParameter("cid", cid),
									new PostParameter("comment", comment),
									new PostParameter("without_mention", "1")}, true));
    }

    /**
     * Return your relationship with the details of a user
     * @param target_id id of the befriended user
     * @return jsonObject
     * @throws WeiboException when Weibo service or network is unavailable
     */
    public JSONObject showFriendships(String target_id) throws WeiboException {
    	return get(getBaseURL() + "friendships/show.json?target_id="+target_id, true).asJSONObject();
    }

    /**
     * Return the details of the relationship between two users
     * @param target_id id of the befriended user
     * @return jsonObject
     * @throws WeiboException when Weibo service or network is unavailable
     * @Ricky  Add source parameter&missing "="
     */
    public JSONObject showFriendships(String source_id,String target_id) throws WeiboException {
    	return get(getBaseURL() + "friendships/show.json?target_id="+target_id+"&source_id="+source_id+"&source="+CONSUMER_KEY, true).asJSONObject();
    }

    /**
     * Return infomation of current user
     * @return a user's object
     * @throws WeiboException when Weibo service or network is unavailable
     */
    public User endSession() throws WeiboException {
    	return new User(get(getBaseURL() + "account/end_session.json", true).asJSONObject());
    }

    /**
     *  Return infomation of current user
     * @param ip a specified ip(Only open to invited partners)
     * @param args User Information args[2]:nickname,args[3]:gender,args[4],password,args[5]:email
     * @return jsonObject
     * @throws WeiboException when Weibo service or network is unavailable
     */
    public JSONObject register(String ip,String ...args) throws WeiboException {
    	return http.post(getBaseURL() + "account/register.json",
                new PostParameter[]{new PostParameter("nick", args[2]),
									new PostParameter("gender", args[3]),
									new PostParameter("password", args[4]),
									new PostParameter("email", args[5]),
									new PostParameter("ip", ip)}, true).asJSONObject();
    }
   
    /**
     * Return to the list of tags specified user
     * @param user_id
     * @return tags
     * @throws WeiboException
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Tags">Tags </a>
     */
    public List<Tag>gettags(String user_id)throws WeiboException{
    	return Tag.constructTags(http.get(getBaseURL()+"tags.json?"+"user_id="+user_id,true));
    }
   
    /**
    * Add user tags
    * @param tags
    * @return tagid
    * @throws WeiboException
    * @throws JSONException
    * @since Weibo4J 1.1220
    * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Tags/create">Tags/create </a>
    */
    
    public List<Tag> createTags(String tags)throws WeiboException, JSONException{
        return Tag.constructTags(http.post(getBaseURL()+"tags/create.json", 
        new PostParameter[]{new PostParameter("tags",tags)},true));
       
       }
    /**
     * Returns the user interested tags
     * @return a list of tags
     * @throws WeiboException
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Tags/suggestions">Tags/suggestions </a>
     */
    
    public List<Tag> getSuggestions()throws WeiboException{
        return Tag.constructTags(get(getBaseURL()+"tags/suggestions.json",true));
       }
    /**
     * Delete tags
     * @param tag_id
     * @return 
     * @throws WeiboException
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Tags/destroy">Tags/destroy </a>
     */
    
    public  JSONObject destoryTag(String tag_id)throws WeiboException{
        return  http.post(getBaseURL()+"tags/destroy.json",
        new PostParameter[]{new PostParameter("tag_id",tag_id)},true).asJSONObject();
       }
    /**
     * Batch delete tags
     * @param ids
     * @return tagid
     * @throws WeiboException
     * @since Weibo4J 1.1220
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Tags/destroy_batch">Tags/destroy_batch </a>
     */
    
    public List<Tag> destory_batchTags(String ids)throws WeiboException{
        return Tag.constructTags(http.post(getBaseURL()+"tags/destroy_batch.json",
        new PostParameter[]{new PostParameter("ids",ids)},true));
       }
    
  

    /**
     * yuch added 
     */
    
    /**
     * updata the current appkey user following friends remark
     * @param _userId user following friend id
     * @param _remark update remark description
     * @return User
     */
    public User updateFriendRemark(long _userId,String _remark)throws WeiboException,UnsupportedEncodingException{
    	return new User(http.post(getBaseURL() + "user/friends/update_remark.json",
    			new PostParameter[]{new PostParameter("user_id", _userId),new PostParameter("remark", URLEncoder.encode(_remark, "UTF-8"))}, true).asJSONObject());
    }
    
    
}