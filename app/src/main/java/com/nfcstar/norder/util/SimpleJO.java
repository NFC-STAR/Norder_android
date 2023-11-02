package com.nfcstar.norder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SimpleJO {
    JsonObject jsonObject;
    String tag;

    public SimpleJO(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        tag = "";
    }

    public SimpleJO(JsonObject jsonObject, String tag) {
        this.jsonObject = jsonObject;
        this.tag = tag;
    }

    public String getAsString(String name) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
           // Log.d("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return "";
        } else {
            return jsonObject.get(name).getAsString();
        }
    }

    public String getAsString(String name, String def) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return def;
        } else {
            return jsonObject.get(name).getAsString();
        }
    }

    public boolean getAsBoolean(String name) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return false;
        } else {
            return jsonObject.get(name).getAsBoolean();
        }
    }

    public boolean getAsBoolean(String name, boolean def) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return def;
        } else {
            return jsonObject.get(name).getAsBoolean();
        }
    }
    public int getAsInt(String name) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return 0;
        } else {
            return jsonObject.get(name).getAsInt();
        }
    }
    public int getAsInt(String name, int def) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return def;
        } else {
            return jsonObject.get(name).getAsInt();
        }
    }

    public long getAsLong(String name, int def) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            return def;
        } else {
            return jsonObject.get(name).getAsLong();
        }

    }

    public float getAsFloat(String name, int def) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return def;
        } else {
            return jsonObject.get(name).getAsFloat();
        }
    }

    public JsonArray getAsJsonArray(String name) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            //a Log.e("Json_Exception", tag + " = " + name + "의 값이 없다.");
            return new JsonArray();
        } else {
            return jsonObject.get(name).getAsJsonArray();
        }

    }

    public JsonObject getASJsonObject(String name) {
        if (jsonObject == null || jsonObject.get(name) == null || jsonObject.get(name).isJsonNull()) {
            return new JsonObject();
        } else {
            return jsonObject.get(name).getAsJsonObject();
        }
    }

}
