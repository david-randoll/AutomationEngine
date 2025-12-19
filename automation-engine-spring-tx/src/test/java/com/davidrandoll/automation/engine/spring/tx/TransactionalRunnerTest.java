package com.davidrandoll.automation.engine.spring.tx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {AESpringTxAutoConfiguration.class})
@Import(TestConfig.class)
class TransactionalRunnerTest {

    @Autowired
    private TransactionalRunner transactionalRunner;

    @Autowired
    private DataSource dataSource;

    @Test
    void testTransactionalRunnerIsAutowired() {
        assertThat(transactionalRunner).isNotNull();
    }

    @Test
    void testRunInTransactionExecutesSuccessfully() {
        AtomicInteger result = new AtomicInteger(0);
        
        Integer returnValue = transactionalRunner.runInTransaction(() -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                    .as("Transaction should be active during execution")
                    .isTrue();
            result.set(42);
            return 42;
        });

        assertThat(returnValue).isEqualTo(42);
        assertThat(result.get()).isEqualTo(42);
    }

    @Test
    void testRunInTransactionRollsBackOnException() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Create a test table
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, test_value VARCHAR(50))");
        jdbcTemplate.execute("DELETE FROM test_table");

        assertThatThrownBy(() -> transactionalRunner.runInTransaction(() -> {
            // Insert a row
            jdbcTemplate.update("INSERT INTO test_table (id, test_value) VALUES (?, ?)", 1, "test");
            
            // Verify it was inserted (within transaction)
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_table", Integer.class);
            assertThat(count).isEqualTo(1);
            
            // Throw an exception to trigger rollback
            throw new RuntimeException("Test exception");
        })).isInstanceOf(RuntimeException.class)
          .hasMessage("Test exception");

        // Verify the insert was rolled back
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_table", Integer.class);
        assertThat(count).isEqualTo(0);
        
        // Clean up
        jdbcTemplate.execute("DROP TABLE test_table");
    }

    @Test
    void testRunInTransactionCommitsOnSuccess() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Create a test table
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_table2 (id INT PRIMARY KEY, test_value VARCHAR(50))");
        jdbcTemplate.execute("DELETE FROM test_table2");

        transactionalRunner.runInTransaction(() -> {
            // Insert a row
            jdbcTemplate.update("INSERT INTO test_table2 (id, test_value) VALUES (?, ?)", 1, "committed");
            return null;
        });

        // Verify the insert was committed
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_table2", Integer.class);
        assertThat(count).isEqualTo(1);
        
        String value = jdbcTemplate.queryForObject("SELECT test_value FROM test_table2 WHERE id = ?", String.class, 1);
        assertThat(value).isEqualTo("committed");
        
        // Clean up
        jdbcTemplate.execute("DROP TABLE test_table2");
    }

    @Test
    void testTransactionIsActiveInsideRunInTransaction() {
        AtomicBoolean wasTransactionActive = new AtomicBoolean(false);

        transactionalRunner.runInTransaction(() -> {
            wasTransactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            return null;
        });

        assertThat(wasTransactionActive.get()).isTrue();
    }

    @Test
    void testTransactionIsNotActiveOutsideRunInTransaction() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }
}
