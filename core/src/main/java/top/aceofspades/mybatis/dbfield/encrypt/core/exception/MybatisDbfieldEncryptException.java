package top.aceofspades.mybatis.dbfield.encrypt.core.exception;

/**
 * @author duanbt
 * @create 2023-06-02 10:49
 **/
public class MybatisDbfieldEncryptException extends RuntimeException {

    public MybatisDbfieldEncryptException(String message) {
        super(message);
    }

    public MybatisDbfieldEncryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
