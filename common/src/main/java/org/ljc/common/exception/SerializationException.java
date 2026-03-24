package org.ljc.common.exception;

/**
 * 序列化/反序列化异常
 * 用于处理JSON转换过程中的错误
 */
public class SerializationException extends RuntimeException {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}