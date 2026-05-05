# Secure Access Management System - Detaylı Teknik Dokümantasyon

## İçindekiler
1. [Projenin Genel Çalışma Mantığı](#1-projenin-genel-çalışma-mantığı)
2. [Nesneye Dayalı Programlama (OOP) Prensipleri](#2-nesneye-dayalı-programlama-oop-prensipleri)
3. [Katmanlı Mimari](#3-katmanlı-mimari)
4. [Security (Güvenlik) Yapısı](#4-security-güvenlik-yapısı)

---

## 1. Projenin Genel Çalışma Mantığı

### 1.1 Proje Ne Yapıyor?

**Secure Access Management System**, kurum içindeki kullanıcıların belirli URL'lere erişim yetkilerini merkezi olarak yönetmek ve test etmek için tasarlanmıştır.

**Ana Özellikleri:**
- 👤 **Kimlik Doğrulama**: JWT token tabanlı güvenli login sistemi
- 🔐 **Yetki Denetimi**: Her kullanıcı sadece kendisine izin verilen URL'lere erişebilir
- 📋 **Whitelist Yönetimi**: Admin tarafından kullanıcılar için izinli URL listesi oluşturma
- 🧪 **Policy Simulator**: Admin tarafından herhangi bir kullanıcının bir URL'e erişip erişemeyeceğini test etme
- 📊 **Access Logging**: Tüm erişim denemelerinin kaydı tutma

### 1.2 Kullanıcı Sisteme Nasıl Giriş Yapıyor?

```
1. Kullanıcı tarayıcıda localhost:8080/login'e gider
   ↓
2. E-posta ve şifre girer → "Sign In" butonuna tıklar
   ↓
3. Frontend (JavaScript/Fetch API) → POST /api/auth/login
   ↓
4. Backend AuthController.login() → 
   - UserService.login() çağırır
   - Kullanıcı ve şifre veritabanında doğrulanır
   - Hata varsa: 401 UNAUTHORIZED
   ↓
5. Doğrulama başarılı → JWT Token oluşturulur
   ↓
6. Client JWT Token'ı localStorage'a kaydeder
   ↓
7. Artık her request'in header'ına Authorization: Bearer <TOKEN> eklenir
   ↓
8. Kullanıcı dashboard'a (Users veya Policy Simulator) yönlendirilir
```

### 1.3 Veri Akışı (Request → Backend → Response)

#### Senaryo 1: Kullanıcı bir URL'e erişmek istiyor

```
┌─ Frontend (JavaScript)
│
├─ POST /api/redirect/url
│  Header: Authorization: Bearer eyJhbGc...
│  Body: {
│    "url": "https://github.com/",
│    "httpMethod": "GET"
│  }
│
├─ Backend RedirectController.redirectToUrl()
│  ├─ Token'ı header'dan çıkart
│  ├─ Token'ı doğrula (JwtAuthenticationProvider.authenticate())
│  ├─ Token'ın süresi dolmuş mı kontrol et
│  ├─ URL'e erişim yetkisi var mı? (authorize())
│  │  ├─ Admin mı? → Her URL'ye izin ver
│  │  └─ Kullanıcı mı? → Whitelist'te kontrol et
│  ├─ Access Log kaydı oluştur
│  └─ Response döndür
│
└─ Frontend
   ├─ Başarılı: "Access Granted" + URL'yi aç
   └─ Başarısız: "Access Denied" + hata mesajı
```

#### Senaryo 2: Admin Policy Simulator ile test yapıyor

```
┌─ Admin tarayıcısında
│
├─ POST /api/redirect/url
│  Header: Authorization: Bearer <admin_token>
│  Body: {
│    "url": "https://github.com/",
│    "httpMethod": "GET",
│    "userId": 2  ← Admin başka kullanıcıyı test etmek istiyor!
│  }
│
├─ Backend RedirectController.redirectToUrl()
│  ├─ Token'ı doğrula (Admin mı?)
│  ├─ "userId" parametresi var mı?
│  ├─ Evet → targetUser = userId ile kullanıcıyı getir
│  ├─ Hayır → targetUser = admin
│  ├─ targetUser'ın URL'ye erişim yetkisi var mı?
│  └─ Sonuç döndür
│
└─ Admin Policy Simulator'da sonuç görüntülenir
```

### 1.4 Sistem Adım Adım Nasıl Çalışıyor?

#### **Adım 1: Uygulama Başlıyor**
```
DataInitializer.java → ApplicationRunner Bean
  ├─ admin kullanıcısı oluştur
  ├─ user1, user2 kullanıcıları oluştur
  ├─ user1 için whitelist URL'leri ekle:
  │  ├─ https://www.google.com (GET)
  │  └─ https://www.github.com (GET)
  └─ user2 için whitelist URL'leri ekle:
     ├─ https://www.stackoverflow.com (GET)
     └─ https://www.linkedin.com (GET)
```

#### **Adım 2: Kullanıcı Login Yapar**
```
1. LoginRequestDTO ile email + şifre gönderilir
2. UserServiceImpl.login() çalışır:
   - Kullanıcı veritabanında aranır
   - Şifre ile eşleşiyor mu? (PasswordEncoder.matches())
   - Kullanıcı aktif mi?
3. Başarılı → JwtTokenProvider.generateToken(username)
4. JWT Token oluşturulur:
   - Payload: username, issuedAt, expirationTime
   - Secret key ile imzalanır
5. LoginResponseDTO döndürülür:
   {
     "message": "Login successful",
     "user": { id, username, email, role },
     "success": true,
     "token": "eyJhbGc..."
   }
```

#### **Adım 3: Kullanıcı Bir URL'e Erişmeye Çalışır**
```
1. Frontend → POST /api/redirect/url
   Authorization: Bearer eyJhbGc...

2. RedirectController.redirectToUrl() → Token işleniyor
   ├─ Token'ı header'dan çıkar: "Bearer " ön eki kaldırıl

ır
   ├─ JwtAuthenticationProvider.authenticate(token):
   │  ├─ JwtTokenProvider.getUsernameFromToken(token)
   │  └─ UserRepository'den kullanıcı getir
   └─ user = User entity

3. JwtAuthenticationProvider.authorize(user, url, httpMethod)
   ├─ Admin mı? → return true (Her URL'ye izin)
   ├─ Normal kullanıcı mı?
   │  ├─ URL'i normalize et (normalizeUrl()):
   │  │  - www. ön ekini kaldır
   │  │  - Trailing slash'i kaldır
   │  │  - Lowercase domain
   │  ├─ URLWhitelistRepository.findByUserAndIsActiveTrue(user)
   │  ├─ Whitelist'te normalized URL var mı?
   │  └─ return true/false
   └─ return result

4. Erişim Sonucu:
   ├─ Başarılı (true):
   │  ├─ AccessLog kaydı: status = ALLOWED
   │  ├─ ResponseEntity.ok()
   │  └─ { success: true, message: "...", redirectUrl: "..." }
   └─ Başarısız (false):
      ├─ AccessLog kaydı: status = DENIED
      ├─ ResponseEntity.status(FORBIDDEN)
      └─ { success: false, message: "Bu URL'e erişim izniniz yok" }

5. Frontend:
   ├─ Başarılı → URL'yi yeni sekmede aç
   └─ Başarısız → "Access Denied" göster
```

#### **Adım 4: Access Log Kaydı**
```
AccessLog Entity kaydedilir:
{
  id: auto-generated,
  user: user entity,
  requestedUrl: "https://github.com/",
  httpMethod: "GET",
  status: "ALLOWED" | "DENIED" | "INVALID_TOKEN" | "EXPIRED_TOKEN",
  reason: "Erişime izin verildi" | "Bu URL'e erişim izniniz yok",
  ipAddress: "192.168.1.1",
  createdAt: now(),
  updatedAt: now()
}
```

---

## 2. Nesneye Dayalı Programlama (OOP) Prensipleri

### 2.1 ENCAPSULATION (Kapsülleme)

**Tanım:** Verileri private olarak tutmak ve sadece kontrollü getter/setter metotları aracılığıyla erişim sağlamak.

**Proje Örneği:**

```java
// User.java - Entity sınıfında kapsülleme
@Entity
public class User extends BaseEntity {
    
    @NotBlank(message = "Username cannot be blank")
    @Column(unique = true, nullable = false)
    private String username;  // ← Private! Doğrudan erişim yok
    
    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;   // ← Private! Sadece @Getter/@Setter üzerinden
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Column(unique = true, nullable = false)
    private String email;      // ← Private!
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;  // ← Private!
    
    // Lombok @Getter/@Setter otomatik olarak getter/setterlar oluşturur
}

// Kullanım:
User user = new User("john", "password123", "john@example.com", Role.USER);
user.setUsername("jane");  // ✅ Setter üzerinden erişim
String username = user.getUsername();  // ✅ Getter üzerinden erişim
// user.username = "jane";  // ❌ Doğrudan erişim yapılamaz!
```

**Neden İyi?**
- ✅ Veri bütünlüğü korunur (validation yapılabilir)
- ✅ İç yapı değiştiğinde dış kod etkilenmez
- ✅ Kontrollü veri değişimi

---

### 2.2 INHERITANCE (Kalıtım)

**Tanım:** Ortak özellikleri bir parent sınıfta toplamak ve alt sınıflardan miras almak.

**Proje Örneği:**

```java
// BaseEntity.java - Tüm Entity'lerin parent'ı
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ← Tüm entity'lerde ortak
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // ← Tüm entity'lerde ortak
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;  // ← Tüm entity'lerde ortak
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

// ─────────────────────────────────

// User.java - BaseEntity'den türeyen
@Entity
public class User extends BaseEntity {  // ← Kalıtım!
    private String username;
    private String password;
    private String email;
    // id, createdAt, updatedAt özellikleri miras alır!
}

// URLWhitelist.java - BaseEntity'den türeyen
@Entity
public class URLWhitelist extends BaseEntity {  // ← Kalıtım!
    private User user;
    private String url;
    private String httpMethod;
    // id, createdAt, updatedAt özellikleri miras alır!
}

// AccessLog.java - BaseEntity'den türeyen
@Entity
public class AccessLog extends BaseEntity {  // ← Kalıtım!
    private User user;
    private String requestedUrl;
    private AccessStatus status;
    // id, createdAt, updatedAt özellikleri miras alır!
}
```

**Hiyerarşi:**
```
BaseEntity (Parent)
    ├── User (Child)
    ├── URLWhitelist (Child)
    └── AccessLog (Child)
```

**Neden İyi?**
- ✅ Kod tekrarını önler (DRY - Don't Repeat Yourself)
- ✅ Ortak güncelleme merkezi (createdAt mantığı)
- ✅ Tutarlı veri tabanı tasarımı

---

### 2.3 POLYMORPHISM (Çok Biçimlilik)

**Tanım:** Aynı interface'i farklı şekillerde implement etmek.

**Proje Örneği:**

#### **Interface Türü: UserService**
```java
// UserService.java - Interface
public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO createUser(UserRequestDTO requestDTO);
    UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO);
    void deleteUser(Long id);
    UserResponseDTO login(LoginRequestDTO loginRequest);
    void resetPassword(String username, String email, String newPassword);
}

// ─────────────────────────────────

// UserServiceImpl.java - Polymorphic Implementation
@Service
public class UserServiceImpl implements UserService {  // ← Polymorphism!
    
    @Override
    public List<UserResponseDTO> getAllUsers() {
        // User Service'in tüm kullanıcıları getirmesi
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        // User Service'in login işlemi
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        
        return convertToDTO(user);
    }
}
```

#### **Abstract Class Türü: AbstractAuthenticationProvider**
```java
// AbstractAuthenticationProvider.java - Abstract Parent
public abstract class AbstractAuthenticationProvider {
    
    public abstract User authenticate(String token);
    
    public abstract boolean authorize(User user, String requestedUrl, String httpMethod);
    
    public abstract String extractUsername(String token);
    
    public abstract boolean isTokenExpired(String token);
    
    public abstract boolean isTokenValid(String token);
}

// ─────────────────────────────────

// JwtAuthenticationProvider.java - Polymorphic Implementation
@Component
public class JwtAuthenticationProvider extends AbstractAuthenticationProvider {  // ← Polymorphism!
    
    @Override
    public User authenticate(String token) {
        // JWT özel implementasyonu
        String username = jwtTokenProvider.getUsernameFromToken(token);
        return userRepository.findByUsername(username).orElse(null);
    }
    
    @Override
    public boolean authorize(User user, String requestedUrl, String httpMethod) {
        // JWT özel authorization mantığı + URL normalizasyonu
        if (user == null || user.getRole().toString().equals("ADMIN")) {
            return true;
        }
        
        String normalizedRequestedUrl = normalizeUrl(requestedUrl);
        var whitelistUrls = whitelistRepository.findByUserAndIsActiveTrue(user);
        return whitelistUrls.stream()
                .anyMatch(whitelist -> 
                    normalizeUrl(whitelist.getUrl()).equals(normalizedRequestedUrl) &&
                    whitelist.getHttpMethod().equals(httpMethod)
                );
    }
    
    @Override
    public String extractUsername(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
    
    @Override
    public boolean isTokenExpired(String token) {
        return jwtTokenProvider.isTokenExpired(token);
    }
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String normalizeUrl(String url) {
        // URL Normalizasyonu
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost().toLowerCase();
            String path = parsedUrl.getPath();
            
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            
            if (path.equals("/")) {
                path = "";
            }
            
            return protocol + "://" + host + path;
        } catch (java.net.MalformedURLException e) {
            return url;
        }
    }
}
```

**Kullanım:**
```java
// Controller'da Polymorphism'in gücü:
@RestController
public class AuthController {
    
    private final AbstractAuthenticationProvider authProvider;  // ← Interface!
    
    public void redirectToUrl(String token, String url) {
        User user = authProvider.authenticate(token);  // ← Hangi implement edilecek?
        boolean hasAccess = authProvider.authorize(user, url, "GET");  // ← Hangi implement edilecek?
        // Cevap: Runtime'da JwtAuthenticationProvider implementasyonu çalışacak!
    }
}
```

**Neden İyi?**
- ✅ Farklı authentication mekanizmaları eklenebilir (OAuth, SAML, vb.)
- ✅ Interface sadece kontrat tanımlar, implementasyon esnek
- ✅ Dependency Injection ile runtime'da mekanizma değiştirilebilir

---

### 2.4 ABSTRACTION (Soyutlama)

**Tanım:** Kompleks iş mantığını gizleyip sadece gerekli interface'i dışarıya sunmak.

**Proje Örneği:**

#### **1. Service Layer'ın Soyutlaması**
```java
// AuthController.java - Soyutlama kullanan kod
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;  // ← Sadece interface
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // Detaylar gizli! Sadece login() çağırıyoruz
            UserResponseDTO user = userService.login(loginRequest);
            String token = jwtTokenProvider.generateToken(user.getUsername());
            
            return ResponseEntity.ok(new LoginResponseDTO(
                    "Login successful",
                    user,
                    true,
                    token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDTO(e.getMessage(), null, false, null));
        }
    }
}

// ────────────────────────────────────

// UserServiceImpl.java - Karmaşık implementasyon gizleniyor
@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        // ← Controller bunu bilmiyor:
        // 1. Kullanıcı veritabanında aranır
        // 2. Şifre hashleniyor ve karşılaştırılıyor
        // 3. Access log kaydı tutulabiliyor
        // 4. Kullanıcı aktiflik durumu kontrol ediliyor
        // ... VS
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().toString());
    }
}
```

**Flow:**
```
AuthController                    UserService (Interface)        UserServiceImpl (Gerçek İş Mantığı)
     │                                 │                              │
     ├─ userService.login(dto)────────>│                              │
     │                                 ├─ repository.findByUser()────>│
     │                                 │<────User entity───────────────│
     │                                 ├─ passwordEncoder.matches()──>│
     │                                 │<────true/false────────────────│
     │                                 ├─ UserResponseDTO convert────>│
     │                                 │<────DTO────────────────────────│
     │<──────UserResponseDTO─────────────────────────────────────────
```

#### **2. Security Layer'ın Soyutlaması**
```java
// RedirectController.java - Güvenlik detayları gizlenmiş
@RestController
@RequestMapping("/api/redirect")
public class RedirectController {
    
    private final JwtAuthenticationProvider authProvider;  // ← Soyut provider
    
    @PostMapping("/url")
    public ResponseEntity<RedirectResponseDTO> redirectToUrl(
            @RequestBody RedirectRequestDTO redirectRequest,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = extractTokenFromHeader(authHeader);
        User user = authProvider.authenticate(token);  // ← Token doğrulama gizli
        
        if (authProvider.isTokenExpired(token)) {  // ← Expiration check gizli
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RedirectResponseDTO(false, "Token süresi dolmuş", null));
        }
        
        if (!authProvider.authorize(user, redirectRequest.getUrl(), "GET")) {  // ← Authorization gizli
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RedirectResponseDTO(false, "Bu URL'e erişim izniniz yok", null));
        }
        
        return ResponseEntity.ok()
                .body(new RedirectResponseDTO(true, "Erişime izin verildi", redirectRequest.getUrl()));
    }
}

// ────────────────────────────────────

// JwtAuthenticationProvider.java - Karmaşık detaylar gizleniyor
public class JwtAuthenticationProvider extends AbstractAuthenticationProvider {
    
    @Override
    public User authenticate(String token) {
        // ← RedirectController bunu bilmiyor:
        // 1. Token format doğrulaması
        // 2. Token signature doğrulaması
        // 3. JWT claim'leri parse ediliyor
        // 4. Username token'dan çıkarılıyor
        // 5. Veritabanında kullanıcı aranıyor
        // ... VS
        
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean authorize(User user, String requestedUrl, String httpMethod) {
        // ← RedirectController bunu bilmiyor:
        // 1. Admin mı normal user mı kontrol
        // 2. URL'i normalize etme
        // 3. Whitelist'ten sorgulanma
        // 4. HTTP method karşılaştırması
        // ... VS
        
        if (user == null || user.getRole().toString().equals("ADMIN")) {
            return true;
        }
        
        String normalizedRequestedUrl = normalizeUrl(requestedUrl);
        return whitelistRepository.findByUserAndIsActiveTrue(user).stream()
                .anyMatch(whitelist -> 
                    normalizeUrl(whitelist.getUrl()).equals(normalizedRequestedUrl) &&
                    whitelist.getHttpMethod().equals(httpMethod)
                );
    }
}
```

**Neden İyi?**
- ✅ Controller'lar karışık değildir, okunması kolay
- ✅ Değişiklik yapılırken sadece implementation'ı değiştiririz
- ✅ Testing daha kolay (mock objeleri kullanabilir)

---

## 3. Katmanlı Mimari (Layered Architecture)

### Mimari Diyagramı

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                    │
│  (Frontend: HTML, CSS, JavaScript - localhost:8080)    │
└──────────────────┬──────────────────────────────────────┘
                   │ HTTP Requests/Responses
                   │
┌──────────────────▼──────────────────────────────────────┐
│              CONTROLLER LAYER                           │
│ (AuthController, UserController, RedirectController)  │
│ - Request'i işler                                       │
│ - Service'i çağırır                                     │
│ - Response hazırlar                                     │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│             SERVICE LAYER                               │
│  (UserService, AccessLogService)                       │
│ - İş mantığı (Business Logic)                           │
│ - Entity'leri DTO'ya dönüştürme                        │
│ - Validasyon ve hata yönetimi                           │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│         SECURITY LAYER (AOP)                            │
│  (AbstractAuthenticationProvider, JwtAuthenticationProvider) │
│ - Authentication                                        │
│ - Authorization                                         │
│ - Token yönetimi                                        │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│          REPOSITORY LAYER (DAO Pattern)                 │
│  (UserRepository, URLWhitelistRepository, AccessLogRepository) │
│ - Veritabanı sorgularını işler                         │
│ - JpaRepository'den extends eder                        │
│ - Entity'ler üzerinde CRUD işlemleri                   │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│              DATABASE LAYER                             │
│  (H2 In-Memory Database)                               │
│ - Tüm verileri saklar                                  │
└──────────────────────────────────────────────────────────┘
```

---

### 3.1 CONTROLLER LAYER

#### **Görevi:**
- HTTP request'leri alır
- Request'in parametrelerini işler
- Service'i çağırır
- Service'ten dönen sonucu HTTP response'a dönüştürür
- Error handling

#### **Proje Dosyaları:**
- `AuthController.java` - Authentication işlemleri
- `UserController.java` - User CRUD işlemleri
- `RedirectController.java` - URL erişim kontrol işlemleri
- `WhitelistController.java` - Whitelist yönetimi

#### **Kod Örneği:**

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {  // ← Request body
        try {
            // Service'i çağır
            UserResponseDTO user = userService.login(loginRequest);
            
            // Token oluştur
            String token = jwtTokenProvider.generateToken(user.getUsername());
            
            // Response hazırla
            LoginResponseDTO response = new LoginResponseDTO(
                    "Login successful",
                    user,
                    true,
                    token);
            
            // HTTP 200 OK döndür
            return ResponseEntity.ok(response);
            
        } catch (InvalidCredentialsException e) {
            // Error response hazırla
            LoginResponseDTO response = new LoginResponseDTO(
                    e.getMessage(),
                    null,
                    false,
                    null);
            
            // HTTP 401 UNAUTHORIZED döndür
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }
}

// HTTP Flow:
// POST http://localhost:8080/api/auth/login
// Content-Type: application/json
//
// Request Body:
// {
//   "username": "user1",
//   "password": "password123"
// }
//
// Response:
// HTTP 200 OK
// {
//   "message": "Login successful",
//   "user": { "id": 2, "username": "user1", ... },
//   "success": true,
//   "token": "eyJhbGc..."
// }
```

---

### 3.2 SERVICE LAYER

#### **Görevi:**
- İş mantığını (Business Logic) içerir
- Entity'leri DTO'ya dönüştürür
- Validasyon ve error handling
- Database işlemlerini organize eder
- Transaction yönetimi

#### **Proje Dosyaları:**
- `UserService.java` (Interface)
- `UserServiceImpl.java` (Implementation)
- `AccessLogService.java` (Interface)
- `AccessLogServiceImpl.java` (Implementation)

#### **Kod Örneği:**

```java
// UserService.java - Interface
public interface UserService {
    UserResponseDTO login(LoginRequestDTO loginRequest);
    UserResponseDTO createUser(UserRequestDTO requestDTO);
    List<UserResponseDTO> getAllUsers();
}

// ─────────────────────────────────

// UserServiceImpl.java - Implementation
@Service
@Transactional  // ← Transaction yönetimi
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        // 1. Validasyon
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new InvalidCredentialsException("Username ve password boş olamaz");
        }
        
        // 2. Veritabanında sorgu (Repository)
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı"));
        
        // 3. Şifre doğrulama
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Şifre hatalı");
        }
        
        // 4. Kullanıcı aktif mi?
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Kullanıcı deaktif");
        }
        
        // 5. Entity'i DTO'ya dönüştür
        return convertToDTO(user);
    }
    
    @Override
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        // 1. Şifre validasyonu
        validatePassword(requestDTO.getPassword());
        
        // 2. Kullanıcı zaten var mı?
        if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username zaten var");
        }
        
        // 3. Email zaten var mı?
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email zaten var");
        }
        
        // 4. Yeni User entity oluştur
        User user = new User(
                requestDTO.getUsername(),
                passwordEncoder.encode(requestDTO.getPassword()),  // ← Şifre hash'lenir
                requestDTO.getEmail(),
                requestDTO.getRole());
        
        // 5. Veritabanına kaydet
        user = userRepository.save(user);
        
        // 6. DTO'ya dönüştür ve döndür
        return convertToDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()  // Repository'den tüm user'ları getir
                .stream()
                .map(this::convertToDTO)  // DTO'ya dönüştür
                .collect(Collectors.toList());
    }
    
    // Yardımcı method
    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().toString());
    }
}
```

---

### 3.3 REPOSITORY LAYER (DAO Pattern)

#### **Görevi:**
- Veritabanı ile iletişimi sağlama
- CRUD (Create, Read, Update, Delete) işlemleri
- Sorguları yazma (Query Methods)
- Entity'leri veritabanında yönetme

#### **Proje Dosyaları:**
- `UserRepository.java`
- `URLWhitelistRepository.java`
- `AccessLogRepository.java`

#### **Kod Örneği:**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom Query Methods
    // JPA otomatik olarak SQL sorgusunu oluşturur!
    
    // Metod adından SQL oluşturulur: SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);
    
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    // SELECT * FROM users WHERE role = ? ORDER BY created_at DESC
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
}

// ─────────────────────────────────

@Repository
public interface URLWhitelistRepository extends JpaRepository<URLWhitelist, Long> {
    
    // SELECT * FROM url_whitelist WHERE user_id = ? AND is_active = true
    List<URLWhitelist> findByUserAndIsActiveTrue(User user);
    
    // SELECT COUNT(*) > 0 FROM url_whitelist 
    // WHERE user_id = ? AND url = ? AND http_method = ?
    boolean existsByUserAndUrlAndHttpMethod(User user, String url, String httpMethod);
    
    // SELECT * FROM url_whitelist WHERE is_active = true ORDER BY created_at DESC
    List<URLWhitelist> findByIsActiveTrueOrderByCreatedAtDesc();
}

// ─────────────────────────────────

// Service'te kullanımı
@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        // Repository method çağrı
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        return convertToDTO(user);
    }
}
```

**JpaRepository Sağlanan Built-in Methods:**
```java
// CREATE
repository.save(user);                          // 1 user kaydet
repository.saveAll(userList);                   // Çok user kaydet

// READ
repository.findById(1L);                        // ID ile getir
repository.findAll();                           // Tümünü getir
repository.count();                             // Toplam sayıyı getir
repository.existsById(1L);                      // ID var mı?

// UPDATE
User user = repository.findById(1L).get();
user.setEmail("newemail@example.com");
repository.save(user);                          // Güncellenmiş haliyle kaydet

// DELETE
repository.deleteById(1L);                      // ID ile sil
repository.delete(user);                        // User objesini sil
repository.deleteAll();                         // Tümünü sil
```

---

### 3.4 ENTITY LAYER

#### **Görevi:**
- Veritabanı tablosunu Java sınıfı olarak temsil etme
- Özellikleri (columns) tanımlama
- İlişkileri (relations) tanımlama
- Validation kurallarını belirtme

#### **Proje Dosyaları:**
- `User.java` - Kullanıcı verisi
- `URLWhitelist.java` - İzinli URL'ler
- `AccessLog.java` - Erişim kayıtları
- `BaseEntity.java` - Ortak özellikler

#### **Kod Örneği:**

```java
// User Entity - "users" tablosunu temsil eder
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @NotBlank(message = "Username cannot be blank")
    @Column(unique = true, nullable = false)
    private String username;           // users.username column
    
    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;           // users.password column
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Column(unique = true, nullable = false)
    private String email;              // users.email column
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;     // users.role column
    
    @Column(nullable = false)
    private Boolean isActive = true;   // users.is_active column
}

// Veritabanı tablosu otomatik oluşturulur:
// CREATE TABLE users (
//     id BIGINT PRIMARY KEY AUTO_INCREMENT,
//     username VARCHAR(255) NOT NULL UNIQUE,
//     password VARCHAR(255) NOT NULL,
//     email VARCHAR(255) NOT NULL UNIQUE,
//     role VARCHAR(255) NOT NULL,
//     is_active BOOLEAN NOT NULL,
//     created_at TIMESTAMP NOT NULL,
//     updated_at TIMESTAMP NOT NULL
// );

// ─────────────────────────────────

// URLWhitelist Entity - "url_whitelist" tablosunu temsil eder
@Entity
@Table(name = "url_whitelist")
public class URLWhitelist extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                 // Foreign Key: users.id
    
    @NotBlank(message = "URL cannot be blank")
    @Column(nullable = false, length = 500)
    private String url;                // url_whitelist.url column
    
    @Column(nullable = false, length = 50)
    private String httpMethod;         // url_whitelist.http_method column
    
    @NotBlank(message = "Description cannot be blank")
    @Column(length = 255)
    private String description;        // url_whitelist.description column
    
    @Column(nullable = false)
    private Boolean isActive = true;   // url_whitelist.is_active column
}

