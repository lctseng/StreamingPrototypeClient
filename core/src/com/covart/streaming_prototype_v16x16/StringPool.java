package com.covart.streaming_prototype_v16x16;

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

    private static Object lock = new Object();
    private static StringPool instance = new StringPool();

    public static StringPool getInstance(){
        return instance;
    }

    public static void addField(String field, String content){
        synchronized (lock){
            instance.fields.put(field, content);
        }
    }

    public static void removeField(String field){
        synchronized (lock) {
            instance.fields.remove(field);
        }
    }

    public static String[] getAllText(){
        synchronized (lock) {
            String[] texts = new String[instance.fields.size() + instance.flashMessages.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : instance.fields.entrySet()) {
                texts[index] = String.format(Locale.TAIWAN, "%s : %s", entry.getKey(), entry.getValue());
                index++;

            }
            for (String text : instance.flashMessages) {
                texts[index] = text;
                index++;
            }
            return texts;
        }
    }

    public static void addFlashMessage(String text){
        synchronized (lock) {
            instance.flashMessages.add(text);
        }
    }

    public static void clear(){
        clearFields();
        clearFlashMessages();
    }

    public static void clearFields(){
        synchronized (lock) {
            instance.fields.clear();
        }
    }

    public static void clearFlashMessages(){
        synchronized (lock) {
            instance.flashMessages.clear();
        }
    }

    private StringPool(){
        synchronized (lock) {
            fields = Collections.synchronizedMap(new LinkedHashMap<String, String>());
            flashMessages = new ArrayList<String>();
        }
    }
}
