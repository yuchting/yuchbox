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

import java.util.Date;

/**
 * Weibo API with a series of asynchronous APIs.<br>
 * With this class, you can call WeiboAPI acynchronously.<br>
 * @see weibo4j.AsyncWeibo
 * @see weibo4j.WeiboListener
 */
public class AsyncWeibo extends Weibo {
    private static final long serialVersionUID = -2008667933225051907L;

    public AsyncWeibo(String id, String password) {
        super(id, password);
    }

    public AsyncWeibo(String id, String password, String baseURL) {
        super(id, password, baseURL);
    }

    /**
     * Returns tweets that match a specified query.
     * <br>This method calls http://api.t.sina.com.cn/users/search.format
     * 
     * @param query - the search condition
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.7
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/search">users/search </a>
     */
    public void searchAcync(Query query, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SEARCH, listener, new Object[]{query}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.searched(search((Query) args[0]));
            }
        });
    }

    /**
     * Returns the top ten topics that are currently trending on Weibo.  The response includes the time of the request, the name of each trend, and the url to the <a href="http://search.t.sina.com.cn/">Weibo Search</a> results page for that topic.
     * @since Weibo4J 2.0.2
     * @param listener a listener object that receives the response
     */
    public void getTrendsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(TRENDS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotTrends(getTrends());
            }
        });
    }

    /**
     * Returns the current top 10 trending topics on Weibo.  The response includes the time of the request, the name of each trending topic, and query used on <a href="http://search.t.sina.com.cn/">Weibo Search</a> results page for that topic.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.2
     */
    public void getCurrentTrendsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CURRENT_TRENDS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotCurrentTrends(getCurrentTrends());
            }
        });
    }

    /**
     * Returns the current top 10 trending topics on Weibo.  The response includes the time of the request, the name of each trending topic, and query used on <a href="http://search.t.sina.com.cn/">Weibo Search</a> results page for that topic.
     * <br>This method calls http://search.t.sina.com.cn/trends/current
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @since Weibo4J 2.0.2
     * @param listener a listener object that receives the response
     */
    public void getCurrentTrendsAsync(boolean excludeHashTags, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CURRENT_TRENDS, listener
                , new Object[]{excludeHashTags}) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotCurrentTrends(getCurrentTrends((Boolean)args[0]));
            }
        });
    }


    /**
     * Returns the top 20 trending topics for each hour in a given day.
     * <br>This method calls http://search.t.sina.com.cn/trends/daily
     * @since Weibo4J 2.0.2
     * @param listener a listener object that receives the response
     */
    public void getDailyTrendsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DAILY_TRENDS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotDailyTrends(getDailyTrends());
            }
        });
    }

    /**
     * Returns the top 20 trending topics for each hour in a given day.
     * <br>This method calls http://search.t.sina.com.cn/trends/daily
     * @param date Permits specifying a start date for the report.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.2
     */
    public void getDailyTrendsAsync(Date date, boolean excludeHashTags, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DAILY_TRENDS, listener
                , new Object[]{date, excludeHashTags}) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotDailyTrends(getDailyTrends((Date) args[0]
                        , (Boolean) args[1]));
            }
        });
    }

    /**
     * Returns the top 30 trending topics for each day in a given week.
     * <br>This method calls http://search.t.sina.com.cn/trends/weekly
     * @since Weibo4J 2.0.2
     * @param listener a listener object that receives the response
     */
    public void getWeeklyTrendsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(WEEKLY_TRENDS, listener
                , null) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotWeeklyTrends(getWeeklyTrends());
            }
        });
    }

    /**
     * Returns the top 30 trending topics for each day in a given week.
     * <br>This method calls http://search.t.sina.com.cn/trends/weekly
     * @param date Permits specifying a start date for the report.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @since Weibo4J 2.0.2
     * @param listener a listener object that receives the response
     */
    public void getWeeklyTrendsAsync(Date date, boolean excludeHashTags, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(WEEKLY_TRENDS, listener
                , new Object[]{date, excludeHashTags}) {
            public void invoke(WeiboListener listener, Object[] args) throws
                    WeiboException {
                listener.gotWeeklyTrends(getWeeklyTrends((Date) args[0]
                        , (Boolean) args[1]));
            }
        });
    }

    /**
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline
     * Returns the 20 most recent statuses from non-protected users who have set a custom user icon.
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     */
    public void getPublicTimelineAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(PUBLIC_TIMELINE, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotPublicTimeline(getPublicTimeline());
            }
        });
    }

    /**
     * Returns only public statuses with an ID greater than (that is, more recent than) the specified ID.
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline
     * @param sinceID String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated use getpublicTimelineAsync(long sinceID, WeiboListener listener)
     */
    public void getPublicTimelineAsync(int sinceID, WeiboListener listener) {
        getPublicTimelineAsync((long)sinceID, listener);
    }

    /**
     * Returns only public statuses with an ID greater than (that is, more recent than) the specified ID.
     * <br>This method calls http://api.t.sina.com.cn/statuses/public_timeline
     * @param sinceID String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     */
    public void getPublicTimelineAsync(long sinceID, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(PUBLIC_TIMELINE, listener, new Long[] {sinceID}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotPublicTimeline(getPublicTimeline( (Long) args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls http://api.t.sina.com.cn/statuses/home_timeline.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @since Weibo4J 2.0.10
     */
    public void getHomeTimelineAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(HOME_TIMELINE, listener,null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotHomeTimeline(getHomeTimeline());
            }
        });
    }

    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls http://api.t.sina.com.cn/statuses/home_timeline.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @since Weibo4J 2.0.10
     */
    public void getHomeTimelineAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(HOME_TIMELINE, listener,new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotHomeTimeline(getHomeTimeline((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user and that user's friends.
     *  It's also possible to request another user's friends_timeline via the id parameter below.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public void getFriendsTimelineAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_TIMELINE, listener,null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFriendsTimeline(getFriendsTimeline());
            }
        });
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user and that user's friends.
     *  It's also possible to request another user's friends_timeline via the id parameter below.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @since Weibo4J 2.0.1
     */

    public void getFriendsTimelineAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_TIMELINE, listener,new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFriendsTimeline(getFriendsTimeline((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param page int
     * @param listener a listener object that receives the response
     * @deprecated Use getFriendsTimelineAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public void getFriendsTimelineByPageAsync(int page, WeiboListener listener) {
        getFriendsTimelineAsync(new Paging(page),listener);
    }
    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param page int
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.8
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated Use getFriendsTimelineAsync(Paging paging, WeiboListener listener) instead
     */
    public void getFriendsTimelineAsync(int page, WeiboListener listener) {
        getFriendsTimelineAsync(new Paging(page),listener);
    }


    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param page int
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.8
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated Use getFriendsTimelineAsync(Paging paging, WeiboListener listener) instead
     */
    public void getFriendsTimelineAsync(long sinceId, int page, WeiboListener listener) {
        getFriendsTimelineAsync(new Paging(page,sinceId),listener);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified user id.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param id String user ID
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineAsync(String id, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified user id.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param id String user ID
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @since Weibo4J 2.0.1
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineAsync(String id, Paging paging, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified user id.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param id String user ID
     * @param page int
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineByPageAsync(String id, int page, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }


    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified user id.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param id String user ID
     * @param page int
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.8
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineAsync(String id, int page, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineAsync(long sinceId, String id, int page, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }


    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param since Date
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated Use getFriendsTimelineAsync(String id, Paging paging, WeiboListener listener) instead
     */
    public void getFriendsTimelineAsync(Date since, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_TIMELINE, listener, new Object[] {since}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFriendsTimeline(getFriendsTimeline( (Date) args[0]));
            }
        });
    }


    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated Use getFriendsTimelineAsync(Paging paging, WeiboListener listener) instead
     */
    public void getFriendsTimelineAsync(long sinceId, WeiboListener listener) {
        getFriendsTimelineAsync(new Paging(sinceId), listener);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param id String user ID
     * @param since Date
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineAsync(String id, Date since, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     *  <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @param id String user ID
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public void getFriendsTimelineAsync(String id, long sinceId, WeiboListener listener) {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param id String
     * @param count int
     * @param since Date
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public void getUserTimelineAsync(String id, int count, Date since, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener, new Object[] {id, count, since}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotUserTimeline(getUserTimeline( (String) args[0], (Integer) args[1], (Date) args[2]));
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id Specifies the ID or screen name of the user for whom to return the user_timeline.
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 2.0.1
     */
    public void getUserTimelineAsync(String id, Paging paging,
                                                  WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener,
                new Object[]{id, paging}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.gotUserTimeline(getUserTimeline((String) args[0],
                        (Paging) args[1]));
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param id       String
     * @param page    int
     * @param sinceId  Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimelineAsync(String id, Paging paging, WeiboListener listener) instead
     */
    public void getUserTimelineAsync(String id, int page,
                                                  long sinceId,
                                                  WeiboListener listener) {
        getUserTimelineAsync(id, new Paging(page, sinceId), listener);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param id String
     * @param since Date
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public void getUserTimelineAsync(String id, Date since, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener, new Object[] {id, since}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotUserTimeline(getUserTimeline( (String) args[0], (Date) args[1]));
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param id String
     * @param count int
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated Use getUserTimelineAsync(String id, Paging paging, WeiboListener listener) instead
     */
    public void getUserTimelineAsync(String id, int count, WeiboListener listener) {
        getUserTimelineAsync(id, new Paging().count(count), listener);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param count int
     * @param since Date
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public void getUserTimelineAsync(int count, Date since, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener, new Object[] {count, since}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotUserTimeline(getUserTimeline( (Integer) args[0], (Date) args[1]));
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param paging   controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 2.0.1
     */
    public void getUserTimelineAsync(Paging paging,
                                                  WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener,
                new Object[]{paging}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.gotUserTimeline(getUserTimeline((Paging) args[0]));
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     *
     * @param count    int
     * @param sinceId  Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimelineAsync(Paging paging, WeiboListener listener) instead
     */
    public void getUserTimelineAsync(int count, long sinceId,
                                                  WeiboListener listener) {
        getUserTimelineAsync(new Paging(sinceId).count(count), listener);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param id String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     */
    public void getUserTimelineAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotUserTimeline(getUserTimeline( (String) args[0]));
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified user id.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param id String
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimelineAsync(String id, Paging paging, WeiboListener listener) instead
     */
    public void getUserTimelineAsync(String id, long sinceId,
                                                  WeiboListener listener) {
        getUserTimelineAsync(id, new Paging(sinceId),listener);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     */
    public void getUserTimelineAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_TIMELINE, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotUserTimeline(getUserTimeline());
            }
        });
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/statuses/user_timeline.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/user_timeline">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimelineAsync(Paging paging, WeiboListener listener) instead
     */
    public void getUserTimelineAsync(long sinceId, WeiboListener listener) {
        getUserTimelineAsync(new Paging(sinceId), listener);
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     * @deprecated Use getMentionsAsync(WeiboListener listener) instead
     */
    public void getRepliesAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(REPLIES, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotReplies(getReplies());
            }
        });
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/mentions.format
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/mentions">Statuses/mentions </a>
     */
    public void getMentionsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(MENTIONS, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotMentions(getMentions());
            }
        });
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/mentions.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/mentions">Statuses/mentions </a>
     */
    public void getMentionsAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(MENTIONS, listener, new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotMentions(getMentions((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     */
    public void getRetweetedByMeAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY_ME, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotRetweetedByMe(getRetweetedByMe());
            }
        });
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user.
     * <br>This method calls http://api.i.sina.com.cn/statuses/retweeted_by_me
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.i.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweeted_by_me">Weibo API Wiki / Weibo REST API Method: statuses/retweeted_by_me</a>
     */
    public void getRetweetedByMeAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_BY_ME, listener, new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotRetweetedByMe(getRetweetedByMe((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user's friends.
     * <br>This method calls http://api.i.sina.com.cn/statuses/retweeted_by_me
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.i.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweeted_to_me">Weibo API Wiki / Weibo REST API Method: statuses/retweeted_to_me</a>
     */
    public void getRetweetedToMeAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_TO_ME, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotRetweetedToMe(getRetweetedToMe());
            }
        });
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user's friends.
     * <br>This method calls http://api.i.sina.com.cn/statuses/retweeted_by_me
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.i.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweeted_to_me">Weibo API Wiki / Weibo REST API Method: statuses/retweeted_to_me</a>
     */
    public void getRetweetedToMeAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETED_TO_ME, listener, new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotRetweetedToMe(getRetweetedToMe((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * <br>This method calls http://api.i.sina.com.cn/statuses/retweeted_by_me
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.i.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweets_of_me">Weibo API Wiki / Weibo REST API Method: statuses/retweets_of_me</a>
     */
    public void getRetweetsOfMeAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETS_OF_ME, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotRetweetsOfMe(getRetweetsOfMe());
            }
        });
    }

    /**
     * Returns the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * <br>This method calls http://api.i.sina.com.cn/statuses/retweeted_by_me
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.i.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweets_of_me">Weibo API Wiki / Weibo REST API Method: statuses/retweets_of_me</a>
     */
    public void getRetweetsOfMeAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEETS_OF_ME, listener, new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotRetweetsOfMe(getRetweetsOfMe((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.8
     * @deprecated Use getMentionsAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public void getRepliesAsync(long sinceId, WeiboListener listener) {
        getMentionsAsync(new Paging(sinceId), listener);
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * @param page int
     * @param listener a listener object that receives the response
     * @deprecated Use getMentionsAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public void getRepliesByPageAsync(int page, WeiboListener listener) {
        getMentionsAsync(new Paging(page), listener);
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     * @param page int
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.8
     * @deprecated Use getMentionsAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public void getRepliesAsync(int page,WeiboListener listener) {
        getMentionsAsync(new Paging(page), listener);
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.t.sina.com.cn/statuses/reply.format
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param page int
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.8
     * @deprecated Use getMentionsAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/reply">statuses/reply </a>
     */
    public void getRepliesAsync(long sinceId, int page, WeiboListener listener) {
        getMentionsAsync(new Paging(page, sinceId), listener);
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/show/id.format
     * @param id int
     * @param listener a listener object that receives the response
     * @deprecated Use showAsync(long id) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/show">statuses/show </a>
     */
    public void showAsync(int id, WeiboListener listener) {
        showAsync((long) id, listener);
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/show/id.format
     * @param id int
     * @param listener a listener object that receives the response
     * @since 1.1.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/show">statuses/show </a>
     * @deprecated Use showStatusAsync(long id, WeiboListener listener) instead
     */
    public void showAsync(long id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotShow(show( (Long) args[0]));
            }
        });
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/show/id.format
     * @param id int
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/show">statuses/show </a>
     */
    public void showStatusAsync(long id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SHOW_STATUS, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotShowStatus(showStatus( (Long) args[0]));
            }
        });
    }

    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @deprecated Use updateStatusAsync(String status, WeiboListener listener) instead
     */
    public void updateAsync(String status, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE, listener, new String[] {status}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.updated(update( (String) args[0]));
            }
        });
    }
    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @deprecated Use updateStatusAsync(String status) instead
     */
    public void updateAsync(String status) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE,  new WeiboAdapter(), new String[] {status}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.updated(update( (String) args[0]));
            }
        });
    }

    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 2.0.1
     */
    public void updateStatusAsync(String status, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener, new String[] {status}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.updatedStatus(updateStatus( (String) args[0]));
            }
        });
    }
    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 2.0.1
     */
    public void updateStatusAsync(String status) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS,  new WeiboAdapter(), new String[] {status}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.updatedStatus(updateStatus( (String) args[0]));
            }
        });
    }

    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 1.1.6
     * @deprecated Use updateStatusAsync(String status, long inReplyToStatusId, WeiboListener listener) instead
     */
    public void updateAsync(String status, long inReplyToStatusId, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE, listener, new Object[]{status, inReplyToStatusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updated(update((String) args[0], (Long) args[1]));
            }
        });
    }

    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 1.1.6
     * @deprecated Use updateStatusAsync(String status, long inReplyToStatusId) instead
     */
    public void updateAsync(String status, long inReplyToStatusId) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE, new WeiboAdapter(), new Object[]{status, inReplyToStatusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updated(update((String) args[0], (Long) args[1]));
            }
        });
    }

    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 2.0.1
     */
    public void updateStatusAsync(String status, long inReplyToStatusId, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, listener, new Object[]{status, inReplyToStatusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updatedStatus(updateStatus((String) args[0], (Long) args[1]));
            }
        });
    }

    /**
     *
     * Updates the user's status asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/update.format
     *
     * @param status String
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
     * @since Weibo4J 2.0.1
     */
    public void updateStatusAsync(String status, long inReplyToStatusId) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_STATUS, new WeiboAdapter(), new Object[]{status, inReplyToStatusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updatedStatus(updateStatus((String) args[0], (Long) args[1]));
            }
        });
    }

    /**
     *
     * Destroys the status specified by the required ID parameter. asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     * @param statusId String
     * @since 1.0.5
     * @deprecated Use destroyStatusAsync(long statuId) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public void destoryStatusAsync(int statusId) {
        destroyStatusAsync((long) statusId);
    }

    /**
     *
     * Destroys the status specified by the required ID parameter. asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     * @param statusId String
     * @since 1.1.2
     * @deprecated Use destroyStatusAsync(long statuId) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public void destroyStatusAsync(int statusId) {
        destroyStatusAsync((long) statusId);
    }

    /**
     * Destroys the status specified by the required ID parameter. asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     *
     * @param statusId String
     * @since 1.1.2
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public void destroyStatusAsync(long statusId) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_STATUS, new WeiboAdapter(), new Long[]{statusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.destroyedStatus(destroyStatus(((Long) args[0])));
            }
        });
    }
    /**
     * Destroys the status specified by the required ID parameter. asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     * @param statusId String
     * @param listener a listener object that receives the response
     * @since 1.0.6
     * @deprecated Use destroyStatusAsync(long statuId) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public void destoryStatusAsync(int statusId, WeiboListener listener) {
        destroyStatusAsync((long) statusId, listener);
    }
    /**
     * Destroys the status specified by the required ID parameter. asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     * @param statusId String
     * @param listener a listener object that receives the response
     * @since 1.1.2
     * @deprecated Use destroyStatusAsync(long statuId) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public void destroyStatusAsync(int statusId, WeiboListener listener) {
        destroyStatusAsync((long) statusId, listener);
    }

    /**
     * Destroys the status specified by the required ID parameter. asynchronously
     * <br>This method calls http://api.t.sina.com.cn/statuses/destroy/id.format
     *
     * @param statusId String
     * @param listener a listener object that receives the response
     * @since 1.1.2
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/destroy">statuses/destroy </a>
     */
    public void destroyStatusAsync(long statusId, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_STATUS, listener, new Long[]{statusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.destroyedStatus(destroyStatus(((Long) args[0])));
            }
        });
    }


    /**
     * Retweets a tweet. Requires the id parameter of the tweet you are retweeting. Returns the original tweet with retweet details embedded.
     * <br>This method calls http://api.t.sina.com.cn/statuses/retweet
     * @param statusId The ID of the status to retweet.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweet">Weibo API Wiki / Weibo REST API Method: statuses retweet</a>
     */
    public void retweetStatusAsync(long statusId, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RETWEET_STATUS, listener, new Long[]{statusId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.retweetedStatus(retweetStatus(((Long) args[0])));
            }
        });
    }

    /**
     * Retweets a tweet. Requires the id parameter of the tweet you are retweeting. Returns the original tweet with retweet details embedded.
     * <br>This method calls http://api.t.sina.com.cn/statuses/retweet
     * @param statusId The ID of the status to retweet.
     * @since Weibo4J 2.0.10
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method%3A-statuses-retweet">Weibo API Wiki / Weibo REST API Method: statuses retweet</a>
     */
    public void retweetStatusAsync(long statusId) {
        retweetStatusAsync(statusId, new WeiboAdapter());
    }

    /**
     * Retrieves extended information of a given user, specified by ID or screen name as per the required id parameter below.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.
     * <br>This method calls http://api.t.sina.com.cn/users/show.format
     * @param id String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/show">users/show </a>
     * @deprecated use showUserAsync(id,listener) instead
     */
    public void getUserDetailAsync(String id, WeiboListener listener) {
        showUserAsync(id, listener);
    }

    /**
     * Retrieves extended information of a given user, specified by ID or screen name as per the required id parameter below.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.
     * <br>This method calls http://api.t.sina.com.cn/users/show.format
     * @param id String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/show">users/show </a>
     * @since Weibo4J 2.0.9
     */
    public void showUserAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(USER_DETAIL, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotUserDetail(showUser( (String) args[0]));
            }
        });
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatusesAsync(listener)
     */
    public void getFriendsAsync(WeiboListener listener) {
        getFriendsStatusesAsync(listener);
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 2.0.9
     */
    public void getFriendsStatusesAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFriends(getFriendsStatuses());
            }
        });
    }


    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatusesAsync(paging,listener) instead
     */
    public void getFriendsAsync(Paging paging,WeiboListener listener) {
        getFriendsStatusesAsync(paging, listener);
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.9
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public void getFriendsStatusesAsync(Paging paging,WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS, listener, new Object[]{paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFriends(getFriendsStatuses((Paging)args[0]));
            }
        });
    }

    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param page number of the page to retrieve friends
     * @param listener a listener object that receives the response
     * @deprecated Use getFriendsStatusesAsync(Paging paging,WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public void getFriendsAsync(int page,WeiboListener listener) {
        getFriendsStatusesAsync(new Paging(page), listener);
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param id String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatusesAsync(id, listener) instead
     */
    public void getFriendsAsync(String id, WeiboListener listener) {
        getFriendsStatusesAsync(id, listener);
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param id String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 2.0.9
     */
    public void getFriendsStatusesAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFriends(getFriendsStatuses( (String) args[0]));
            }
        });
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param id String
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @deprecated use getFriendsStatusesAsync(id, paging, listener) instead
     */
    public void getFriendsAsync(String id, Paging paging, WeiboListener listener) {
        getFriendsStatusesAsync(id, paging, listener);
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param id String
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 2.0.9
     */
    public void getFriendsStatusesAsync(String id, Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS, listener, new Object[] {id,paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFriends(getFriendsStatuses((String) args[0], (Paging) args[1]));
            }
        });
    }

    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     * @param id String
     * @param page int
     * @param listener a listener object that receives the response
     * @deprecated Use getFriendsStatusesAsync(String id, Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public void getFriendsAsync(String id,int page, WeiboListener listener) {
        getFriendsStatusesAsync(id, new Paging(page), listener);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatusesAsync(listener)
     */
    public void getFollowersAsync(WeiboListener listener) {
        getFollowersStatusesAsync(listener);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @since Weibo4J 2.0.9
     */
    public void getFollowersStatusesAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFollowers(getFollowers());
            }
        });
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * deprecated use getFollowersStatusesAsync(paging, listener) instead
     */
    public void getFollowersAsync(Paging paging, WeiboListener listener) {
        getFollowersStatusesAsync(paging, listener);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.9
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public void getFollowersStatusesAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS, listener, new Object[]{paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowers(getFollowersStatuses((Paging) args[0]));
            }
        });
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param page Retrieves the next 100 followers.
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.0
     * @deprecated Use getFollowersStatusesAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public void getFollowersAsync(int page, WeiboListener listener) {
        getFollowersStatusesAsync(new Paging(page), listener);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id       The ID or screen name of the user for whom to request a list of followers.
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.0
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatusesAsync(id, listener) instead
     */
    public void getFollowersAsync(String id, WeiboListener listener) {
        getFollowersStatusesAsync(id, listener);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     *
     * @param id       The ID or screen name of the user for whom to request a list of followers.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.9
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public void getFollowersStatusesAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS, listener, new Object[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowers(getFollowersStatuses((String) args[0]));
            }
        });
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param id The ID or screen name of the user for whom to request a list of followers.
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     * @deprecated use getFollowersStatusesAsync(id, paging, listener) instead
     */
    public void getFollowersAsync(String id, Paging paging, WeiboListener listener) {
        getFollowersStatusesAsync(id, paging, listener);
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param id The ID or screen name of the user for whom to request a list of followers.
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.9
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public void getFollowersStatusesAsync(String id, Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS, listener, new Object[]{id, paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowers(getFollowersStatuses((String) args[0], (Paging) args[1]));
            }
        });
    }

    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.t.sina.com.cn/statuses/followers.format
     * @param id The ID or screen name of the user for whom to request a list of followers.
     * @param page Retrieves the next 100 followers.
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.1.0
     * @deprecated Use getFollowersStatusesAsync(String id, Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/followers">statuses/followers </a>
     */
    public void getFollowersAsync(String id, int page, WeiboListener listener) {
        getFollowersStatusesAsync(id, new Paging(page), listener);
    }

    /**
     * Returns a list of the users currently featured on the site with their current statuses inline.
     * @param listener a listener object that receives the response
     */
    public void getFeaturedAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FEATURED, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFeatured(getFeatured());
            }
        });
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public void getDirectMessagesAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener,null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotDirectMessages(getDirectMessages());
            }
        });
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public void getDirectMessagesAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener, new Object[] {paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotDirectMessages(getDirectMessages( (Paging) args[0]));
            }
        });
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     * @param page int
     * @param listener a listener object that receives the response
     * @deprecated Use getDirectMessagesAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public void getDirectMessagesByPageAsync(int page, WeiboListener listener) {
        getDirectMessagesAsync(new Paging(page), listener);
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     * @param page int
     * @param sinceId Returns only direct messages with an ID greater than (that is, more recent than) the specified ID.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.0
     * @deprecated Use getDirectMessagesAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public void getDirectMessagesByPageAsync(int page
            , int sinceId, WeiboListener listener) {
        getDirectMessagesAsync(new Paging(page,(long)sinceId), listener);
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     * @param sinceId int
     * @param listener a listener object that receives the response
     * @deprecated Use getDirectMessagesAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public void getDirectMessagesAsync(int sinceId, WeiboListener listener) {
        getDirectMessagesAsync(new Paging((long)sinceId), listener);
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format
     * @param since Date
     * @param listener a listener object that receives the response
     * @deprecated using long sinceId is suggested.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages">direct_messages </a>
     */
    public void getDirectMessagesAsync(Date since, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener, new Object[] {since}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotDirectMessages(getDirectMessages( (Date) args[0]));
            }
        });
    }
    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">发送私信 </a>
     */
    public void getSentDirectMessagesAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener,null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotSentDirectMessages(getSentDirectMessages());
            }
        });
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     * @param paging controls pagination
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">发送私信 </a>
     */
    public void getSentDirectMessagesAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener, new Object[] {paging}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotSentDirectMessages(getSentDirectMessages( (Paging) args[0]));
            }
        });
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     * @param since Date
     * @param listener a listener object that receives the response
     * @deprecated using long sinceId is suggested.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">发送私信 </a>
     */
    public void getSentDirectMessagesAsync(Date since, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DIRECT_MESSAGES, listener, new Object[] {since}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotSentDirectMessages(getSentDirectMessages( (Date) args[0]));
            }
        });
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     * @param sinceId returns only sent direct messages with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @deprecated Use getSentDirectMessagesAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">发送私信 </a>
     */
    public void getSentDirectMessagesAsync(int sinceId, WeiboListener listener) {
        getSentDirectMessagesAsync(new Paging((long) sinceId), listener);
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages/sent.format
     * @param page Retrieves the 20 next most recent direct messages.
     * @param sinceId returns only sent direct messages with an ID greater than (that is, more recent than) the specified ID
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.0
     * @deprecated Use getSentDirectMessagesAsync(Paging paging, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/sent">发送私信 </a>
     */
    public void getSentDirectMessagesAsync(int page
            , int sinceId, WeiboListener listener) {
        getSentDirectMessagesAsync(new Paging(page, (long) sinceId), listener);
    }

    /**
     * Sends a new direct message to the specified user from the authenticating user.  Requires both the user and text parameters below.
     * The text will be trimed if the length of the text is exceeding 140 characters.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format/new
     * @param id String
     * @param text String
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/new">发送私信 </a>
     */
    public void sendDirectMessageAsync(String id, String text, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(SEND_DIRECT_MESSAGE, listener, new String[] {id, text}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.sentDirectMessage(sendDirectMessage( (String) args[0], (String) args[1]));
            }
        });
    }

    /**
     * Sends a new direct message to the specified user from the authenticating user.  Requires both the user and text parameters below.
     * The text will be trimed if the length of the text is exceeding 140 characters.
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format/new
     * @param id String
     * @param text String
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/new">发送私信 </a>
     */
    public void sendDirectMessageAsync(String id, String text) {
        getDispatcher().invokeLater(new AsyncTask(SEND_DIRECT_MESSAGE, new WeiboAdapter(), new String[]{id, text}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.sentDirectMessage(sendDirectMessage( (String) args[0], (String) args[1]));
            }
        });
    }

    /**
     * Delete specified direct message
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format/destroy
     * @param id int
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/destroy">Weibo API Wiki / Weibo REST API Method: direct_messages destroy</a>
     * @deprecated Use destroyDirectMessageAsync(int id, WeiboListener listener) instead
     */
    public void deleteDirectMessageAsync(int id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_DIRECT_MESSAGES, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.deletedDirectMessage(deleteDirectMessage((Integer) args[0]));
            }
        });
    }

    /**
     * Delete specified direct message
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format/destroy
     * @param id int
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/destroy">Weibo API Wiki / Weibo REST API Method: direct_messages destroy</a>
     * @since Weibo4J 2.0.1
     */
    public void destroyDirectMessageAsync(int id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_DIRECT_MESSAGES, listener, new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.destroyedDirectMessage(destroyDirectMessage((Integer) args[0]));
            }
        });
    }

    /**
     * Delete specified direct message
     * <br>This method calls http://api.t.sina.com.cn/direct_messages.format/destroy
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Direct_messages/destroy">Weibo API Wiki / Weibo REST API Method: direct_messages destroy</a>
     * @since Weibo4J 2.0.1
     */
    public void destroyDirectMessageAsync(int id) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_DIRECT_MESSAGES, new WeiboAdapter(), new Object[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.destroyedDirectMessage(destroyDirectMessage((Integer) args[0]));
            }
        });
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/create
     * @param id String
     * @param listener a listener object that receives the response
     * @deprecated Use createFriendshipAsync(String id, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public void createAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE, listener, new String[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.created(create( (String) args[0]));
            }
        });
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/create
     *
     * @param id       String
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public void createFriendshipAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FRIENDSHIP, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.createdFriendship(createFriendship((String) args[0]));
            }
        });
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/create
     *
     * @param id the ID or screen name of the user to be befriended
     * @param follow Enable notifications for the target user in addition to becoming friends.
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public void createFriendshipAsync(String id, boolean follow, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATE_FRIENDSHIP, listener, new Object[]{id,follow}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.createdFriendship(createFriendship((String) args[0],(Boolean)args[1]));
            }
        });
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/create
     * @param id String
     * @deprecated Use createFriendshipAsync(String id) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public void createAsync(String id) {
        getDispatcher().invokeLater(new AsyncTask(CREATE,  new WeiboAdapter(), new String[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.created(create( (String) args[0]));
            }
        });
    }

    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/create
     *
     * @param id String
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/create">friendships/create </a>
     */
    public void createFriendshipAsync(String id) {
        createFriendshipAsync(id, new WeiboAdapter());
    }

    /**
     * Discontinues friendship with the specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/destroy
     * @param id String
     * @param listener a listener object that receives the response
     * @deprecated Use destroyFriendshipAsync(String id, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/destroy">friendships/destroy </a>
     */
    public void destroyAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY, listener, new String[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.destroyed(destroy( (String) args[0]));
            }
        });

    }

    /**
     * Discontinues friendship with the specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/destroy
     * @param id String
     * @deprecated Use destroyFriendshipAsync(String id) instead
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/destroy">friendships/destroy </a>
     */
    public void destroyAsync(String id) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY, new WeiboAdapter(), new String[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.destroyed(destroy( (String) args[0]));
            }
        });

    }

    /**
     * Discontinues friendship with the specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/destroy
     * @param id String
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/destroy">friendships/destroy </a>
     */
    public void destroyFriendshipAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROY_FRIENDSHIP, listener, new String[] {id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.destroyedFriendship(destroyFriendship((String) args[0]));
            }
        });
    }

    /**
     * Discontinues friendship with the specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/friendships/destroy
     *
     * @param id String
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/destroy">friendships/destroy </a>
     */
    public void destroyFriendshipAsync(String id) {
        destroyFriendshipAsync(id, new WeiboAdapter());
    }

    /**
     * Tests if a friendship exists between two users.
     * <br>This method calls http://api.t.sina.com.cn/friendships/exists
     *
     * @param userA The ID or screen_name of the first user to test friendship for.
     * @param userB The ID or screen_name of the second user to test friendship for.
     * @deprecated existsFriendshipAsync(String user_a, String user_b, WeiboListener listener)
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/exists">friendships/exists </a>
     */
    public void existsAsync(String userA, String userB, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(EXISTS, listener, new String[]{userA, userB}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotExists(exists((String) args[0], (String) args[1]));
            }
        });
    }

    /**
     * Tests if a friendship exists between two users.
     * <br>This method calls http://api.t.sina.com.cn/friendships/exists
     *
     * @param userA The ID or screen_name of the first user to test friendship for.
     * @param userB The ID or screen_name of the second user to test friendship for.
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friendships/exists">friendships/exists </a>
     */
    public void existsFriendshipAsync(String userA, String userB, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(EXISTS_FRIENDSHIP, listener, new String[]{userA, userB}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotExistsFriendship(existsFriendship((String) args[0], (String) args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.0
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     */
    public void getFriendsIDsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs());
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param listener a listener object that receives the response
     * @param paging   Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.1
     * @deprecated use getFriendsIDsAsync(long cursor, WeiboListener listener) instead
     */
    public void getFriendsIDsAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener
                , new Object[]{paging}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((Paging) args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param listener a listener object that receives the response
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.10
     */
    public void getFriendsIDsAsync(long cursor, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener
                , new Object[]{cursor}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((Long) args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param userId   Specfies the ID of the user for whom to return the friends list.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.0
     */
    public void getFriendsIDsAsync(int userId, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener, new Integer[]{userId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((Integer) args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param userId   Specfies the ID of the user for whom to return the friends list.
     * @param paging   Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.1
     * @deprecated use getFriendsIDsAsync(int userId, long cursor, WeiboListener listener) instead
     */
    public void getFriendsIDsAsync(int userId, Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener, new Object[]{userId, paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((Integer) args[0],(Paging)args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param userId   Specifies the ID of the user for whom to return the friends list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.10
     */
    public void getFriendsIDsAsync(int userId, long cursor, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener, new Object[]{userId, cursor}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((Integer) args[0],(Long)args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param listener   a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.0
     */
    public void getFriendsIDsAsync(String screenName, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener, new String[]{screenName}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((String) args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param paging   Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener   a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.1
     * @deprecated use getFriendsIDsAsync(String screenName, long cursor, WeiboListener listener) instead
     */
    public void getFriendsIDsAsync(String screenName,Paging paging
            , WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener
                , new Object[]{screenName, paging}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((String) args[0]
                        , (Paging)args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is following.
     * <br>This method calls http://api.t.sina.com.cn/friends/ids%C2%A0%C2%A0
     *
     * @param screenName Specfies the screen name of the user for whom to return the friends list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener   a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Friends/ids">friends/ids</a>
     * @since Weibo4J 2.0.10
     */
    public void getFriendsIDsAsync(String screenName, long cursor
            , WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FRIENDS_IDS, listener
                , new Object[]{screenName, cursor}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.gotFriendsIDs(getFriendsIDs((String) args[0]
                        , (Long)args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.0
     */
    public void getFollowersIDsAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs());
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param listener a listener object that receives the response
     * @param paging   Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.1
     * @deprecated use getFollowersIDsAsync(long cursor, WeiboListener listener) instead
     */
    public void getFollowersIDsAsync(Paging paging, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener
                , new Object[]{paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((Paging)(args[0])));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.10
     */
    public void getFollowersIDsAsync(long cursor, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener
                , new Object[]{cursor}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((Long)(args[0])));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param userId   Specfies the ID of the user for whom to return the followers list.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.0
     */
    public void getFollowersIDsAsync(int userId, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener, new Integer[]{userId}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((Integer) args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param userId   Specfies the ID of the user for whom to return the followers list.
     * @param paging   Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.1
     * @deprecated use getFollowersIDsAsync(int userId, long cursor, WeiboListener listener) instead
     */
    public void getFollowersIDsAsync(int userId, Paging paging
            , WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener
                , new Object[]{userId, paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((Integer) args[0],(Paging)args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param userId   Specfies the ID of the user for whom to return the followers list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.10
     */
    public void getFollowersIDsAsync(int userId, long cursor
            , WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener
                , new Object[]{userId, cursor}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((Integer) args[0],(Long)args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param screenName Specfies the screen name of the user for whom to return the followers list.
     * @param listener   a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.0
     */
    public void getFollowersIDsAsync(String screenName, WeiboListener listener) throws WeiboException {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener, new String[]{screenName}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((String) args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param screenName Specfies the screen name of the user for whom to return the followers list.
     * @param paging     Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener   a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.1
     * @deprecated use getFollowersIDsAsync(String screenName, long cursor, WeiboListener listener) instead
     */
    public void getFollowersIDsAsync(String screenName, Paging paging
            , WeiboListener listener) throws WeiboException {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener
                , new Object[]{screenName, paging}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((String) args[0]
                        , (Paging) args[1]));
            }
        });
    }

    /**
     * Returns an array of numeric IDs for every user the specified user is followed by.
     * <br>This method calls http://api.t.sina.com.cn/followers/ids
     *
     * @param screenName Specfies the screen name of the user for whom to return the followers list.
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @param listener   a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids </a>
     * @since Weibo4J 2.0.10
     */
    public void getFollowersIDsAsync(String screenName, long cursor
            , WeiboListener listener) throws WeiboException {
        getDispatcher().invokeLater(new AsyncTask(FOLLOWERS_IDS, listener
                , new Object[]{screenName, cursor}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotFollowersIDs(getFollowersIDs((String) args[0]
                        , (Long) args[1]));
            }
        });
    }

    /**
     * Updates the location
     *
     * @param location the current location of the user
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.0.4
     * @deprecated Use updateProfileAsync(String name, String email, String url, String location, String description, WeiboListener listener) instead
     */
    public void updateLocationAsync(String location, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_LOCATION, listener, new Object[]{location}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updatedLocation(updateLocation((String) args[0]));
            }
        });
    }

    /**
     * Sets values that users are able to set under the "Account" tab of their settings page. Only the parameters specified(non-null) will be updated.
     *
     * @param name        Optional. Maximum of 20 characters.
     * @param email       Optional. Maximum of 40 characters. Must be a valid email address.
     * @param url         Optional. Maximum of 100 characters. Will be prepended with "http://" if not present.
     * @param location    Optional. Maximum of 30 characters. The contents are not normalized or geocoded in any way.
     * @param description Optional. Maximum of 160 characters.
     * @param listener    a listener object that receives the response
     * @since Weibo4J 2.0.2
     */
    public void updateProfileAsync(String name, String email, String url
            , String location, String description, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_PROFILE, listener,
                new String[]{name, email, url, location, description}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updatedProfile(updateProfile((String) args[0]
                        , (String) args[1], (String) args[2], (String) args[3]
                        , (String) args[4]));
            }
        });
    }

    /**
     * Sets values that users are able to set under the "Account" tab of their settings page. Only the parameters specified(non-null) will be updated.
     *
     * @param name        Optional. Maximum of 20 characters.
     * @param email       Optional. Maximum of 40 characters. Must be a valid email address.
     * @param url         Optional. Maximum of 100 characters. Will be prepended with "http://" if not present.
     * @param location    Optional. Maximum of 30 characters. The contents are not normalized or geocoded in any way.
     * @param description Optional. Maximum of 160 characters.
     * @since Weibo4J 2.0.2
     */
    public void updateProfileAsync(String name, String email, String url
            , String location, String description) {
        updateProfileAsync(name, email, url, location, description
                , new WeiboAdapter());
    }

    /**
     * Gets the remaining number of API requests available to the requesting user before the API limit is reached for the current hour. Calls to rate_limit_status do not count against the rate limit.  If authentication credentials are provided, the rate limit status for the authenticating user is returned.  Otherwise, the rate limit status for the requester's IP address is returned.
     * <br>This method calls http://api.t.sina.com.cn/account/rate_limit_status
     *
     * @since Weibo4J 1.1.4
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Account/rate_limit_status">account/rate_limit_status </a>
     */
    public void rateLimitStatusAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(RATE_LIMIT_STATUS, listener, new Object[]{}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotRateLimitStatus(rateLimitStatus());
            }
        });
    }

    /**
     * Sets which device Weibo delivers updates to for the authenticating user.  Sending none as the device parameter will disable IM or SMS updates.
     * <br>This method calls http://api.t.sina.com.cn/account/update_delivery_device
     *
     * @param device   new Delivery device. Must be one of: IM, SMS, NONE.
     * @param listener a listener object that receives the response
     * @since Weibo4J 1.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-account%C2%A0update_delivery_device">Weibo API Wiki / Weibo REST API Method: account%C2%A0update_delivery_device</a>
     */
    public void updateDeliverlyDeviceAsync(Device device, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_LOCATION, listener, new Object[]{device}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.updatedDeliverlyDevice(updateDeliverlyDevice((Device) args[0]));
            }
        });
    }

    /**
     * Sets one or more hex values that control the color scheme of the authenticating user's profile page on open.t.sina.com.cn.  These values are also returned in the getUserDetail() method.
     * <br>This method calls http://api.t.sina.com.cn/account/update_profile_colors
     * @param profileBackgroundColor optional, can be null
     * @param profileTextColor optional, can be null
     * @param profileLinkColor optional, can be null
     * @param profileSidebarFillColor optional, can be null
     * @param profileSidebarBorderColor optional, can be null
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.0
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-account%C2%A0update_profile_colors">Weibo API Wiki / Weibo REST API Method: account%C2%A0update_profile_colors</a>
     */
    public void updateProfileColorsAsync(
            String profileBackgroundColor, String profileTextColor,
            String profileLinkColor, String profileSidebarFillColor,
            String profileSidebarBorderColor, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(UPDATE_PROFILE_COLORS,
                listener, new Object[]{profileBackgroundColor, profileTextColor,
                        profileLinkColor, profileSidebarFillColor,
                        profileSidebarBorderColor}) {
            public void invoke(WeiboListener listener, Object[] args)
                    throws WeiboException {
                listener.updatedProfileColors(updateProfileColors(
                        (String) args[0], (String) args[1], (String) args[2],
                        (String) args[3], (String) args[4]));
            }
        });
    }

    /**
     * Sets one or more hex values that control the color scheme of the authenticating user's profile page on open.t.sina.com.cn.  These values are also returned in the getUserDetail() method.
     * <br>This method calls http://api.t.sina.com.cn/account/update_profile_colors
     * @param profileBackgroundColor optional, can be null
     * @param profileTextColor optional, can be null
     * @param profileLinkColor optional, can be null
     * @param profileSidebarFillColor optional, can be null
     * @param profileSidebarBorderColor optional, can be null
     * @since Weibo4J 2.0.0
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-account%C2%A0update_profile_colors">Weibo API Wiki / Weibo REST API Method: account%C2%A0update_profile_colors</a>
     */
    public void updateProfileColorsAsync(
            String profileBackgroundColor, String profileTextColor,
            String profileLinkColor, String profileSidebarFillColor,
            String profileSidebarBorderColor) {
        updateProfileColorsAsync(profileBackgroundColor, profileTextColor,
                profileLinkColor, profileSidebarFillColor,
                profileSidebarBorderColor, new WeiboAdapter());
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavoritesAsync(WeiboListener listener) instead
     */
    public void favoritesAsync(WeiboListener listener) {
        getFavoritesAsync(listener);
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 2.0.1
     */
    public void getFavoritesAsync(WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, null) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFavorites(getFavorites());
            }
        });
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param page number of page to retrieve favorites
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavoritesAsync(int page, WeiboListener listener) instead
     */
    public void favoritesAsync(int page, WeiboListener listener) {
        getFavoritesAsync(page, listener);
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param page number of page to retrieve favorites
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 2.0.1
     */
    public void getFavoritesAsync(int page, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[] {page}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFavorites(getFavorites((Integer)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavoritesAsync(String id,WeiboListener listener) instead
     */
    public void favoritesAsync(String id,WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[]{id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFavorites(favorites((String)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 2.0.1
     */
    public void getFavoritesAsync(String id,WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[]{id}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFavorites(getFavorites((String)args[0]));
            }
        });
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses.
     * @param page retrieves the 20 next most recent favorite statuses.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @deprecated Use getFavoritesAsync(String id,int page, WeiboListener listener) instead
     */
    public void favoritesAsync(String id,int page, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[] {id,page}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFavorites(favorites((String)args[0],(Integer)args[1]));
            }
        });
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * <br>This method calls http://api.t.sina.com.cn/favorites
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses.
     * @param page retrieves the 20 next most recent favorite statuses.
     * @param listener a listener object that receives the response
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites">favorites </a>
     * @since Weibo4J 2.0.1
     */
    public void getFavoritesAsync(String id,int page, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[] {id,page}) {
            public void invoke(WeiboListener listener,Object[] args) throws WeiboException {
                listener.gotFavorites(getFavorites((String)args[0],(Integer)args[1]));
            }
        });
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/create%C2%A0
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses.
     * @param listener a listener object that receives the response
     * @deprecated Use createFavoriteAsync(long id, WeiboListener listener) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/create">favorites/create </a>
     */
    public void createFavoriteAsync(int id, WeiboListener listener) {
        createFavoriteAsync((long) id, listener);
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/create%C2%A0
     *
     * @param id       the ID or screen name of the user for whom to request a list of favorite statuses.
     * @param listener a listener object that receives the response
     * @since 1.1.2
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/create">favorites/create </a>
     */
    public void createFavoriteAsync(long id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.createdFavorite(createFavorite((Long) args[0]));
            }
        });
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/create%C2%A0
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses.
     * @deprecated Use createFavoriteAsync(long id, WeiboListener listener) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/create">favorites/create </a>
     */
    public void createFavoriteAsync(int id) {
        createFavoriteAsync((long) id);
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/create%C2%A0
     *
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses.
     * @since 1.1.2
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/create">favorites/create </a>
     */
    public void createFavoriteAsync(long id) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, new WeiboAdapter(), new Object[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.createdFavorite(createFavorite((Long) args[0]));
            }
        });
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/destroy
     * @param id the ID or screen name of the user for whom to request a list of un-favorite statuses.
     * @param listener a listener object that receives the response
     * @deprecated Use destroyFavoriteAsync(long id, WeiboListener listener) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/destroy">favorites/destroy </a>
     */
    public void destroyFavoriteAsync(int id, WeiboListener listener) {
        destroyFavoriteAsync((long) id, listener);
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/destroy
     *
     * @param id       the ID or screen name of the user for whom to request a list of un-favorite statuses.
     * @param listener a listener object that receives the response
     * @since 1.1.2
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/destroy">favorites/destroy </a>
     */
    public void destroyFavoriteAsync(long id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, listener, new Object[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.destroyedFavorite(destroyFavorite((Long) args[0]));
            }
        });
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/destroy
     * @param id the ID or screen name of the user for whom to request a list of un-favorite statuses.
     * @deprecated Use destroyFavoriteAsync(long id) instead.
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/destroy">favorites/destroy </a>
     */
    public void destroyFavoriteAsync(int id) {
        destroyFavoriteAsync((long) id);
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     * <br>This method calls http://api.t.sina.com.cn/favorites/destroy
     *
     * @param id the ID or screen name of the user for whom to request a list of un-favorite statuses.
     * @since 1.1.2
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Favorites/destroy">favorites/destroy </a>
     */
    public void destroyFavoriteAsync(long id) {
        getDispatcher().invokeLater(new AsyncTask(FAVORITES, new WeiboAdapter(), new Object[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.destroyedFavorite(destroyFavorite((Long) args[0]));
            }
        });
    }

    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/follow
     *
     * @param id       String
     * @param listener a listener object that receives the response
     * @deprecated Use enableNotificationAsync(String id, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0follow">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0follow</a>
     */
    public void followAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOW, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.followed(follow((String) args[0]));
            }
        });
    }

    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/follow
     *
     * @param id       String
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0follow">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0follow</a>
     */
    public void enableNotificationAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(ENABLE_NOTIFICATION, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.enabledNotification((enableNotification((String) args[0])));
            }
        });
    }

    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/follow
     *
     * @param id String
     * @deprecated Use enableNotificationAsync(String id) instead
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0follow">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0follow</a>
     */
    public void followAsync(String id) {
        getDispatcher().invokeLater(new AsyncTask(FOLLOW, new WeiboAdapter(), new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.followed(follow((String) args[0]));
            }
        });
    }

    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/leave
     * @param id       String
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0follow">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0follow</a>
     */
    public void enableNotificationAsync(String id) {
        enableNotificationAsync(id, new WeiboAdapter());
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/leave
     *
     * @param id       String
     * @param listener a listener object that receives the response
     * @deprecated Use disableNotificationAsync(String id, WeiboListener listener) instead
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0leave">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0leave</a>
     */
    public void leaveAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(LEAVE, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.left(leave((String) args[0]));
            }
        });
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/leave
     *
     * @param id       String
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0leave">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0leave</a>
     */
    public void disableNotificationAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DISABLE_NOTIFICATION, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.disabledNotification(disableNotification((String) args[0]));
            }
        });
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/leave
     *
     * @param id String
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0leave">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0leave</a>
     * @deprecated Use disableNotificationAsync(String id) instead
     */
    public void leaveAsync(String id) {
        getDispatcher().invokeLater(new AsyncTask(LEAVE, new WeiboAdapter(), new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.left(leave((String) args[0]));
            }
        });
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * <br>This method calls http://api.t.sina.com.cn/notifications/leave
     *
     * @param id       String
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-notifications%C2%A0leave">Weibo API Wiki / Weibo REST API Method: notifications%C2%A0leave</a>
     */
    public void disableNotificationAsync(String id) {
        disableNotificationAsync(id, new WeiboAdapter());
    }


    /* Block Methods */

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.  Returns the blocked user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/blocks/create%C2%A0
     *
     * @param id the ID or screen_name of the user to block
     * @since Weibo4J 1.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-blocks%C2%A0create">Weibo API Wiki / Weibo REST API Method: blocks%C2%A0create</a>
     * @deprecated Use createBlockAsync(String id) instead
     */
    public void blockAsync(String id) {
        getDispatcher().invokeLater(new AsyncTask(BLOCK, new WeiboAdapter(), new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.blocked(block((String) args[0]));
            }
        });
    }

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.  Returns the blocked user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/blocks/create%C2%A0
     *
     * @param id the ID or screen_name of the user to block
     * @param listener a listener object that receives the response
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-blocks%C2%A0create">Weibo API Wiki / Weibo REST API Method: blocks%C2%A0create</a>
     */
    public void createBlockAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(CREATED_BLOCK, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.createdBlock(createBlock((String) args[0]));
            }
        });
    }

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.  Returns the blocked user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/blocks/create%C2%A0
     *
     * @param id the ID or screen_name of the user to block
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-blocks%C2%A0create">Weibo API Wiki / Weibo REST API Method: blocks%C2%A0create</a>
     */
    public void createBlockAsync(String id) {
        createBlockAsync(id, new WeiboAdapter());
    }


    /**
     * Un-blocks the user specified in the ID parameter as the authenticating user.  Returns the un-blocked user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/blocks/create%C2%A0
     *
     * @param id the ID or screen_name of the user to block
     * @since Weibo4J 1.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-blocks%C2%A0destroy">Weibo API Wiki / Weibo REST API Method: blocks%C2%A0destroy</a>
     * @deprecated Use destroyBlockAsync(String id, WeiboListener listener) instead
     */
    public void unblockAsync(String id) {
        getDispatcher().invokeLater(new AsyncTask(UNBLOCK, new WeiboAdapter(), new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.unblocked(unblock((String) args[0]));
            }
        });
    }

    /**
     * Un-blocks the user specified in the ID parameter as the authenticating user.  Returns the un-blocked user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/blocks/create%C2%A0
     *
     * @param id the ID or screen_name of the user to block
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-blocks%C2%A0destroy">Weibo API Wiki / Weibo REST API Method: blocks%C2%A0destroy</a>
     */
    public void destroyBlockAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(DESTROYED_BLOCK, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.destroyedBlock(destroyBlock((String) args[0]));
            }
        });
    }

    /**
     * Un-blocks the user specified in the ID parameter as the authenticating user.  Returns the un-blocked user in the requested format when successful.
     * <br>This method calls http://api.t.sina.com.cn/blocks/create%C2%A0
     *
     * @param id the ID or screen_name of the user to block
     * @since Weibo4J 2.0.1
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-blocks%C2%A0destroy">Weibo API Wiki / Weibo REST API Method: blocks%C2%A0destroy</a>
     */
    public void destroyBlockAsync(String id) {
        destroyBlockAsync(id, new WeiboAdapter());
    }


    /**
     * Tests if a friendship exists between two users.
     * <br>This method calls http://api.t.sina.com.cn/blocks/exists/id.xml
     *
     * @param id The ID or screen_name of the potentially blocked user.
     * @since Weibo4J 2.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method%3A-blocks-exists">Weibo API Wiki / Weibo REST API Method: blocks exists</a>
     */
    public void existsBlockAsync(String id, WeiboListener listener) {
        getDispatcher().invokeLater(new AsyncTask(EXISTS_BLOCK, listener, new String[]{id}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotExistsBlock(existsBlock((String) args[0]));
            }
        });
    }

    /**
     * Returns a list of user objects that the authenticating user is blocking.
     * <br>This method calls http://api.t.sina.com.cn/blocks/blocking.xml
     *
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method%3A-blocks-blocking">Weibo API Wiki / Weibo REST API Method: blocks blocking</a>
     */
    public void getBlockingUsersAsync(WeiboListener listener) throws
            WeiboException {
        getDispatcher().invokeLater(new AsyncTask(GET_BLOCKING_USERS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotBlockingUsers(getBlockingUsers());
            }
        });
    }

    /**
     * Returns a list of user objects that the authenticating user is blocking.
     * <br>This method calls http://api.t.sina.com.cn/blocks/blocking.xml
     *
     * @param page the number of page
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method%3A-blocks-blocking">Weibo API Wiki / Weibo REST API Method: blocks blocking</a>
     */
    public void getBlockingUsersAsync(int page, WeiboListener listener) throws
            WeiboException {
        getDispatcher().invokeLater(new AsyncTask(GET_BLOCKING_USERS, listener, new Integer[]{page}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotBlockingUsers(getBlockingUsers((Integer)args[0]));
            }
        });
    }

    /**
     * Returns an array of numeric user ids the authenticating user is blocking.
     * <br>This method calls http://api.t.sina.com.cn/blocks/blocking/ids
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method%3A-blocks-blocking-ids">Weibo API Wiki / Weibo REST API Method: blocks blocking ids</a>
     */
    public void getBlockingUsersIDsAsync(WeiboListener listener) throws WeiboException {
        getDispatcher().invokeLater(new AsyncTask(GET_BLOCKING_USERS_IDS, listener, null) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.gotBlockingUsersIDs(getBlockingUsersIDs());
            }
        });
    }

    /* Help Methods */

    /**
     * Returns the string "ok" in the requested format with a 200 OK HTTP status code.
     * <br>This method calls http://api.t.sina.com.cn/help/test
     *
     * @since Weibo4J 1.0.4
     * @see <a href="http://open.t.sina.com.cn/Weibo-REST-API-Method:-help%C2%A0test">Weibo API Wiki / Weibo REST API Method: help%C2%A0test</a>
     */
    public void testAsync() {
        getDispatcher().invokeLater(new AsyncTask(TEST, new WeiboAdapter(), new Object[]{}) {
            public void invoke(WeiboListener listener, Object[] args) throws WeiboException {
                listener.tested(test());
            }
        });
    }

    private static transient Dispatcher dispatcher;
    private boolean shutdown = false;

    /**
     * Shuts down internal dispather thread.
     *
     * @since Weibo4J 2.0.2
     */
    public void shutdown(){
        synchronized (AsyncWeibo.class) {
            if (shutdown = true) {
                throw new IllegalStateException("Already shut down");
            }
            getDispatcher().shutdown();
            dispatcher = null;
            shutdown = true;
        }
    }
    private Dispatcher getDispatcher(){
        if(true == shutdown){
            throw new IllegalStateException("Already shut down");
        }
        if (null == dispatcher) {
            dispatcher = new Dispatcher("Weibo4J Async Dispatcher", Configuration.getNumberOfAsyncThreads());
        }
        return dispatcher;
    }

    /**
     * Returns the same text displayed on http://api.t.sina.com.cn/home when a maintenance window is scheduled, in the requested format.
     *
     * @since Weibo4J 1.0.4
     */
    public void getDowntimeScheduleAsync() {
        throw new RuntimeException(
                "this method is not supported by the Weibo API anymore"
                , new NoSuchMethodException("this method is not supported by the Weibo API anymore"));
    }

    /**
     * Retrieves extended information of the authenticated user.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.<br>
     * The call Weibo.getAuthenticatedUser() is equivalent to the call:<br>
     * weibo.getUserDetailAsync(weibo.getUserId(), listener);
     *
     * @since Weibo4J 1.1.3
     */
    public void getAuthenticatedUserAsync(WeiboListener listener) {
        if (null == getUserId()) {
            throw new IllegalStateException("User Id not specified.");
        }
        getUserDetailAsync(getUserId(), listener);
    }

    abstract class AsyncTask implements Runnable {
        WeiboListener listener;
        Object[] args;
        int method;
        AsyncTask(int method, WeiboListener listener, Object[] args) {
            this.method = method;
            this.listener = listener;
            this.args = args;
        }

        abstract void invoke(WeiboListener listener,Object[] args) throws WeiboException;

        public void run() {
            try {
                   invoke(listener,args);
            } catch (WeiboException te) {
                if (null != listener) {
                    listener.onException(te,method);
                }
            }
        }
    }
    public final static int PUBLIC_TIMELINE = 0;
    public final static int HOME_TIMELINE = 51;
    public final static int FRIENDS_TIMELINE = 1;
    public final static int USER_TIMELINE = 2;
    /**
     * @deprecated Use SHOW_STATUS instead.
     */
    public final static int SHOW = 3;
    public final static int SHOW_STATUS = 38;
    /**
     * @deprecated Use UPDATE_STATUS instead.
     */
    public final static int UPDATE = 4;
    public final static int UPDATE_STATUS = 39;
    /**
     * @deprecated Use DESTROY_FRIENDSHIP instead.
     */
    public final static int REPLIES = 5;
    public final static int MENTIONS = 37;
    public final static int RETWEETED_BY_ME = 53;
    public final static int RETWEETED_TO_ME = 54;
    public final static int RETWEETS_OF_ME = 55;
    public final static int FRIENDS = 6;
    public final static int FOLLOWERS = 7;
    public final static int FEATURED = 8;
    public final static int USER_DETAIL = 9;
    public final static int DIRECT_MESSAGES = 10;
    public final static int DESTROY_DIRECT_MESSAGES = 40;
    public final static int SEND_DIRECT_MESSAGE = 11;
    public final static int CREATE = 12;
    public final static int CREATE_FRIENDSHIP = 32;
    /**
     * @deprecated Use DESTROY_FRIENDSHIP instead.
     */
    public final static int DESTORY = 13;
    /**
     * @deprecated Use DESTROY_FRIENDSHIP instead.
     */
    public final static int DESTROY = 13;
    public final static int DESTROY_FRIENDSHIP = 33;
    /**
     * @deprecated Use EXISTS_FRIENDSHIP instead.
     */
    public final static int EXISTS = 28;
    public final static int EXISTS_FRIENDSHIP = 34;
    /**
     * @deprecated Use ENABLE_NOTIFICATION instead.
     */
    public final static int FOLLOW = 14;
    public final static int ENABLE_NOTIFICATION = 35;
    /**
     * @deprecated Use DISABLE_NOTIFICATION instead.
     */
    public final static int LEAVE = 15;
    public final static int DISABLE_NOTIFICATION = 36;
    public final static int FAVORITES = 17;
    public final static int FRIENDS_IDS = 29;
    public final static int FOLLOWERS_IDS = 30;
    public final static int CREATE_FAVORITE = 18;
    public final static int DESTROY_FAVORITE = 19;
    /**
     * @deprecated Use UPDATE_PROFILE instead.
     */
    public final static int UPDATE_LOCATION = 20;
    public final static int UPDATE_PROFILE = 41;
    public final static int UPDATE_PROFILE_COLORS = 31;
    public final static int RATE_LIMIT_STATUS = 28;
    public final static int UPDATE_DELIVERLY_DEVICE = 21;
    /**
     * @deprecated Use CREATED_BLOCK instead.
     */
    public final static int BLOCK = 22;
    public final static int CREATED_BLOCK = 43;
    /**
     * @deprecated Use DESTROYED_BLOCK instead.
     */
    public final static int UNBLOCK = 23;
    public final static int DESTROYED_BLOCK = 42;
    private static final int EXISTS_BLOCK = 48;
    private static final int GET_BLOCKING_USERS = 49;
    private static final int GET_BLOCKING_USERS_IDS = 50;

    public final static int TEST = 24;
    /**
     * @deprecated not supported by Weibo API anymore
     */
    public final static int GET_DOWNTIME_SCHEDULE = 25;
    public final static int DESTROY_STATUS = 26;
    public final static int RETWEET_STATUS = 52;
    public final static int SEARCH = 27;

    public final static int TRENDS = 44;
    public final static int CURRENT_TRENDS = 45;
    public final static int DAILY_TRENDS = 46;
    public final static int WEEKLY_TRENDS = 47;
}
