package com.yuchting.yuchberry.client;

final public class msg_head {
	
	final public static byte msgMail 			= 0;
	final public static byte msgSendMail 		= 1;

	final public static byte msgConfirm 			= 2;
	final public static byte msgNote 			= 3;
	
	final public static byte msgBeenRead 		= 4;
	final public static byte msgMailAttach 		= 5;
	final public static byte msgFetchAttach 		= 6;
	
	final public static byte msgKeepLive 		= 7;
	final public static byte msgMailConfirm		= 8;
	
	final public static byte msgSponsorList		= 9;
	
	
	final public static byte msgWeibo			= 10;
	final public static byte msgWeiboConfirm		= 11;
	final public static byte msgWeiboHeadImage	= 12;
	
	final public static byte msgLatestVersion	= 13;
		
	
	final public static String noteErrorUserPassword = "illegal client connect\n meybe a error user password";

		
}