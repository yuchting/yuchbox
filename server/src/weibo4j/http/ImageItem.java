/**
 *
 */
package weibo4j.http;


//import com.sun.imageio.plugins.bmp.BMPImageReader;
//import com.sun.imageio.plugins.gif.GIFImageReader;
//import com.sun.imageio.plugins.jpeg.JPEGImageReader;
//import com.sun.imageio.plugins.png.PNGImageReader;

/**
 * 临时存储上传图片的内容，格式，文件信息等
 * 
 * @author zhulei
 * 
 */
public class ImageItem {
	private byte[] content;
	private String name;
	private String contentType;
	
	public ImageItem(String name,byte[] content,String imgtype) throws Exception{
		
	    if(imgtype !=null && (imgtype.equalsIgnoreCase("image/gif")|| imgtype.equalsIgnoreCase("image/png")
	            ||imgtype.equalsIgnoreCase("image/jpeg"))){
	    	
	    	this.content=content;
	    	this.name=name;
	    	this.contentType=imgtype;
	    	
	    }else{
	    	throw new IllegalStateException(
            "Unsupported image type, Only Suport JPG ,GIF,PNG!");
	    }
	}
	
	public byte[] getContent() {
		return content;
	}
	public String getName() {
		return name;
	}
	public String getContentType() {
		return contentType;
	}

	
}
