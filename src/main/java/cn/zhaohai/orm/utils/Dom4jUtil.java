package cn.zhaohai.orm.utils;

import cn.zhaohai.orm.constant.NumberConstant;
import cn.zhaohai.orm.constant.ZhOrmConstant;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于dom4j的工具类
 */

public class Dom4jUtil {
    private static final Logger logger = Logger.getLogger(Dom4jUtil.class);

    /**
     * 通过文件的路径获取xml的document对象
     *
     * @param path 文件的路径
     * @return 返回的文档对象
     */
    public static Document getXMLByFilePath(String path) {
        if (path == null) {
            return null;
        }
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(new File(path));
        } catch (Exception e) {
            logger.error("Dom4jUtil | getXMLByFilePath error : e : {}", e);
        }
        return document;
    }

    /**
     * 获取某文档中的某元素内某属性的值和元素的文本信息
     *
     * @param document xml文档对象
     * @param elementName 元素名
     * @param attrName 属性名
     * @return 返回一个Map集合
     */
    public static Map<String, String> elements2Map(Document document, String elementName, String attrName) {
        List<Element> elements = document.getRootElement().elements(elementName);
        Map<String, String> propConfig = Maps.newHashMap();
        for (Element element1 : elements) {
            String key = element1.attribute(attrName).getValue();
            String value = element1.getTextTrim();
            propConfig.put(key, value);
        }
        return propConfig;
    }

    /**
     * 针对mapper.xml文件，获得映射信息并存到Map集合中
     *
     * @param document xml文档对象
     * @return 返回一个Map集合
     */
    public static Map<String, String> elements2Map(Document document) {
        Element classElement = document.getRootElement().element(ZhOrmConstant.CLASS_CONSTANT);
        Map<String, String> mapping = getRootElementMap(document);
        List<Element> propELements = classElement.elements(ZhOrmConstant.PROPERTY_CONSTANT);
        for (Element element : propELements) {
            String propKey = element.attribute(ZhOrmConstant.NAME_CONSTANT).getValue();
            String propValue = element.attribute(ZhOrmConstant.COLUMN_CONSTANT).getValue();
            mapping.put(propKey, propValue);
        }
        return mapping;
    }

    /**
     * 针对mapper.xml文件，获得主键的映射信息并存到Map集合中
     *
     * @param document xml文档对象
     * @return 返回一个Map集合
     */
    public static Map<String, String> elementsId2Map(Document document) {
        Map<String, String> mapping = getRootElementMap(document);
        return mapping;
    }

    /**
     * 获得某文档中某元素内某属性的值
     *
     * @param document xml文档对象
     * @param elementName 元素名
     * @param attrName 属性名
     * @return 返回一个Set集合
     */
    public static Set<String> elements2Set(Document document, String elementName, String attrName) {
        List<Element> elements = document.getRootElement().elements(elementName);
        Set<String> mappingSet = Sets.newHashSet();
        for (Element element : elements) {
            String value = element.attribute(attrName).getValue();
            mappingSet.add(value);
        }
        return mappingSet;
    }

    /**
     * 获得某文档中某元素内某属性的值
     *
     * @param document xml文档对象
     * @param elementName 元素名
     * @param attrName 属性名
     * @return 返回一个set集合
     */
    public static String getPropValue(Document document, String elementName, String attrName) {
        Element element = (Element)document.getRootElement().elements(elementName).get(NumberConstant.ZERO_CONSTANT);
        return element.attribute(attrName).getValue();
    }

    private static Map<String, String> getRootElementMap(Document document) {
        Element classElement = document.getRootElement().element(ZhOrmConstant.CLASS_CONSTANT);
        Map<String, String> mapping = Maps.newHashMap();
        Element idElement = classElement.element(ZhOrmConstant.ID_CONSTANT);
        String idKey = idElement.attribute(ZhOrmConstant.NAME_CONSTANT).getValue();
        String idValue = idElement.attribute(ZhOrmConstant.COLUMN_CONSTANT).getValue();
        mapping.put(idKey, idValue);
        return mapping;
    }
}
