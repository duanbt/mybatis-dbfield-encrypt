package top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.*;

/**
 * sql参数解析器
 * <p>
 * 解析出可加密的?参数位置
 *
 * @author duanbt
 * @create 2023-04-27 16:19
 **/
public class SqlExpressionParser {

    /**
     * 解析出可加密的?参数信息
     *
     * @param sqls sql语句，支持含分隔符的多条语句
     * @return 可加密的?参数信息
     */
    public static List<EncryptableSqlParam> parseEncryptableParam(String sqls) {
        Statements statements;
        try {
            statements = CCJSqlParserUtil.parseStatements(sqls);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
        List<Statement> statementList = statements.getStatements();
        if (statementList == null || statementList.size() == 0) {
            return Collections.emptyList();
        }
        List<EncryptableSqlParam> result = new LinkedList<>();
        for (Statement statement : statementList) {
            List<EncryptableSqlParam> encryptableSqlParams = new LinkedList<>();
            SelectDeParser selectDeParser = new SelectDeParser();
            ExpressionDeParser expressionDeParser = new ExpressionParser(encryptableSqlParams);
            StatementVisitor statementVisitor = new StatementParser(expressionDeParser, selectDeParser, encryptableSqlParams);
            TablesNamesAliasFinder tablesNamesAliasFinder = new TablesNamesAliasFinder();
            List<String> tableList = tablesNamesAliasFinder.getTableList(statement);
            Map<String, String> aliasTableMap = tablesNamesAliasFinder.getAliasTableMap();
            statement.accept(statementVisitor);
            if (tableList.size() == 1) {
                String tableName = tableList.get(0);
                encryptableSqlParams.forEach(encryptableSqlParam -> encryptableSqlParam.setTableName(tableName));
            } else {
                encryptableSqlParams.forEach(encryptableSqlParam -> fillTableName(encryptableSqlParam, aliasTableMap));
            }
            result.addAll(encryptableSqlParams);
        }
        return result;
    }

    private static void fillTableName(EncryptableSqlParam encryptableSqlParam, Map<String, String> aliasTableMap) {
        String tableName = encryptableSqlParam.getTableName();
        encryptableSqlParam.setTableName(aliasTableMap.getOrDefault(tableName, tableName));
    }

    private static EncryptableSqlParam createEncryptableSqlParam(JdbcParameter jdbcParameter, Column column) {
        Integer index = jdbcParameter.getIndex();
        String columnName = column.getColumnName();
        Table table = column.getTable();
        String tableName = table == null ? null : table.getName();
        return new EncryptableSqlParam(index, columnName, tableName);
    }

    private static class StatementParser extends StatementDeParser {
        private final List<EncryptableSqlParam> encryptableSqlParams;

        public StatementParser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser, List<EncryptableSqlParam> encryptableSqlParams) {
            super(expressionDeParser, selectDeParser, new StringBuilder());
            this.encryptableSqlParams = encryptableSqlParams;
        }

        @Override
        public void visit(Insert insert) {
            super.visit(insert);
            List<Column> columns = insert.getColumns();
            ItemsList itemsList = insert.getItemsList();
            if (itemsList instanceof MultiExpressionList) {
                MultiExpressionList multiExpressionList = (MultiExpressionList) itemsList;
                List<ExpressionList> expressionLists = multiExpressionList.getExpressionLists();
                for (ExpressionList expressionList : expressionLists) {
                    parseInsertExpressionList(columns, expressionList);
                }
            } else if (itemsList instanceof ExpressionList) {
                ExpressionList expressionList = (ExpressionList) itemsList;
                parseInsertExpressionList(columns, expressionList);
            }
        }

        @Override
        public void visit(Update update) {
            super.visit(update);
            ArrayList<UpdateSet> updateSets = update.getUpdateSets();
            for (UpdateSet updateSet : updateSets) {
                ArrayList<Column> columns = updateSet.getColumns();
                ArrayList<Expression> expressions = updateSet.getExpressions();
                for (int i = 0; i < columns.size(); i++) {
                    Column column = columns.get(i);
                    Expression expression = expressions.get(i);
                    if (!(expression instanceof JdbcParameter)) {
                        continue;
                    }
                    this.encryptableSqlParams.add(createEncryptableSqlParam((JdbcParameter) expression, column));
                }
            }
        }

        private void parseInsertExpressionList(List<Column> columns, ExpressionList expressionList) {
            List<Expression> expressions = expressionList.getExpressions();
            for (Expression expression : expressions) {
                if(expression instanceof RowConstructor) {
                    ExpressionList exprList = ((RowConstructor) expression).getExprList();
                    List<Expression> jdbcParameters = exprList.getExpressions();
                    for (int i = 0; i < jdbcParameters.size(); i++) {
                        this.encryptableSqlParams.add(createEncryptableSqlParam((JdbcParameter) jdbcParameters.get(i), columns.get(i)));
                    }
                }
            }
        }
    }


    private static class ExpressionParser extends ExpressionDeParser {

        private final List<EncryptableSqlParam> encryptableSqlParams;

        public ExpressionParser(List<EncryptableSqlParam> encryptableSqlParams) {
            this.encryptableSqlParams = encryptableSqlParams;
        }

        @Override
        public void visit(EqualsTo equalsTo) {
            super.visit(equalsTo);
            parseComparisonOperator(equalsTo);
        }

        @Override
        public void visit(NotEqualsTo notEqualsTo) {
            super.visit(notEqualsTo);
            parseComparisonOperator(notEqualsTo);
        }

        private void parseComparisonOperator(ComparisonOperator comparisonOperator) {
            Expression leftExpression = comparisonOperator.getLeftExpression();
            Expression rightExpression = comparisonOperator.getRightExpression();
            if (leftExpression instanceof Column && rightExpression instanceof JdbcParameter) {
                this.encryptableSqlParams.add(createEncryptableSqlParam((JdbcParameter) rightExpression, (Column) leftExpression));
            }
            if (leftExpression instanceof JdbcParameter && rightExpression instanceof Column) {
                this.encryptableSqlParams.add(createEncryptableSqlParam((JdbcParameter) leftExpression, (Column) rightExpression));
            }
        }

        @Override
        public void visit(InExpression inExpression) {
            super.visit(inExpression);
            Expression leftExpression = inExpression.getLeftExpression();
            if (!(leftExpression instanceof Column)) {
                return;
            }
            Column leftColumn = (Column) leftExpression;
            ItemsList rightItemsList = inExpression.getRightItemsList();
            if (rightItemsList == null) {
                return;
            }
            if (!(rightItemsList instanceof ExpressionList)) {
                return;
            }
            List<Expression> expressions = ((ExpressionList) rightItemsList).getExpressions();
            for (Expression expression : expressions) {
                if (expression instanceof JdbcParameter) {
                    this.encryptableSqlParams.add(createEncryptableSqlParam((JdbcParameter) expression, leftColumn));
                }
            }
        }
    }


}
