package com.covart.streaming_prototype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by lctseng on 2017/2/10.
 * NTU COV-ART Lab, for NCP project
 */



public class StringPool {

    private Map<String, String> fields;
    private ArrayList<String> flashMessages;

    private static StringPool instance = new StringPool();

    public static StringPool getInstance(){
        return instance;
    }

    public static void addField(String field, String content){
        instance.fields.put(field, content);
    }

    public static String[] getAllText(){
        String[] texts = new String[instance.fields.size() + instance.flashMessages.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : instance.fields.entrySet())
        {
            texts[index] = String.format(Locale.TAIWAN,"%s : %s", entry.getKey(), entry.getValue());
            index++;

        }
        for(String text : instance.flashMessages){
            texts[index] = text;
            index++;
        }
        return texts;
    }

    public static void addFlashMessage(String text){
        instance.flashMessages.add(text);
    }

    public static void clear(){
        clearFields();
        clearFlashMessages();
    }

    public static void clearFields(){
        instance.fields.clear();
    }

    public static void clearFlashMessages(){
        instance.flashMessages.clear();
    }

    private StringPool(){
        fields = Collections.synchronizedMap(new LinkedHashMap<String, String>());
        flashMessages = new ArrayList<String>();
    }
}
