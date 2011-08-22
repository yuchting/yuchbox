package com.yuchting.yuchberry.client.screen;

public interface ISendAttachmentCallback {

	public void sendStart();
	public void sendProgress(int _fileIndex,int _uploaded,int _totalSize);
	public void sendPause();
	public void sendError();
	public void sendFinish();
}