// Veritabanı tablosu:
// CREATE TABLE url_whitelist (
//     id BIGINT PRIMARY KEY AUTO_INCREMENT,
//     user_id BIGINT NOT NULL,
//     url VARCHAR(500) NOT NULL,
//     http_method VARCHAR(50) NOT NULL,
//     description VARCHAR(255),
//     is_active BOOLEAN NOT NULL,
//     created_at TIMESTAMP NOT NULL,
//     updated_at TIMESTAMP NOT NULL,
//     FOREIGN KEY (user_id) REFERENCES users(id)
// );
```

---

### 3.5 DTO LAYER (Data Transfer Object)

#### **Görevi:**
- Entity'i client'a göndermeden önce dönüştürme
- Hassas veriler (şifre, vb.) gizleme
- API response/request formatını standartlaştırma
- Entity ile DTO arasında mapping

#### **Proje Dosyaları:**
- `LoginRequestDTO.java`
- `LoginResponseDTO.java`
- `UserRequestDTO.java`
- `UserResponseDTO.java`
- `RedirectRequestDTO.java`
- `URLWhitelistRequestDTO.java`

#### **Kod Örneği:**

```java
// ─────── REQUEST SIDE ────────

// Client gönderir (Frontend)
// POST /api/auth/login
// {
//   "username": "user1",
//   "password": "password123"
// }

