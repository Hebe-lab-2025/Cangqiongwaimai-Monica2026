package com.sky.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.sky.exception.BaseException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;


/**
 * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
 * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
 * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
 */

public class JacksonObjectMapper extends ObjectMapper {
    public static final String DEFALUT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFALUT_DATE_TIME_FORMAT2 = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFALUT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DEFALUT_TIME_FORMAT = "HH:mm:ss";
    public JacksonObjectMapper()
        super();
        //收到未知属性时不抛出异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);


        //反序列化时，属性不匹配时，不抛出异常
        this.getDeserializationConfig().withoutFeature(), DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFALUT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFALUT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFALUT_TIME_FORMAT)))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFALUT_DATE_TIME_FORMAT2)))
                .addDeserializer(LocalDate.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFALUT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFALUT_DATE_FORMAT)));
        this.registerModule(simpleModule);

    }

}
