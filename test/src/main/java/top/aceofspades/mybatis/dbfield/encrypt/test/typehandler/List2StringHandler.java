package top.aceofspades.mybatis.dbfield.encrypt.test.typehandler;

import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author duanbt
 * @create 2023-06-26 10:00
 **/
@MappedJdbcTypes(JdbcType.VARCHAR)  //数据库类型
@MappedTypes(List.class)
public class List2StringHandler implements TypeHandler<List<String>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, String.join(",", parameter));
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        return StrUtil.split(rs.getString(columnName), ",");
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return StrUtil.split(rs.getString(columnIndex), ",");
    }

    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return StrUtil.split(cs.getString(columnIndex), ",");
    }
}
