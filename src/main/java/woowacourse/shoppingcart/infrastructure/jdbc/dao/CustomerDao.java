package woowacourse.shoppingcart.infrastructure.jdbc.dao;

import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import woowacourse.shoppingcart.domain.Customer;
import woowacourse.shoppingcart.exception.InvalidCustomerException;

@Repository
public class CustomerDao {

    public static final RowMapper<Customer> ROW_MAPPER =
            (resultSet, rowNum) -> new Customer(
                    resultSet.getLong("id"),
                    resultSet.getString("email"),
                    resultSet.getString("userName"),
                    resultSet.getString("password")
            );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public CustomerDao(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("Customer")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(final Customer customer) {
        final SqlParameterSource parameters = new BeanPropertySqlParameterSource(customer);
        return jdbcInsert.executeAndReturnKey(parameters)
                .longValue();
    }

    public Optional<Customer> findById(final long id) {
        try {
            final String query = "SELECT id, email, userName, password FROM customer WHERE id = (:id)";
            final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, parameters, ROW_MAPPER));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Customer> findByEmail(final String customerEmail) {
        try {
            final String query = "SELECT id, email, userName, password FROM customer WHERE email = (:email)";
            final SqlParameterSource parameters = new MapSqlParameterSource("email", customerEmail);
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, parameters, ROW_MAPPER));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long findIdByUserName(final String userName) {
        try {
            final String query = "SELECT id FROM customer WHERE username = (:userName)";
            final SqlParameterSource parameters = new MapSqlParameterSource("userName",
                    userName.toLowerCase(Locale.ROOT));
            return jdbcTemplate.queryForObject(query, parameters,
                    (resultSet, rowNum) -> resultSet.getLong("id"));
        } catch (final EmptyResultDataAccessException e) {
            throw new InvalidCustomerException();
        }
    }

    public boolean existsByEmail(final String email) {
        final String query = "SELECT EXISTS(SELECT id FROM customer WHERE email=(:email)) as existable";
        final SqlParameterSource parameters = new MapSqlParameterSource("email", email);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, parameters,
                (resultSet, rowNum) -> resultSet.getBoolean("existable")));
    }

    public void deleteById(final long id) {
        final String query = "DELETE FROM customer WHERE id=(:id)";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(query, parameters);
    }

    public void update(final Customer customer) {
        final String query = "UPDATE customer SET username=(:username), password=(:password) WHERE id=(:id)";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", customer.getId())
                .addValue("username", customer.getUserName())
                .addValue("password", customer.getPassword());
        jdbcTemplate.update(query, parameters);
    }
}