@Getter
@Setter
public class LoginRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}

// ─────── RESPONSE SIDE ────────

// Server gönderir (Backend)
// HTTP 200 OK
// {
//   "message": "Login successful",
//   "user": { ... },
//   "success": true,
//   "token": "eyJhbGc..."
// }

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String message;
    private UserResponseDTO user;
    private boolean success;
    private String token;
}

// ─────── Entity vs DTO Karşılaştırması ────────

// Entity (Veritabanı)
@Entity
public class User {
    private Long id;
    private String username;
    private String password;      // ← Veritabanında (encrypted)
    private String email;
    private Role role;
    private Boolean isActive;
}

// DTO (API)
@Getter
public class UserResponseDTO {
    private Long id;
    private String username;
    // private String password;   ← DTO'da YOKSUN! (Güvenlik)
    private String email;
    private String role;
}

// Neden DTO?
// ✅ Hassas veriler gizlenir (şifre, internal flags)
// ✅ API'nin public interface'ini tanımlar
// ✅ Entity internal yapısı değişirse API etkilenmez
// ✅ Version kontrolü kolay (API v1, v2, vb.)
// ✅ Frontend'e sadece ihtiyaç olan veriler gönderilir
```

---

## 4. Security (Güvenlik) Yapısı

### 4.1 Authentication (Kimlik Doğrulama)

**Tanım:** Kullanıcının "kim" olduğunu doğrulama (username/password).

#### **Akış:**

```
Frontend
    │
    ├─ Username: "user1"
    ├─ Password: "password123"
    └─ POST /api/auth/login
       │
       └─ Backend
          │
          ├─ AuthController.login()
          ├─ UserService.login(loginRequest)
          │  │
          │  ├─ Step 1: Veritabanında user ara
          │  │  UserRepository.findByUsername("user1")
          │  │  → User entity döner
          │  │
          │  ├─ Step 2: Şifre karşılaştır
          │  │  PasswordEncoder.matches("password123", "$2a$10$abc...")
          │  │  → true/false
          │  │
          │  └─ Step 3: Başarılı oldu
          │     → UserResponseDTO (şifre yoktur!)
          │
          ├─ JwtTokenProvider.generateToken("user1")
          │  → JWT Token oluştur
          │
          └─ LoginResponseDTO gönder
             {
               "success": true,
               "token": "eyJhbGc...",
               "user": { "id": 2, "username": "user1", ... }
             }
    │
    └─ Frontend
       ├─ Token'ı localStorage'a kaydet
       └─ Authorization header'ında kullan
