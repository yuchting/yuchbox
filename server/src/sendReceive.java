import java.net.Socket;
import java.io.*;


public class sendReceive{

	static final int scm_bufferSize = 2048;


	private byte[]		m_buffer = new byte[scm_bufferSize];

	private int		m_nextFlag;
	private int		m_packLen;
	private int		m_remainPackLen ;
	private int		m_recvLen;

	private int		m_waitSecond;
	private int		m_timeOutCounter;

	//! millisecond
	private int		m_timeOutSecond;

	boolean			m_closeRecv;


	public sendReceive(){
		ResetState();
		m_closeRecv = false;
	}

	//! receive subfunction
	public int RecvFunc(Socket _socket,byte[] _buffer){

		try{
			_socket.getOutputStream().write(_buffer);
		}catch(Exception _e){
			if(_socket.isClosed()){
				return 0;
			}
		}finally{
			return _buffer.length;
		}		
	}

	//! reset state
	public void ResetState()
	{
		m_nextFlag		= 0;
		m_packLen		= 0;
		m_remainPackLen = 0;
		m_recvLen		= 0;
	}

	//! is close recv
	public boolean IsCloseRecv()const{return m_closeRecv;}

	//! set the time wait
	public void TimeUpSecond(int _second){m_waitSecond = _second;}

	//! get the package len
	public int GetPackageLen()const{return m_packLen;}

	//! send buffer
	public static BOOL SendRecvMiddle::SendBufferToSvr(SOCKET _socket,const char* _buffer,int _size,BOOL _que = FALSE);

	//! recv buffer
	public byte[] RecvBufferFromSvr(Socket _socket,byte[] _recvBuff){

		if(_socket.isClosed() || m_closeRecv){
			return null;
		}

		if(_recvBuff == null){
			_recvBuff = m_buffer;
		}

		byte[] t_stackBuffer = new byte[1024];
		byte[] t_recvBuffer = t_stackBuffer;

		if(m_nextFlag == 2){
			// former buffer has pack
			//
			m_nextFlag = 0;

			// copy former buffer data
			//
			t_recvBuffer = _recvBuff + m_packLen;

			if(m_recvLen == 1){
				const int t_len = RecvFunc(_socket,t_recvBuffer + 1,ct_stackBuffLen);
				if(!t_len){
					m_nextFlag = 2;
					return NULL;
				}

				m_recvLen += t_len;
			}

		}else{

			TestOutput("\nRecvBufferFromSvr 2 <%d>",_socket);

			m_recvLen = RecvFunc(_socket,t_recvBuffer,ct_stackBuffLen);
			if(m_recvLen == 0){
				return NULL;
			}

			TestOutput("\nRecvBufferFromSvr 3 <%d>",_socket);
		}

		if(m_recvLen > 0){

			// recv normal pack head
			//
			if(m_nextFlag != 1){

				TestOutput("\nRecvBufferFromSvr 4 <%d>",_socket);

				m_packLen = (int)(*(WORD*)t_recvBuffer);

				if(m_packLen <= 0 || m_packLen > ct_stackBuffLen){
					// error data (other illegal client/server ...)
					//
					return NULL;
				}

				m_recvLen -= 2;

				if(m_packLen == m_recvLen){

					TestOutput("\nRecvBufferFromSvr 5 <%d>",_socket);

					// whole pack
					//
					m_nextFlag = 0;
					CopyMemory(_recvBuff,t_recvBuffer + 2,m_recvLen);

					return (m_packLen>0)?_recvBuff:NULL;

				}else if(m_packLen > m_recvLen){

					TestOutput("\nRecvBufferFromSvr 6 <%d>",_socket);

					// has remain pack
					//
					m_nextFlag = 1;
					CopyMemory(_recvBuff,t_recvBuffer + 2,m_recvLen);
					m_remainPackLen = m_packLen - m_recvLen;

					const char* t_ret = RecvBufferFromSvr(_socket,_recvBuff + m_recvLen);

					// if the t_ret value is NULL (socket is closed)
					// will return NULL
					//
					return (m_packLen > 0 && t_ret)?_recvBuff:NULL;

				}else{

					TestOutput("\nRecvBufferFromSvr 7 <%d>",_socket);

					//_packLen < t_recvLen
					//
					// more than one package
					//
					CopyMemory(_recvBuff,t_recvBuffer + 2,m_recvLen);
					m_recvLen = m_recvLen - m_packLen;
					m_nextFlag = 2;	

					return (m_packLen>0)?_recvBuff:NULL;
				}

			}else{

				// has continue to receive pack
				//
				if(m_recvLen == m_remainPackLen){

					TestOutput("\nRecvBufferFromSvr 9 <%d>",_socket);


					// the remain pack receive over
					//
					m_nextFlag = 0;
					CopyMemory(_recvBuff,t_recvBuffer,m_recvLen);

					return (m_packLen>0)?_recvBuff:NULL;

				}else if(m_remainPackLen > m_recvLen){

					TestOutput("\nRecvBufferFromSvr 10 <%d>",_socket);

					// receive again
					//
					m_nextFlag = 1;
					CopyMemory(_recvBuff,t_recvBuffer,m_recvLen );
					m_remainPackLen -= m_recvLen;

					const char* t_ret = RecvBufferFromSvr(_socket,_recvBuff + m_recvLen);

					// if the t_ret value is NULL (socket is closed)
					// will return NULL
					//
					return (m_packLen > 0 && t_ret)?_recvBuff:NULL;

				}else{

					TestOutput("\nRecvBufferFromSvr 11 <%d>",_socket);

					// has other package append
					//
					CopyMemory(_recvBuff,t_recvBuffer,m_recvLen);
					m_recvLen = m_recvLen - m_remainPackLen;
					m_nextFlag = 2;

					return (m_packLen>0)?_recvBuff:NULL;
				}

			}		
		}

		return NULL;
	}

	//! get the time up counter
	public int GetTimeOutMilliSecond(){
		return m_timeOutSecond;
	}

	//! reset the Time Out counter
	public void ResetTimeOutCounter(){
		
	}
}