package com.example.SynchronyDemo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Example SSL properties (uncomment if you have a truststore configured):
        // dataSource.addDataSourceProperty("sslMode", "VERIFY_CA");
        // dataSource.addDataSourceProperty("trustCertificateKeyStoreUrl", "file:/path/to/truststore.jks");
        // dataSource.addDataSourceProperty("trustCertificateKeyStorePassword", "changeit");

        return dataSource;
    }
}
