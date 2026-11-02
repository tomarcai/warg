package net.yura.domination.engine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaCompatUtil {

    public static Hashtable asHashtable(Map map) {
        return map instanceof Hashtable ? (Hashtable) map : new Hashtable(map);
    }

    public static Vector asVector(Collection list) {
        return list instanceof Vector ? (Vector) list : new Vector(list);
    }

    public static void setLambda(Object obj, String setter, String lambdaClass, final Runnable lambda) {
        try {
            Class<?> someInterface = Class.forName(lambdaClass);
            Object instance = Proxy.newProxyInstance(someInterface.getClassLoader(), new Class<?>[]{someInterface}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    lambda.run();
                    return null;
                }
            });
            Method setAboutHandler = obj.getClass().getMethod(setter, someInterface);
            setAboutHandler.invoke(obj, instance);
        }
        catch(Throwable th) {
            Logger.getLogger(JavaCompatUtil.class.getName()).log(Level.INFO, "unable to call", th);
        }
    }

    /**
     * TODO move this to use the java 1.5 method
     * @see String#replace(java.lang.CharSequence, java.lang.CharSequence)
     */
    public static String replaceAll(String string, String notregex, String replacement) {
        return string.replaceAll(quote(notregex), quoteReplacement(replacement));
    }

    /**
     * @see java.util.regex.Pattern#quote(java.lang.String)
     */
    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1) {
            return "\\Q" + s + "\\E";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }

    /**
     * @see java.util.regex.Matcher#quoteReplacement(java.lang.String)
     */
    public static String quoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1)) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\');
                sb.append('\\');
            } else if (c == '$') {
                sb.append('\\');
                sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * @see String#join(java.lang.CharSequence, java.lang.CharSequence...)
     */
    public static String join(Collection list, String delimiter) {
        Iterator i = list.iterator();
        if (! i.hasNext()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (;;) {
            Object e = i.next();
            sb.append(e);
            if (!i.hasNext()) {
                return sb.toString();
            }
            sb.append(delimiter);
        }
    }
    
    public static String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for(int i = 0; i < listOfStrings.size() - 1 ; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if(listOfStrings.size() > 0){
            sb.append(listOfStrings.get(listOfStrings.size()-1));
        }

        return sb.toString();
    }
}
