# SQL Test Data Files

This directory contains SQL files for different testing and development scenarios.

## File Purpose and Usage

### `development-minimal.sql`
- **Purpose**: Minimal dataset for basic development testing
- **Contains**: 9 basic instruments (5 NSE equity, 1 BSE, 2 NFO options, 1 NFO future), 2 subscriptions
- **Used by**: Development environment (`application-dev.properties`)
- **When to use**: Quick development testing, minimal data overhead
- **Size**: ~3KB, loads quickly

### `development-ui-testing.sql`
- **Purpose**: Comprehensive dataset for UI/UX testing with realistic scenarios
- **Contains**: 35+ instruments across exchanges, 19 orders with all statuses, 16 subscriptions
- **Used by**: Manual UI testing, comprehensive workflow validation
- **When to use**: Testing complete user workflows, portfolio P&L, order management
- **Size**: ~14KB, realistic market data simulation

### `integration-testing.sql`
- **Purpose**: Structured dataset for automated integration tests and CI/CD
- **Contains**: 15 instruments, 9 orders with different statuses, active/inactive subscriptions
- **Used by**: Test environment (`application-test.properties`), CI/CD pipelines
- **When to use**: Automated testing, predictable test scenarios
- **Size**: ~9KB, optimized for test automation

## Configuration References

- **Development**: `spring.sql.init.data-locations=file:test-configs/sql/development-minimal.sql`
- **Testing**: `spring.sql.init.data-locations=classpath:integration-testing.sql`
- **UI Testing**: Manually reference `development-ui-testing.sql` when needed

## Data Categories

All files include:
- **Instruments**: NSE/BSE equity, NFO options/futures
- **Orders**: Various statuses (COMPLETE, PENDING, CANCELLED, REJECTED, PARTIAL)
- **Subscriptions**: Active ticker subscriptions
- **Mock Sessions**: User session data for testing

## Usage Examples

```bash
# Start development with minimal data
./claude-test.sh --start-dev

# Use comprehensive data for UI testing
# (Update application-dev.properties to reference development-ui-testing.sql)

# Run integration tests
./claude-test.sh --backend
```

## File Maintenance

- Update instrument tokens to match current market data
- Ensure order quantities and prices are realistic
- Maintain referential integrity between instruments and orders
- Keep test data focused and purpose-specific