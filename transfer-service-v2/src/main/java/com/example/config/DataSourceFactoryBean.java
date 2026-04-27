package com.example.config;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DataSourceFactoryBean.class);

    private String url;
    private String username;
    private String password;

    public DataSourceFactoryBean(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public DataSource getObject() throws Exception {
        logger.info("FactoryBean — creating DataSource");
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}

// JPA
// EntityManagerFactoryBean -> EntityManagerFactory -> EntityManager