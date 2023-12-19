/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package net.sberg.elbook.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.sql.DataSource;

@Configuration
@ComponentScan({"net.sberg.elbook"})
@EnableTransactionManagement
@EnableScheduling
@RequiredArgsConstructor
public class AppConfig {

	private final Environment env;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	// ToDo: delete in Version > 2.0.0, needed one time to repair sql update files
	@Bean
	public FlywayMigrationStrategy cleanMigrateStrategy() {
		return flyway -> {
			flyway.repair();
			flyway.migrate();
		};
	}

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setThreadNamePrefix("eldixjobs");
		return threadPoolTaskScheduler;
	}
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

	@Bean(name="dsElbook")
	public DataSource dataSourceElbook() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(env.getProperty("jdbc.elbook.driverClassName"));
		dataSource.setUrl(env.getProperty("jdbc.elbook.url"));
		dataSource.setUsername(env.getProperty("jdbc.elbook.username"));
		dataSource.setPassword(env.getProperty("jdbc.elbook.password"));
		dataSource.setDefaultAutoCommit(false);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setMaxTotal(40);
		dataSource.setInitialSize(5);
		dataSource.setMaxIdle(10);
		dataSource.setConnectionProperties("autoReconnect=true;characterEncoding=UTF-8;serverTimezone=Europe/Berlin");
		return dataSource;
	}

	@Bean
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }

	@Bean
	@Qualifier("dsElbook")
	public DataSourceTransactionManager createTransactionManager(DataSource dsElbook) {
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
		dataSourceTransactionManager.setDataSource(dsElbook);
		return dataSourceTransactionManager;
	}

	@Bean(name = "jdbcElbook")
    @Autowired
    @Qualifier("dsElbook")
    public JdbcTemplate elbookJdbcTemplate(DataSource dsElbook) {
        return new JdbcTemplate(dsElbook);
    }

}
