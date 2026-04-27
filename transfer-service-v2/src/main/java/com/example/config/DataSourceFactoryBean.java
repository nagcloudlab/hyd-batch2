package com.example.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.zaxxer.hikari.HikariDataSource;

// FactoryBean<T> — interface for creating complex beans
// Spring calls getObject() to produce the actual bean
// getBean("dataSource") returns the product (DataSource)
// getBean("&dataSource") returns the factory itself (DataSourceFactoryBean)
public class DataSourceFactoryBean implements FactoryBean<DataSource> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactoryBean.class);

    private final String url;
    private final String username;
    private final String password;

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
        return true;
    }

}
