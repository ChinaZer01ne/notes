# SpringMVC



## 参数封装

### Request Hack

原理：前端的request请求，@RequestParam会封装到request的paramMap中的，@RequestBody会封装到request的inputStream中。SpringMVC读取RequestParam和RequestBody是通过`HttpServletRequestWrapper`中的`getParameterNames()`和`getInputStream()`来完成的，我们只须hack其中的值来达到一个后门的效果。



1、通过修改Request的方式，实现参数封装

```java
package com.github.web.filter;


import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.util.Assert;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParameterRequestWrapper extends HttpServletRequestWrapper {

    private Map<String , String[]> params = new HashMap<>();
    private String body;


    public ParameterRequestWrapper(HttpServletRequest request) {
        super(request);
        //将参数表，赋予给当前的Map以便于持有request中的参数
        this.params.putAll(request.getParameterMap());
    }

    public ParameterRequestWrapper(HttpServletRequest request, String body) {
        this(request);
        //将参数表，赋予给当前的Map以便于持有request中的参数
        this.body = body;
    }

    //重载一个构造方法
    public ParameterRequestWrapper(HttpServletRequest request , Map<String , Object> extendParams) {
        this(request);
        // 这里将扩展参数写入参数表
        addAllParameters(extendParams);
    }
    /**
     * 重写getParameter，代表参数从当前类中的map获取
     * */
    @Override
    public String getParameter(String name) {
        String[]values = params.get(name);
        if(values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }
    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }


    public void addAllParameters(Map<String , Object>otherParams) {
        for(Map.Entry<String , Object>entry : otherParams.entrySet()) {
            addParameter(entry.getKey() , entry.getValue());
        }
    }


    public void addParameter(String name , Object value) {
        if (value != null) {
            if (value instanceof String[]) {
                params.put(name, (String[]) value);
            } else if (value instanceof String) {
                params.put(name, new String[]{(String) value});
            } else {
                params.put(name, new String[]{String.valueOf(value)});
            }
        }
    }
    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }
    /**
     * SpringMVC 会调用这个方法来获取表单提交的参数
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return new CustomizedEnumeration<>(params.keySet().iterator());
    }
    /**
     * SpringMVC 会调用这个方法来获取非表单提交的参数
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new DelegatingServletInputStream(new ByteArrayInputStream(body.getBytes()));
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 装饰类，包装成 {@code ServletInputStream} 返回
     */
    private static class DelegatingServletInputStream extends ServletInputStream  {
        private final InputStream sourceStream;
        private boolean finished = false;

        DelegatingServletInputStream(InputStream sourceStream) {
            Assert.notNull(sourceStream, "Source InputStream must not be null");
            this.sourceStream = sourceStream;
        }

        public final InputStream getSourceStream() {
            return this.sourceStream;
        }
        @Override
        public int read() throws IOException {
            int data = this.sourceStream.read();
            if (data == -1) {
                this.finished = true;
            }

            return data;
        }
        @Override
        public int available() throws IOException {
            return this.sourceStream.available();
        }
        @Override
        public void close() throws IOException {
            super.close();
            this.sourceStream.close();
        }
        @Override
        public boolean isFinished() {
            return this.finished;
        }
        @Override
        public boolean isReady() {
            return true;
        }
        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 遍历集合 {@code Enumeration}
     */
    private static class CustomizedEnumeration<E> implements Enumeration<E> {

        private Iterator<E> iterator;

        CustomizedEnumeration(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public E nextElement() {
            return iterator.next();
        }

    }
}
```

2、配置过滤器，拦截所有请求

```java
package com.github.web.filter;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.fusesource.hawtbuf.BufferInputStream;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

//@Component
//@WebFilter(urlPatterns={"/*"})
public class RequestWrapperFilter implements Filter {

    private static final String FORM_SUBMIT = "application/x-www-form-urlencoded";
    private static final int BUFFER_SIZE = 512;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String contentType = request.getContentType();

        ParameterRequestWrapper parameterWrapper = new ParameterRequestWrapper(request);

        parameterWrapper.addParameter("groupId", request.getHeader("groupId"));
        parameterWrapper.addParameter("companyId",request.getHeader("companyId"));
        parameterWrapper.addParameter("userId",request.getHeader("userId"));

        // 如果不是文件上传，也不是"application/x-www-form-urlencoded",
        if (!ServletFileUpload.isMultipartContent(request) && !FORM_SUBMIT.equalsIgnoreCase(contentType)) {
            // request body handle
            parameterWrapper.setBody(wrapperRequestBody(request));
        }

        filterChain.doFilter(parameterWrapper,servletResponse);
    }
    /**
     * 封装请求体
     */
    @SuppressWarnings("unchecked")
    private String wrapperRequestBody(HttpServletRequest request) throws IOException {
        Map<String,Object> sourceMap = JSONObject.parseObject(getRequestBody(request), Map.class);
        sourceMap.put("groupId", request.getHeader("groupId"));
        sourceMap.put("companyId", request.getHeader("companyId"));
        sourceMap.put("userId", request.getHeader("userId"));
        return JSONObject.toJSONString(sourceMap);
    }
    /**
     * 读取请求体
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        // request body
        byte[] data = new byte[BUFFER_SIZE];

        ServletInputStream inputStream = request.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        int length = bufferedInputStream.read(data);
        while (length != -1){
            length = bufferedInputStream.read(data);
            stringBuilder.append(new String(data));
        }

        return stringBuilder.toString();
    }


}
```



