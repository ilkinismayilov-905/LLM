# Token Management and Scheduler System

## Overview

This feature implements a comprehensive token management system with automatic cleanup scheduling for the LMS authentication system.

## Key Features

### 1. Refresh Token Database Storage
- **New Entity**: `RefreshToken.java` - JPA entity for storing refresh tokens in database
- **New Repository**: `RefreshTokenRepository.java` - CRUD operations for refresh tokens
- **Database Table**: `refresh_tokens` - Stores all active refresh tokens with user mapping
- **Smart Management**: When a new refresh token is created for a user:
  - Check if user already has a refresh token
  - If exists: Delete the old token
  - Create and save new token
  - This ensures only ONE active refresh token per user at any time

### 2. Password Reset Token Management
- **Enhanced Behavior**: When generating password reset token:
  - Delete all previous unused tokens for the user
  - Create single new token
  - Ensures clean token lifecycle
- **Updated Repository**: Added `deleteExpiredTokens()` method for automated cleanup

### 3. Automatic Token Cleanup Scheduler
- **New Service**: `TokenCleanupScheduler.java` - Scheduled task runner
- **Cleanup Tasks**:
  - **Refresh Tokens**: Deleted every 1 hour (3600000ms)
  - **Password Reset Tokens**: Deleted every 30 minutes (1800000ms)
  - Initial delay: 5 minutes (300000ms)
- **Error Handling**: Comprehensive exception handling with logging
- **Application Startup**: Added `@EnableScheduling` to `LmsApplication.java`

### 4. Refresh Token Service
- **New Service**: `RefreshTokenService.java` - Token lifecycle management
- **Methods**:
  - `createOrUpdateRefreshToken()` - Creates new or replaces existing
  - `validateRefreshToken()` - Validates token existence and expiry
  - `getRefreshToken()` - Retrieves user's current token
  - `deleteRefreshToken()` - Removes token for user
  - `cleanupExpiredTokens()` - Called by scheduler

## Architecture

```
User Login/Register
    ↓
AuthenticationService
    ↓
RefreshTokenService.createOrUpdateRefreshToken()
    ↓
Check existing token for user → Delete if exists → Create new
    ↓
Store in refresh_tokens table
    ↓
Return token to client
```

## Token Refresh Flow

```
Client presents Refresh Token
    ↓
POST /api/v1/auth/refresh
    ↓
AuthenticationService.refreshToken()
    ↓
RefreshTokenService.validateRefreshToken()
    ↓
Check database (not JWT parsing)
    ↓
If valid: Generate new access token + new refresh token
    ↓
RefreshTokenService.createOrUpdateRefreshToken()
    ↓
Delete old refresh token → Create new one
    ↓
Return both tokens to client
```

## Password Reset Token Flow

```
User requests password reset
    ↓
PasswordResetTokenService.generateResetToken()
    ↓
Find all unused tokens for user → Delete them
    ↓
Create single new token
    ↓
Store in password_reset_tokens table
    ↓
Send token via email
```

## Scheduler Execution

### Refresh Token Cleanup
- **Schedule**: Every 1 hour
- **Initial Delay**: 5 minutes
- **Task**: Delete all expired refresh tokens
- **Log Level**: INFO with timestamps

### Password Reset Token Cleanup
- **Schedule**: Every 30 minutes
- **Initial Delay**: 5 minutes
- **Task**: Delete all expired password reset tokens
- **Log Level**: INFO with timestamps

## Database Schema

