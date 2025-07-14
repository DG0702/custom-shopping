package com.example.shopping.domain.product.repository;

import com.example.shopping.domain.product.dto.ViewCountUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcProductRepositoryImpl implements JdbcProductRepository {

    private final JdbcTemplate jdbcTemplate;

    AtomicInteger queryCount = new AtomicInteger();


    @Override
    public void batchUpdateDailyViewCount(List<ViewCountUpdateDto> updateList) {
        String sql = "UPDATE products SET view_count = view_count + ? WHERE id = ? FOR UPDATE";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ViewCountUpdateDto dto = updateList.get(i);
                ps.setLong(1, dto.getViewCount());
                ps.setLong(2, dto.getProductId());
                queryCount.incrementAndGet();
            }

            @Override
            public int getBatchSize() {
                return updateList.size();
            }
        });

        log.info("Executed {} update queries in batch", queryCount.get());
        queryCount.set(0);
    }
}
