package anzihe.com.agent.config;

import anzihe.com.agent.utils.DateTimeFormatterUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;


@Slf4j
@Configuration
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonConfiguration{

    @Resource
    private JacksonProperties jacksonProperties;

    @Bean
    public JsonMapperBuilderCustomizer autoJsonMapperBuilderCustomizer() {
        return mapperBuilder -> {
            // 1. 基础环境配置 (时区、Locale)
            TimeZone timeZone = jacksonProperties.getTimeZone();
            if (timeZone == null) {
                timeZone = TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai"));
            }
            mapperBuilder.defaultTimeZone(timeZone);

            Locale locale = jacksonProperties.getLocale();
            if (locale == null) {
                locale = Locale.CHINA;
            }
            mapperBuilder.defaultLocale(locale);

            // 2. 日期格式化配置 (使用 DateTimeFormatterUtil)
            String dateFormat = this.jacksonProperties.getDateFormat();
            if (dateFormat == null) {
                dateFormat = DateTimeFormatterUtil.DATE_TIME_STR;
            }
            // Jackson 3 只接受 DateFormat 实例，不再接受字符串
            mapperBuilder.defaultDateFormat(new SimpleDateFormat(dateFormat));

            // Jackson 3 已把 java.time 支持内置进 databind，自定义格式需注册为模块
            SimpleModule javaTimeModule = new SimpleModule("customJavaTime");
            // 注册 Java8 时间模块的序列化器
            javaTimeModule.addSerializer(new LocalDateTimeSerializer(DateTimeFormatterUtil.DATE_TIME_FORMAT));
            javaTimeModule.addSerializer(new LocalDateSerializer(DateTimeFormatterUtil.DATE_FORMAT));
            javaTimeModule.addSerializer(new LocalTimeSerializer(DateTimeFormatterUtil.TIME_FORMAT));

            // 注册 Java8 时间模块的反序列化器
            javaTimeModule.addDeserializer(LocalDateTime.class,
                    new LocalDateTimeDeserializer(DateTimeFormatterUtil.DATE_TIME_FORMAT));
            javaTimeModule.addDeserializer(LocalDate.class,
                    new LocalDateDeserializer(DateTimeFormatterUtil.DATE_FORMAT));
            javaTimeModule.addDeserializer(LocalTime.class,
                    new LocalTimeDeserializer(DateTimeFormatterUtil.TIME_FORMAT));
            mapperBuilder.addModule(javaTimeModule);

            // 3. 特性开关配置
            // 序列化：对象为空时不抛异常
            mapperBuilder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            // 序列化：禁用时间戳，使用格式化字符串
            // Jackson 3 中 WRITE_DATES_AS_TIMESTAMPS 已从 SerializationFeature 移至 DateTimeFeature
            mapperBuilder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
            // 反序列化：遇到未知属性不抛异常
            mapperBuilder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            log.info("Jackson 全局配置已加载: TimeZone={}, Locale={}", timeZone.getID(), locale);
        };
    }




}
