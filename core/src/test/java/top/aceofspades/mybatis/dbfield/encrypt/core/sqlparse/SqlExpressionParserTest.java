package top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse;

import org.junit.jupiter.api.Test;

import java.util.List;

class SqlExpressionParserTest {

    @Test
    void parseEncryptableParam() {
        String sql1 = "    select\n" +
                "        count(*)         \n" +
                "    from\n" +
                "        access_record a         \n" +
                "    left join\n" +
                "        (\n" +
                "            select\n" +
                "                * \n" +
                "            from\n" +
                "                people_info \n" +
                "            where\n" +
                "                del_flag = '0' \n" +
                "                and cea_user_type != 3 \n" +
                "            GROUP BY\n" +
                "                identity\n" +
                "        ) p         \n" +
                "            on a.identity = p.identity         \n" +
                "    left join\n" +
                "        sys_dict d         \n" +
                "            on p.post_id = d.`value`         \n" +
                "    where\n" +
                "        d.type = 'postType'         \n" +
                "        and d.other = 'key'         \n" +
                "        and a.access_type = '2'                       \n" +
                "        and a.bid_no = ?                                 \n" +
                "        and a.bid_no like concat('%', ?, '%')                                 \n" +
                "        and p.depart_type_id = ?                                 \n" +
                "        and p.post_id = ?                                 \n" +
                "        and p.user_name like concat('%', ?, '%')                                 \n" +
                "        and a.access_time BETWEEN ? and ?";

        String sql2 = "select p.post_id from (select * from (select * from union_people_info) a) p where post_id in (?)";

        String sql3 = "insert into a.union_people_info(id, single_project_code, idcard, gender)\n" +
                "values (?, ?, ?, ?)";

        String sql4 = "update union_people_info set idcard = ?, gender = ?, post_name = ? where single_project_code = ?";

        String sql5 = "select p.post_id from (select * from (select * from union_people_info) a) p where post_id in (?, ?);" +
                "" +
                "update union_people_info set idcard = ?, gender = ?, post_name = ? where single_project_code = ?";
        List<EncryptableSqlParam> encryptableSqlParams = SqlExpressionParser.parseEncryptableParam(sql5);
        System.out.println(encryptableSqlParams);
    }
}