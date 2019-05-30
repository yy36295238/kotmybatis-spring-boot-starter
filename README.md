# kotmybatis-spring-boot-starter
mybatis 增强工具，包含通用的增删改查功能

> - 基于springboot-2.1.4  
> - 通用代码无需手写，使用代码生成工具快速生成：[https://github.com/yy36295238/kot-mybatis-generator](https://github.com/yy36295238/kot-mybatis-generator/)  
> - 可以参考样例工程：[https://github.com/yy36295238/kot-mybatis](https://github.com/yy36295238/kot-mybatis/)
> - 包含一些通用的操作方法

### 官方文档：[http://www.yyself.com](http://www.yyself.com)

# 更新日志
- 2019-05-30
> 增加插件List<Map>结果的数据，转换为驼峰  

**开启驼峰转换**
```properties
# List<Map>结果转驼峰
kot.mybatis.under-sore-to-camel=true
```