```

#### **Kod İmplementasyonu:**

```java
// 1. Login Request
@PostMapping("/login")
public ResponseEntity<LoginResponseDTO> login(
        @Valid @RequestBody LoginRequestDTO loginRequest) {
    
    UserResponseDTO user = userService.login(loginRequest);  // ← Authentication
    String token = jwtTokenProvider.generateToken(user.getUsername());
    
    return ResponseEntity.ok(new LoginResponseDTO(
            "Login successful",
            user,
            true,
            token));
}

// 2. UserService.login() - Gerçek doğrulama
@Override
public UserResponseDTO login(LoginRequestDTO loginRequest) {
    // Step 1: Kullanıcı veritabanında var mı?
    User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    
    // Step 2: Şifre doğru mu?
    // PasswordEncoder hash'lenmiş şifreyle karşılaştırır
    if (!passwordEncoder.matches(
            loginRequest.getPassword(),  // Gelen şifre (plain text)
            user.getPassword())) {        // DB'de hash'lenmiş şifre
        throw new InvalidCredentialsException("Invalid password");
    }
    
    // Step 3: Başarılı - DTO döndür
    return convertToDTO(user);
}

// 3. Şifre Hash'leme
// Bir kez yapılır (registration sırasında)
String hashedPassword = passwordEncoder.encode("password123");
// Sonuç: "$2a$10$Ro9CvDkGo..." (BCrypt)

