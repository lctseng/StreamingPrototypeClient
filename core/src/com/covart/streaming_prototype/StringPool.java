package com.covart.streaming_prototype;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by lctseng on 2017/2/10.
 */

public class StringPool {

    private Map<String, String> fields;
    private ArrayList<String> flashMessages;

    private static StringPool instance = null;
    public static StringPool getInstance(){
        if(instance == null){
            instance = new StringPool();
        }
        return instance;
    }

    StringPool(){
        fields = new LinkedHashMap<String, String>();
        flashMessages = new ArrayList<String>();
    }
    synchronized public void addField(String field, String content){
        fields.put(field, content);
    }

    public String[] getAllText(){
        String[] texts = new String[fields.size() + flashMessages.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : fields.entrySet())
        {
            texts[index] = String.format(Locale.TAIWAN,"%10s:%s", entry.getKey(), entry.getValue());
            index++;

        }
        for(String text : flashMessages){
            texts[index] = text;
            index++;
        }
        return texts;
    }

    public void addFlashMessage(String text){
        flashMessages.add(text);
    }

    public void clear(){
        clearFields();
        clearFlashMessages();
    }

    public void clearFields(){
        fields.clear();
    }

    public void clearFlashMessages(){
        flashMessages.clear();
    }
}
