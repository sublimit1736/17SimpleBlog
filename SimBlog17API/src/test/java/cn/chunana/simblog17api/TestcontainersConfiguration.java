package cn.chunana.simblog17api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer(Environment environment) {
        DockerImageName postgresImage = DockerImageName.parse(
                environment.getProperty("POSTGRES_IMAGE", "postgres:latest")
                                                             );

        return new PostgreSQLContainer(postgresImage)
                .withDatabaseName(environment.getProperty("POSTGRES_DB", "simblog17_test"))
                .withUsername(environment.getProperty("POSTGRES_USER", "simblog17"))
                .withPassword(environment.getProperty("POSTGRES_PASSWORD", "simblog17"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer(Environment environment) {
        DockerImageName redisImage = DockerImageName.parse(
                environment.getProperty("REDIS_IMAGE", "redis:latest")
                                                          );
        String redisPassword = environment.getProperty("REDIS_PASSWORD", "your_password");

        return new GenericContainer<>(redisImage)
                .withExposedPorts(6379)
                .withCommand("redis-server", "--requirepass", redisPassword);
    }

}
