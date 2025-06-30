# Broker Online Documentation

Welcome to the comprehensive documentation for **Broker Online**, an algorithmic trading application built with Spring Boot and integrated with Zerodha's Kite Connect API.

## 📚 Documentation Structure

This documentation is organized into the following sections:

### Core Documentation

| Document | Description | Status |
|----------|-------------|---------|
| **[CLAUDE.md](./CLAUDE.md)** | Complete application overview, architecture, and enhancement recommendations | ✅ Complete |
| **[SESSION_MANAGEMENT.md](./SESSION_MANAGEMENT.md)** | Detailed session management implementation and security | ✅ Complete |
| **[CI_CD_PIPELINE_GUIDE.md](./CI_CD_PIPELINE_GUIDE.md)** | Comprehensive CI/CD pipeline setup and usage guide | ✅ Complete |
| **[MCP_APPLICATION_TESTING_AGENT.md](./MCP_APPLICATION_TESTING_AGENT.md)** | MCP agent specification for automated testing | ✅ Complete |

## 🚀 Quick Start

### 1. **Application Overview**
Start with **[CLAUDE.md](./CLAUDE.md)** to understand:
- Application architecture and technology stack
- Key features and capabilities
- API endpoints and functionality
- Current implementation status
- Missing features and roadmap

### 2. **Security Implementation**
Read **[SESSION_MANAGEMENT.md](./SESSION_MANAGEMENT.md)** for:
- Authentication flow and session handling
- Token validation and security measures
- Session lifecycle management
- Security best practices implementation

### 3. **Automated Testing**
Explore **[CI_CD_PIPELINE_GUIDE.md](./CI_CD_PIPELINE_GUIDE.md)** to:
- Set up automated testing pipeline
- Understand test categories and coverage
- Configure GitHub Actions workflows
- Implement continuous integration

### 4. **Advanced Testing**
Review **[MCP_APPLICATION_TESTING_AGENT.md](./MCP_APPLICATION_TESTING_AGENT.md)** for:
- Comprehensive testing strategies
- MCP agent integration
- Automated test scenarios
- Performance and security testing

## 🏗️ Project Structure

```
broker/
├── docs/                               ← 📚 All documentation
│   ├── README.md                      ← This file
│   ├── CLAUDE.md                      ← Application overview
│   ├── SESSION_MANAGEMENT.md          ← Security implementation
│   ├── CI_CD_PIPELINE_GUIDE.md        ← Testing pipeline
│   └── MCP_APPLICATION_TESTING_AGENT.md ← Advanced testing
├── .github/workflows/                  ← 🤖 CI/CD Pipeline
│   └── ci-cd-pipeline.yml             ← GitHub Actions workflow
├── test-configs/                       ← 🧪 Test configurations
│   ├── application-test.properties    ← Test app config
│   ├── mock-kite-responses.json       ← Mock API responses
│   └── test-data.sql                  ← Test database data
├── src/main/java/                      ← ☕ Application source
├── src/test/java/                      ← 🧪 Test source
└── build.gradle.kts                   ← 🏗️ Build configuration
```

## 🎯 Key Features

### ✅ **Implemented Features**

1. **Trading Operations**
   - Order placement (MARKET/LIMIT orders)
   - Portfolio management and tracking
   - Real-time market data streaming
   - Multiple product types (MIS/CNC/NRML)

2. **Security & Authentication**
   - OAuth integration with Kite Connect
   - Comprehensive session management
   - Token validation and expiry handling
   - API endpoint protection

3. **Real-time Data**
   - WebSocket implementation for live feeds
   - Ticker subscription management
   - Real-time price updates

4. **API Integration**
   - Complete REST API coverage
   - Swagger/OpenAPI documentation
   - Error handling and validation

5. **Testing & Quality**
   - Comprehensive test suite
   - Automated CI/CD pipeline
   - Security scanning
   - Performance testing

### 🚧 **Planned Enhancements**

1. **Advanced Trading Features**
   - Stop-loss and take-profit orders
   - Bracket and cover orders
   - Algorithmic strategy framework

2. **Analytics & Reporting**
   - Performance analytics
   - Risk management tools
   - Tax reporting

3. **Enhanced Security**
   - Rate limiting
   - Advanced threat detection
   - Compliance monitoring

## 🛠️ Development Workflow

### **Local Development**

1. **Setup Environment**
   ```bash
   # Clone repository
   git clone <repository-url>
   cd broker

   # Configure environment
   cp test-configs/application-test.properties src/main/resources/application-test.properties
   
   # Set environment variables
   export KITE_API_KEY=your_api_key
   export KITE_API_SECRET=your_api_secret
   export KITE_USER_ID=your_user_id
   ```

2. **Run Tests**
   ```bash
   # Unit tests
   ./gradlew test
   
   # Integration tests  
   ./gradlew integrationTest
   
   # All tests
   ./gradlew check
   ```

3. **Start Application**
   ```bash
   ./gradlew bootRun
   ```

