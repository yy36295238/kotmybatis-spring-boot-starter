# kotmybatis-spring-boot-starter
mybatis 增强工具，包含通用的增删改查功能

> - 基于springboot-2.1.4  
> - 代码生成工具：[https://github.com/yy36295238/kot-mybatis-generator](https://github.com/yy36295238/kot-mybatis-generator/)  
> - 包含一些通用的操作方法

### 样例
```java
package com.kot.kotmybatis;

import com.kot.kotmybatis.entity.User;
import com.kot.kotmybatis.service.impl.UserService;
import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KotMybatisApplicationTests {


    @Autowired
    private UserService userService;

    @Test
    public void insert() {
        final User user = User.builder().name("张三").cellPhone("13800138000").email("13800138000@139.com").userName("zhangsan").password("123").userStatus(1).createUser(1L).build();
        final int insert = userService.newQuery().insert(user);
        System.err.println(insert);
    }

    @Test
    public void save() {
        final User user = User.builder().id(19L).name("张三1").cellPhone("13800138000").email("13800138000@139.com").userName("zhangsan").password("123").userStatus(1).createUser(1L).build();
        final int save = userService.newQuery().save(user);
        System.err.println(save);
    }

    @Test
    public void findOne() {
        final User user = userService.newQuery()
                .fields(Arrays.asList("id", "user_name", "password"))
                .eq("id", 15)
                .isNull("create_user")
                .findOne(User.builder().userStatus(1).build());
        System.err.println(user);
    }

    @Test
    public void list() {
        final List<User> list = userService.newQuery()
                .fields(Arrays.asList("user_name", "password"))
                .eq("user_name", "test")
                .eq("id", 2)
                .eq("password", "123")
                .list(User.builder().userStatus(1).build());
        System.err.println(list);
    }

    @Test
    public void page() {
        final Page<User> page = userService.newQuery()
                .fields(Arrays.asList("id", "user_name", "password"))
                .between("id", 1, 12)
                .selectPage(new Page<>(2, 10, "id", CT.DESC), User.builder().userStatus(1).build());
        System.err.println(page);
    }

    @Test
    public void delete() {
        final int delete = userService.newUpdate().eq("user_status", 1).gte("id", 17).delete(new User("zhangsan"));
        System.err.println(delete);
    }

    @Test
    public void updateById() {
        final int update = userService.newUpdate().updateById(User.builder().id(15L).cellPhone("13800138000").build());
        System.err.println(update);
    }

    @Test
    public void update() {
        final int update = userService.newUpdate().between("id", 13, 15).update(User.builder().password("123").build(), User.builder().userStatus(1).build());
        System.err.println(update);
    }

}
```
### POM
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.4.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.kot</groupId>
    <artifactId>kot-mybatis</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>kot-mybatis</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>kotmybatis-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.6</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.6</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### 数据库配置
```java
import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.kot.kotmybatis.mapper")
public class DbConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return new DruidDataSource();
    }
}
```
### 实体层
```java
package com.kot.kotmybatis.entity;

import kot.bootstarter.kotmybatis.annotation.Exist;
import kot.bootstarter.kotmybatis.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author YangYu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user")
public class User {

    private Long id;
    private String name;
    private String cellPhone;
    private String email;
    private String userName;
    private String password;
    private Integer userStatus;
    private Long createUser;
    private Date createTime;
    private Date updateTime;
    @Exist(value = false)
    private String test;

    public User(Long id) {
        this.id = id;
    }

    public User(String userName) {
        this.userName = userName;
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}

```

### mapper(dao)层
```java
package com.kot.kotmybatis.mapper;

import com.kot.kotmybatis.entity.User;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;

/**
 * @author YangYu
 */
public interface UserMapper extends BaseMapper<User> {
}

```

### 业务层
```java
package com.kot.kotmybatis.service.impl;

import com.kot.kotmybatis.entity.User;
import kot.bootstarter.kotmybatis.service.impl.MapperManagerServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserService extends MapperManagerServiceImpl<User> {
}
```
