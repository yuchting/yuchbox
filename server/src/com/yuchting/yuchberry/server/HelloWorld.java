/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.server;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import weibo4j.http.OAuth;
import weibo4j.http.OAuthToken;
import weibo4j.http.PostParameter;

import com.mime.qweibo.OauthKey;
import com.mime.qweibo.QOAuth;
import com.mime.qweibo.QParameter;

/**
 * 实现一个简单的Web浏览器，支持HTML和HTM页面的显示。使用了JEditorPane组件
 **/
class WebBrowser extends JFrame implements HyperlinkListener,
  PropertyChangeListener {
    
    /**下面是使用的Swing组件**/
 
 // 显示HTML的面板
    JEditorPane textPane; 
    // 最底下的状态栏
    JLabel messageLine; 
    // 网址URL输入栏
    JTextField urlField;
    // 文件选择器，打开本地文件时用
    JFileChooser fileChooser;
    
    // 后退和前进 按钮
    JButton backButton;
    JButton forwardButton;

    // 保存历史记录的列表
    java.util.List history = new ArrayList(); 
    // 当前页面的在历史记录列表中位置
    int currentHistoryPage = -1;  
    // 当历史记录超过MAX_HISTORY时，清除旧的历史
    public static final int MAX_HISTORY = 50;

    // 当前已经打开的浏览器窗口数
    static int numBrowserWindows = 0;
    // 标识当所有浏览器窗口都被关闭时，是否退出应用程序
    static boolean exitWhenLastWindowClosed = false;

    // 默认的主页
    String home = "http://www.hao123.com";

    /**
     * 构造函数
     */
    public WebBrowser() {
        super("WebBrowser");

        // 新建显示HTML的面板，并设置它不可编辑
        textPane = new JEditorPane(); 
        textPane.setEditable(false);

        // 注册事件处理器，用于超连接事件。
        textPane.addHyperlinkListener(this);
        // 注册事件处理器，用于处理属性改变事件。当页面加载结束时，触发该事件
        textPane.addPropertyChangeListener(this);

        // 将HTML显示面板放入主窗口，居中显示
        this.getContentPane().add(new JScrollPane(textPane),
                                  BorderLayout.CENTER);

        // 创建状态栏标签，并放在主窗口底部
        messageLine = new JLabel(" ");
        this.getContentPane().add(messageLine, BorderLayout.SOUTH);

        // 初始化菜单和工具栏
        this.initMenu();
        this.initToolbar();
        
        // 将当前打开窗口数增加1
        WebBrowser.numBrowserWindows++;
        
        // 当关闭窗口时，调用close方法处理
        this.addWindowListener(new WindowAdapter() {
   public void windowClosing(WindowEvent e) {
    close();
   }
  });
    }
    
    /**
     * 初始化菜单栏
     */
    private void initMenu(){
     
     // 文件菜单，下面有四个菜单项：新建、打开、关闭窗口、退出
     JMenu fileMenu = new JMenu("文件");
     fileMenu.setMnemonic('F');
     JMenuItem newMenuItem = new JMenuItem("新建");
     newMenuItem.setMnemonic('N');
     // 当“新建”时打开一个浏览器窗口
     newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             newBrowser();
            }
        });
     
     JMenuItem openMenuItem = new JMenuItem("打开");
     openMenuItem.setMnemonic('O');
     // 当“打开”是打开文件选择器，选择待打开的文件
     openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             openLocalPage();
            }
        });
     
     JMenuItem closeMenuItem = new JMenuItem("关闭窗口");
     closeMenuItem.setMnemonic('C');
     // 当“关闭窗口”时关闭当前窗口
     closeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             close();
            }
        });
     
     JMenuItem exitMenuItem = new JMenuItem("退出");
     exitMenuItem.setMnemonic('E');
     // 当“退出”时退出应用程序
     exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             exit();
            }
        });
     
     fileMenu.add(newMenuItem);
     fileMenu.add(openMenuItem);
     fileMenu.add(closeMenuItem);
     fileMenu.add(exitMenuItem);
     
     //帮助菜单，就一个菜单项：关于
     JMenu helpMenu = new JMenu("帮助");
     fileMenu.setMnemonic('H');
     JMenuItem aboutMenuItem = new JMenuItem("关于");
     aboutMenuItem.setMnemonic('A');
     helpMenu.add(aboutMenuItem);
     
     JMenuBar menuBar = new JMenuBar();
     menuBar.add(fileMenu);
     menuBar.add(helpMenu);
     
     // 设置菜单栏到主窗口
     this.setJMenuBar(menuBar);
    }
    
    /**
     * 初始化工具栏
     */
    private void initToolbar(){
     // 后退按钮，退到前一页面。初始情况下该按钮不可用
        backButton = new JButton("后退");
        backButton.setEnabled(false);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             back();
            }
        });
        
        // 前进按钮，进到前一页面。初始情况下该按钮不可用
        forwardButton = new JButton("前进");
        forwardButton.setEnabled(false);
        forwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             forward();
            }
        });
        
        // 前进按钮，进到前一页面。初始情况下该按钮不可用
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             reload();
            }
        });
        
        // 主页按钮，打开主页
        JButton homeButton = new JButton("主页");
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             home();
            }
        });
        
        JToolBar toolbar = new JToolBar();
        toolbar.add(backButton);
        toolbar.add(forwardButton);
        toolbar.add(refreshButton);
        toolbar.add(homeButton);

        // 输入网址的文本框
        urlField = new JTextField();
        // 当用户输入网址、按下回车键时，触发该事件
        urlField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    displayPage(urlField.getText());
                }
            });

        // 地址标签
        toolbar.add(new JLabel("         地址："));
        toolbar.add(urlField);

        // 将工具栏放在主窗口的北部
        this.getContentPane().add(toolbar, BorderLayout.NORTH);
    }

    /**
     * 设置浏览器是否在所有窗口都关闭时退出
     * @param b
     */
    public static void setExitWhenLastWindowClosed(boolean b) {
        exitWhenLastWindowClosed = b;
    }

    /**
  * 设置主页
  * @param home 新主页
  */
 public void setHome(String home) {
  this.home = home;
 }
 /**
  * 获取主页
  */
    public String getHome() {
  return home;
 }

    /**
     * 访问网址URL
     */
    private boolean visit(URL url) {
        try {
            String href = url.toString();
            // 启动动画，当网页被加载完成时，触发propertyChanged事件，动画停止。
            startAnimation("加载 " + href + "...");
            
            // 设置显示HTML面板的page属性为待访问的URL，
            // 在这个方法里，将打开URL，将URL的流显示在textPane中。
            // 当完全打开后，该方法才结束。
            textPane.setPage(url); 
            
            // 页面打开后，将浏览器窗口的标题设为URL
            this.setTitle(href);  
            // 网址输入框的内容也设置为URL
            urlField.setText(href); 
            return true;
        } catch (IOException ex) { 
         // 停止动画
            stopAnimation();
            // 状态栏中显示错误信息
            messageLine.setText("不能打开页面：" + ex.getMessage());
            return false;
        }
    }

    /**
     * 浏览器打开URL指定的页面，如果成功，将URL放入历史列表中
     */
    public void displayPage(URL url) {
     // 尝试访问页面
        if (visit(url)) { 
         // 如果成功，则将URL放入历史列表中。
            history.add(url); 
            int numentries = history.size();
            if (numentries > MAX_HISTORY+10) { 
                history = history.subList(numentries-MAX_HISTORY, numentries);
                numentries = MAX_HISTORY;
            }
            // 将当前页面下标置为numentries-1
            currentHistoryPage = numentries - 1;
            // 如果当前页面不是第一个页面，则可以后退，允许点击后退按钮。
            if (currentHistoryPage > 0){
             backButton.setEnabled(true);
            }
        }
    }

    /**
     * 浏览器打开字符串指定的页面
     * @param href 网址
     */
    public void displayPage(String href) {
        try {
         // 默认为HTTP协议
         if (!href.startsWith("http://")){
          href = "http://" + href;
         }
            displayPage(new URL(href));
        }
        catch (MalformedURLException ex) {
            messageLine.setText("错误的网址: " + href);
        }
    }

    /**
     * 打开本地文件
     */
    public void openLocalPage() {
        // 使用“懒创建”模式，当需要时，才创建文件选择器。
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            // 使用文件过滤器限制只能够HTML和HTM文件
            FileFilter filter = new FileFilter() {
                    public boolean accept(File f) {
                        String fn = f.getName();
                        if (fn.endsWith(".html") || fn.endsWith(".htm")){
                            return true;
                        }else {
                         return false;
                        }
                    }
                    public String getDescription() { 
                     return "HTML Files"; 
                    }
                };
            fileChooser.setFileFilter(filter);
            // 只允许选择HTML和HTM文件
            fileChooser.addChoosableFileFilter(filter);
        }

        // 打开文件选择器
        int result = fileChooser.showOpenDialog(this);
        // 如果确定打开文件，则在当前窗口中打开选择的文件
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile( );
            try {
    displayPage(selectedFile.toURL());
   } catch (MalformedURLException e) {
    e.printStackTrace();
   }
        }
    }
    /**
     * 后退，回到前一页
     */
    public void back() {
        if (currentHistoryPage > 0){
         // 访问前一页
            visit((URL)history.get(--currentHistoryPage));
        }
        // 如果当前页面下标大于0，允许后退
        backButton.setEnabled((currentHistoryPage > 0));
        // 如果当前页面下标不是最后，允许前进
        forwardButton.setEnabled((currentHistoryPage < history.size()-1));
    }
    /**
     * 前进，回到后一页
     */
    public void forward() {
        if (currentHistoryPage < history.size( )-1){
            visit((URL)history.get(++currentHistoryPage));
        }
        // 如果当前页面下标大于0，允许后退
        backButton.setEnabled((currentHistoryPage > 0));
        // 如果当前页面下标不是最后，允许前进
        forwardButton.setEnabled((currentHistoryPage < history.size()-1));
    }
    /**
     * 重新加载当前页面
     */
    public void reload() {
        if (currentHistoryPage != -1) {
            // 先显示为空白页
            textPane.setDocument(new javax.swing.text.html.HTMLDocument());
            // 再访问当前页
            visit((URL)history.get(currentHistoryPage));
        }
    }
    /**
     * 显示主页 
     */
    public void home() {
     displayPage(getHome()); 
    }
    /**
     * 打开新的浏览器窗口 
     */
    public void newBrowser() {
        WebBrowser b = new WebBrowser();
        // 新窗口大小和当前窗口一样大
        b.setSize(this.getWidth(), this.getHeight());
        b.setVisible(true);
    }
    /**
     * 关闭当前窗口，如果所有窗口都关闭，则根据exitWhenLastWindowClosed属性，
     * 判断是否退出应用程序
     */
    public void close() {
     // 先隐藏当前窗口，销毁窗口中的组件。
        this.setVisible(false);
        this.dispose();
        // 将当前打开窗口数减1。
        // 如果所有窗口都已关闭，而且exitWhenLastWindowClosed为真，则退出
        // 这里采用了synchronized关键字，保证线程安全
        synchronized(WebBrowser.class) {    
            WebBrowser.numBrowserWindows--; 
            if ((numBrowserWindows==0) && exitWhenLastWindowClosed){
                System.exit(0);
            }
        }
    }
    /**
     * 退出应用程序
     */
    public void exit() {
     // 弹出对话框，请求确认，如果确认退出，则退出应用程序
  if ((JOptionPane.showConfirmDialog(this, "你确定退出Web浏览器？", "退出",
    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)){
   System.exit(0);
  }
 }
    /**
     * 实现HyperlinkListener接口。处理超连接事件
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
     // 获取事件类型
        HyperlinkEvent.EventType type = e.getEventType();
        // 如果是点击了一个超连接，则显示被点击的连接
        if (type == HyperlinkEvent.EventType.ACTIVATED) {
            displayPage(e.getURL());
        }
        // 如果是鼠标移动到超连接上，则在状态栏中显示超连接的网址
        else if (type == HyperlinkEvent.EventType.ENTERED) {
            messageLine.setText(e.getURL().toString());  
        }
        // 如果是鼠标离开了超连接，则状态栏显示为空
        else if (type == HyperlinkEvent.EventType.EXITED) { 
            messageLine.setText(" ");
        }
    }

    /**
     * 实现PropertyChangeListener接口。处理属性改变事件。
     * 显示HTML面板textPane的属性改变时，由该方法处理。
     * 当textPane调用完setPage方法时，page属性便改变了。
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("page")) {
         // 页面加载完毕时，textPane的page属性发生改变，此时停止动画。
            stopAnimation();
        }
    }

    // 动画消息，显示在最底下状态栏标签上，用于反馈浏览器的状态
    String animationMessage;
    // 动画当前的帧的索引
    int animationFrame = 0;
    // 动画所用到的帧，是一些字符。
    String[] animationFrames = new String[] {
        "-", "//", "|", "/", "-", "//", "|", "/", 
        ",", ".", "o", "0", "O", "#", "*", "+"
    };

    /**
     * 新建一个Swing的定时器，每个125毫秒更新一次状态栏标签的文本
     */
    javax.swing.Timer animator =
        new javax.swing.Timer(125, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                 animate(); 
                }
            });

    /**
     * 显示动画的当前帧到状态栏标签上，并将帧索引后移
     */
    private void animate() {
        String frame = animationFrames[animationFrame++];
        messageLine.setText(animationMessage + " " + frame);
        animationFrame = animationFrame % animationFrames.length;
    }

    /**
     * 启动动画
     */
    private void startAnimation(String msg) {
        animationMessage = msg;
        animationFrame = 0; 
        // 启动定时器
        animator.start();
    }

    /**
     * 停止动画
     */
    private void stopAnimation() {  
     // 停止定时器
        animator.stop();
        messageLine.setText(" ");
    }
    
    public static void main(String[] args) throws IOException {
        // 设置浏览器，当所有浏览器窗口都被关闭时，退出应用程序
  WebBrowser.setExitWhenLastWindowClosed(true);
  // 创建一个浏览器窗口
  WebBrowser browser = new WebBrowser(); 
  // 设置浏览器窗口的默认大小
  browser.setSize(800, 600);
  // 显示窗口
        browser.setVisible(true);

        // 打开主页
        browser.displayPage(browser.getHome());
    }
} 


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * representing unauthorized Request Token which is passed to the service provider when acquiring the authorized Access Token
 */
