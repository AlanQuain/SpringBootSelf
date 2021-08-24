//package com.legend.springbootmybatis.config;
//
//import com.legend.springbootmybatis.enums.DBTypeEnum;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 多数据源配置
// *
// * @author legend
// * @version 1.0
// * @description
// * @date 2021/1/29
// */
////@Configuration
//public class DataSourceConfig {
//
//    @Bean(name = "masterDataSource")
//    @ConfigurationProperties("spring.datasource.master")
//    public DataSource masterDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "slave1DataSource")
//    @ConfigurationProperties("spring.datasource.slave1")
//    public DataSource slave1DataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    /*@Bean
//    @Primary
//    public DataSource myRoutingDataSource(@Qualifier("masterDataSource") DataSource masterDataSource, @Qualifier("slave1DataSource") DataSource slave1DataSource) {
//        Map<Object, Object> targetDataSources = new HashMap<>();
//        targetDataSources.put(DBTypeEnum.MASTER, masterDataSource);
//        targetDataSources.put(DBTypeEnum.SLAVE1, slave1DataSource);
//        MyRoutingDataSource myRoutingDataSource = new MyRoutingDataSource();
//        myRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
//        myRoutingDataSource.setTargetDataSources(targetDataSources);
//        return myRoutingDataSource;
//    }*/
//}
