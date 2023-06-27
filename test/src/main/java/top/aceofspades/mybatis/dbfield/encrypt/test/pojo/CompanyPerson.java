package top.aceofspades.mybatis.dbfield.encrypt.test.pojo;

import lombok.Data;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Decrypt;
import top.aceofspades.mybatis.dbfield.encrypt.test.entity.Person;

import java.util.List;

/**
 * @author duanbt
 * @create 2023-06-27 13:43
 **/
@Data
public class CompanyPerson {

    @Decrypt
    private String companyCode;

    @Decrypt
    private List<Person> personList;
}
