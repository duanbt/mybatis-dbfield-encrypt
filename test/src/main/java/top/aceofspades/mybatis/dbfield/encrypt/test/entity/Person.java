package top.aceofspades.mybatis.dbfield.encrypt.test.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Decrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Encrypt;

/**
 * @author duanbt
 * @create 2023-06-25 15:00
 **/
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
