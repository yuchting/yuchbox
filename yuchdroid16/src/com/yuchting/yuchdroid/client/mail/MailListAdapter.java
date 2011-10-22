package com.yuchting.yuchdroid.client.mail;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuchting.yuchdroid.client.R;

public class MailListAdapter extends BaseAdapter{
	
	private class ItemHolder{
        View 		background;
        CheckBox	markBut;
        ImageView	attachPic;
        TextView	subject;
        TextView	body;
        TextView	mailAddr;
        TextView	latestTime;
    }
	
	private int m_cursorBackgroundIndex;
    private int m_cursorMarkIndex;
    private int m_cursorAttachIndex;
    private int m_cursorSubjectIndex;
    private int m_cursorBodyIndex;
    private int m_cursorMailAddrIndex;
    private int m_cursorlatestTimeIndex;
	
	private LayoutInflater m_inflater;   	
	private Cursor			m_mainCursor;
	
	public MailListAdapter(Context context,Cursor _cursor){
		m_inflater = LayoutInflater.from(context);
		m_mainCursor = _cursor;
		
		m_cursorBackgroundIndex = m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_READ);
		m_cursorMarkIndex		= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_MARK);
		m_cursorAttachIndex		= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_HAS_ATTACH);
		m_cursorSubjectIndex	= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_SUBJECT);
		m_cursorBodyIndex		= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_LEATEST_BODY);
		m_cursorMailAddrIndex	= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_ADDR_LIST);
		m_cursorlatestTimeIndex	= m_mainCursor.getColumnIndex(MailDbAdapter.GROUP_ATTR_LEATEST_TIME);
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
            convertView = m_inflater.inflate(R.layout.mail_list_item, null);

            holder = new ItemHolder();
            holder.background	= convertView.findViewById(R.id.mail_item);
            holder.markBut		= (CheckBox)convertView.findViewById(R.id.mail_mark_btn);
            holder.attachPic	= (ImageView)convertView.findViewById(R.id.mail_attach_pic);
            holder.subject		= (TextView)convertView.findViewById(R.id.mail_subject);
            holder.body			= (TextView)convertView.findViewById(R.id.mail_body);
            holder.mailAddr		= (TextView)convertView.findViewById(R.id.mail_from_to);
            holder.latestTime	= (TextView)convertView.findViewById(R.id.mail_time);

            convertView.setTag(holder);
            
        }else{
            holder = (ItemHolder) convertView.getTag();
        }
        
        m_mainCursor.moveToPosition(position);

        holder.background.setBackgroundColor(m_mainCursor.getInt(m_cursorBackgroundIndex) == 1?0xf0f0f0:0xffffff);
        holder.markBut.setSelected(m_mainCursor.getInt(m_cursorMarkIndex) == 1?true:false);
        holder.attachPic.setVisibility(m_mainCursor.getInt(m_cursorAttachIndex) == 1?View.VISIBLE:View.INVISIBLE);
        holder.subject.setText(m_mainCursor.getString(m_cursorSubjectIndex));
        holder.body.setText(m_mainCursor.getString(m_cursorBodyIndex));
        
    	// address
		//
		Address[] t_addr = fetchMail.parseAddressList(m_mainCursor.getString(m_cursorMailAddrIndex).split(";"));
		StringBuffer t_display = new StringBuffer();
		int t_num = 0;
		for(int i = t_addr.length - 1;i >= 0 && t_num < 3;i--,t_num++){
			t_display.append(t_addr[i].m_name).append(fetchMail.fsm_vectStringSpliter);
		}
		t_display.append("(").append(t_addr.length).append(")");
		holder.mailAddr.setText(t_display.toString());
		
		// time
		//
		// mail least time
		//
		Date t_current = new Date();
		Date t_leastDate = new Date(m_mainCursor.getLong(m_cursorlatestTimeIndex));
		String t_time;
		if(t_leastDate.getYear() == t_current.getYear()
		&& t_leastDate.getMonth() == t_current.getMonth()
		&& t_leastDate.getDay() == t_current.getDay()){
			t_time = t_leastDate.getHours() + ":" + t_leastDate.getMinutes();
		}else if(t_leastDate.getYear() == t_current.getYear()){
			t_time = t_leastDate.getMonth() + "-" + t_leastDate.getDay();
		}else {
			t_time = t_leastDate.getYear() + "-" + t_leastDate.getMonth() + "-" + t_leastDate.getDay();
		}
		
		holder.latestTime.setText(t_time);
		
        return convertView;
    }
}

