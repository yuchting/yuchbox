package com.yuchting.yuchberry.client.weibo;

import java.util.Vector;

import local.localResource;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ActiveFieldContext;
import net.rim.device.api.ui.component.ActiveFieldCookie;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.ui.component.CookieProvider;
import net.rim.device.api.util.AbstractString;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Factory;
import net.rim.device.api.util.StringPattern;
import net.rim.device.api.util.StringPatternRepository;

import com.yuchting.yuchberry.client.recvMain;

/**
 *  copy from the blackberry system sample
 * 
 * The ActiveFieldCookie implementation allows for the factory to
 * provide a container for the zipcode information when moving
 * between the StringPattern and the MenuItem.
 */
class WeiboUserFindActiveFieldCookie implements ActiveFieldCookie
{
    private String m_name;            // The ZipCode that matched the pattern.

    private MenuItem m_getInfoItem = new MenuItem(recvMain.sm_local.getString(localResource.WEIBO_CHECK_USER_MENU_LABEL),5,5){
    	public void run(){
    		
    	}
    };
    
    /**
     * Create the ZipCodeLookupActiveFieldCookie by passing in the zip code
     * @param data the zip code to lookup
     */
    public WeiboUserFindActiveFieldCookie( String data )
    {
        // Save the zipcode for later use.
    	m_name = data;
    }

    /**
     * This is an abstract method for the ActiveFieldCookie and must be implemented.
     * @return always return false.
     */
    public boolean invokeApplicationKeyVerb()
    {
        return false;
    }

    /**
     * In this case, we are under-utilizing the getFocusVerbs method by simply adding
     * our menu item to the list of items and returning.  Refer to the javadocs for
     * all of the different options here.
     */
    public MenuItem getFocusVerbs(CookieProvider provider, Object context, Vector items)
    {
        items.addElement( m_getInfoItem );
        return (MenuItem)items.elementAt(0);
    }
}

/**
 * This class provides an implementation of the StringPattern.
 * It is important to note that every single time a character
 * is entered into an active field that this method will be executed.
 * As such, it is incredibly important to stress that the findMatch method
 * must be meticulously written for speed and efficiency so as to not
 * alter the user experience.
 */
class WeiboUserFind extends StringPattern
{
    public WeiboUserFind() {}

    /**
     * For the purposes of this implementation, findMatch will match any five digit continuous number (no spaces, etc)
     * that it finds.  It is better to err on the side of caution and accept a zip code than exclude it.
     * @param str the AbstractString to search through.
     * @param beginIndex the beginning index in the string.
     * @param maxIndex the ending index in the string.
     * @param match the holder for information on the match if applicable
     * @return true if a match was found and false otherwise
     */
    public boolean findMatch( AbstractString str, int beginIndex, int maxIndex, StringPattern.Match match ){
        // Because we are matching against a zip code which is a minimum of 5 numbers, we can
        // perform a quick check here to ensure that it is worth evaluating the actual characters.
//        if( maxIndex - beginIndex < 5 ) {
//            return false;
//        }
//
//        int numCounter = 0;
//
//        for( int i = beginIndex; i < maxIndex; i++ ) {
//            if( CharacterUtilities.isDigit( str.charAt( i ))) {
//                numCounter++;
//                if( numCounter == 5 ) {
//                    match.beginIndex = i - 4;   // Begin Index is zero based
//                    match.endIndex = i + 1;     // End Index is expecting the end index after the last matching character
//                    match.id = ZipCodeLookupSample.FACTORY_ID;
//                    match.prefixLength = 0;
//                    return true;
//                }
//            } else {
//                numCounter = 0;
//            }
//        }
//
//        return false;
    	
		char a = str.charAt(beginIndex);
		
		if(a == '@'){
			
			int i = beginIndex + 1;
			for(;i < maxIndex;i++){
				
				a = str.charAt(i);
				
				if(!isLeagalNameCharacter(a)){
					break;
				}
			}
			
			if(i != beginIndex + 1){
				match.beginIndex 	= beginIndex;
				match.endIndex		= i;
				match.id			= WeiboUserFindFactory.fsm_pattern_factory_id;
				match.prefixLength	= 0;
				
				return false;
			}
		}
		
		return false;
    }
    
    public static boolean isLeagalNameCharacter(char a){
		
		if(a == '，' || a == '；' ||a == '：' ||a == '？' ||a == '‘'){
			return false;
		}
		
		if(Character.isDigit(a) || isChinese(a) || isAlpha(a)){
			return true;
		}
		
		if(a == '-' || a== '_' ){
			return true;
		}
		
		return false;
	}
	
	public static boolean isChinese(char a){ 
		int v = (int)a; 
		return (v>=19968 && v <=171941);	
	}
	
	public static boolean isAlpha(char a){
		return Character.isLowerCase(a) || Character.isUpperCase(a);
	}
}

/**
 * The ZipCodeLookupFactory provides the necessary piece
 * which allows the StringPattern.Match class to indicate
 * how it should move forward when a match is found.
 * One will note that the ID for the Factory is the same
 * ID that is passed back from the findMatch method in the
 * StringPattern.
 */
public class WeiboUserFindFactory implements Factory
{
	public final static long		fsm_pattern_factory_id = 155394686315L;
	public final static long		fsm_pattern_instance_id = 217827014192L;
	
	static {
		WeiboUserFindFactory factory = new WeiboUserFindFactory();
		RuntimeStore.getRuntimeStore().put( fsm_pattern_factory_id, factory );
		
		WeiboUserFind pattern = new WeiboUserFind();
		StringPatternRepository.addPattern( pattern );
	}
	
    public WeiboUserFindFactory()
    {
        // Do nothing for now
    }

    /**
     * Create a new instance of the ActiveFieldCookie
     * using the information passed into this method.
     * @param initialData An ActiveFieldContext that contains
     * the necessary information from the StringPattern class.
     * @return An ActiveFieldCookie embodying the matched pattern.
     */
    public Object createInstance( Object initialData )
    {
        if(initialData instanceof ActiveFieldContext) {
            ActiveFieldContext afc = (ActiveFieldContext)initialData;
            String stringData = (String)afc.getData();
            return new WeiboUserFindActiveFieldCookie( stringData );
        }
        return null;
    }
}





