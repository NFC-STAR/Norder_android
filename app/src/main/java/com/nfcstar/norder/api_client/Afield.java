package com.nfcstar.norder.api_client;

import java.util.HashMap;
import java.util.Map;

public class Afield {
    Map<String, Object> fields;

    public Afield() {
        fields = new HashMap<>();
    }
    public void setF(String s,Object o){
        fields.put(s,o);
    }

    public Map<String, Object> getF() {
        return fields;
    }
}
