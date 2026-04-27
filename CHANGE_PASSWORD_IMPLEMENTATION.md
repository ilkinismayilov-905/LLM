# Change Password System - Implementation Guide

## 📋 Summary

LMS sisteminize Gmail doğrulaması ile **Change Password** mekanizması başarıyla implement edildi. Bu sistem 3 adımlı güvenli bir işlem sunar.

---

## 🎯 Features

✅ **Email Doğrulama** - Gmail SMTP üzerinden doğrulama kodu gönderimi  
✅ **Token Management** - UUID-tabanlı bir kerelik kullanım token'ları  
✅ **Token Expiration** - 60 dakikalık geçerlilik süresi (konfigüre edilebilir)  
✅ **Password Encryption** - BCrypt ile güvenli şifre şifreleme  
✅ **Confirmation Email** - Şifre değişikliği onay e-maili  
✅ **Comprehensive Error Handling** - Detaylı hata mesajları  
✅ **Audit Logging** - Tüm işlemler loglanır  

---

## 🔧 Installation & Setup

### 1. Environment Variables Ayarla

`.env.example` dosyasını kopyala:

```bash
cp .env.example .env
```

Aşağıdaki değişkenleri doldur:

```env
# Gmail Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Token Expiration (in minutes)
PASSWORD_RESET_EXPIRATION_MINUTES=60
```

### 2. Gmail App Password Oluştur

1. https://myaccount.google.com/apppasswords adresine git
2. "Mail" → "Windows Bilgisayar" seç
3. Oluşturulan 16 karakterlik şifreyi kopyala
4. `.env` dosyasında `MAIL_PASSWORD` olarak yapıştır

### 3. Database Migration

Migration dosyası otomatik olarak çalışacak:
```
V2__add_password_reset_tokens_table.sql
```

Veya manuel çalıştır:
```sql
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 4. Build & Run

```bash
# Maven ile build
mvn clean install

# Uygulamayı başlat
mvn spring-boot:run

# Veya JAR ile
java -jar target/LMS-0.0.1-SNAPSHOT.jar
```

---

## 📁 Created Files Structure

```
com.example/
├── entity/
│   └── PasswordResetToken.java          ← Token entity
├── repository/
│   └── PasswordResetTokenRepository.java ← Token repository
├── service/
│   ├── EmailService.java                 ← Email gönderme
│   ├── PasswordResetTokenService.java     ← Token yönetimi
│   └── AuthenticationService.java         ← (modified) Password change methods
├── controller/
│   └── AuthController.java               ← (modified) New endpoints
├── dto/
│   ├── request/
│   │   ├── ChangePasswordVerificationRequest.java
│   │   ├── VerifyPasswordResetTokenRequest.java
│   │   └── ResetPasswordRequest.java
│   └── response/
│       ├── ChangePasswordResponse.java
│       ├── PasswordResetTokenResponse.java
│       └── TokenVerificationResponse.java
└── exception/
    ├── InvalidPasswordTokenException.java
    ├── InvalidPasswordException.java
    └── EmailSendingException.java
```

---

## 🔄 API Workflow

### Step 1: Request Password Change
```
POST /api/v1/auth/change-password/request
Content-Type: application/json

{
  "email": "user@example.com"
}

Response (200):
{
  "message": "Verification code sent to your email",
  "success": true,
  "token": "UUID..."
}
```

### Step 2: Verify Token (Optional)
```
POST /api/v1/auth/change-password/verify
Content-Type: application/json

{
  "token": "UUID..."
}

Response (200):
{
  "message": "Token is valid",
  "is_valid": true
}
```

### Step 3: Reset Password
```
POST /api/v1/auth/change-password/confirm
Content-Type: application/json

{
  "token": "UUID...",
  "newPassword": "NewPassword123",
  "confirmPassword": "NewPassword123"
}

Response (200):
{
  "message": "Password changed successfully",
  "success": true
}
```

---

## 🛡️ Security Features

### Token Security
- **UUID-based**: Rastgele UUID token'ları
- **One-time Use**: Her token sadece bir kez kullanılabilir
- **Expiration**: 60 dakikalık geçerlilik süresi
- **Database Tracking**: Tüm token'lar veritabanında tutulur

### Password Security
- **BCrypt Encryption**: 10 strength ile şifrele
- **Validation**: Minimum 6 karakter
- **Match Check**: Yeni şifre ve confirm şifre eşleşmesi
- **Audit**: Tüm şifre değişiklikleri loglanır

### Email Security
- **TLS/STARTTLS**: Güvenli bağlantı
- **No Plain Text**: Şifreler hiçbir zaman plain-text gönderilmez
- **SMTP Auth**: Kullanıcı doğrulama

---

## 🧪 Testing

### Curl ile Test

1. **Request Token:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password/request \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

2. **Verify Token:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password/verify \
  -H "Content-Type: application/json" \
  -d '{"token":"YOUR_TOKEN_HERE"}'
```

