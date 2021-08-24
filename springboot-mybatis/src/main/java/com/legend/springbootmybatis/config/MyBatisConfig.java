//package com.legend.springbootmybatis.config;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.annotation.Resource;
//import javax.sql.DataSource;
//
///**
// * MyBatis的配置，因为现在有3个数据源，所以需要为事务管理器和MyBatis手动指定一个明确的数据源。
// *
// * @author legend
// * @version 1.0
// * @description
// * @date 2021/1/29
// */
//@Configuration
//public class MyBatisConfig {
//
//    @Resource(name = "myRoutingDataSource")
//    private DataSource myRoutingDataSource;
//
//    @Bean
//    public SqlSessionFactory sqlSessionFactory() throws Exception {
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(myRoutingDataSource);
//        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
//        return sqlSessionFactoryBean.getObject();
//    }
//
//
//    @Bean
//    public PlatformTransactionManager platformTransactionManager() {
//        return new DataSourceTransactionManager(myRoutingDataSource);
//    }
//}