// Karşılaştırma
boolean matches = passwordEncoder.matches("password123", "$2a$10$Ro9CvDkGo...");
// true (şifre doğru)
```

### 4.2 JWT Token Yönetimi

**JWT (JSON Web Token) Nedir?**

JWT, kullanıcı bilgisini encode eden stateless authentication token'ıdır.

```
JWT Token Yapısı:
╔════════════╦══════════════════════╦═════════════════╗
║  HEADER    ║      PAYLOAD         ║    SIGNATURE    ║
╚════════════╩══════════════════════╩═════════════════╝

Header:                 Payload:                    Signature:
{                       {                           HMACSHA256(
  "alg": "HS256",       "username": "user1",        base64(header) + "." +
  "typ": "JWT"          "iat": 1683043200,          base64(payload),
}                       "exp": 1683046800           secret_key
                      }                             )

Örnek Token:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InVzZXIxIiwiaWF0IjoxNjgzMDQzMjAwLCJleHAiOjE2ODMwNDY4MDB9.1ab2cd3ef...
```

#### **JWT Oluşturma:**

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationInMillis;
    
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMillis);
        
        return Jwts.builder()
                .setSubject(username)        // ← Kimlik
                .setIssuedAt(now)            // ← Token oluşturma zamanı
                .setExpiration(expiryDate)   // ← Expiration zamanı
                .signWith(SignatureAlgorithm.HS512, jwtSecret)  // ← İmza
                .compact();
    }
    
    // Dönen Token Örneği:
    // eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTY4MzA0MzIwMCwiZXhwIjoxNjgzMDQ2ODAwfQ.abc123...
}
```

