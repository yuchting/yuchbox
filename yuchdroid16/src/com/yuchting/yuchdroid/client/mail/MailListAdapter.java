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
package com.yuchting.yuchdroid.client.mail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.database.Cursor;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuchting.yuchdroid.client.R;
import com.yuchting.yuchdroid.client.YuchDroidApp;

public class MailListAdapter extends BaseAdapter{
	
	// this MAIL_GROUP_ITEM_HEIGHT MAIL_GROUP_ITEM_SPLITER_HEIGHT will be calculated 
	// in YuchDroidApp.retrieveGroupListItemHeight to retrieve from inflater in native system size 
	// 
	private static int	MAIL_GROUP_ITEM_HEIGHT = (YuchDroidApp.sm_displyHeight >= 640)?(YuchDroidApp.sm_displyHeight >= 1280?120:70):50;
	private static int	MAIL_GROUP_ITEM_SPLITER_HEIGHT = (YuchDroidApp.sm_displyHeight >= 640)?(YuchDroidApp.sm_displyHeight >= 1280?30:20):10;
	
	private static boolean	MAIL_GROUP_ITEM__HEIGHT_ASSIGN = false;
	
	private int m_cursorIDIndex;
    private int m_cursorMarkIndex;
    private int m_cursorGroupFlagIndex;
    private int m_cursorSubjectIndex;
    private int m_cursorBodyIndex;
    private int m_cursorMailAddrIndex;
    private int m_cursorlatestTimeIndex;
	
	private LayoutInflater m_inflater;   	
	private HomeActivity	m_context;
	private Cursor			m_groupCursor;
	
	final static Calendar 	sm_calendar 	= Calendar.getInstance();
	final static Date		sm_timeDate 	= new Date();
	
    static SimpleDateFormat sm_yearMonthDayFormat = null;
	static SimpleDateFormat sm_monthDayHourFormat = null;
	
	private int m_currYear;
	private int m_currMonth;
	private int m_currDay;
	
