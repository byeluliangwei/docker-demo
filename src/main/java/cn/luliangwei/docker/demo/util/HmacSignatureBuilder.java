/*
 * Copyright © 2017 signit.cn. All rights reserved.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package cn.luliangwei.docker.demo.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * HMAC认证请求相关构建按工具（包括摘要计算，摘要值比较等)类.<br/>
 * <br/>
 * 算法使用规则：<br/>
 * <ul>
 * <li>1.使用 <code>apiSecret</code> 作为HMAC认证的密钥 <code>KEY</code></li>
 * <li>2.根据 <code>KEY</code> 和 <code>algorithm</code> 初始化HMAC，
 * 将加入运算的字段根据"名称"字典排序后取其对应值， 值间分别以某分隔符(如：'\n')分隔,
 * 且最终也以该分隔符(如：'\n')结尾，构造成的数据作为HMAC输入参数来生成最终摘要值</li>
 * <li>3.将2中得到的原始摘要值（不可打印）进行Base64/16进制编码，得到最终可打印的 <code>signature</code>
 * 字符串值</li>
 * </ul>
 *
 * @author zhd
 * @since 1.0.0
 */
public class HmacSignatureBuilder {

    /**
     * 构建模式的枚举.<br>
     * 即：指明构建时哪些字段需要加入HMAC认证的运算中.
     *
     * @author zhd
     * @since 1.0.0
     */
    public static enum BuilderMode {
        /**
         * 所有字段均加入运算.
         *
         * @since 1.0.0
         */
        FULL,
        /**
         * 只有请求头字段加入运算.
         *
         * @since 1.0.0
         */
        ONLY_HEADER
    }

    private static final Logger LOG = LoggerFactory.getLogger(HmacSignatureBuilder.class);

    public static final String DEFAULT_ALGORITHM = "HmacSHA512";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final byte DEFAULT_DELIMITER = '\n';
    private byte delimiter = DEFAULT_DELIMITER;
    private String charset = DEFAULT_CHARSET;
    private String algorithm = DEFAULT_ALGORITHM;
    private String scheme;
    private String host;
    private String method;
    private String resource;
    private String nonce;
    private String apiKey;
    private byte[] apiSecret;
    private byte[] payload;
    private String date;
    private String contentType;

    /**
     * 默认无参构造方法.
     *
     */
    public HmacSignatureBuilder() {
    }

    /**
     * 基础信息的构造方法.
     *
     * @param algorithm
     *            HMAC的算法（默认：<code>HmacSHA512</code>）
     * @param charset
     *            指定加入运算的相关字符的字符集编码（默认：<code>UTF-8</code>）
     * @param delimiter
     *            指定加入运算的字符间的分隔符（默认：<code>\n</code>）
     */
    public HmacSignatureBuilder(String algorithm, String charset, byte delimiter) {
        this(algorithm, charset, delimiter, null, null);
    }

