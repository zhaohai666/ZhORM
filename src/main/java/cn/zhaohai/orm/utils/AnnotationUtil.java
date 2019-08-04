package cn.zhaohai.orm.utils;

import cn.zhaohai.orm.annotation.ORMColumn;
import cn.zhaohai.orm.annotation.ORMTable;
import cn.zhaohai.orm.annotation.ORMid;
import cn.zhaohai.orm.constant.NumberConstant;
import cn.zhaohai.orm.constant.ZhOrmConstant;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 使用反射解析实体类中注解的工具类
 */
public class AnnotationUtil {
    private static final Logger logger = Logger.getLogger(AnnotationUtil.class);

    /**
     * 得到类名
     *
     * @param clz 类的class对象
     * @return 返回类名
     */
    public static String getCLassName(Class clz) {
        return clz.getName();
    }

    /**
     * 得到ORMTable注解中的表名
     *
     * @param clz 类的class对象
     * @return 返回表名
     */
    public static String getTableName(Class clz) {
        if (clz.isAnnotationPresent(ORMTable.class)) {
            ORMTable ormTable = (ORMTable) clz.getAnnotation(ORMTable.class);
            return ormTable.name();
        }
        logger.error("缺少ORMTable注解");
        return null;
    }

    /**
     * 得到主键属性和对应的字段
     *
     * @param clz 类的class对象
     * @return 返回字段集合
     */
    public static Map<String, String> getIdMapper(Class clz) {
        boolean flag = true;
        Map<String, String> map = Maps.newHashMap();
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ORMid.class)) {
                flag = false;
                if (field.isAnnotationPresent(ORMColumn.class)) {
                    String fieldName = field.getName();
                    ORMColumn ormColumn = field.getAnnotation(ORMColumn.class);
                    String columnName = ormColumn.name();
                    map.put(fieldName, columnName);
                    break;
                } else {
                    logger.error("缺少ORMColumn注解");
                }
            }
        }
        if (flag) {
            logger.error("缺少ORMId注解");
        }
        return map;
    }

    /**
     * 得到类中所有属性和对应的字段
     *
     * @param clz 类的class对象
     * @return 返回属性对应的字段集合
     */
    public static Map<String, String> getPropMapping(Class clz) {
        Map<String, String> map = Maps.newHashMap();
        map.putAll(getIdMapper(clz));
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ORMColumn.class)) {
                ORMColumn ormColumn = field.getAnnotation(ORMColumn.class);
                String fieldName = field.getName();
                String columnName = ormColumn.name();
                map.put(fieldName, columnName);
            }
        }
        return map;
    }

    /**
     * 获得某包下面的所有类名
     *
     * @param packagePath 包路径
     * @return 返回类名
     */
    public static Set<String> getClassNameByPackage(String packagePath) {
        Set<String> names = Sets.newHashSet();
        String packageFile = packagePath.replace(ZhOrmConstant.DOT_CONSTANT, ZhOrmConstant.SPRIT_CONSTANT);
        String classPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(ZhOrmConstant.EMPTY_CONSTANT)).getPath();
        if (classPath == null) {
            classPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(ZhOrmConstant.SPRIT_CONSTANT)).getPath();
        }
        try {
            classPath = URLDecoder.decode(classPath, ZhOrmConstant.UTF_8_CONSTANT);
        } catch (UnsupportedEncodingException e) {
            logger.error("AnnotationUtil | getClassNameByPackage error : {}", e);
        }
        File dir = new File(classPath + packageFile);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String name = file.getName();
                if (file.isFile() && name.endsWith(ZhOrmConstant.DOT_CLASS_CONSTANT)) {
                    name = packagePath + ZhOrmConstant.DOT_CONSTANT + name.substring(NumberConstant.ZERO_CONSTANT, name.lastIndexOf(ZhOrmConstant.DOT_CONSTANT));
                    names.add(name);
                }
            }
        } else {
            logger.error("包路径不存在");
        }
        return names;
    }

}