class SinaRequestToken extends OAuthToken {
  
    private static final long serialVersionUID = -821436584546957952L;

    SinaRequestToken(String token, String tokenSecret) {
        super(token, tokenSecret);
    }

    public String getAuthorizationURL() {
        return "http://api.t.sina.com.cn/oauth/authorize" + "?oauth_token=" + getToken();
    }

    /**
     * since Weibo4J 2.0.10
     */
    public String getAuthenticationURL() {
        return "api.t.sina.com.cn/oauth/authenticate" + "?oauth_token=" + getToken();
    }
}

class QQRequestToken extends OAuthToken {
	  
    private static final long serialVersionUID = -45365845469512122L;

    QQRequestToken(String token, String tokenSecret) {
        super(token, tokenSecret);
    }

    public String getAuthorizationURL() {
        return "https://open.t.qq.com/cgi-bin/authorize" + "?oauth_token=" + getToken();
    }

    /**
     * since Weibo4J 2.0.10
     */
    public String getAuthenticationURL() {
        return "open.t.qq.com/cgi-bin/authenticate" + "?oauth_token=" + getToken();
    }
}

final class EncodeFormat{
	public int width = 0;
	public String encode = "";
}

/*!
 *  @brief note
 *  @author tzz
 *  @version 0.1
 */
