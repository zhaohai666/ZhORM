package cn.zhaohai.orm.core;

import cn.zhaohai.orm.constant.ZhOrmConstant;
import cn.zhaohai.orm.utils.AnnotationUtil;
import cn.zhaohai.orm.utils.Dom4jUtil;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.dom4j.Document;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 用来解析并封装框架的核心配置文件中的数据
 */
public class ORMConfig {
    private static final Logger logger = Logger.getLogger(ORMConfig.class);

    /**
     * classPath路径
     */
    private static String classPath;

    /**
     * 核心配置文件
     */
    private static File cfgFile;

    /**
     * <property> 标签中的数据
     */
    private static Map<String, String> propConfig;

    /**
     * 映射配置文件路径
     */
    private static Set<String> mappingSet;

    /**
     * 实体类
     */
    private static Set<String> entitySet;

    /**
     * 映射信息
     */
    private static List<Mapper> mapperList;

    static {
        // 得到的classpath路径
        classPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(ZhOrmConstant.SPRIT_CONSTANT)).getPath();
        // 针对中文路径进行转码
        try {
            classPath = URLDecoder.decode(classPath, ZhOrmConstant.UTF_8_CONSTANT);
        } catch (UnsupportedEncodingException e) {
            logger.error("ORMConfig | static error : {}", e);
        }
        // 得到核心配置文件
        cfgFile = new File(classPath + ZhOrmConstant.ZHORMCONFIG_CONSTANT);
        if (cfgFile.exists()) {
            // 解析核心配置文件中的数据
            Document document = Dom4jUtil.getXMLByFilePath(cfgFile.getPath());
            propConfig = Dom4jUtil.elements2Map(document, ZhOrmConstant.PROPERTY_CONSTANT, ZhOrmConstant.NAME_CONSTANT);
            mappingSet = Dom4jUtil.elements2Set(document, ZhOrmConstant.MAPPING_CONSTANT, ZhOrmConstant.RESOURCE_CONSTANT);
            entitySet = Dom4jUtil.elements2Set(document, ZhOrmConstant.ENTITY_CONSTANT, ZhOrmConstant.PACKAGE_CONSTANT);
        } else {
            cfgFile = null;
            logger.error("未找到核心配置文件ZhORMCfg.xml");
        }
    }

    public ORMSession buildORMSession() {
        // 1. 连接数据库
        Connection connection = getConnection();
        // 2. 得到映射数据
        try {
            getMapping();
        } catch (Exception e) {
            logger.error("ORMConfig | buildORMSession error : {}", e);
        }
        // 3. 创建ORMSession对象
        return new ORMSession(connection, mapperList);
    }

    private Connection getConnection() {
        String url = propConfig.get(ZhOrmConstant.CONNECTION_URL_CONSTANT);
        String driverCLass = propConfig.get(ZhOrmConstant.CONNECTION_DRIVER_CLASS_CONSTANT);
        String username = propConfig.get(ZhOrmConstant.CONNECTION_USERNAME_CONSTANT);
        String password = propConfig.get(ZhOrmConstant.CONNECTION_PASSWORD_CONSTANT);
        Connection connection = null;
        try {
            Class.forName(driverCLass);
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("ORMConfig | getConnection error : {}", e);
        }
        return connection;
    }

    private void getMapping() throws Exception{
        mapperList = Lists.newArrayList();
        // 1. 解析xxxmapper.xml文件拿到映射数据
        for (String xmlPath : mappingSet) {
            Document document = Dom4jUtil.getXMLByFilePath(classPath + xmlPath);
            String className = Dom4jUtil.getPropValue(document, ZhOrmConstant.CLASS_CONSTANT, ZhOrmConstant.NAME_CONSTANT);
            String tableName = Dom4jUtil.getPropValue(document, ZhOrmConstant.CLASS_CONSTANT, ZhOrmConstant.TABLE_CONSTANT);
            Map<String, String> idKeyValue = Dom4jUtil.elementsId2Map(document);
            Map<String, String> mapping = Dom4jUtil.elements2Map(document);
            mapperList.add(Mapper.builder().tableName(tableName).className(className).idMapper(idKeyValue).propMapper(mapping).build());
        }
        getAnnotationList(mapperList);

    }

    private void getAnnotationList(List<Mapper> mapperList) throws ClassNotFoundException {
        // 2. 解析实体类中的注解拿到映射数据
        for (String packagePath : entitySet) {
            Set<String> nameSet = AnnotationUtil.getClassNameByPackage(packagePath);
            for (String name : nameSet) {
                Class<?> clz = Class.forName(name);
                String cLassName = AnnotationUtil.getCLassName(clz);
                String tableName = AnnotationUtil.getTableName(clz);
                Map<String, String> idMapper = AnnotationUtil.getIdMapper(clz);
                Map<String, String> propMapping = AnnotationUtil.getPropMapping(clz);
                mapperList.add(Mapper.builder().tableName(tableName).className(cLassName).idMapper(idMapper).propMapper(propMapping).build());
            }
        }
    }
}
