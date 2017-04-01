package com.net.lnk.netty;

import java.util.Properties;
import java.util.Set;

import com.luhuiguo.chinese.ChineseUtils;
import com.luhuiguo.chinese.pinyin.PinyinFormat;

/**
 * Hello world!
 */
public class UtilsTest {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		System.out.println(ChineseUtils.toSimplified("張偉偉"));
		System.out.println(ChineseUtils.toTraditional("张伟伟"));
		System.out.println(ChineseUtils.toPinyin("张伟伟", PinyinFormat.TONELESS_PINYIN_FORMAT));

		System.out.println("---" + System.getProperty("line.separator") + "---");
		Properties props = System.getProperties();
		Set<Object> propsKey = props.keySet();
		for (Object o : propsKey) {
			System.out.println((String) o + "=" + (String) props.get(o));
		}
	}
}
