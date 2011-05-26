package com.mime.qweibo;


public class QParameter implements java.io.Serializable, Comparable {
	
	private static final long serialVersionUID = 5164951358145483848L;
	String mName;
	String mValue;
	
	public QParameter(String name, String value) {
		this.mName = name;
		this.mValue = value;
	}

	@Override
	public boolean equals(Object arg0) {
		if (null == arg0) {
            return false;
        }
        if (this == arg0) {
            return true;
        }
        if (arg0 instanceof QParameter) {
            QParameter param = (QParameter) arg0;
            
            return this.mName.equals(param.mName) &&
                this.mValue.equals(param.mValue);
        }
        return false;
	}
	
	public int compareTo(Object o) {
        int compared;
        QParameter param = (QParameter) o;
        compared = mName.compareTo(param.mName);
        if (0 == compared) {
            compared = mValue.compareTo(param.mValue);
        }
        return compared;
    }
}
