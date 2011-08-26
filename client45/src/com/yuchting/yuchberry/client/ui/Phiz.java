package com.yuchting.yuchberry.client.ui;

import net.rim.device.api.ui.Graphics;

public class Phiz extends ImageButton{
	public final static int		fsm_phizSize = 22;
	
	public Phiz(ImageUnit _image,ImageSets _imageSets){
        super( "",_image,_image,_imageSets,0);
    }
	
	public void focusPaint(Graphics g,boolean focus){
		super.focusPaint(g,focus);
		
		if(focus){
			int t_color = g.getColor();
			try{
				g.setColor(0x4694ea);
				g.drawRect(0,0,m_image.getWidth(),m_image.getHeight());
			}finally{
				g.setColor(t_color);
			}
		}
	}
	
	public String getPhizName(){
		return m_image.getName();
	}
}
