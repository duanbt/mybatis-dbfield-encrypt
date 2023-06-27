package top.aceofspades.mybatis.dbfield.encrypt.test.pojo;

import lombok.Data;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Encrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.MapCrypt;
import top.aceofspades.mybatis.dbfield.encrypt.test.entity.Person;

import java.util.List;
import java.util.Map;

/**
 * @author duanbt
 * @create 2023-06-25 18:14
 **/
@Data
public class PersonQuery {

    @Encrypt
    private Person person;

    @Encrypt
    private List<Person> personList;

    @Encrypt
    private List<String> identityList;

    @Encrypt(map = {@MapCrypt(key = "identity")})
    private Map<String, String> params;

    @Encrypt
    private String identity;

    private String companyCode;
}
