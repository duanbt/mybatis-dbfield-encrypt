package top.aceofspades.mybatis.dbfield.encrypt.test;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.aceofspades.mybatis.dbfield.encrypt.test.entity.Person;
import top.aceofspades.mybatis.dbfield.encrypt.test.mapper.PersonMapper;
import top.aceofspades.mybatis.dbfield.encrypt.test.pojo.CompanyPerson;
import top.aceofspades.mybatis.dbfield.encrypt.test.pojo.PersonAdd;
import top.aceofspades.mybatis.dbfield.encrypt.test.pojo.PersonQuery;
import top.aceofspades.mybatis.dbfield.encrypt.test.service.PersonService;

import java.util.*;

@SpringBootTest
class MybatisDbfieldEncryptTest {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private PersonService personService;

    @BeforeEach
    void refreshData() {
        personMapper.delete(Wrappers.emptyWrapper());

        Person person1 = new Person();
        person1.setName("person1");
        person1.setIdentity("id1");
        person1.setPhoneNumber("1111");
        person1.setCompanyCode("company1");

        Person person2 = new Person();
        person2.setName("person2");
        person2.setIdentity("id2");
        person2.setPhoneNumber("2222");
        person2.setCompanyCode("company1");

        ArrayList<Person> list = new ArrayList<>();
        list.add(person1);
        list.add(person2);
        personService.saveBatch(list);

        Person person3 = new Person();
        person3.setName("person3");
        person3.setIdentity("id3");
        person3.setPhoneNumber("3333");
        person3.setCompanyCode("company2");
        personMapper.insert(person3);
    }

    @Test
    void test1() {
        String identity = "id1";
        Person query = new Person();
        query.setIdentity(identity);
        Person result = personMapper.queryByIdentity1(query);
        Assertions.assertEquals(result.getIdentity(), identity);
    }

    @Test
    void test2() {
        String identity = "id1";
        Person person = new Person();
        person.setIdentity(identity);
        PersonQuery query = new PersonQuery();
        query.setPerson(person);
        Person result = personMapper.queryByIdentity2(query);
        Assertions.assertEquals(result.getIdentity(), identity);
    }

    @Test
    void test3() {
        String identity1 = "id1";
        Person person1 = new Person();
        person1.setIdentity(identity1);

        String identity2 = "id2";
        Person person2 = new Person();
        person2.setIdentity(identity2);

        ArrayList<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        PersonQuery query = new PersonQuery();
        query.setPersonList(personList);

        List<Person> people = personMapper.queryListByIdentityIn1(query);
        Assertions.assertTrue(() -> people.size() != 0
                && people.stream().allMatch(person -> person.getIdentity().equals(identity1) || person.getIdentity().equals(identity2))
        );
    }


    @Test
    void test4() {
        PersonQuery query = new PersonQuery();
        List<String> identityList = new ArrayList<>();
        String identity1 = "id1";
        String identity2 = "id2";
        identityList.add(identity1);
        identityList.add(identity2);
        query.setIdentityList(identityList);
        List<Person> people = personMapper.queryListByIdentityIn2(query);
        Assertions.assertTrue(() -> people.size() == 2
                && people.stream().allMatch(person -> person.getIdentity().equals(identity1) || person.getIdentity().equals(identity2))
        );
    }

    @Test
    void test5() {
        PersonQuery query = new PersonQuery();
        List<String> identityList = new ArrayList<>();
        String identity1 = "id1";
        String identity2 = "id2";
        identityList.add(identity1);
        identityList.add(identity2);
        query.setIdentityList(identityList);
        List<Person> people = personMapper.queryListByIdentityIn3(query);
        Assertions.assertTrue(() -> people.size() == 2
                && people.stream().allMatch(person -> person.getIdentity().equals(identity1) || person.getIdentity().equals(identity2))
        );
    }

    @Test
    void test6() {
        PersonQuery query = new PersonQuery();
        String identity = "id1";
        Map<String, String> params = new HashMap<>();
        params.put("identity", identity);
        query.setParams(params);
        Person result = personMapper.queryByIdentity3(query);
        Assertions.assertEquals(result.getIdentity(), identity);
    }

    @Test
    void test7() {
        String identity = "id1";
        Person result = personMapper.queryByIdentity4(identity);
        Assertions.assertEquals(result.getIdentity(), identity);
    }

    @Test
    void test8() {
        List<String> result = new ArrayList<>();
        String identity1 = "id1";
        String identity2 = "id2";
        result.add(identity1);
        result.add(identity2);
        List<Person> people = personMapper.queryListByIdentityIn4(result);
        Assertions.assertTrue(() -> people.size() == 2
                && people.stream().allMatch(person -> person.getIdentity().equals(identity1) || person.getIdentity().equals(identity2))
        );
    }

