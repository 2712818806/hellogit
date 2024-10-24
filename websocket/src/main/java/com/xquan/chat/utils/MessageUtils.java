package com.xquan.chat.utils;

import com.alibaba.fastjson.JSON;
import com.xquan.chat.pojo.ResultMessage;

public class MessageUtils {
    public static String getMEssage(boolean isSystemMessage, String fromName, Object message){
        ResultMessage result = new ResultMessage();
        result.setSystem(isSystemMessage);
        result.setMessage(message);
        if (fromName!=null) {
            result.setFromName(fromName);
        }
        return JSON.toJSONString(result);
    }
}
