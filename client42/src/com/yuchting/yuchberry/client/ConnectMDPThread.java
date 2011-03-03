/**
 *  Thanks RAiN for this class
 */
package com.yuchting.yuchberry.client;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import net.rim.device.api.io.DatagramBase;
import net.rim.device.api.io.DatagramConnectionBase;
import net.rim.device.api.io.DatagramStatusListener;
import net.rim.device.api.io.IOCancelledException;


interface MDPThreadStatusListener {
	void informStatus(String _text);
	void mdpThreadFinished(int _result);
}

public class ConnectMDPThread extends Thread implements DatagramStatusListener
{
   private MDPThreadStatusListener _listener;
   private String _address;
   private DatagramConnectionBase connection = null;
   private Datagram sendingDiagram;

   // Constructor
   public ConnectMDPThread(String address, MDPThreadStatusListener listener)
   {
       _address = address;
       _listener = listener;
   }

   /**
    * Implementation of Thread.
    */
   public void run()
   {
       int result = 0;
       try
       {
           _listener.informStatus("  正在建立连接...");

           Class mdpClass = Class.forName("net.rim.device.cldc.io.mdp.Protocol");
           connection = (DatagramConnectionBase)mdpClass.newInstance();
           connection = (DatagramConnectionBase)connection.openPrim(_address, Connector.READ_WRITE,false);

           //connection.setup(1462989747, null);
           connection.setDatagramStatusListener(this);

           _listener.informStatus("  连接已建立。");

           _listener.informStatus("  正在发送数据...");

           sendingDiagram = connection.newDatagram();
           sendingDiagram.setAddress(_address);
           if (sendingDiagram instanceof DatagramBase){
        	   ((DatagramBase)sendingDiagram).setDatagramId(connection.allocateDatagramId(sendingDiagram));
           }

           connection.send(sendingDiagram);

           _listener.informStatus("  数据发送成功。");
       }
       catch(IOCancelledException ex)
       {
           result = -1;
       }
       catch(Throwable t)
       {
           result = -1;
           _listener.informStatus("  " + t.toString());
       }
       finally
       {
           try
           {
               _listener.informStatus("  正在关闭连接...");
               connection.close();
               _listener.informStatus("  连接正常关闭。");
           }
           catch(IOException ioe)
           {
               _listener.informStatus(ioe.toString());
           }

           _listener.mdpThreadFinished(result);
       }
   }

   public void updateDatagramStatus(int dgId, int code, Object context)
   {
       switch (code)
       {
           case DatagramStatusListener.PENDING:
               _listener.informStatus("    等待发送...");
               break;
           case DatagramStatusListener.SENDING:
               _listener.informStatus("    发送中...");
       }
   }

   public void cancel()
   {
       try
       {
           connection.cancel(sendingDiagram);
       }
       catch (IOException ex)
       {
           _listener.informStatus(ex.toString());
       }
   }
}