#### **JWT Doğrulama:**

```java
public User authenticate(String token) {
    try {
        // Step 1: Token'ı validate et
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        // Step 2: Username'den User'ı getir
        return userRepository.findByUsername(username)
                .orElse(null);
    } catch (Exception e) {
        return null;  // Token invalid
    }
}

public boolean isTokenExpired(String token) {
    try {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());  // Süresi dolmuş mı?
    } catch (ExpiredJwtException e) {
        return true;  // Süresi dolmuş
    }
}
```

#### **Frontend'te Token Kullanımı:**

```javascript
// 1. Login sırasında token'ı al
const response = await fetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
        username: 'user1',
        password: 'password123'
    })
});

const data = await response.json();
const token = data.token;  // "eyJhbGc..."

// 2. Token'ı localStorage'a kaydet
localStorage.setItem('jwtToken', token);

// 3. Sonraki request'lerde Authorization header'ına ekle
const redirectResponse = await fetch('/api/redirect/url', {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer ' + token,  // ← Token eklendi!
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        url: 'https://github.com/',
        httpMethod: 'GET'
    })
});
```

---

### 4.3 Authorization (Yetki Denetimi)

**Tanım:** Doğrulanan kullanıcının "ne" yapabileceğini kontrol etme.

#### **Proje'de Authorization:**

