package uk.gov.companieshouse.pscdataapi.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoPscConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.name}")
    private String databaseName;

    @Value("${spring.data.mongodb.uri}")
    private String databaseUri;

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    protected String getDatabaseName() {
        return this.databaseName;
    }

    protected String getDatabaseUri() {
        return this.databaseUri;
    }

    @Override
    public MongoClient mongoClient() {
        final ConnectionString connectionString =
                new ConnectionString(getDatabaseUri());
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    /** Mongo Mapping. */
    @Bean
    public MongoMappingContext mongoMappingContext(MongoCustomConversions customConversions) {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setFieldNamingStrategy(new SnakeCaseFieldNamingStrategy());
        return mappingContext;
    }
}