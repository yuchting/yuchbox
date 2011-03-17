/* *
 *功能：设置帐户有关信息及返回路径（基础配置页面）
 *版本：3.1
 *日期：2010-11-01
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.访问支付宝首页(www.alipay.com)，然后用您的签约支付宝账号登陆.
 *2.点击导航栏中的“商家服务”，即可查看
	
 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 * */
package com.yuchting.yuchberry.yuchsign.server;


public class AlipayConfig {
	// 如何获取安全校验码和合作身份者ID
	// 1.访问支付宝商户服务中心(b.alipay.com)，然后用您的签约支付宝账号登陆.
	// 2.访问“技术服务”→“下载技术集成文档”（https://b.alipay.com/support/helperApply.htm?action=selfIntegration）
	// 3.在“自助集成帮助”中，点击“合作者身份(Partner ID)查询”、“安全校验码(Key)查询”
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "";
	
	// 交易安全检验码，由数字和字母组成的32位字符串
	public static String key = "";
	
	// 签约支付宝账号或卖家收款支付宝帐户
	public static String seller_email = "";
	
	// notify_url 交易过程中服务器通知的页面 要用 http://格式的完整路径，不允许加?id=123这类自定义参数
	public static String notify_url = "http://www.xxx.cn/js_jsp_utf8/notify_url.jsp";
	
	// 付完款后跳转的页面 要用 http://格式的完整路径，不允许加?id=123这类自定义参数
	//return_url的域名不能写成http://localhost/js_jsp_utf8/return_url.jsp ，否则会导致return_url执行无效
	public static String return_url = "http://www.xxx.com/js_jsp_utf8/return_url.jsp";
	
	// 网站商品的展示地址，不允许加?id=123这类自定义参数
	public static String show_url = "http://www.alipay.com";
	
	//收款方名称，如：公司名称、网站名称、收款人姓名等
	public static String mainname = "收款方名称";
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	
	

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "UTF-8";
	
	// 签名方式 不需修改
	public static String sign_type = "MD5";
	
	//访问模式,根据自己的服务器是否支持ssl访问，若支持请选择https；若不支持请选择http
	public static String transport = "http";
}
