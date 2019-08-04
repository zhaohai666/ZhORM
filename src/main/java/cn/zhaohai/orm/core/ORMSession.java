package cn.zhaohai.orm.core;

import cn.zhaohai.orm.constant.NumberConstant;
import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ORMSession {
    private static final Logger logger = Logger.getLogger(ORMSession.class);

    private Connection connection;

    private List<Mapper> mapperList;

    /**
     * 保存数据
     *
     * @param entity 保存的数据实体类
     */
    public void save(Object entity) throws Exception{
        // 1. 从ORMConfig中获的保存有映射信息的集合 mapperList
        // 2. 遍历集合，从集合中找到和entity参数相对应的mapper对象
        StringBuilder into = new StringBuilder();
        StringBuilder value = new StringBuilder();
        String insertSQL = "";
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(entity.getClass().getName())) {
                String tableName = mapper.getTableName();
                String insertSqlInto = "insert into " + tableName + "(";
                String insertSqlValue = " ) values (";
                into.append(insertSqlInto);
                value.append(insertSqlValue);
                // 3. 得到当前对象所属类中的所以属性
                Field[] fields = entity.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(Boolean.TRUE);
                    // 4. 遍历过程中根据属性得到它的值
                    String columnName = mapper.getPropMapper().get(field.getName());
                    // 5. 遍历过程中根据属性得到它的值
                    String columnValue = field.get(entity).toString();
                    // 6. 拼接SQL语句
                    into.append(columnName).append(",");
                    value.append("'").append(columnValue).append("',");
                }
                insertSQL = into.toString().substring(NumberConstant.ZERO_CONSTANT, into.toString().length() - 1) + value.toString().substring(NumberConstant.ZERO_CONSTANT, value.toString().length() - 1) + ")";
                break;
            }
        }
        logger.info("ORMSession | save : " + insertSQL);
        // 7. 通过JDBC发送并执行SQL
        PreparedStatement statement = connection.prepareStatement(insertSQL);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * 根据主键进行数据删除 delete from 表名 where 主键 = 值
     *
     * @param entity 保存的数据实体类
     */
    public void delete(Object entity) throws Exception{
        String delSql = "delete from ";
        // 1. 从ORMConfig中获得保存有映射信息的集合
        // 2. 遍历集合，从集合中找到和entity参数相对应的mapper对象
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(entity.getClass().getName())) {
                // 3. 得到我们想要的mapper对象，并得到表名
                String tableName = mapper.getTableName();
                delSql += tableName + " where ";
                // 4. 得到主键的字段名和属性名
                Object[] idKeys = mapper.getIdMapper().keySet().toArray();
                Object[] idValues = mapper.getIdMapper().values().toArray();

                // 5. 得到主键的值
                Field field = entity.getClass().getDeclaredField(idKeys[0].toString());
                field.setAccessible(Boolean.TRUE);
                String idVal = field.get(entity).toString();
                // 6. 拼接SQL
                delSql += idValues[0].toString() + " = " + idVal;
                break;
            }
        }
        logger.info("ORMSession | save : " + delSql);
        // 7. 通过JDBC发送并执行SQL
        PreparedStatement statement = connection.prepareStatement(delSql);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * 根据主键进行查询 select * from 表名 where 主键字段 = 值
     *
     * @param clz 类的class对象
     * @param id 主键值
     * @return
     */
    public Object findOne(Class clz, Object id) throws Exception{
        // 1。 从ORMConfig中的到存有映射信息的集合
        String querySQL = "select * from ";
        // 2。 遍历集合拿到我们想要的mapper对象
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(clz.getName())) {
                // 3. 获得表名
                String tableName = mapper.getTableName();
                // 4. 获得主键字段名
                Object[] idValues = mapper.getIdMapper().values().toArray();
                // 5. 拼接SQL
                querySQL += tableName + " where " + idValues[0].toString() + " = " + id;
                break;
            }
        }
        logger.info("ORMSession | save : " + querySQL);
        // 6. 通过jdbc发送并执行sql，得到结果集
        PreparedStatement statement = connection.prepareStatement(querySQL);
        ResultSet resultSet = statement.executeQuery();
        // 7. 封装结果集，返回对象
        if (!resultSet.next()) {
            return null;
        }
        // 查询到一行数据
        // 8。 创建一个对象，目前属性的值都是初始值
        Object object = clz.newInstance();
        // 9. 遍历mapperList集合找到我们想要的mapper对象
        for (Mapper mapper : mapperList) {
            if (mapper.getClassName().equals(clz.getName())) {
                // 10。得到存有属性 - 字段的映射信息
                Map<String, String> parpMap = mapper.getPropMapper();
                // 11。遍历集合分别拿到属性名和字段名
                // prop就是属性名
                for (String prop : parpMap.keySet()) {
                    // column就是和属性对应的字段名
                    String column = parpMap.get(prop);
                    Field field = clz.getDeclaredField(prop);
                    field.setAccessible(Boolean.TRUE);
                    // 先获取查询结果的数据，再赋值给对象的属性
                    field.set(object, resultSet.getObject(column));
                }
                break;
            }
        }
        // 12。释放资源
        statement.close();
        resultSet.close();
        // 13. 返回查询出来的对象
        return object;
    }

    /**
     * 关闭连接，释放资源
     *
     * @throws Exception
     */
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
