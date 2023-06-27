package top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * 表名&别名 finder
 *
 * @author duanbt
 * @create 2023-04-28 14:22
 **/
class TablesNamesAliasFinder extends TablesNamesFinder {
    /**
     * alias -> table
     */
    private final Map<String, String> aliasTableMap = new HashMap<>();

    public Map<String, String> getAliasTableMap() {
        return aliasTableMap;
    }

    @Override
    public void visit(Table tableName) {
        super.visit(tableName);
        Alias alias = tableName.getAlias();
        if (alias != null) {
            String tableAlias = alias.getName();
            aliasTableMap.put(tableAlias, extractTableName(tableName));
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        super.visit(subSelect);
        Alias alias = subSelect.getAlias();
        if (alias == null) {
            return;
        }
        SelectBody selectBody = subSelect.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                aliasTableMap.put(alias.getName(), extractTableName(table));
            }
        }
    }
}
