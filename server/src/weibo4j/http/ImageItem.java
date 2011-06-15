/**
 *
 */
package weibo4j.http;

import java.io.IOException;

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
	
	public ImageItem(String name,byte[] content) throws Exception{
		String imgtype=getContentType(content);
		
	    if(imgtype!=null&&(imgtype.equalsIgnoreCase("image/gif")||imgtype.equalsIgnoreCase("image/png")
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

	public static String getContentType(byte[] mapObj) throws IOException {

		return "";
//		String type = "";
//		ByteArrayInputStream bais = null;
//		MemoryCacheImageInputStream mcis = null;
//		try {
//			bais = new ByteArrayInputStream(mapObj);
//			mcis = new MemoryCacheImageInputStream(bais);
//			Iterator itr = ImageIO.getImageReaders(mcis);
//			while (itr.hasNext()) {
//				ImageReader reader = (ImageReader) itr.next();
//				if (reader instanceof GIFImageReader) {
//					type = "image/gif";
//				} else if (reader instanceof JPEGImageReader) {
//					type = "image/jpeg";
//				} else if (reader instanceof PNGImageReader) {
//					type = "image/png";
//				} else if (reader instanceof BMPImageReader) {
//					type = "application/x-bmp";
//				}
//			}
//		} finally {
//			if (bais != null) {
//				try {
//					bais.close();
//				} catch (IOException ioe) {
//
//				}
//			}
//			if (mcis != null) {
//				try {
//					mcis.close();
//				} catch (IOException ioe) {
//
//				}
//			}
//		}
//		return type;
	}
}