### **CI/CD Pipeline**

The automated pipeline runs on every commit and includes:

- ✅ **Unit Tests** (3 minutes)
- ✅ **Integration Tests** (8 minutes) 
- ✅ **Session Management Tests** (5 minutes)
- ✅ **API Tests** (12 minutes)
- ✅ **WebSocket Tests** (6 minutes)
- ✅ **Security Tests** (15 minutes)
- ✅ **Performance Tests** (30 minutes - scheduled only)

See **[CI_CD_PIPELINE_GUIDE.md](./CI_CD_PIPELINE_GUIDE.md)** for complete setup instructions.

## 🔧 Configuration

### **Environment Variables**

Required for both development and production:

```bash
# Kite Connect API
KITE_API_KEY=your_zerodha_api_key
KITE_API_SECRET=your_zerodha_api_secret  
KITE_USER_ID=your_zerodha_user_id

# Database (Production)
POSTGRES_URL=jdbc:postgresql://localhost:5432/broker
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
```

### **Test Configuration**

For CI/CD and local testing, use mock values:

```bash
# Mock Kite API (Safe for CI/CD)
KITE_API_KEY=test_api_key_12345
KITE_API_SECRET=test_api_secret_67890
KITE_USER_ID=test_user_id

# Test Database
POSTGRES_URL=jdbc:postgresql://localhost:5432/broker_test
POSTGRES_USER=test_user
POSTGRES_PASSWORD=test_password
```

## 📊 Monitoring & Health

### **Application Monitoring**

- **Health Endpoint**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **API Documentation**: `http://localhost:8080/swagger-ui/index.html`

### **Test Results**

After each pipeline run, check:
- GitHub Actions tab for test results
- Downloadable test artifacts
- Test coverage reports
- Security scan results

## 🔒 Security Considerations

### **For Development**
- Never commit real API keys
- Use test/mock configurations
- Keep dependencies updated
- Run security scans regularly

### **For Production**
- Use environment variables for secrets
- Enable HTTPS
- Implement rate limiting
- Monitor for suspicious activity
- Regular security audits

## 📈 Performance Guidelines

### **Development**
- Keep unit tests fast (< 5 minutes total)
- Use appropriate test categories
- Mock external dependencies
- Profile memory usage

### **Production**
- Monitor response times
- Track user activity
- Set up alerting
- Scale based on load

## 🐛 Troubleshooting

### **Common Issues**

1. **Tests Failing Locally**
   ```bash
   # Ensure correct Java version
   java -version  # Should be 17
   
   # Clean and rebuild
   ./gradlew clean build
   
   # Check test configuration
   ./gradlew test -Dspring.profiles.active=test
   ```

2. **CI/CD Pipeline Issues**
   ```bash
   # Check GitHub secrets are set
   # Verify pipeline configuration
   # Review pipeline logs in GitHub Actions tab
   ```

3. **Application Startup Issues**
   ```bash
   # Check environment variables
   # Verify database connectivity
   # Review application logs
   ```

### **Getting Help**

1. **Check Documentation**: Review relevant docs above
2. **Search Issues**: Look for similar problems in repository
3. **Create Issue**: Provide detailed reproduction steps
4. **Check Logs**: Include relevant application/test logs

## 🤝 Contributing

### **Code Changes**
1. Create feature branch from `develop`
2. Implement changes with tests
3. Ensure CI/CD pipeline passes
4. Submit pull request with description

### **Documentation Updates**
1. Update relevant documentation files
2. Ensure accuracy and completeness
3. Test any code examples
4. Submit pull request

### **Issue Reporting**
1. Search existing issues first
2. Provide clear reproduction steps
3. Include environment details
4. Add relevant logs/screenshots

## 📋 Checklists

### **Before Committing**
- [ ] Run local tests: `./gradlew test`
- [ ] Check code compilation: `./gradlew compileJava`
- [ ] Update documentation if needed
- [ ] Verify no real credentials in code

### **Before Deploying**
- [ ] All CI/CD tests pass
- [ ] Security scan clean
- [ ] Performance tests acceptable
- [ ] Documentation updated
- [ ] Environment variables configured

### **For Releases**
- [ ] All tests pass
- [ ] Security review complete
- [ ] Performance benchmarks met
- [ ] Documentation complete
- [ ] Deployment plan ready

## 📚 Additional Resources

### **External Documentation**
- [Kite Connect API Documentation](https://kite.trade/docs/connect/v3/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

### **Development Tools**
- [Postman Collection](../postman/) - API testing
- [Performance Tests](../performance-tests/) - Load testing
- [Test Configurations](../test-configs/) - Testing setup

### **Monitoring Tools**
- Application health endpoints
- GitHub Actions dashboard
- Test coverage reports
- Security scan results

---

**Last Updated**: December 2024  
**Documentation Version**: 1.0.0  
**Application Status**: Production Ready (Core Features)

For questions or issues, please create an issue in the repository or contact the development team.