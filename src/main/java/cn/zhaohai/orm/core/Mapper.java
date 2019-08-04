package cn.zhaohai.orm.core;

import com.google.common.collect.Maps;
import lombok.Builder;

import java.util.Map;

/**
 * 用来封装和存储映射信息
 */
@Builder
public class Mapper {

    /**
     * 类名
     */
    private String className;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 主键信息
     */
    private Map<String, String> idMapper = Maps.newHashMap();

    /**
     * 普通的属性和字段信息
     */
    private Map<String, String> propMapper = Maps.newHashMap();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getIdMapper() {
        return idMapper;
    }

    public void setIdMapper(Map<String, String> idMapper) {
        this.idMapper = idMapper;
    }

    public Map<String, String> getPropMapper() {
        return propMapper;
    }

    public void setPropMapper(Map<String, String> propMapper) {
        this.propMapper = propMapper;
    }

    @Override
    public String toString() {
        return "Mapper{" +
                "className='" + className + '\'' +
                ", tableName='" + tableName + '\'' +
                ", idMapper=" + idMapper +
                ", propMapper=" + propMapper +
                '}';
    }
}
