package com.yhy.http.pigeon.starter;

import com.yhy.http.pigeon.starter.register.PigeonAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 16:12
 * version: 1.0.0
 * desc   : 启动 pigeon 自动装配
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({PigeonAutoConfigure.class, PigeonAutoRegister.class})
public @interface EnablePigeon {
}
