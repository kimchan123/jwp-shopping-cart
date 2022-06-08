package woowacourse.shoppingcart.infrastructure.jdbc.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import woowacourse.shoppingcart.domain.Orders;

@Repository
public class OrderDao {

    private static final RowMapper<Orders> ROW_MAPPER =
            (resultSet, rowNum) -> new Orders(
                    resultSet.getLong("id"),
                    resultSet.getDate("order_date")
            );
    
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public OrderDao(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("orders")
                .usingGeneratedKeyColumns("id");
    }

    public Long addOrder(final Long customerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("customer_id", customerId);
        params.put("order_date", LocalDateTime.now());
        return jdbcInsert.executeAndReturnKey(params)
                .longValue();
    }


    public Optional<Orders> findById(final long id) {
        try {
            final String query = "SELECT id, order_date FROM orders WHERE id = (:id)";
            final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, parameters, ROW_MAPPER));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean isValidOrderId(final Long customerId, final Long orderId) {
        final String sql = "SELECT EXISTS(SELECT * FROM orders WHERE customer_id = (:customerId) AND id = (:orderId))";

        final SqlParameterSource parameters = new MapSqlParameterSource("customerId", customerId)
                .addValue("orderId", orderId);
        return jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
    }
}
