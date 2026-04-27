# Password Change System - API Documentation

## Overview
Bu sistem, kullanıcıların şifrelerini güvenli bir şekilde değiştirmelerini sağlar. İşlem 3 adımdan oluşur:

1. **Doğrulama Kodu İsteği**: Kullanıcı e-posta adresini girerek doğrulama kodu istedi
2. **Token Doğrulaması**: Frontend'de alınan kodu doğrula
3. **Şifre Sıfırlama**: Yeni şifre ile şifre değiştirme

---

## API Endpoints

### 1. Adım: Şifre Değişikliği İstek (Password Change Request)

**Endpoint:** `POST /api/v1/auth/change-password/request`

**Açıklama:** Kullanıcı şifre değiştirmek istediğinde, e-posta adresini göndererek doğrulama kodu talep eder. Sistema bu kodu Gmail'e gönderir.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Query Parameters:** Yok

**Request Headers:**
```
Content-Type: application/json
```

**Success Response (200 OK):**
```json
{
  "message": "Verification code sent to your email",
  "success": true,
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error Responses:**
- `400 Bad Request` - Email boş veya geçersiz format
- `404 Not Found` - Bu email'e ait kullanıcı bulunamadı
- `500 Internal Server Error` - Email gönderilemedi

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password/request \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

---

### 2. Adım: Token Doğrulama (Verify Password Reset Token)

**Endpoint:** `POST /api/v1/auth/change-password/verify`

**Açıklama:** E-mailde alınan doğrulama kodunun geçerli olup olmadığını kontrol eder. 

**Request Body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response (200 OK):**
```json
{
  "message": "Token is valid",
  "is_valid": true
}
```

**Error Response (200 OK) - Invalid Token:**
```json
{
  "message": "Token is invalid or expired",
  "is_valid": false
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password/verify \
  -H "Content-Type: application/json" \
  -d '{"token":"550e8400-e29b-41d4-a716-446655440000"}'
```

---

### 3. Adım: Şifre Sıfırlama (Confirm Password Change)

**Endpoint:** `POST /api/v1/auth/change-password/confirm`

**Açıklama:** Geçerli token ile yeni şifre belirler ve şifre değişikliğini tamamlar.

**Request Body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewPassword123",
  "confirmPassword": "NewPassword123"
}
```

**Validation Rules:**
- Token zorunludur ve geçerli olmalıdır
- Yeni şifre minimum 6 karakter olmalıdır
- Yeni şifre ve Confirm password eşleşmelidir

**Success Response (200 OK):**
```json
{
  "message": "Password changed successfully",
  "success": true
}
```

**Error Responses:**
- `400 Bad Request` - Şifreler eşleşmez
- `400 Bad Request` - Şifre çok kısa (6 karakterden az)
- `400 Bad Request` - Token geçersiz veya süresi dolmuş
- `500 Internal Server Error` - Confirmation e-maili gönderilemedi

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "token":"550e8400-e29b-41d4-a716-446655440000",
    "newPassword":"NewPassword123",
    "confirmPassword":"NewPassword123"
  }'
```

---

## Complete Workflow Example

### Frontend Flow (JavaScript Example)

```javascript
// Step 1: Request password reset
async function requestPasswordReset(email) {
  const response = await fetch('/api/v1/auth/change-password/request', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email })
  });
  
  const data = await response.json();
  if (data.success) {
    // Token e-mailde gönderildi
    console.log('Check your email for verification code');
    return data.token;
  }
}

// Step 2: Verify token (optional but recommended)
async function verifyToken(token) {
  const response = await fetch('/api/v1/auth/change-password/verify', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ token })
  });
  
  const data = await response.json();
  return data.is_valid;
}

// Step 3: Reset password
async function resetPassword(token, newPassword, confirmPassword) {
  const response = await fetch('/api/v1/auth/change-password/confirm', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      token,
      newPassword,
      confirmPassword
    })
  });
  
  const data = await response.json();
  if (data.success) {
    console.log('Password changed successfully');
    // Redirect to login page
  } else {
    console.error('Password reset failed:', data.message);
  }
}

// Usage
const email = 'user@example.com';
const token = await requestPasswordReset(email);
// User enters verification code from email
const isValid = await verifyToken(token);
if (isValid) {
  await resetPassword(token, 'NewPassword123', 'NewPassword123');
}
```

---

## Email Configuration

### Gmail SMTP Setup

1. **Gmail Account Ayarla:**
   - Gmail hesabınıza giriş yapın
   - "Güvenlik" ayarlarına gidin
   - "2 Aşamalı Doğrulamayı" etkinleştirin

2. **App Password Oluştur:**
   - https://myaccount.google.com/apppasswords adresine gidin
   - "Mail" ve "Windows Bilgisayar" seçin
   - Oluşturulan şifreyi kopyalayın

3. **.env Dosyasını Ayarla:**
   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-generated-app-password
   ```

4. **Alternative Email Providers:**

   **SendGrid:**
   ```
   MAIL_HOST=smtp.sendgrid.net
   MAIL_PORT=587
   MAIL_USERNAME=apikey
   MAIL_PASSWORD=your-sendgrid-api-key
   ```

   **AWS SES:**
   ```
   MAIL_HOST=email-smtp.region.amazonaws.com
   MAIL_PORT=587
   MAIL_USERNAME=your-smtp-username
   MAIL_PASSWORD=your-smtp-password
   ```

---

## Security Considerations

### Token Expiration
- Token'ın varsayılan geçerlilik süresi: **60 dakika**
- `.env` dosyasında `PASSWORD_RESET_EXPIRATION_MINUTES` ile ayarlanabilir

