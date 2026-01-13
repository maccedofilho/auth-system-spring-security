# CORS Testing Guide

This document provides instructions for manually testing the CORS configuration implemented in this application.

## Configuration Summary

The CORS configuration (`CorsConfig.java`) is set up with the following strict rules:

- **Allowed Origins**: `http://localhost:3000`, `https://yourdomain.com` (configure as needed)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All headers (`*`)
- **Allow Credentials**: `true` (supports cookies and authorization headers)
- **Max Age**: 3600 seconds (1 hour for preflight caching)

## Manual Testing with curl

### Prerequisites

1. Start the application (default port: 8080)
   ```bash
   ./mvnw spring-boot:run
   ```

2. Ensure the application is running at `http://localhost:8080`

### Test 1: Preflight OPTIONS Request (Valid Origin)

This tests the CORS preflight request with an allowed origin.

```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v
```

**Expected Results:**
- HTTP Status: `200 OK`
- Response Headers should include:
  - `Access-Control-Allow-Origin: http://localhost:3000`
  - `Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS`
  - `Access-Control-Allow-Credentials: true`
  - `Access-Control-Max-Age: 3600`

### Test 2: Preflight OPTIONS Request (Invalid Origin)

This tests that CORS blocks requests from disallowed origins.

```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:9999" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v
```

**Expected Results:**
- HTTP Status: `403 Forbidden`
- No `Access-Control-Allow-Origin` header present
- Browser would block the actual request

### Test 3: Simple GET Request (Valid Origin)

```bash
curl -X GET http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -v
```

**Expected Results:**
- Response should include `Access-Control-Allow-Origin: http://localhost:3000`
- Response should include `Access-Control-Allow-Credentials: true`

### Test 4: POST Request with Credentials (Valid Origin)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}' \
  -v
```

**Expected Results:**
- Response includes proper CORS headers
- Credentials are allowed (cookies, authorization headers)

### Test 5: Request with Disallowed Method

```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: PATCH" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

**Expected Results:**
- HTTP Status: `403 Forbidden`
- No `Access-Control-Allow-Methods` header for PATCH

## Browser Testing

For a more realistic test, use browser developer tools:

### Using Browser Console

1. Open your browser's developer console
2. Navigate to your frontend application (e.g., running on `http://localhost:3000`)
3. Execute a fetch request:

```javascript
fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'testuser',
    password: 'password'
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Using a Different Origin

Create a simple HTML file on a different port (e.g., `http://localhost:4000`):

```html
<!DOCTYPE html>
<html>
<head>
  <title>CORS Test</title>
</head>
<body>
  <h1>CORS Test Page</h1>
  <button onclick="testCors()">Test CORS</button>
  <script>
    function testCors() {
      fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: 'testuser',
          password: 'password'
        })
      })
      .then(response => response.json())
      .then(data => console.log('Success:', data))
      .catch(error => console.error('Error:', error));
    }
  </script>
</body>
</html>
```

Serve this file on a different port and test if CORS blocks the request (it should, unless you add that origin to the allowed list).

## Testing Checklist

- [ ] Preflight request with allowed origin succeeds (200 OK)
- [ ] Preflight request with disallowed origin is blocked (403 Forbidden)
- [ ] Simple requests include proper CORS headers
- [ ] Credentials are properly supported (cookies, auth headers)
- [ ] Disallowed methods are rejected
- [ ] Browser console shows no CORS errors for allowed origins
- [ ] Browser console shows CORS errors for disallowed origins

## Modifying Allowed Origins

To add or modify allowed origins, edit `CorsConfig.java`:

```java
private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
    "http://localhost:3000",
    "https://yourdomain.com",
    "https://anotherdomain.com"  // Add your domain here
);
```

## Troubleshooting

### Issue: CORS still blocks requests from allowed origins

**Solution**: Check browser console for specific error messages. Ensure:
- The origin matches exactly (including protocol and port)
- No trailing slashes in origin URLs
- HTTPS vs HTTP matches

### Issue: Credentials not working

**Solution**: Ensure:
- `setAllowCredentials(true)` is set in CorsConfig
- Frontend request includes `credentials: 'include'`
- When using credentials, you cannot use `AllowedOrigins: *`

### Issue: Preflight caching issues

**Solution**: Clear browser cache or use incognito/private mode to test fresh requests.

## Additional Resources

- [MDN Web Docs on CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring Boot CORS Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#cors)
