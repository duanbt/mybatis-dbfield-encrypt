package top.aceofspades.mybatis.dbfield.encrypt.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Decrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Encrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.MapCrypt;
import top.aceofspades.mybatis.dbfield.encrypt.test.entity.Person;
import top.aceofspades.mybatis.dbfield.encrypt.test.pojo.CompanyPerson;
import top.aceofspades.mybatis.dbfield.encrypt.test.pojo.PersonAdd;
import top.aceofspades.mybatis.dbfield.encrypt.test.pojo.PersonQuery;

import java.util.List;
import java.util.Map;

/**
 * @author duanbt
 * @create 2023-06-25 16:01
 **/
@Mapper
public interface PersonMapper extends BaseMapper<Person> {

    @Select("select * from person where identity = #{query.identity} limit 1")
    Person queryByIdentity1(@Param("query") Person query);

    @Select("select * from person where identity = #{query.person.identity} limit 1")
    Person queryByIdentity2(@Param("query") PersonQuery query);

    @Select("select * from person where identity = #{query.params.identity} limit 1")
    Person queryByIdentity3(@Param("query") PersonQuery query);

    @Select("select * from person where identity = #{identity}")
    Person queryByIdentity4(@Encrypt @Param("identity") String identity);

    @Select("select * from person where identity = #{query.identity}")
    Person queryByIdentity5(@Encrypt(map = {@MapCrypt(key = "identity")}) @Param("query") Map<String, Object> query);

    List<Person> queryListByIdentityIn1(@Param("query") PersonQuery query);

    List<Person> queryListByIdentityIn2(@Param("query") PersonQuery query);

    List<Person> queryListByIdentityIn3(@Param("query") PersonQuery query);

    List<Person> queryListByIdentityIn4(@Encrypt @Param("identityList") List<String> identityList);

    @Select("select * from person where company_code = #{companyCode}")
    List<Person> queryListByCompanyCode(@Param("companyCode") String companyCode);

    @Select("select * from person where company_code != #{companyCode}")
    List<Person> queryListByCompanyCodeNotEquals(@Param("companyCode") String companyCode);

    List<Person> queryListByCompanyCodeIn1(@Param("companyCodeList") List<String> companyCodeList);

    @Insert("insert into person (id, name, identity, phone_number, company_code) VALUES (#{add.id}, #{add.name}, #{add.identity}, #{add.phoneNumber}, #{add.companyCode})")
    void insertBySql(@Param("add") PersonAdd add);

    void batchInsertBySql(@Param("addList") List<PersonAdd> addList);

    @Update("update person set company_code = #{newCompanyCode} where company_code = #{oldCompanyCode}")
    void updateCompanyCodeByCompanyCode(@Param("oldCompanyCode") String oldCompanyCode, @Param("newCompanyCode") String newCompanyCode);

    @Update("update person set company_code = #{companyCode} where company_code != #{companyCode}")
    void updateCompanyCodeByCompanyCodeNotEquals(@Param("companyCode") String companyCode);

    void updateCompanyCodeByCompanyCodeIn(@Param("oldCompanyCodeList") List<String> oldCompanyCodeList, @Param("newCompanyCode") String newCompanyCode);

    @Delete("delete from person where company_code = #{companyCode}")
    void deleteByCompanyCode(@Param("companyCode") String companyCode);

    @Delete("delete from person where company_code != #{companyCode}")
    void deleteByCompanyCodeNotEquals(@Param("companyCode") String companyCode);

    void deleteByCompanyCodeIn(@Param("companyCodeList") List<String> companyCodeList);

    @Select("select * from person where identity = #{query.identity} and company_code = #{query.companyCode}")
    Person queryByIdentityAndCompanyCode(@Param("query") PersonQuery query);

    List<CompanyPerson> listCompanyPerson();

    @Decrypt
    @Select("select identity from person")
    List<String> listIdentity();

    @Decrypt
    @Select("select identity from person where identity = #{identity} limit 1")
    String getOneIdentity(@Encrypt @Param("identity") String identity);

    @Decrypt(map = {
            @MapCrypt(key = {"identity", "companyCode"}),
            @MapCrypt(key = "phoneNumber", cipherInstance = "phone-aes"),
    })
    @Select("select id, name, identity, phone_number as phoneNumber, company_code as companyCode from person where identity = #{identity} limit 1")
    Map<String, Object> queryMapByIdentity(@Encrypt @Param("identity") String identity);

    @Decrypt(map = {
            @MapCrypt(key = {"identity", "companyCode"}),
            @MapCrypt(key = "phoneNumber", cipherInstance = "phone-aes"),
    })
    @Select("select id, name, identity, phone_number as phoneNumber, company_code as companyCode from person where company_code = #{companyCode}")
    List<Map<String, Object>> queryListMapByCompanyCode(@Encrypt @Param("companyCode") String companyCode);


    void batchInsertMapBySql(@Encrypt(map = {@MapCrypt(key = {"identity", "companyCode"}), @MapCrypt(key = "phoneNumber", cipherInstance = "phone-aes"),})
                             @Param("addList")
                                     List<Map<String, Object>> addList);
}
