/**
 *  HelloWorld.java
 *  Copyright (C) 2001-2005 Research In Motion Limited. All rights reserved.
 */
 
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;
 
public class recvMain extends UiApplication {
    public static void main(String[] args) {
    	recvMain theApp = new recvMain();
       theApp.enterEventDispatcher();
       }
   
    public recvMain() {
       pushScreen(new HelloWorldScreen());
       }
    }
 
 
final class HelloWorldScreen extends MainScreen {
    public HelloWorldScreen() {
       super();
        LabelField title = new LabelField("HelloWorld Sample", LabelField.ELLIPSIS
                                         | LabelField.USE_ALL_WIDTH);
       setTitle(title);
       add(new RichTextField("Hello World!"));
       }
      
    public boolean onClose() {
       Dialog.alert("Goodbye!");
       System.exit(0);
       return true;
       }
    }