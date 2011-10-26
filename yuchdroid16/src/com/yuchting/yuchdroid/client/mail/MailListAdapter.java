package com.yuchting.yuchdroid.client.mail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuchting.yuchdroid.client.R;

public class MailListAdapter extends BaseAdapter{
	
	public class ItemHolder{
        View 		background;
        CheckBox	markBut;
        ImageView	attachPic;
        TextView	subject;
        TextView	body;
        TextView	mailAddr;
        TextView	latestTime;
        
        int			preGroupId = -1;
        int			groupId;
        int			nextGroupId = -1;
        
        int			cursorPos;
    }
	
	private int m_cursorIDIndex;
	private int m_cursorBackgroundIndex;
    private int m_cursorMarkIndex;
    private int m_cursorAttachIndex;
    private int m_cursorSubjectIndex;
    private int m_cursorBodyIndex;
    private int m_cursorMailAddrIndex;
    private int m_cursorlatestTimeIndex;
	
	private LayoutInflater m_inflater;   	
	private Cursor			m_mainCursor;
	private Context		m_ctx;
	
	final Calendar 	m_calendar 	= Calendar.getInstance();
	final Date		m_timeDate 	= new Date();
	
	SimpleDateFormat m_yearMonthDayFormat = null;
	SimpleDateFormat m_monthDayHourFormat = null;
	
	
	public MailListAdapter(Context context,Cursor _cursor){
		m_ctx 			= context;
		m_inflater		= LayoutInflater.from(context);
		m_mainCursor	= _cursor;
		
		m_cursorIDIndex			= m_mainCursor.getColumnIndex(MailDbAdapter.KEY_ID);
		m_cursorBackgroundIndex = m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_READ);
		m_cursorMarkIndex		= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MARK);
		m_cursorAttachIndex		= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_HAS_ATTACH);
		m_cursorSubjectIndex	= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_SUBJECT);
		m_cursorBodyIndex		= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_LEATEST_BODY);
		m_cursorMailAddrIndex	= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_ADDR_LIST);
		m_cursorlatestTimeIndex	= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_LEATEST_TIME);

		
		m_yearMonthDayFormat = new SimpleDateFormat("yyyy"+context.getString(R.string.mail_time_year)+
													"MM"+context.getString(R.string.mail_time_month)+
													"dd" + context.getString(R.string.mail_time_day));
		
		m_monthDayHourFormat = new SimpleDateFormat("MM"+context.getString(R.string.mail_time_month)+
													"dd" + context.getString(R.string.mail_time_day) +
													" HH:mm");
	}
	

    public int getCount() {
        return m_mainCursor.getCount();
    }

    public Object getItem(int position) {
         return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {

    	ItemHolder holder;
    	
        if(convertView == null) {
            convertView = m_inflater.inflate(R.layout.mail_list_item,new LinearLayout(m_ctx));
            
            holder = new ItemHolder();
            holder.background	= convertView.findViewById(R.id.mail_item);
           // holder.markBut		= (CheckBox)convertView.findViewById(R.id.mail_mark_btn);
            holder.attachPic	= (ImageView)convertView.findViewById(R.id.mail_attach_pic);
            holder.subject		= (TextView)convertView.findViewById(R.id.mail_subject);
            holder.body			= (TextView)convertView.findViewById(R.id.mail_body);
            holder.mailAddr		= (TextView)convertView.findViewById(R.id.mail_from_to);
            holder.latestTime	= (TextView)convertView.findViewById(R.id.mail_time);

            convertView.setTag(holder);
            
        }else{
            holder = (ItemHolder) convertView.getTag();
      
            holder.preGroupId = -1;
            holder.nextGroupId = -1;            
        }
        
        // fill the group data
        //
        m_mainCursor.moveToPosition(position);
        holder.groupId = m_mainCursor.getInt(m_cursorIDIndex);
        if(position != 0){
        	m_mainCursor.moveToPosition(position - 1);
        	holder.nextGroupId = m_mainCursor.getInt(m_cursorIDIndex);
        }
        
        if(position < m_mainCursor.getCount() - 1){
        	m_mainCursor.moveToPosition(position + 1);
        	holder.preGroupId = m_mainCursor.getInt(m_cursorIDIndex);
        }
        
        // fill the mail text
        //
        m_mainCursor.moveToPosition(position);
        
        holder.cursorPos = position;
        holder.background.setBackgroundColor(m_mainCursor.getInt(m_cursorBackgroundIndex) == 1?0xf0f0f0:0xffffff);
       // holder.markBut.setSelected(m_mainCursor.getInt(m_cursorMarkIndex) == 1?true:false);
        holder.attachPic.setVisibility(m_mainCursor.getInt(m_cursorAttachIndex) == 1?View.VISIBLE:View.INVISIBLE);
        holder.subject.setText(m_mainCursor.getString(m_cursorSubjectIndex));
        holder.body.setText(m_mainCursor.getString(m_cursorBodyIndex));
        
    	// address
		//
		holder.mailAddr.setText(getShortAddrList(
							fetchMail.parseAddressList(m_mainCursor.getString(m_cursorMailAddrIndex).split(";"))));
		
		// time
		//
		// mail least time
		//
		m_calendar.setTimeInMillis(System.currentTimeMillis());
		final int t_currYear 		= m_calendar.get(Calendar.YEAR);
		
		final long t_latestTime = m_mainCursor.getLong(m_cursorlatestTimeIndex);
		m_calendar.setTimeInMillis(t_latestTime);
		final int t_year 		= m_calendar.get(Calendar.YEAR);

		
		String t_time;
		if(t_year == t_currYear){
			t_time = m_monthDayHourFormat.format(new Date(t_latestTime));
		}else{
			t_time = m_yearMonthDayFormat.format(new Date(t_latestTime));
		}
		
		holder.latestTime.setText(t_time);
		
        return convertView;
    }
    
    public static String getShortAddrList(Address[] _list){
    	
    	StringBuffer t_display = new StringBuffer();
		int t_num = 0;
		for(int i = _list.length - 1;i >= 0 && t_num < 3;i--,t_num++){
			t_display.append(_list[i].m_name).append(fetchMail.fsm_vectStringSpliter);
		}
		t_display.append("(").append(_list.length).append(")");
		
		return t_display.toString();
    }
}