    /**
     * 基础信息和认证密钥对初始化的构造方法.
     *
     * @param algorithm
     *            HMAC的算法（默认：<code>HmacSHA512</code>）
     * @param charset
     *            指定加入运算的相关字符的字符集编码（默认：<code>UTF-8</code>）
     * @param delimiter
     *            指定加入运算的字符间的分隔符（默认：<code>\n</code>）
     * @param apiKey
     *            需要加入运算的服务器授权给客户端的唯一公开标识.（与<code>apiSecret</code>配套，允许公开）
     * @param apiSecret
     *            需要加入运算的服务器授权给客户端的私密密钥.（与<code>apiKey</code>配套，但不公开）
     */
    public HmacSignatureBuilder(String algorithm, String charset, byte delimiter, String apiKey, byte[] apiSecret) {
        this.algorithm = algorithm;
        this.charset = charset;
        this.delimiter = delimiter;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * 获取默认的HMAC的算法.
     *
     * @return 默认HMAC的算法字符串表示
     * @author zhd
     * @since 1.0.0
     */
    public String getDefaultAlgorithm() {
        return DEFAULT_ALGORITHM;
    }

    /**
     * 获取默认加入运算的相关字符的字符集编码规则.
     *
     * @return 默认加入运算的相关字符的字符集编码规则字符串表示
     * @author zhd
     * @since 1.0.0
     */
    public String getDefaultCharset() {
        return DEFAULT_CHARSET;
    }

    /**
     * 获取默认加入运算的字符间的分隔符.
     *
     * @return 默认加入运算的字符间的分隔符字节表示
     * @author zhd
     * @since 1.0.0
     */
    public byte getDefaultDelimiter() {
        return DEFAULT_DELIMITER;
    }

    /**
     * 设置加入运算的相关字符的字符集编码规则.
     *
     * @param charset
     *            字符集编码规则
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder charset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置加入运算的字符间的分隔符.
     *
     * @param delimiter
     *            字符间的分隔符
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder delimiter(byte delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * 设置HMAC的算法.
     *
     * @param algorithm
     *            HMAC的算法
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder algorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * 设置需要加入运算的协议名称.
     *
     * @param scheme
     *            协议名称（如: http/https/ftp/...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * 设置需要加入运算的主机名称.
     *
     * @param host
     *            主机名称（如: signit.cn/10.10.10.10/...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder host(String host) {
        this.host = host;
        return this;
    }

    /**
     * 设置需要加入运算的服务器授权给客户端的唯一公开标识.（与<code>apiSecret</code>配套，允许公开）.
     *
     * @param apiKey
     *            服务器授权给客户端的唯一公开标识.（与<code>apiSecret</code>配套，允许公开）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * 设置需要加入运算的请求方法.
     *
     * @param method
     *            请求方法（如: GET/POST/PUT/DELETE/...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder method(String method) {
        this.method = method;
        return this;
    }

    /**
     * 设置需要加入运算的请求资源地址（URI）.
     *
     * @param resource
     *            请求资源地址（URI）（如: '/v1/users','/v1/users/123',...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder resource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * 设置需要加入运算的请求内容类型（Content-Type）.
     *
     * @param contentType
     *            请求内容类型（Content-Type）（如: 'application/json','image/jpeg',...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置需要加入运算的请求时间（Date）.
     *
     * @param date
     *            请求时间（Date）（如: 'Sat, 02 Jul 2011 20:45:57 GMT','Wed, 02 Nov
     *            2016 03:25:54 GMT',...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder date(String date) {
        this.date = date;
        return this;
    }

    /**
     * 设置需要加入运算的随机数.
     *
     * @param nonce
     *            随机数（如: '6m0S4nyH1dg7K2gh','8NQq53cLR4g5Y52I',...）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * 设置需要加入运算的服务器授权给客户端的私密密钥.（与<code>apiKey</code>配套，但不公开）.
     *
     * @param apiSecretBytes
     *            服务器授权给客户端的私密密钥.（与<code>apiKey</code>配套，但不公开）
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder apiSecret(byte[] apiSecret) {
        this.apiSecret = apiSecret;
        return this;
    }

    /**
     * 设置需要加入运算的请求数据.
     *
     * @param payloadBytes
     *            请求数据
     * @return 当前HMAC建造器对象
     * @author zhd
     * @since 1.0.0
     */
    public HmacSignatureBuilder payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    /**
     * 完成HMAC认证消息的构建,并获得签名摘要值.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     *
     * @return HMAC原始签名摘要值的内存数据字节.若构建失败，则返回<code>null</code>
     * @author zhd
     * @since 1.0.0
     */
    public byte[] build() {
        return build(BuilderMode.FULL);
    }

    /**
     * 完成HMAC认证消息的构建,并获得签名摘要值.
     *
     * @param builderMode
     *            构建模式的枚举
     * @return HMAC原始签名摘要值的内存数据字节.若构建失败，则返回<code>null</code>
     * @author zhd
     * @since 1.0.0
     */
    public byte[] build(BuilderMode builderMode) {
        try {
            LOG.debug("使用的摘要算法为:{}", algorithm);
            Assert.notNull(algorithm, "algorithm not null");
            final Mac digest = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(apiSecret, algorithm);
            digest.init(secretKey);
            switch (builderMode) {
            case FULL:
                updateFullBuildDigest(digest);
                break;
            case ONLY_HEADER:
                updateOnlyHeaderBuildDigest(digest);
                break;
            default:
                LOG.error("不支持的参数类型: {}", builderMode);
                return null;
            }
            final byte[] signatureBytes = digest.doFinal();
            digest.reset();
            return signatureBytes;
        } catch (Throwable e) {
            LOG.error("未能成功创建摘要: " + e.getMessage());
            return null;
        }
    }

    // 完整的数据运算
    private void updateFullBuildDigest(Mac digest) throws IllegalStateException, UnsupportedEncodingException {
        Assert.notNull(apiKey, "apiKey not null");
        digest.update(apiKey.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(contentType, "contentType not null");
        digest.update(contentType.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(date, "date not null");
        digest.update((date.getBytes(charset)));
        digest.update(delimiter);
        Assert.notNull(host, "host not null");
        digest.update(host.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(method, "method not null");
        digest.update(method.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(nonce, "nonce not null");
        digest.update(nonce.getBytes(charset));
        if (payload != null) {
            digest.update(delimiter);
            Assert.notNull(payload, "payload not empty");
            digest.update(payload);
        }
        digest.update(delimiter);
        Assert.notNull(resource, "resource not null");
        digest.update(resource.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(scheme, "scheme not null");
        digest.update(scheme.getBytes(charset));
        digest.update(delimiter);
    }

    // 仅包含请求头的数据运算
    private void updateOnlyHeaderBuildDigest(Mac digest) throws IllegalStateException, UnsupportedEncodingException {
        Assert.notNull(apiKey, "apiKey not null");
        digest.update(apiKey.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(contentType, "contentType not null");
        digest.update(contentType.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(date, "date not null");
        digest.update(date.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(host, "host not null");
        digest.update(host.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(method, "method not null");
        digest.update(method.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(nonce, "nonce not null");
        digest.update(nonce.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(resource, "resource not null");
        digest.update(resource.getBytes(charset));
        digest.update(delimiter);
        Assert.notNull(scheme, "scheme not null");
        digest.update(scheme.getBytes(charset));
        digest.update(delimiter);
    }

    /**
     * 完成HMAC认证消息的构建,并获得16进制编码表示的签名摘要值.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     * 
     * @return HMAC的16进制编码表示的签名摘要值字符串.若构建失败，则返回<code>null</code>
     * @author zhd
     * @since 1.0.0
     */
    public String buildAsHex() {
        return buildAsHex(BuilderMode.FULL);
    }

    /**
     * 完成HMAC认证消息的构建,并获得16进制编码表示的签名摘要值.
     *
     * @param builderMode
     *            构建模式的枚举
     * @return HMAC的16进制编码表示的签名摘要值字符串.若构建失败，则返回<code>null</code>
     * @author zhd
     * @since 1.0.0
     */
    public String buildAsHex(BuilderMode builderMode) {
        byte[] ret = build(builderMode);
        if (ret == null) {
            return null;
        }
        return DatatypeConverter.printHexBinary(ret);
    }

    /**
     * 完成HMAC认证消息的构建,并获得base64编码表示的签名摘要值.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     * 
     * @return HMAC的base64编码表示的签名摘要值字符串.若构建失败，则返回<code>null</code>
     * @author zhd
     * @since 1.0.0
     */
    public String buildAsBase64() {
        return buildAsBase64(BuilderMode.FULL);
    }

    /**
     * 完成HMAC认证消息的构建,并获得base64编码表示的签名摘要值.
     *
     * @param builderMode
     *            构建模式的枚举
     * @return HMAC的base64编码表示的签名摘要值字符串.若构建失败，则返回<code>null</code>
     * @author zhd
     * @since 1.0.0
     */
    public String buildAsBase64(BuilderMode builderMode) {
        byte[] ret = build(builderMode);
        if (ret == null) {
            return null;
        }
        return DatatypeConverter.printBase64Binary(ret);
    }

    /**
     * 断言认证成功.<br>
     * 若认证不成功,则抛出异常{@link java.lang.IllegalArgumentException}.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthSuccess(byte[] expectedSignature) {
        assertAuthSuccess(expectedSignature, BuilderMode.FULL, null);
    }

    /**
     * 断言认证成功.<br>
     * 若认证不成功,则抛出异常{@link java.lang.IllegalArgumentException}.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param errorMessage
     *            自定义异常抛出的消息
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthSuccess(byte[] expectedSignature, String errorMessage) {
        assertAuthSuccess(expectedSignature, BuilderMode.FULL, errorMessage);
    }

    /**
     * 断言认证成功.<br>
     * 若认证不成功,则抛出异常{@link java.lang.IllegalArgumentException}.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param builderMode
     *            采用的构建模式
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthSuccess(byte[] expectedSignature, BuilderMode builderMode) {
        assertAuthSuccess(expectedSignature, BuilderMode.FULL, null);
    }

    /**
     * 断言认证成功.<br>
     * 若认证不成功,则抛出异常{@link java.lang.IllegalArgumentException}.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param builderMode
     *            采用的构建模式
     * @param errorMessage
     *            自定义异常抛出的消息
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthSuccess(byte[] expectedSignature, BuilderMode builderMode, String errorMessage) {
        if (!isHashEquals(expectedSignature, builderMode)) {
            if (errorMessage == null || errorMessage.equals("") || errorMessage.isEmpty()) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * 断言认证失败.<br>
     * 若认证成功,则抛出异常{@link java.lang.IllegalArgumentException}.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthFail(byte[] expectedSignature) {
        assertAuthFail(expectedSignature, BuilderMode.FULL, null);
    }

    /**
     * 断言认证失败.<br>
     * 若认证成功,则抛出异常{@link java.lang.IllegalArgumentException}.<br/>
     * 默认采用：<code>BuilderMode.FULL</code> 方式构建加入运算的数据.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param errorMessage
     *            自定义异常抛出的消息
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthFail(byte[] expectedSignature, String errorMessage) {
        assertAuthFail(expectedSignature, BuilderMode.FULL, errorMessage);
    }

    /**
     * 断言认证失败.<br>
     * 若认证成功,则抛出异常{@link java.lang.IllegalArgumentException}.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param builderMode
     *            采用的构建模式
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthFail(byte[] expectedSignature, BuilderMode builderMode) {
        assertAuthFail(expectedSignature, BuilderMode.FULL, null);
    }

    /**
     * 断言认证失败.<br>
     * 若认证成功,则抛出异常{@link java.lang.IllegalArgumentException}.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param builderMode
     *            采用的构建模式
     * @param errorMessage
     *            自定义异常抛出的消息
     * @throws IllegalArgumentException
     *             参数不合法异常
     * @author zhd
     * @since 1.0.0
     */
    public void assertAuthFail(byte[] expectedSignature, BuilderMode builderMode, String errorMessage) {
        if (isHashEquals(expectedSignature, builderMode)) {
            if (errorMessage == null || errorMessage.equals("") || errorMessage.isEmpty()) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * 判断期望摘要是否与已构建的摘要相等.<br/>
     * 默认基于构建模式: <code>BuilderMode.FULL</code> 的方式判断
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @return <code>true</code> - 期望摘要与已构建的摘要相等; <code>false</code> -
     *         期望摘要与已构建的摘要不相等
     * @author zhd
     * @since 1.0.0
     */
    public boolean isHashEquals(byte[] expectedSignature) {
        return isHashEquals(expectedSignature, BuilderMode.FULL);
    }

    /**
     * 判断期望摘要是否与已构建的摘要相等.
     *
     * @param expectedSignature
     *            传入的期望摘要
     * @param builderMode
     *            采用的构建模式
     * @return <code>true</code> - 期望摘要与已构建的摘要相等; <code>false</code> -
     *         期望摘要与已构建的摘要不相等
     * @author zhd
     * @since 1.0.0
     */
    public boolean isHashEquals(byte[] expectedSignature, BuilderMode builderMode) {
        final byte[] signature = build(builderMode);
        return MessageDigest.isEqual(signature, expectedSignature);
    }

    /**
     * 判断期望摘要是否与已构建的摘要相等.<br/>
     * 默认基于构建模式: <code>BuilderMode.FULL</code> 的方式判断
     *
     * @param expectedSignatureHex
     *            传入的期望摘要16进制编码表示的字符串
     * @return <code>true</code> - 期望摘要与已构建的摘要相等; <code>false</code> -
     *         期望摘要与已构建的摘要不相等
     * @author zhd
     * @since 1.0.0
     */
    public boolean isHashEqualsWithHex(String expectedSignatureHex) {
        return isHashEqualsWithHex(expectedSignatureHex, BuilderMode.FULL);
    }

    /**
     * 判断期望摘要是否与已构建的摘要相等.
     *
     * @param expectedSignatureHex
     *            传入的期望摘要16进制编码表示的字符串
     * @param builderMode
     *            采用的构建模式
     * @return <code>true</code> - 期望摘要与已构建的摘要相等; <code>false</code> -
     *         期望摘要与已构建的摘要不相等
     * @author zhd
     * @since 1.0.0
     */
    public boolean isHashEqualsWithHex(String expectedSignatureHex, BuilderMode builderMode) {
        try {
            final byte[] signature = build(builderMode);
            return MessageDigest.isEqual(signature, DatatypeConverter.parseHexBinary(expectedSignatureHex));
        } catch (Throwable e) {
            LOG.warn(e.getMessage());
            return false;
        }
    }

    /**
     * 判断期望摘要是否与已构建的摘要相等.<br/>
     * 默认基于构建模式: <code>BuilderMode.FULL</code> 的方式判断
     *
     * @param expectedSignatureBase64
     *            传入的期望摘要base64编码表示的字符串
     * @return <code>true</code> - 期望摘要与已构建的摘要相等; <code>false</code> -
     *         期望摘要与已构建的摘要不相等
     * @author zhd
     * @since 1.0.0
     */
    public boolean isHashEqualsWithBase64(String expectedSignatureBase64) {
        return isHashEqualsWithBase64(expectedSignatureBase64, BuilderMode.FULL);
    }

    /**
     * 判断期望摘要是否与已构建的摘要相等.
     *
     * @param expectedSignatureBase64
     *            传入的期望摘要base64编码表示的字符串
     * @param builderMode
     *            采用的构建模式
     * @return <code>true</code> - 期望摘要与已构建的摘要相等; <code>false</code> -
     *         期望摘要与已构建的摘要不相等
     * @author zhd
     * @since 1.0.0
     */
    public boolean isHashEqualsWithBase64(String expectedSignatureBase64, BuilderMode builderMode) {
        try {
            final byte[] signature = build(builderMode);
            return MessageDigest.isEqual(signature, DatatypeConverter.parseBase64Binary(expectedSignatureBase64));
        } catch (Throwable e) {
            LOG.warn(e.getMessage());
            return false;
        }
    }
}