### Token Usage
- Token tek kullanımlık (one-time use)
- Token kullanıldıktan sonra tekrar kullanılamaz
- Yeni şifre isteği, önceki tüm geçerli token'ları geçersiz kılar

### Password Encryption
- Şifreler BCrypt ile şifrelenir (strength: 10)
- Şifreler asla plain-text olarak kaydedilmez

### Email Security
- SMTP şifresi `.env` dosyasında saklanır (`.gitignore` tarafından görmezden gelinir)
- TLS/STARTTLS kullanılarak güvenli bağlantı

---

## Error Handling

### Common Error Cases

| Error | Status | Message | Solution |
|-------|--------|---------|----------|
| Email Not Found | 404 | User not found with email | Check email spelling |
| Invalid Token | 400 | Token is invalid or expired | Request new token |
| Token Expired | 400 | Token is invalid or expired | Token 60 dakika geçerli |
| Password Mismatch | 400 | Passwords do not match | Confirm password alanını kontrol et |
| Weak Password | 400 | Password must be at least 6 characters | 6+ karakter kullan |
| Email Send Error | 500 | Failed to send email | SMTP ayarlarını kontrol et |
| Inactive Account | 400 | User account is inactive | Admin'e başvur |

---

## Database Schema

### password_reset_tokens Table

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date)
);
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary Key |
| token | VARCHAR(255) | Unique token (UUID) |
| user_id | BIGINT | User's ID (Foreign Key) |
| expiry_date | DATETIME | Token expiration time |
| is_used | BOOLEAN | Token kullanıldı mı? |
| created_at | DATETIME | Token creation time |

---

## Testing

### Postman Collection Example

```json
{
  "info": {
    "name": "Password Change System",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Request Password Change",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"email\":\"user@example.com\"}"
        },
        "url": {
          "raw": "{{base_url}}/api/v1/auth/change-password/request",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "auth", "change-password", "request"]
        }
      }
    },
    {
      "name": "Verify Token",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"token\":\"550e8400-e29b-41d4-a716-446655440000\"}"
        },
        "url": {
          "raw": "{{base_url}}/api/v1/auth/change-password/verify",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "auth", "change-password", "verify"]
        }
      }
    },
    {
      "name": "Reset Password",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"token\":\"550e8400-e29b-41d4-a716-446655440000\",\"newPassword\":\"NewPassword123\",\"confirmPassword\":\"NewPassword123\"}"
        },
        "url": {
          "raw": "{{base_url}}/api/v1/auth/change-password/confirm",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "auth", "change-password", "confirm"]
        }
      }
    }
  ]
}
```

---

## Troubleshooting

### Problem: "Failed to send password reset email"

**Solutions:**
1. `.env` dosyasında MAIL_USERNAME ve MAIL_PASSWORD'u kontrol et
2. Gmail App Password'u doğru şekilde oluşturup eklediğini doğrula
3. 2FA'yı Gmail'de etkinleştir
4. Firewall/Antivirus SMTP portunu bloke etmiş olabilir

### Problem: "Token is invalid or expired"

**Solutions:**
1. Token 60 dakikalık süresi geçmiş olabilir
2. Yeni token isteğinde bulun
3. `PASSWORD_RESET_EXPIRATION_MINUTES` süresini arttır

### Problem: "Passwords do not match"

**Solutions:**
1. Şifrelerin tamamen aynı olduğundan emin ol
2. Caps Lock'u kontrol et
3. Boşluk eklemediğini kontrol et

---

## Dependencies Added

```xml
<!-- Email Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## Files Created/Modified

### Created:
- `/src/main/java/com/example/entity/PasswordResetToken.java` - Token entity
- `/src/main/java/com/example/repository/PasswordResetTokenRepository.java` - Token repository
- `/src/main/java/com/example/service/EmailService.java` - Email gönderme service
- `/src/main/java/com/example/service/PasswordResetTokenService.java` - Token yönetimi
- `/src/main/java/com/example/dto/request/ChangePasswordVerificationRequest.java` - DTO
- `/src/main/java/com/example/dto/request/ResetPasswordRequest.java` - DTO
- `/src/main/java/com/example/dto/request/VerifyPasswordResetTokenRequest.java` - DTO
- `/src/main/java/com/example/dto/response/ChangePasswordResponse.java` - DTO
- `/src/main/java/com/example/dto/response/PasswordResetTokenResponse.java` - DTO
- `/src/main/java/com/example/dto/response/TokenVerificationResponse.java` - DTO
- `/src/main/java/com/example/exception/InvalidPasswordTokenException.java` - Exception
- `/src/main/java/com/example/exception/InvalidPasswordException.java` - Exception
- `/src/main/java/com/example/exception/EmailSendingException.java` - Exception
- `/src/main/resources/db/migration/V2__add_password_reset_tokens_table.sql` - DB Migration
- `/.env.example` - Environment variables template

### Modified:
- `/pom.xml` - Added spring-boot-starter-mail dependency
- `/src/main/java/com/example/service/AuthenticationService.java` - Added password change methods
- `/src/main/java/com/example/controller/AuthController.java` - Added password change endpoints
- `/src/main/resources/application.yaml` - Added email configuration

---

## Next Steps

1. `.env` dosyasını kopyala ve ayarla:
   ```bash
   cp .env.example .env
   ```

2. MAIL_USERNAME ve MAIL_PASSWORD'u ekle

3. Projeyi build et:
   ```bash
   mvn clean install
   ```

4. Uygulamayı başlat:
   ```bash
   mvn spring-boot:run
   ```

5. API'yi Postman/Swagger'da test et

6. Database'de `password_reset_tokens` tablosunun oluştuğunu doğrula

---

## Support

Sorunlar veya sorularınız için `support@lms.com` adresine yazabilirsiniz.