    @Test
    void test9() {
        String identity = "id1";
        Map<String, Object> query = new HashMap<>();
        query.put("identity", identity);
        Person result = personMapper.queryByIdentity5(query);
        Assertions.assertEquals(result.getIdentity(), identity);
    }

    @Test
    void test10() {
        String companyCode = "company1";
        List<Person> people = personMapper.queryListByCompanyCode(companyCode);
        Assertions.assertTrue(() -> people.size() == 2
                && people.stream().allMatch(person -> person.getCompanyCode().equals(companyCode))
        );
    }

    @Test
    void test11() {
        String companyCode = "company1";
        List<Person> people = personMapper.queryListByCompanyCodeNotEquals(companyCode);
        Assertions.assertTrue(() -> people.size() == 1
                && people.stream().noneMatch(person -> person.getCompanyCode().equals(companyCode))
        );
    }

    @Test
    void test12() {
        List<String> companyCodeList = new ArrayList<>();
        String companyCode1 = "company1";
        String companyCode2 = "company2";
        companyCodeList.add(companyCode1);
        companyCodeList.add(companyCode2);
        List<Person> people = personMapper.queryListByCompanyCodeIn1(companyCodeList);
        Assertions.assertEquals(3, people.size());
    }

    @Test
    void test13() {
        PersonAdd add = new PersonAdd();
        String id = "person4";
        add.setId(id);
        add.setName("person4");
        add.setIdentity("id4");
        add.setPhoneNumber("4444");
        add.setCompanyCode("company4");
        personMapper.insertBySql(add);
        Person result = personMapper.selectById(id);
        Assertions.assertEquals(result.getCompanyCode(), "company4");
    }

    @Test
    void test14() {
        for (int i = 0; i < 5; i++) {
            List<PersonAdd> addList = new ArrayList<>();
            String companyCode = "company_test" + i;

            PersonAdd add1 = new PersonAdd();
            add1.setId(IdUtil.getSnowflakeNextIdStr());
            add1.setName("person4");
            add1.setIdentity("id4");
            add1.setPhoneNumber("4444");
            add1.setCompanyCode(companyCode);

            PersonAdd add2 = new PersonAdd();
            add2.setId(IdUtil.getSnowflakeNextIdStr());
            add2.setName("person5");
            add2.setIdentity("id5");
            add2.setPhoneNumber("5555");
            add2.setCompanyCode(companyCode);

            addList.add(add1);
            addList.add(add2);

            personMapper.batchInsertBySql(addList);

            List<Person> people = personMapper.queryListByCompanyCode(companyCode);
            Assertions.assertTrue(() -> people.size() == 2
                    && people.stream().allMatch(person -> person.getCompanyCode().equals(companyCode))
            );
        }
    }

    @Test
    void test15() {
        String oldCompanyCode = "company2";
        String newCompanyCode = "company3";
        personMapper.updateCompanyCodeByCompanyCode(oldCompanyCode, newCompanyCode);
        List<Person> people = personMapper.queryListByCompanyCode(newCompanyCode);
        Assertions.assertTrue(() -> people.size() == 1
                && people.stream().allMatch(person -> person.getCompanyCode().equals(newCompanyCode))
        );
    }

    @Test
    void test16() {
        String companyCode = "company2";
        personMapper.updateCompanyCodeByCompanyCodeNotEquals(companyCode);
        List<Person> people = personMapper.queryListByCompanyCode(companyCode);
        Assertions.assertTrue(() -> people.size() == 3
                && people.stream().allMatch(person -> person.getCompanyCode().equals(companyCode))
        );
    }

    @Test
    void test17() {
        List<String> oldCompanyCodeList = new ArrayList<>();
        oldCompanyCodeList.add("company1");
        oldCompanyCodeList.add("company2");
        String newCompanyCode = "company3";
        personMapper.updateCompanyCodeByCompanyCodeIn(oldCompanyCodeList, newCompanyCode);
        List<Person> people = personMapper.queryListByCompanyCode(newCompanyCode);
        Assertions.assertTrue(() -> people.size() == 3
                && people.stream().allMatch(person -> person.getCompanyCode().equals(newCompanyCode))
        );
    }

    @Test
    void test18() {
        String companyCode = "company2";
        personMapper.deleteByCompanyCode(companyCode);
        Long count = personMapper.selectCount(Wrappers.emptyWrapper());
        Assertions.assertEquals(2, count);
    }

