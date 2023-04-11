package com.youhogeon.credits.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Http {
    
    public static String encodeURIComponent(String s) {
        String result = null;
        try {
            result = URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }

    public static String parseScriptContent(String URL) {
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements scriptElements = doc.getElementsByTag("script");
            String script = scriptElements.stream().filter(e -> !e.hasAttr("src")).map(Element::toString).collect(Collectors.joining());

            return script;
        } catch (IOException e) {
            return null;
        }
    }

}
