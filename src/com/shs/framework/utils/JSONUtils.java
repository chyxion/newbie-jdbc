package com.shs.framework.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @version 0.1
 * @author chyxion
 * @describe: JSON
 * @date created: Jan 18, 2013 5:33:10 PM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by: 
 * @copyright: Shenghang Soft All Right Reserved.
 */
public class JSONUtils {
	public static List<Object> toMapList(JSONArray ja) {
		List<Object> listRtn = new LinkedList<Object>();
		try {
			for (int i = 0; i < ja.length(); ++i) {
				Object o = ja.get(i);
				if (o instanceof JSONArray) {
					listRtn.add(toMapList((JSONArray) o));
				} else if (o instanceof JSONObject) {
					listRtn.add(toMap((JSONObject) o));
				} else {
					listRtn.add(o);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return listRtn;
	}
	public static Map<String, Object> toMap(JSONObject jo) {
		Map<String, Object> mapRtn = new HashMap<String, Object>();
		String[] names = JSONObject.getNames(jo);
		try {
			for (String name : names) {
				Object o = jo.get(name);
				if (o instanceof JSONArray) {
					mapRtn.put(name, toMapList((JSONArray) o));
				} else if (o instanceof JSONObject) {
					mapRtn.put(name, toMap((JSONObject) o));
				} else {
					mapRtn.put(name, o);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return mapRtn;
	}
	public static JSONArray merge(JSONArray ja1, JSONArray ja2) {
		try {
			JSONArray jaResult = new JSONArray();
			for (int i = 0, l = ja1.length(); i < l; ++i) {
			    jaResult.put(ja1.get(i));
			}
			for (int i = 0, l = ja2.length(); i < l; ++i) {
			    jaResult.put(ja2.get(i));
			}
			return jaResult;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static JSONArray concat(JSONArray ja1, JSONArray ja2) {
		try {
			for (int i = 0, l = ja2.length(); i < l; ++i) {
			    ja1.put(ja2.get(i));
			}
			return ja1;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static JSONArray prepend(JSONArray ja, Object obj) {
		return ja = concat(new JSONArray().put(obj), ja);
	}
	public static JSONObject newJSONObject(File file, String encoding) {
		try {
			return new JSONObject(readSource(file));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	public static JSONArray newJSONArray(File file, String encoding) {
		try {
			return new JSONArray(readSource(file));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	// 移除注释
	public static String readSource(File file) {
		try {
			StringBuffer sbContent = new StringBuffer();
			// 注释行，空白行
			Pattern p = Pattern.compile("^(\\s*//|\\s*$)");
			BOMInputStream bis = new BOMInputStream(new FileInputStream(file));
			ByteOrderMark bom = bis.getBOM();
			for (String line : IOUtils.readLines(bis, bom == null ? "utf-8" : bom.getCharsetName())) {
				if (!p.matcher(line).find()) {
					sbContent.append(line);
				}
			}
			return sbContent.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