    @Test
    void test19() {
        String companyCode = "company2";
        personMapper.deleteByCompanyCodeNotEquals(companyCode);
        Long count = personMapper.selectCount(Wrappers.emptyWrapper());
        Assertions.assertEquals(1, count);
    }

    @Test
    void test20() {
        List<String> companyCodeList = new ArrayList<>();
        String companyCode1 = "company1";
        String companyCode2 = "company2";
        companyCodeList.add(companyCode1);
        companyCodeList.add(companyCode2);
        personMapper.deleteByCompanyCodeIn(companyCodeList);
        Long count = personMapper.selectCount(Wrappers.emptyWrapper());
        Assertions.assertEquals(0, count);
    }

    @Test
    void test21() {
        Long company2Count = personMapper.selectCount(
                Wrappers.lambdaQuery(Person.class).eq(Person::getCompanyCode, "company2")
        );
        Assertions.assertEquals(1, company2Count);

        //queryWrapper中仅支持 sql-parse-encrypt-table-columns 中配置的字段加密
        Long id1Count = personMapper.selectCount(
                Wrappers.lambdaQuery(Person.class).eq(Person::getIdentity, "id1")
        );
        Assertions.assertEquals(0, id1Count);
    }

    @Test
    void test22() {
        IPage<Person> page = new Page<>(1, 2);
        IPage<Person> pageResult = personMapper.selectPage(
                page,
                Wrappers.lambdaQuery(Person.class)
                        .in(Person::getCompanyCode, "company1", "company2")
        );
        Assertions.assertEquals(3, pageResult.getTotal());
        Assertions.assertEquals(2, pageResult.getSize());
    }

    @Test
    void test23() {
        Person person = personMapper.queryByIdentity4("id1");
        person.setIdentity("id1_update");
        personMapper.updateById(person);
        person = personMapper.queryByIdentity4("id1_update");
        Assertions.assertTrue(person != null && person.getIdentity().equals("id1_update"));
    }

    @Test
    void test24() {
        personMapper.delete(Wrappers.lambdaQuery(Person.class)
                .eq(Person::getCompanyCode, "company1"));
        Long count = personMapper.selectCount(Wrappers.emptyWrapper());
        Assertions.assertEquals(1, count);
    }

    @Test
    void test25() {
        PersonQuery query = new PersonQuery();
        query.setIdentity("id1");
        query.setCompanyCode("company1");
        Person result = personMapper.queryByIdentityAndCompanyCode(query);
        Assertions.assertTrue(result != null && result.getIdentity().equals("id1"));
    }

    @Test
    void test26() {
        List<CompanyPerson> result = personMapper.listCompanyPerson();
        for (CompanyPerson companyPerson : result) {
            System.out.println(companyPerson);
        }
        Assertions.assertTrue(() -> result.size() == 2
                && result.stream()
                .flatMap(companyPerson -> companyPerson.getPersonList().stream())
                .allMatch(person -> person.getCompanyCode().equals("company1") || person.getCompanyCode().equals("company2"))
        );
    }

    @Test
    void test27() {
        List<String> identityList = personMapper.listIdentity();
        Assertions.assertTrue(identityList.size() == 3
                && identityList.containsAll(Arrays.asList("id1", "id2", "id3")));
    }

    @Test
    void test28() {
        String identity = personMapper.getOneIdentity("id1");
        Assertions.assertEquals("id1", identity);
    }

    @Test
    void test29() {
        Map<String, Object> person = personMapper.queryMapByIdentity("id1");
        Assertions.assertTrue(() -> person.get("identity").equals("id1")
                && person.get("companyCode").equals("company1")
                && person.get("phoneNumber").equals("1111"));
    }

    @Test
    void test30() {
        List<Map<String, Object>> people = personMapper.queryListMapByCompanyCode("company1");
        Assertions.assertTrue(() -> people.size() == 2
                && people.stream().allMatch(person -> person.get("companyCode").equals("company1")));
    }

    @Test
    void test31() {
        String companyCode = "company_test";
        List<Map<String, Object>> addList = new ArrayList<>();
        int insertCount = 5;
        for (int i = 0; i < insertCount; i++) {
            Map<String, Object> add = new HashMap<>();
            add.put("id", IdUtil.getSnowflakeNextIdStr());
            add.put("name", "name_test" + i);
            add.put("identity", "id_test" + i);
            add.put("phoneNumber", "phone_test" + i);
            add.put("companyCode", companyCode);
            addList.add(add);
        }
        personMapper.batchInsertMapBySql(addList);
        List<Person> people = personMapper.queryListByCompanyCode(companyCode);
        Assertions.assertTrue(() -> people.size() == insertCount
                && people.stream().allMatch(person -> person.getCompanyCode().equals(companyCode))
        );
    }
}