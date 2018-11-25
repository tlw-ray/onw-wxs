package com.xskr.onw.wxs.intf;

import java.util.Date;

public interface IMessage {
    String getMessage();
    int getCommand();
    Object getData();
    Date getDate();
}