3. **Reset Password:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "token":"YOUR_TOKEN_HERE",
    "newPassword":"NewPassword123",
    "confirmPassword":"NewPassword123"
  }'
```

### Postman ile Test

1. Postman'i aç
2. New Request oluştur
3. Method: POST
4. URL: http://localhost:8080/api/v1/auth/change-password/request
5. Body → raw → JSON seç
6. Aşağıdaki payload'ı gönder:

```json
{
  "email": "user@example.com"
}
```

---

## 📝 Email Templates

### 1. Password Reset Email
Kullanıcı aşağıdaki bilgileri içeren e-mail alır:
- Şifre sıfırlama linki
- 60 dakikalık geçerlilik süresi
- Doğrulama kodu
- Uyarı mesajı

**Format:** Azerbaycan dilinde

### 2. Confirmation Email
Şifre başarıyla değiştirildikten sonra:
- Şifre değişikliği onay mesajı
- Eğer bu işlemi yapmadıysa yapacağı şey hakkında uyarı
- Support contact bilgisi

---

## 🔍 Monitoring & Logging

Tüm işlemler SLF4J logger üzerinden loglanır:

```
INFO: Password change initiated for email: user@example.com
INFO: Password reset token generated for user: user@example.com
INFO: Password reset email sent to: user@example.com
INFO: Verifying password reset token
INFO: Password reset token is valid for user: user@example.com
INFO: Password updated for user: user@example.com
INFO: Password reset successfully for user: user@example.com
```

Log seviyesi `application.yaml` dosyasında ayarlanabilir:
```yaml
logging:
  level:
    com.example: DEBUG
```

---

## ⚙️ Configuration Options

### Email Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### Token Expiration
```yaml
app:
  password-reset:
    expiration-minutes: ${PASSWORD_RESET_EXPIRATION_MINUTES:60}
```

Alternatif providers:
- **SendGrid**: smtp.sendgrid.net:587
- **AWS SES**: email-smtp.[region].amazonaws.com:587
- **Office 365**: smtp.office365.com:587

---

## 🚨 Error Handling

| Error | Cause | Solution |
|-------|-------|----------|
| User not found | Email yanlış | Email adresini kontrol et |
| Token invalid/expired | 60 dakika geçti | Yeni token iste |
| Passwords mismatch | Şifreler farklı | Tekrar gir |
| Password too short | < 6 karakter | 6+ karakter kullan |
| Email send failed | SMTP error | `.env` ayarlarını kontrol et |
| User account inactive | Hesap devre dışı | Admin'e başvur |

---

## 📊 Database Schema

```sql
password_reset_tokens:
├── id (PK, AUTO_INCREMENT)
├── token (UNIQUE, VARCHAR 255)
├── user_id (FK → users.id)
├── expiry_date (DATETIME)
├── is_used (BOOLEAN, DEFAULT FALSE)
├── created_at (DATETIME, DEFAULT NOW)
└── Indexes: token, user_id, expiry_date
```

---

## 🔄 Dependency Injection

Services otomatik olarak injected olur:

```java
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final EmailService emailService;
    private final PasswordResetTokenService passwordResetTokenService;
    // ... other dependencies
}
```

---

## 📦 Dependencies Added

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## ✅ Checklist

- [x] Entity ve Repository oluşturuldu
- [x] Email Service implement edildi
- [x] Token Service implement edildi
- [x] DTOs oluşturuldu
- [x] Exceptions oluşturuldu
- [x] API Endpoints oluşturuldu
- [x] Database Migration oluşturuldu
- [x] Email Configuration ayarlandı
- [x] Security Config güncelendi
- [x] Documentation yazıldı
- [x] Environment variables template oluşturuldu

---

## 🚀 Next Steps

1. `.env` dosyasını Gmail bilgileri ile doldur
2. `mvn clean install` ile build et
3. Uygulamayı başlat
4. Swagger UI'de test et: http://localhost:8080/swagger-ui.html
5. API'yı frontend'e integre et

---

## 📞 Support

Sorunlar için:
1. Logs'u kontrol et (DEBUG mode'da çalıştır)
2. `.env` ayarlarını doğrula
3. Database migration'ı kontrol et
4. Email provider ayarlarını kontrol et

---

## 📄 Related Documentation

- [API Documentation](./CHANGE_PASSWORD_API_DOCUMENTATION.md)
- [Deployment Guide](./DEPLOYMENT_GUIDE.md)
- [README](./README.md)