```
┌─────────────────────────────────────────────────────┐
│  User istektedir: https://github.com/'e erişmek    │
└─────────────────────────────────────────────────────┘
         │
         └─ POST /api/redirect/url
            Header: Authorization: Bearer <TOKEN>
            Body: { "url": "https://github.com/", ... }
            │
            └─ RedirectController.redirectToUrl()
               │
               ├─ authenticate(token)
               │  └─ User.id = 2, User.role = USER
               │
               └─ authorize(user, "https://github.com/", "GET")
                  │
                  ├─ Step 1: Admin mı?
                  │  User.role = USER → Hayır
                  │
                  ├─ Step 2: Whitelist'te kontrol et
                  │  URLWhitelistRepository.findByUserAndIsActiveTrue(user)
                  │  [
                  │    { url: "https://www.google.com", method: "GET" },
                  │    { url: "https://www.github.com", method: "GET" }  ← Bulduk!
                  │  ]
                  │
                  ├─ Step 3: URL karşılaştır (normalized)
                  │  Input: "https://github.com/"
                  │  Normalized: "https://github.com"
                  │  
                  │  Whitelist: "https://www.github.com"
                  │  Normalized: "https://github.com"
                  │
                  │  ✅ MATCH!
                  │
                  └─ return true
                     │
                     └─ Response: { success: true, message: "..." }
```

#### **Kod İmplementasyonu:**

```java
public boolean authorize(User user, String requestedUrl, String httpMethod) {
    // Case 1: Null user
    if (user == null) {
        return false;
    }
    
    // Case 2: Admin - Herkese izin ver
    if (user.getRole().toString().equals("ADMIN")) {
        return true;  // ← Admin tüm URL'lere erişebilir
    }
    
    // Case 3: Normal user - Whitelist kontrol et
    String normalizedRequestedUrl = normalizeUrl(requestedUrl);
    
    // Kullanıcının whitelist'indeki URL'leri getir
    var whitelistUrls = whitelistRepository.findByUserAndIsActiveTrue(user);
    
    // Normalize edilmiş URL ve HTTP method karşılaştır
    return whitelistUrls.stream()
            .anyMatch(whitelist -> 
                normalizeUrl(whitelist.getUrl()).equals(normalizedRequestedUrl) &&
                whitelist.getHttpMethod().equals(httpMethod)
            );
}

// URL Normalizasyonu Örneği
normalizeUrl("https://www.github.com/")
  → "https://github.com"

normalizeUrl("https://GITHUB.COM")
  → "https://github.com"

normalizeUrl("https://github.com/user/repo")
  → "https://github.com/user/repo"
```

---

### 4.4 Admin vs Normal Kullanıcı Ayrımı

#### **Database Level:**

```sql
-- Users tablosu
┌────┬──────────┬──────────┬─────────────────┬──────┬──────────┐
│ id │ username │ password │ email           │ role │ is_active│
├────┼──────────┼──────────┼─────────────────┼──────┼──────────┤
│ 1  │ admin    │ $2a$... │ admin@example   │ ADMIN│ true     │
│ 2  │ user1    │ $2a$... │ user1@example   │ USER │ true     │
│ 3  │ user2    │ $2a$... │ user2@example   │ USER │ true     │
└────┴──────────┴──────────┴─────────────────┴──────┴──────────┘
```

#### **Uygulama Level (Role Enum):**

```java
public enum Role {
    ADMIN,   // Admin kullanıcı
    USER     // Normal kullanıcı
}

// Usage
User admin = new User("admin", "...", "admin@...", Role.ADMIN);
User user = new User("user1", "...", "user1@...", Role.USER);
```

#### **Authorization Level:**

```java
public boolean authorize(User user, String requestedUrl, String httpMethod) {
    
    // Case 1: Admin
    if (user.getRole().toString().equals("ADMIN")) {
        return true;  // ← Admin her URL'ye erişebilir (whitelist yok)
    }
    
    // Case 2: Normal User
    // Sadece whitelist'teki URL'lere erişebilir
    return whitelistRepository.existsByUserAndUrlAndHttpMethod(
            user, requestedUrl, httpMethod);
}
```

