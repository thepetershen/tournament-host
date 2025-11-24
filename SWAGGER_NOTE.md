# Note About Swagger/OpenAPI Documentation

## Status: Removed Due to Compatibility Issues

Swagger/OpenAPI documentation was initially added but had to be removed due to compatibility issues between:
- Spring Boot 3.5.3 (latest version)
- springdoc-openapi (all tested versions)
- Java 24

## The Issue

The `springdoc-openapi-starter-webmvc-ui` library has a `NoSuchMethodError` with Spring Boot 3.5.3:
```
java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
```

This is because Spring Boot 3.5.x made internal API changes that springdoc hasn't caught up with yet.

## Options for API Documentation

### Option 1: Wait for springdoc update
The springdoc team will likely release a compatible version soon. Check:
- https://github.com/springdoc/springdoc-openapi/issues

### Option 2: Downgrade Spring Boot (Not Recommended)
You could downgrade to Spring Boot 3.3.5, but you'd lose the latest features and security patches.

### Option 3: Use Alternative Documentation
- **Postman Collections**: Export API endpoints to a Postman collection
- **Manual Documentation**: Document endpoints in README.md
- **Spring REST Docs**: Generate docs from tests (more work but test-driven)

## How to Re-enable Swagger (When Compatible)

When springdoc releases a compatible version:

1. **Add dependency to `pom.xml`**:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>COMPATIBLE_VERSION_HERE</version>
</dependency>
```

2. **Add configuration to `application.properties`**:
```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
```

3. **Create OpenAPI configuration** (optional, for customization):
Create `src/main/java/.../Config/OpenApiConfig.java` - see git history for example

## Current Recommendation

For now, your API is well-structured with clear REST endpoints. Use:
- Postman for testing
- README documentation for endpoint descriptions
- Your existing frontend as the primary API consumer

The lack of Swagger doesn't impact production functionality - it's purely a development convenience feature.