	private GestureLibrary m_gesLibrary;
	
	
	public MailListAdapter(HomeActivity _ctx){
		m_context		= _ctx;
		m_inflater		= LayoutInflater.from(m_context);

		m_cursorIDIndex			= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.KEY_ID);
		m_cursorMarkIndex		= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MARK);
		m_cursorGroupFlagIndex	= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_GROUP_FLAG);
		m_cursorSubjectIndex	= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_SUBJECT);
		m_cursorBodyIndex		= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_LEATEST_BODY);
		m_cursorMailAddrIndex	= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_ADDR_LIST);
		m_cursorlatestTimeIndex	= m_context.m_groupCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_LEATEST_TIME);

		
		if(sm_yearMonthDayFormat == null){
			sm_yearMonthDayFormat = new SimpleDateFormat("yyyy"+_ctx.getString(R.string.mail_time_year)+
											"MM"+_ctx.getString(R.string.mail_time_month)+
											"dd" + _ctx.getString(R.string.mail_time_day));

			sm_monthDayHourFormat = new SimpleDateFormat("MM"+_ctx.getString(R.string.mail_time_month)+
											"dd" + _ctx.getString(R.string.mail_time_day) +
											" HH:mm");
		}
		
		sm_timeDate.setTime(System.currentTimeMillis());
		sm_calendar.setTime(sm_timeDate);
		
		m_currYear	= sm_calendar.get(Calendar.YEAR);
		m_currMonth = sm_calendar.get(Calendar.MONTH);
		m_currDay	= sm_calendar.get(Calendar.DAY_OF_MONTH);
		
		m_gesLibrary = GestureLibraries.fromRawResource(_ctx, R.raw.gestures);
		if(!m_gesLibrary.load()){
			m_gesLibrary = null;
		}
	}
	

    public int getCount() {
        return m_context.m_groupCursor.getCount();
    }

    public Object getItem(int position) {
         return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	Cursor t_groupCursor = m_context.m_groupCursor;
   	
    	ItemHolder holder;
    	
    	if(convertView == null) {       	
            convertView = inflateMailGroupView();
    	}
    	holder = (ItemHolder) convertView.getTag();
        
    	int t_cmpYear;
    	int t_cmpMonth;
    	int t_cmpDay;
    	
        // fill the group data
        //
        t_groupCursor.moveToPosition(position);
        
        long t_currTime = t_groupCursor.getLong(m_cursorlatestTimeIndex);
        holder.groupId = t_groupCursor.getInt(m_cursorIDIndex);
        holder.selected.setChecked(m_context.isSelectedGroup(holder.groupId));
                
        if(position != 0){
        	t_groupCursor.moveToPosition(position - 1);
        	holder.nextGroupId = t_groupCursor.getInt(m_cursorIDIndex);
        	
        	long t_time = t_groupCursor.getLong(m_cursorlatestTimeIndex);
          	sm_timeDate.setTime(t_time);
    		sm_calendar.setTime(sm_timeDate);
    		
    		t_cmpYear 	= sm_calendar.get(Calendar.YEAR);
    		t_cmpMonth 	= sm_calendar.get(Calendar.MONTH);
    		t_cmpDay 	= sm_calendar.get(Calendar.DAY_OF_MONTH);
    		    		
        }else{
        	
        	holder.nextGroupId = -1;
        	
        	t_cmpYear	= m_currYear;
        	t_cmpMonth	= m_currMonth;
        	t_cmpDay	= m_currDay;
        }    
    	
        //compare the time and adjuge whether show the date spliter
        //
      	sm_timeDate.setTime(t_currTime);
		sm_calendar.setTime(sm_timeDate);
		
		if(sm_calendar.get(Calendar.YEAR) != t_cmpYear
			|| sm_calendar.get(Calendar.MONTH) != t_cmpMonth
			|| sm_calendar.get(Calendar.DAY_OF_MONTH) != t_cmpDay){
			holder.showDateSpliter(true, t_currTime);
		}else{
			holder.showDateSpliter(false,0);
		}
        
        if(position < t_groupCursor.getCount() - 1){
        	t_groupCursor.moveToPosition(position + 1);
        	holder.preGroupId = t_groupCursor.getInt(m_cursorIDIndex);
        }else{
        	holder.preGroupId = -1;
        }
        
        // fill the mail text
        //
        t_groupCursor.moveToPosition(position);
        
        holder.cursorPos = position;        
        holder.updateView(t_groupCursor.getInt(m_cursorGroupFlagIndex),
        				t_groupCursor.getString(m_cursorSubjectIndex), 
        				getShortAddrList(fetchMail.parseAddressList(t_groupCursor.getString(m_cursorMailAddrIndex).split(fetchMail.fsm_vectStringSpliter))), 
        				t_groupCursor.getString(m_cursorBodyIndex),
        				t_groupCursor.getLong(m_cursorlatestTimeIndex));
		
        return convertView;
    }
    
    private View inflateMailGroupView(){
    	
    	ItemHolder holder = new ItemHolder();
    	
        View convertView = m_inflater.inflate(R.layout.mail_list_item,null);
                
        holder.background 	= (ViewGroup)convertView.findViewById(R.id.mail_item);
        holder.groupFlag	= (ImageView)convertView.findViewById(R.id.mail_group_flag);
        holder.subject		= (TextView)convertView.findViewById(R.id.mail_subject);
        holder.body			= (TextView)convertView.findViewById(R.id.mail_body);
        holder.mailAddr		= (TextView)convertView.findViewById(R.id.mail_from_to);
        holder.latestTime	= (TextView)convertView.findViewById(R.id.mail_time);
        holder.mailDateSpliter	= (TextView)convertView.findViewById(R.id.mail_date_spliter);
        holder.selected		= (CheckBox)convertView.findViewById(R.id.mail_item_checkbox);
        
        holder.selected.setTag(holder);
        holder.groupFlag.setTag(holder);
        
        convertView.setTag(holder);
        
        holder.groupFlag.setOnClickListener(m_context);
        holder.selected.setOnClickListener(m_context);
                
        // assign the item height
        //
        synchronized (this) {
        	
			if(!MAIL_GROUP_ITEM__HEIGHT_ASSIGN){
			 	
				MAIL_GROUP_ITEM__HEIGHT_ASSIGN = true;
			 	
			 	float t_subjectHeight 		= holder.subject.getPaint().getTextSize();
				float t_bodyHeight 			= holder.body.getPaint().getTextSize();
				float t_timeSpliterHeight	= holder.mailDateSpliter.getPaint().getTextSize();
				
				MAIL_GROUP_ITEM_SPLITER_HEIGHT = (int)t_timeSpliterHeight;
				MAIL_GROUP_ITEM_HEIGHT = (int)(t_subjectHeight +  t_bodyHeight + MAIL_GROUP_ITEM_SPLITER_HEIGHT) + 5;
			}
		}
                
        return convertView;
    }
    
    
    
    public static String getShortAddrList(fetchMail.Address[] _list){
    	
    	StringBuffer t_display = new StringBuffer();
		int t_num = 0;
		
		boolean t_remain = false;
		while(t_num < _list.length){
			
			t_display.append(_list[t_num].m_name.length() == 0?_list[t_num].m_addr:_list[t_num].m_name);
			t_num++;
			
			if(t_num >= 3){
				if(t_num < _list.length){
					t_remain = true;
					t_display.append("...");
				}				
				break;
			}else{
				
				if(t_num < _list.length){
					t_display.append(",");
				}				
			}
		}
		
		if(t_remain){
			t_display.append("(").append(_list.length).append(")");
		}
		
		
		return t_display.toString();
    }
    
    public static class ItemHolder{
    	
    	public final static int MAX_DELETE_PROMPT_WIDTH = YuchDroidApp.sm_displyWidth / 4;
    	public final static int MAX_READ_PROMPT_WIDTH = YuchDroidApp.sm_displyWidth / 4;
    	
		ViewGroup	background;
        ImageView	groupFlag;
        TextView	subject;
        TextView	body;
        TextView	mailAddr;
        TextView	latestTime;
        TextView	mailDateSpliter;
        CheckBox	selected;
                
        long		preGroupId = -1;
        long		groupId;
        long		nextGroupId = -1;
        
        int			cursorPos;
        
        public void updateView(int _groupFlag,String _subject,String _mailAddr,String _body,long _latestTime){
        	
        	setGroupFlag(_groupFlag);
            
            subject.setText(_subject);
            if(_body.length() > 20){
            	_body = _body.substring(0,20);
            }
            body.setText("-" + _body);             
     		mailAddr.setText(_mailAddr);
     		
     		// time
     		//
     		sm_calendar.setTimeInMillis(System.currentTimeMillis());
     		final int t_currYear 		= sm_calendar.get(Calendar.YEAR);
     		
     		sm_calendar.setTimeInMillis(_latestTime);
     		final int t_year 		= sm_calendar.get(Calendar.YEAR);
     		
     		String t_time;
     		if(t_year == t_currYear){
     			t_time = sm_monthDayHourFormat.format(new Date(_latestTime));
     		}else{
     			t_time = sm_yearMonthDayFormat.format(new Date(_latestTime));
     		}
     		
     		latestTime.setText(t_time);
        }
        
        public void setGroupFlag(int _groupFlag){

    		int t_backgroud = 0xd0ededed;
        	if(_groupFlag == fetchMail.GROUP_FLAG_RECV_ATTACH
        		|| _groupFlag == fetchMail.GROUP_FLAG_RECV){
        		t_backgroud = 0xd0ffffff;
        	}
        	
        	background.setBackgroundColor(t_backgroud);
        	groupFlag.setImageResource(getMailFlagImageId(_groupFlag));
        }
        
        public void showDateSpliter(boolean _show,long _time){
        	
        	if(background == null){
        		return;
        	}
        	
        	if(_show){
                
                // this is a bug of RelativeLayout
                // can't set the height in layout xml file
                //
                // please check the android.widget.ListView.setupChild line.1688
                //        		
        		background.setLayoutParams(
        				new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, MAIL_GROUP_ITEM_HEIGHT + MAIL_GROUP_ITEM_SPLITER_HEIGHT));
        		
        		mailDateSpliter.setVisibility(View.VISIBLE);
        		mailDateSpliter.setText(sm_yearMonthDayFormat.format(new Date(_time)));
        	}else{
        		background.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, MAIL_GROUP_ITEM_HEIGHT));
        		mailDateSpliter.setVisibility(View.GONE);
        	}
        }
    }
	
	public static int getMailFlagImageId(int _groupFlag){
		int t_id = R.drawable.mail_list_flag_recv;
    	switch(_groupFlag){
    	case fetchMail.GROUP_FLAG_RECV_ATTACH:
    		t_id = R.drawable.mail_list_flag_recv_attach;
    		break;
    	case fetchMail.GROUP_FLAG_RECV_READ:
    		t_id = R.drawable.mail_list_flag_recv_read;
    		break;
    	case fetchMail.GROUP_FLAG_RECV_READ_ATTACH:
    		t_id = R.drawable.mail_list_flag_recv_read_attach;
    		break;
    	case fetchMail.GROUP_FLAG_SEND_DRAFT:
    		t_id = R.drawable.mail_list_flag_send_draft;
    		break;
    	case fetchMail.GROUP_FLAG_SEND_ERROR:
    		t_id = R.drawable.mail_list_flag_send_error;
    		break;
    	case fetchMail.GROUP_FLAG_SEND_PADDING:
    		t_id = R.drawable.mail_list_flag_send_padding;
    		break;
    	case fetchMail.GROUP_FLAG_SEND_SENDING:
    		t_id = R.drawable.mail_list_flag_send_sending;
    		break;
    	case fetchMail.GROUP_FLAG_SEND_SENT:
    		t_id = R.drawable.mail_list_flag_send_sent;
    		break;
    	}
    	return t_id;
	}

}

