package top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 可加密的sql参数信息
 *
 * @author duanbt
 * @create 2023-04-28 11:13
 **/
@Getter
@Setter
@ToString
public class EncryptableSqlParam {
    /**
     * 参数序号，从1开始
     */
    private int jdbcParamIndex;

    private String columnName;

    private String tableName;

    private String cipherInstance;

    public EncryptableSqlParam(int jdbcParamIndex, String columnName, String tableName) {
        this.jdbcParamIndex = jdbcParamIndex;
        this.columnName = columnName;
        this.tableName = tableName;
    }
}