### AOP

AOP可以拦截到方法级别，也可以拦截到参数级别。使用AOP可以实现参数的封装。

1、定义切面

```java
package com.github.web.aop;

import com.github.web.interceptor.ParameterWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version: V1.0
 * @author: Zer01ne
 * @className: ParameterWrapperAspect
 * @packageName: com.github.web.aop
 * @description:
 * @data: 2019-11-12 19:14
 **/
@Aspect
@Component
public class ParameterWrapperAspect {


    @Pointcut("execution(public * com.github.web.controller.*.*(..))")
    public void parameterWrapperService() {

    }

    @Before("parameterWrapperService()")
    public void doBefore(JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Object[] args = joinPoint.getArgs();
        // 参数注解，1维是参数，2维是注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // 参数
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Object param = args[i];
            Annotation[] annotations = parameterAnnotations[i];
            // 注解
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(ParameterWrapper.class)){
                    Class<?> aClass = param.getClass();
                    Field[] declaredFields = aClass.getDeclaredFields();
                    List<Field> fields = Arrays.asList(declaredFields);
                    List<String> fieldName = fields.stream().map(Field::getName).collect(Collectors.toList());
                    if (fieldName.contains("groupId")){
                        Field groupId = aClass.getDeclaredField("groupId");
                        groupId.setAccessible(true);
                        groupId.set(param,100);
                    }
                    if (fieldName.contains("companyId")){
                        Field companyId = aClass.getDeclaredField("companyId");
                        companyId.setAccessible(true);
                        companyId.set(param,100001);
                    }
                    if (fieldName.contains("userId")){
                        Field userId = aClass.getDeclaredField("userId");
                        userId.setAccessible(true);
                        userId.set(param,1003);
                    }
                }
            }
        }

    }
}
```



2、实现自定义注解

```java
package com.github.web.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 主要是来测试aop
 *
 * @author Zer01ne
 * @version 1.0
 * @date 2019/8/6 11:19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.PARAMETER})
public @interface ParameterWrapper {

    String value() default "test message...";
}

```



3、测试

```java

@RestController
public class TestController {

    @PostMapping("test")
    public String test(@RequestParam("test") String test,@ParameterWrapper @RequestBody ParameterEntity entity){
        System.out.println(entity.getGroupId());
        System.out.println(entity.getCompanyId());
        System.out.println(entity.getUserId());
        return "ok， groupId = " + entity.getGroupId() + ", companyId = " + entity.getCompanyId() + ", userId = " + entity.getUserId();
    }
```



### 自定义参数封装

通过实现`HandlerMethodArgumentResolver`来自定义参数封装。SpringMVC只支持给Controller方法参数增加一个的注解（由了`HandlerMethodArgumentResolver`管理的）。比如有一个@RequestBody注解了，增加一个自定义的注解，是会被忽略的。



1、实现HandlerMethodArgumentResolver

```java
package com.github.web.interceptor;

import com.github.web.aop.CustomizedAnnotation;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @version: V1.0
 * @author: Zer01ne
 * @className: ParameterInterceptor
 * @packageName: com.github.web.interceptor
 * @description:
 * @data: 2019-11-04 16:29
 **/
public class ParameterResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return methodParameter.hasParameterAnnotation(ParameterWrapper.class);

    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        AnnotatedElement annotatedElement = methodParameter.getAnnotatedElement();
        ParameterWrapper annotation = annotatedElement.getAnnotation(ParameterWrapper.class);

        if (annotation != null){
            try {

                Constructor<?> constructor = methodParameter.getConstructor();
                Object o = Objects.requireNonNull(constructor).newInstance();

                Field groupId = methodParameter.getParameter().getType().getField("groupId");
                groupId.setAccessible(true);
                groupId.setInt(o,100);

                Field companyId = methodParameter.getParameter().getType().getField("companyId");
                companyId.setAccessible(true);
                companyId.setInt(o,100);

                Field userId = methodParameter.getParameter().getType().getField("userId");
                userId.setAccessible(true);
                userId.setInt(o,100);
                System.err.println("参数绑定测试");
                return o;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.err.println("参数绑定测试");
        return null;
    }
}
```



2、自定义注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.PARAMETER})
public @interface ParameterWrapper {

    String value() default "test message...";
}

```



3、增加配置

```java

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ParameterResolver parameterResolver;
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        resolvers.add(parameterResolver);
    }
}
```



### 拦截器

通过拦截器无法获取参数列表中的参数对象，无法对参数进行赋值。

