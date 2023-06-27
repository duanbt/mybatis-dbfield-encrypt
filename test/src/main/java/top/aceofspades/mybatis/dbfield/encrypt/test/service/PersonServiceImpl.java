package top.aceofspades.mybatis.dbfield.encrypt.test.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.aceofspades.mybatis.dbfield.encrypt.test.entity.Person;
import top.aceofspades.mybatis.dbfield.encrypt.test.mapper.PersonMapper;

/**
 * @author duanbt
 * @create 2023-06-25 16:04
 **/
@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements PersonService {
}