#### **Controller Level (Admin Features):**

```java
@PostMapping("/users/{id}/manage-urls")
@PreAuthorize("hasRole('ADMIN')")  // ← Sadece admin çalıştırabilir
public ResponseEntity<?> manageUserUrls(
        @PathVariable Long id,
        @RequestBody URLWhitelistRequestDTO dto) {
    
    // Sadece admin burada olabilir!
    User targetUser = userRepository.findById(id).orElseThrow();
    URLWhitelist whitelist = new URLWhitelist();
    whitelist.setUser(targetUser);
    whitelist.setUrl(dto.getUrl());
    whitelist.setHttpMethod(dto.getHttpMethod());
    
    whitelistRepository.save(whitelist);
    
    return ResponseEntity.ok("URL whitelist'e eklendi");
}
```

#### **Features Karşılaştırması:**

```
┌──────────────────────┬──────────┬────────────┐
│      Özellik         │  ADMIN   │   USER     │
├──────────────────────┼──────────┼────────────┤
│ Tüm URL'lere erişim  │    ✅    │     ❌     │
│ Whitelist oluştur    │    ✅    │     ❌     │
│ Kullanıcı yönetimi   │    ✅    │     ❌     │
│ Policy Simulator     │    ✅    │     ❌     │
│ Access Logs görüntüle│    ✅    │     ❌     │
├──────────────────────┼──────────┼────────────┤
│ Whitelist'te URL'ye  │    ✅    │    Evet*   │
│ erişim               │          │  (varsa)   │
│ Kendi bilgilerini    │    ✅    │     ✅     │
│ görüntüle            │          │            │
└──────────────────────┴──────────┴────────────┘
```

---

### 4.5 Flow Diyagramı: Login → Authorization

```
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND                              │
│  Email: user1@example.com                                  │
│  Password: ••••••••                                        │
│  [Sign In Button]                                          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
        ┌──────────────────────────────┐
        │  POST /api/auth/login        │
        │  Body: {                     │
        │   "username": "user1",       │
        │   "password": "password123"  │
        │  }                           │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ AuthController.login()       │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ UserService.login()          │
        │ • userRepository.find()      │
        │ • passwordEncoder.matches()  │
        │ • convertToDTO()             │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ JwtTokenProvider.generate()  │
        │ Token oluştur                │
        │ "eyJhbGc..."                │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ LoginResponseDTO             │
        │ {                            │
        │  "token": "eyJhbGc...",     │
        │  "success": true,            │
        │  "user": { ... }             │
        │ }                            │
        └──────────┬───────────────────┘
                   │
                   ▼ (HTTP 200 OK)
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND                              │
│  localStorage.setItem('jwtToken', token)                   │
│  → Dashboard'a yönlendir                                   │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ (Kullanıcı Policy Simulator'ı açıyor)
                 │
                 ▼
        ┌──────────────────────────────┐
        │ POST /api/redirect/url       │
        │ Headers: {                   │
        │   Authorization: Bearer ...  │
        │ }                            │
        │ Body: {                      │
        │   "url": "github.com/",      │
        │   "userId": 2,               │
        │   "httpMethod": "GET"        │
        │ }                            │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ RedirectController.         │
        │   redirectToUrl()            │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ JwtAuthenticationProvider    │
        │   .authenticate(token)       │
        │ → User bulundu! (user1)      │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ JwtAuthenticationProvider    │
        │   .authorize()               │
        │ Admin mi? → Hayır            │
        │ Whitelist'te URL var mı?     │
        │ → Evet! (normalized)         │
        │ → return true                │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ AccessLog kaydı oluştur      │
        │ status: ALLOWED              │
        └──────────┬───────────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ RedirectResponseDTO          │
        │ {                            │
        │  "success": true,            │
        │  "message": "Access ok",    │
        │  "redirectUrl": "github.com" │
        │ }                            │
        └──────────┬───────────────────┘
                   │
                   ▼ (HTTP 200 OK)
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND                              │
│  ✅ Access Granted                                         │
│  [→ Open GitHub]                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Özet: Mimarinin Tüm Parçaları Bir Araya

```
┌──────────────────────────────────────────────────────────────┐
│                   Secure Access System                       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  1. PRESENTATION: Frontend (HTML/JS) → User Interface     │
│                                                              │
│  2. HTTP/REST API → JSON veri transfer                     │
│                                                              │
│  3. CONTROLLER: AuthController, UserController            │
│     - Request işler                                         │
│     - Service çağırır                                       │
│                                                              │
│  4. SERVICE: UserServiceImpl, AccessLogServiceImpl          │
│     - İş mantığı                                            │
│     - DTO conversion                                        │
│                                                              │
│  5. SECURITY:  JwtAuthenticationProvider                   │
│     - Authentication (JWT)                                  │
│     - Authorization (Role + Whitelist)                      │
│                                                              │
│  6. REPOSITORY: UserRepository, WhitelistRepository        │
│     - Database queries                                      │
│     - JPA/Hibernate                                         │
│                                                              │
│  7. ENTITY: User, URLWhitelist, AccessLog                 │
│     - ORM mapping                                           │
│     - Database structure                                    │
│                                                              │
│  8. DATABASE: H2 In-Memory                                 │
│     - Data persistence                                      │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│  OOP Prensipleri: Encapsulation, Inheritance,              │
│                  Polymorphism, Abstraction                  │
│                                                              │
│  Design Patterns: MVC, DAO, DTO, Strategy                 │
└──────────────────────────────────────────────────────────────┘
```

---

## Kaynaklar ve Notlar

- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **Spring Security:** https://spring.io/projects/spring-security
- **JWT (JSON Web Tokens):** https://jwt.io/
- **JPA/Hibernate:** https://hibernate.org/
- **Layered Architecture:** https://en.wikipedia.org/wiki/Multitier_architecture
