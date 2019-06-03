# kotmybatis-spring-boot-starter

### 官方文档  
[http://www.yyself.com](http://www.yyself.com)

### 工具介绍  
> - 基于springboot-2.1.4, mybatis-spring-boot-starter-2.0.1   
> - 通用代码无需手写，使用代码生成工具快速生成：[https://github.com/yy36295238/kot-mybatis-generator](https://github.com/yy36295238/kot-mybatis-generator/)  
> - 可以参考样例工程：[https://github.com/yy36295238/kot-mybatis](https://github.com/yy36295238/kot-mybatis/)
> - 包含一些通用的操作方法

### 更新日志

- 2019-06-02
> 增加update，updateById全字段更新方法  

- 2019-05-30
> 增加插件List<Map>结果的数据，转换为驼峰  

```properties
# List<Map>结果转驼峰
kot.mybatis.under-sore-to-camel=true
```

- 2019-05-25
> 增加逻辑删除功能

```properties
# 逻辑删除
kot.mybatis.logic-delete=true
```
