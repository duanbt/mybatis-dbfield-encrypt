# mybatis 字段加解密插件

## 功能

- 基于mybatis interceptor，实现数据入库加密和出库解密
- mapper加密支持：对象字段注解加密，其他参数注解加密(String, List<String>, Map<String,?>, List<Map<String, ?>>)，sql解析参数加密(配置的加密表字段)
- mapper解密支持：对象字段注解解密，其他返回值注解解密(String, List<String>, Map<String,?>, List<Map<String, ?>>)
- 兼容性：兼容mybatis-plus queryWrapper查询条件(通过sql解析参数加密)，兼容分页插件
- 支持多种加密算法(可扩展)，支持不同字段使用不同的加密算法和密钥

## 实现原理

加密：

- 拦截Executor的update和query方法，进行mapper方法参数注解解密
- 拦截Executor的query方法进行查询语句的sql解析参数加密
- 拦截StatementHandler的parameterize方法进行非查询语句的sql解析参数加密

解密：

- 拦截ResultSetHandler的handleResultSets方法，在方法执行之后进行返回值注解解密

## 使用方式

1. Spring Boot 项目引入依赖

```xml

<dependency>
    <groupId>top.aceofspades</groupId>
    <artifactId>mybatis-dbfield-encrypt-core</artifactId>
    <version>${project.version}</version>
</dependency>
```

```xml
<!--按需引入加密算法，可引入多个-->
<dependency>
    <groupId>top.aceofspades</groupId>
    <artifactId>mybatis-dbfield-encrypt-cipher-aes</artifactId>
</dependency>
```

2. 配置项
```yaml
mybatis-dbfield-encrypt:
  enable: true # 开关
  default-cipher-instance: aes # 默认的算法实例id
  cipher-instances: # 算法实例列表
    - id: aes # 算法实例id
      props: # 算法参数
        key: d79d9f0bf41bf14d6e09271847c60c25
      class-name: top.aceofspades.mybatis.dbfield.encrypt.cipher.AesCipher # 算法实现类
    - id: phone-aes
      props:
        key: 33cc4765cab81496c1ed92d9b11058a4
      class-name: top.aceofspades.mybatis.dbfield.encrypt.cipher.AesCipher
  sql-parse-encrypt-table-columns: # sql解析加密 的表字段配置
    person: # 表名
      - column: company_code # 列名
        cipherInstance: aes # 使用的算法实例id
```

3. 注解标注要加解密的字段
```java
@Data
@TableName("person")
public class Person {

    @TableId
    private String id;

    private String name;

    @Encrypt
    @Decrypt
    private String identity;

    @Encrypt(cipherInstance = "phone-aes")
    @Decrypt(cipherInstance = "phone-aes")
    private String phoneNumber;

    @Encrypt
    @Decrypt
    private String companyCode;
}

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
```

4. 更多测试用例见 test模块的top.aceofspades.mybatis.dbfield.encrypt.test.MybatisDbfieldEncryptTest 和 top.aceofspades.mybatis.dbfield.encrypt.test.mapper.PersonMapper

支持mybatis-plus queryWrapper查询条件加密
```java
class MybatisDbfieldEncryptTest {
    
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
}
```

## 扩展加密算法
项目中目前默认支持了aes算法，可十分简单扩展其他算法。

参考AesCipher的实现，继承AbstractCipher实现相关方法即可

## 注意
- 加密过程会改变原始对象中的值