### refresh_tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date)
);
```

### password_reset_tokens Table (Updated)
```sql
-- Added cleanup method for expired tokens
-- All expired tokens are cleaned up every 30 minutes
```

## Updated Components

### AuthenticationService.java
- Modified `login()` - Now uses `RefreshTokenService` to store token
- Modified `register()` - Now uses `RefreshTokenService` to store token
- Modified `refreshToken()` - Now validates token from database instead of JWT
- Token rotation: Delete old → Create new → Store in DB

### PasswordResetTokenService.java
- Enhanced `generateResetToken()` - Deletes previous tokens before creating new one
- New `cleanupExpiredTokens()` - Called by scheduler

### RefreshTokenService.java (NEW)
- Complete token lifecycle management
- One-token-per-user strategy
- Database validation support

### TokenCleanupScheduler.java (NEW)
- Scheduled cleanup tasks
- Error handling and logging
- Runs automatically on application startup

### LmsApplication.java
- Added `@EnableScheduling` annotation
- Enables scheduled task execution

## Benefits

1. **Security**: Refresh tokens stored in database with database-level validation
2. **Control**: One active token per user at a time
3. **Cleanup**: Automatic removal of expired tokens prevents database bloat
4. **Reliability**: Comprehensive error handling and logging
5. **Performance**: Indexed database queries for fast token lookups
6. **Compliance**: Regular token rotation implemented for both token types

## Implementation Details

### Token One-Per-User Strategy

Before this feature:
- Tokens could accumulate in database
- No automatic cleanup
- Multiple tokens per user possible

After this feature:
- Only ONE refresh token per user in database
- Previous token deleted before creating new one
- Password reset tokens also cleaned up
- Expired tokens removed automatically

### Database Validation

Refresh tokens are now validated against database instead of JWT signature:
- Check if token exists in `refresh_tokens` table
- Verify user is still active
- Check token hasn't expired
- More secure than JWT-only validation

## Files Created/Modified

### Created (5 files)
- `RefreshToken.java` - Entity
- `RefreshTokenRepository.java` - Repository
- `RefreshTokenService.java` - Service
- `TokenCleanupScheduler.java` - Scheduler
- `V3__add_refresh_tokens_table.sql` - Migration

### Modified (3 files)
- `AuthenticationService.java` - Updated token handling
- `PasswordResetTokenService.java` - Enhanced cleanup
- `LmsApplication.java` - Added @EnableScheduling
- `PasswordResetTokenRepository.java` - Added cleanup method

## Configuration

No additional configuration needed. The scheduler runs automatically with default intervals:
- Refresh token cleanup: Every hour
- Password reset cleanup: Every 30 minutes

To customize intervals, modify `TokenCleanupScheduler.java`:
```java
@Scheduled(fixedDelay = 3600000) // Change to desired milliseconds
```

## Testing

### Test Refresh Token Storage
1. Login → Token stored in `refresh_tokens` table
2. Login again → Previous token deleted, new one stored
3. Only one token exists in database

### Test Scheduler
1. Start application → Scheduler initializes after 5 minutes
2. Check logs for: "Starting cleanup of expired tokens"
3. Verify expired tokens removed from database

### Test Token Validation
1. Use valid token → Validates from database
2. Use invalid token → Returns 401 Unauthorized
3. Use expired token → Deleted by scheduler, returns 401

## Security Considerations

1. **One Token Per User**: Prevents token accumulation
2. **Database Validation**: Tokens validated against database records
3. **Automatic Cleanup**: Expired tokens don't consume resources
4. **Foreign Key Constraints**: Tokens deleted when user deleted
5. **Indexed Queries**: Fast lookups don't create performance issues

## Performance Impact

- **Storage**: One token per user (minimal overhead)
- **Query**: Indexed database lookups are fast
- **Cleanup**: Runs asynchronously, doesn't block API calls
- **Scalability**: Efficient for thousands of users

## Logging

All operations logged with timestamps:
```
INFO: Creating/updating refresh token for user: user@example.com
DEBUG: Deleting old refresh token for user: user@example.com
INFO: New refresh token created for user: user@example.com
INFO: Starting cleanup of expired refresh tokens
INFO: Refresh tokens cleanup completed successfully
```

## Future Enhancements

- Token revocation list
- Login session tracking
- Device-based token management
- Token usage analytics
- Suspicious activity detection

---

**Status**: ✅ FULLY IMPLEMENTED  
**Build**: ✅ SUCCESS  
**Ready**: ✅ YES

