package me.chyxion.dao.utils;

import java.util.Iterator;
import java.util.Collection;

/**
 * @version 0.0.1
 * @since 0.0.1
 * @author Shaun Chyxion
 */
public class StringUtils {

	public static String join(Collection<?> collection, String separator) {

        // handle null, zero and one elements before building a buffer
        if (collection == null) {
            return null;
        }
        Iterator<?> it = collection.iterator();
		if (!it.hasNext()) {
            return "";
        }
        Object first = it.next();
        if (!it.hasNext()) {
            return first instanceof String ? 
            	(String) first : first != null ? 
            		first.toString() : "";
        }

        // two or more elements
        StringBuilder sbResult = new StringBuilder(256);
        if (first != null) {
            sbResult.append(first);
        }

        while (it.hasNext()) {
            if (separator != null) {
                sbResult.append(separator);
            }
            Object obj = it.next();
            if (obj != null) {
                sbResult.append(obj);
            }
        }
        return sbResult.toString();
    }

	/**
	 * @param sql
	 * @param string
	 * @return
	 */
    public static int indexOfIgnoreCase(String str, String searchStr) {
    	int indexNotFound = -1;
        if (str == null || searchStr == null) {
            return indexNotFound;
        }
        int startPos = 0;
        int endLimit = str.length() - searchStr.length() + 1;
        if (startPos > endLimit) {
            return indexNotFound;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }
        for (int i = startPos; i < endLimit; i++) {
            if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return indexNotFound;
    }
}