public class HelloWorld {
	/*!
	 *  @brief main function
	 *  @param arg  parameters
	 */
	private static abstract class TaskTest<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }
	
	
	public static void main(String arg[])throws Exception{
		Vector<String> t_list = new Vector<String>();
		Vector<String> t_list1 = new Vector<String>();
		t_list1.add("aa");
		t_list1.add("bb");
		t_list1.add("vv");
		
		t_list.setSize(t_list1.size());
		
		System.out.println(t_list.size());
		
		Collections.copy(t_list,t_list1);
	}
	
	public final static String	fsm_vectStringSpliter = "<>";
	public final static String	fsm_vectStringSpliter_sub = "@#&";
	
	public final static String[] fsm_groupSubjectPrefix = 
    {
    	"Re: ",
    	"Re:",
    	"Re： ",
    	"Re：",
    	"RE: ",
    	"RE:",
    	"RE： ",
    	"RE：",
    	"回复: ",
    	"回复:",
    	"回复： ",
    	"回复：",
    	"答复: ",
    	"答复:",
    	"答复： ",
    	"答复：",
    };
	
	private static String groupSubject(String _orgSub){
		int t_index = -1;
    	int t_length = 0;
    	for(String pre:fsm_groupSubjectPrefix){
    		int last = _orgSub.lastIndexOf(pre); 
    		if(last != -1){
    			if(last > t_index){
    				t_length = pre.length();
    				t_index = last;
    			}
    		}
    	}
    	
    	if(t_index != -1){
    		_orgSub = _orgSub.substring(t_index + t_length);
    	} 	
    	
    	return _orgSub.replace('\'', ' ');
    }
	
	static public void testBoolean(Object bool){
		if(((Boolean)bool).booleanValue()){
			System.out.print("aa");
		}
	}
		
	static public void testMDS()throws Exception{
		String host = "45562.yuchberry.info";
		int port	= 19781;
		byte[] bytes = {0x10,0x08,0,0,0,0,0,0,0,0,0,0,0,0,0};
		
		DatagramSocket ds = new DatagramSocket();
		DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress
                .getByName(host), port);
		ds.send(dp);
		
		bytes = new byte[64];
		dp = new DatagramPacket(bytes, bytes.length);
		ds.receive(dp);
		
		System.out.print(bytes);
	}
	
	static public void qgetOAuthRequest(){

		try{
//			String t_consumer_key = fetchSinaWeibo.fsm_consumer_key;
//			String t_consumer_secret = fetchSinaWeibo.fsm_consumer_serect;
//			
//			String t_request_token_api = "http://api.t.sina.com.cn/oauth/request_token";
//			String t_auth_api = "http://api.t.sina.com.cn/oauth/authorize";
//			String t_access_api = "http://api.t.sina.com.cn/oauth/access_token";
			
			String t_consumer_key = fetchQWeibo.fsm_consumerKey;
			String t_consumer_secret = fetchQWeibo.fsm_consumerSecret;
			
			String t_request_token_api = "https://open.t.qq.com/cgi-bin/request_token";
			String t_auth_api = "http://open.t.qq.com/cgi-bin/authorize";
			String t_access_api = "https://open.t.qq.com/cgi-bin/access_token";
			
//			String t_consumer_key = "4f362fc10f797de70f6d78e18246d2ae04dfaae00";
//			String t_consumer_secret = "8ed9d6ede362a23dc38981bd71c333fc";
//			
//			String t_request_token_api = "http://api.imgur.com/oauth/request_token";
//			String t_auth_api = "http://api.imgur.com/oauth/authorize";
//			String t_access_api = "http://api.imgur.com/oauth/access_token";
			
			OauthKey key = new OauthKey();
			key.customKey = t_consumer_key;
			key.customSecrect = t_consumer_secret;
			key.reset();
			key.callbackUrl = "http://127.0.0.1:8888/?bber=aaa.@gg.com";
			String t_response = null;
			
			QOAuth oauth = new QOAuth();
			
//			QWeiboRequest t_request = new QWeiboRequest();
//			t_response = t_request.syncRequest(t_request_token_api, "GET", key, new ArrayList<QParameter>(), null);

			StringBuffer sbQueryString = new StringBuffer();
			oauth.getOauthUrl(t_request_token_api, "GET", key.customKey,
					key.customSecrect, key.tokenKey, key.tokenSecrect, key.verify,
					key.callbackUrl, new ArrayList<QParameter>(), sbQueryString);
			
			String queryString = sbQueryString.toString();
						
//			java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.ALL);
//			java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.ALL);
//
//			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
//			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
//			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
			
			//QHttpClient http = new QHttpClient();
			//t_response = http.httpGet(t_request_token_api,queryString);
			
			
			URL url = new URL(t_request_token_api + "?" + queryString);
			URLConnection con = url.openConnection();


			con.setAllowUserInteraction(false);
			//con.connect();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();
			}
			
			String[] t_arr = t_response.split("&");
			//SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
			//SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[1].split("=")[1],t_arr[2].split("=")[1].replace("\n", ""));
			QQRequestToken t_requestToken = new QQRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
						
			System.out.println("Open URL:" + t_auth_api+"?oauth_token="+t_requestToken.getToken());
			
			System.out.print("input PIN:");
			BufferedReader bufin = new BufferedReader(new InputStreamReader(System.in)); 
			String PIN = bufin.readLine();			
			
			key.tokenKey = t_requestToken.getToken();
			key.tokenSecrect = t_requestToken.getTokenSecret();
			key.verify = PIN;
			key.callbackUrl = null;
			
			sbQueryString = new StringBuffer();
			oauth.getOauthUrl(t_access_api, "GET", key.customKey,
					key.customSecrect, key.tokenKey, key.tokenSecrect, key.verify,
					key.callbackUrl, new ArrayList<QParameter>(), sbQueryString);
			
			url = new URL(t_access_api + "?" + sbQueryString.toString());
			con = url.openConnection();
			
			con.setAllowUserInteraction(false);
			con.connect();
						
			in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();	
			}
			
			System.out.println("access response: " + t_response);
	
		}catch(Exception e){
			prt(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	static public void getOAuthRequest(){
		
		try{
			String t_consumer_key = fetchSinaWeibo.fsm_consumer_key;
			String t_consumer_secret = fetchSinaWeibo.fsm_consumer_serect;
			
			String t_request_token_api = "http://api.t.sina.com.cn/oauth/request_token";
			String t_auth_api = "http://api.t.sina.com.cn/oauth/authorize";
			String t_access_api = "http://api.t.sina.com.cn/oauth/access_token";
			
//			String t_consumer_key = fetchQWeibo.fsm_consumerKey;
//			String t_consumer_secret = fetchQWeibo.fsm_consumerSecret;
//			
//			String t_request_token_api = "http://open.t.qq.com/cgi-bin/request_token";
//			String t_auth_api = "https://open.t.qq.com/cgi-bin/authorize";
//			String t_access_api = "https://open.t.qq.com/cgi-bin/access_token";
			
//			String t_consumer_key = "4f362fc10f797de70f6d78e18246d2ae04dfaae00";
//			String t_consumer_secret = "8ed9d6ede362a23dc38981bd71c333fc";
//			
//			String t_request_token_api = "http://api.imgur.com/oauth/request_token";
//			String t_auth_api = "http://api.imgur.com/oauth/authorize";
//			String t_access_api = "http://api.imgur.com/oauth/access_token";
			
			OAuth t_auth = new OAuth(t_consumer_key,t_consumer_secret);
						
			URL url = new URL(t_request_token_api);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();

			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			
			PostParameter[] t_param = 
			{
					new PostParameter("oauth_callback","http://127.0.0.1:8888"),
					new PostParameter("source",t_consumer_key)
			};
			
			String t_authHeader = t_auth.generateAuthorizationHeader("POST", t_request_token_api, t_param, null);
			con.setRequestProperty("Authorization",t_authHeader);
			
			String t_params = "oauth_callback=http://127.0.0.1:8888" + "&source=" + t_consumer_key;
			byte[] bytes = t_params.getBytes("UTF-8");
			
			con.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded");
			
			con.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));
			
			System.out.println("param: " + t_params);
			
			OutputStream t_os = con.getOutputStream();		
					
			t_os.write(bytes);
			t_os.flush();
			t_os.close();			
			
			String t_response = null;
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();	
			}
			
			String[] t_arr = t_response.split("&");
			SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[0].split("=")[1],t_arr[1].split("=")[1].replace("\n", ""));
			//SinaRequestToken t_requestToken = new SinaRequestToken(t_arr[1].split("=")[1],t_arr[2].split("=")[1].replace("\n", ""));
			
			
			System.out.println("Open URL:" + t_auth_api+"?oauth_token="+t_requestToken.getToken());
			
			System.out.print("input PIN:");
			BufferedReader bufin = new BufferedReader(new   InputStreamReader(System.in)); 
			String PIN = bufin.readLine();			
						
			
			url = new URL(t_access_api);
			HttpURLConnection tcon = (HttpURLConnection)url.openConnection();
			
			tcon.setDoInput(true);
			tcon.setRequestMethod("POST");
			tcon.setDoOutput(true);
			
			String authString = t_auth.generateAuthorizationHeader("POST", t_access_api, 
										new PostParameter[]
										{
											new PostParameter("oauth_verifier", PIN),
											new PostParameter("source",t_consumer_key)
										},
										t_requestToken);
			
			tcon.setRequestProperty("Authorization",authString);
			System.out.println("AuthString: " + authString);
			
			t_params = "oauth_verifier=" + PIN + "&source=" + t_consumer_key;
			bytes = t_params.getBytes("UTF-8");
			
			tcon.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded");
			
			tcon.setRequestProperty("Content-Length",
                    Integer.toString(bytes.length));
			
			System.out.println("param: " + t_params);
			
			t_os = tcon.getOutputStream();		
					
			t_os.write(bytes);
			t_os.flush();
			t_os.close();
			
			in = new BufferedReader(new InputStreamReader(tcon.getInputStream(), "UTF-8"));
			try{
				StringBuffer t_stringBuffer = new StringBuffer();
				
				String temp;
				while ((temp = in.readLine()) != null) {
					t_stringBuffer.append(temp+"\n");
				}
				
				t_response = t_stringBuffer.toString();
			}finally{
				in.close();	
			}
			
			System.out.println("access response: " + t_response);
	
		}catch(Exception e){
			prt(e.getMessage());
			e.printStackTrace();
		}
				
	}
	
	
	static public void berrySendTest(){
		
		try{
			
			Socket t_socket = GetSocketServer("111111","localhost",9716,false);
			sendReceive t_receive = new sendReceive(t_socket);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(msg_head.msgConfirm);
			sendReceive.WriteString(os, "111111",false);
			sendReceive.WriteInt(os,4);
			os.write(0);
			sendReceive.WriteString(os,"1.1.715",false);
			t_receive.SendBufferToSvr(os.toByteArray(), false);
			
			fetchMail t_mail = new fetchMail(false);
			
			String[] t_string = {"yuchting@gmail.com"};
			t_mail.SetSendToVect(t_string);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			t_mail.SetContain(df.format(new Date()));
			t_mail.SetSubject(t_mail.GetContain());
			
			final int t_math = (int)(Math.random() * 100);
			t_mail.SetMailIndex(t_math);
						
			
			os = new ByteArrayOutputStream();
			os.write(msg_head.msgMail);
			
			t_mail.OutputMail(os);
			os.write(fetchMail.NOTHING_STYLE);
			
			os.write(1);
			
			t_receive.SendBufferToSvr(os.toByteArray(), true);
			
			ByteArrayInputStream in = new ByteArrayInputStream(t_receive.RecvBufferFromSvr());
			
			if(in.read() == msg_head.msgSendMail
				&& t_math == sendReceive.ReadInt(in)){
				prt(t_mail.GetSubject() + " mail deliver succ id<" + Integer.toString(t_math) + ">");
			}
						
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	static public void berrySendWeiboTest(){
		try{
			
			Socket t_socket = GetSocketServer("111111","192.168.10.20",9716,false);
			sendReceive t_receive = new sendReceive(t_socket);
			
			ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
			t_stream.write(msg_head.msgConfirm);
			sendReceive.WriteString(t_stream, "111111",false);
			sendReceive.WriteInt(t_stream,5);
			t_stream.write(0);
			sendReceive.WriteString(t_stream,"1.1.715",false);
			sendReceive.WriteString(t_stream,cryptPassword.md5("111"),false);
			
			t_receive.SendBufferToSvr(t_stream.toByteArray(), false);
			
			fetchWeibo t_weibo = new fetchWeibo(false);
			t_weibo.SetText("我要发发试试,评论发不了？");
			t_weibo.SetCommectWeiboId(5572863863L);
			
			t_stream.reset();
			t_stream.write(msg_head.msgWeibo);
			
			t_weibo.OutputWeibo(t_stream);
			
			t_receive.SendBufferToSvr(t_stream.toByteArray(), true);
			
			Thread.sleep(10000000);
			
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	static public void berryRecvTest(){
		try{
			
			Socket t_socket = GetSocketServer("111111","127.0.0.1",9716,false);
			sendReceive t_receive = new sendReceive(t_socket);
			
			ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
			t_stream.write(msg_head.msgConfirm);
			sendReceive.WriteString(t_stream, "111111",false);
			sendReceive.WriteInt(t_stream,5);
			t_stream.write(0);
			sendReceive.WriteString(t_stream,"1.1.715",false);
			sendReceive.WriteString(t_stream,cryptPassword.md5("111"),false);
			
			t_receive.SendBufferToSvr(t_stream.toByteArray(), false);
			
			while(true){

				ByteArrayInputStream in = new ByteArrayInputStream(t_receive.RecvBufferFromSvr());
				switch(in.read()){
					case msg_head.msgMail:
						fetchMail t_mail = new fetchMail(false);
						t_mail.InputMail(in);
						prt("receive idx: " + t_mail.GetMailIndex() + " subject: " + t_mail.GetSubject() + "\n" + t_mail.GetContain());
												
						// TODO display in berry
						//
						
						break;
						
					case msg_head.msgWeibo:
						fetchWeibo t_weibo = new fetchWeibo(false);
						t_weibo.InputWeibo(in);
						
						prt("receive weibo id" + t_weibo.GetId() + " text:" + t_weibo.GetText());
						break;
					case msg_head.msgSendMail:
						
						// TODO display in berry
						// the post mail has been send
						//
						
						break;
				}
			}
			
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	static public Socket GetSocketServer(String _userPassword,String _host,int _port,boolean _ssl)throws Exception{
		
		if(_ssl){

			String	key				= "YuchBerryKey";  
			
			char[] keyStorePass		= _userPassword.toCharArray();
			char[] keyPassword		= _userPassword.toCharArray();
			
			KeyStore ks				= KeyStore.getInstance(KeyStore.getDefaultType());
			
			ks.load(new FileInputStream(key),keyStorePass);
			
			KeyManagerFactory kmf	= KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks,keyPassword);
			
			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(kmf.getKeyManagers(),null,null);
			  
			SSLSocketFactory factory=sslContext.getSocketFactory();
			
			return factory.createSocket(_host,_port);
			
		}else{
			
			return new Socket(InetAddress.getByName(_host),_port); 
		}	  
	}

	static void prt(String s) {
		System.out.println(s);
	}
	
	static void prtA(byte[] a) {
		
		for(int i = 0;i < a.length;i++){
			prt(String.valueOf(a[i]));
		}
	}
}
