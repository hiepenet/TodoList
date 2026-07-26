# 📱 SMART TODO - Android Native App

> Ứng dụng quản lý công việc thông minh được xây dựng bằng **Java + XML**, kiến trúc **MVVM**, tích hợp **Firebase**, **Cloudinary Storage**, **App Widgets** và **Material Design 3**.

---

## 👥 Phân công công việc nhóm (4 thành viên)

> 📖 *Tài liệu phân công chi tiết và cấu trúc file phụ trách xem tại: [PHAN_CONG_CONG_VIEC.md](file:///c:/Users/OS/StudioProjects/todolist/PHAN_CONG_CONG_VIEC.md)*

| STT | Thành viên | Vai trò | Phụ trách chính | Màn hình / Components |
|:---:|:---|:---|:---|:---|
| **1** | **Thành viên 1** | **Leader / Core Task Manager** | • Xây dựng vòng đời Công việc (Task CRUD đầy đủ)<br>• Tìm kiếm từ khóa Real-time & Bộ lọc độ ưu tiên / danh mục<br>• Tối ưu cơ chế Optimistic UI updates & DiffUtil | `MainActivity`<br>`AddTaskActivity`<br>`EditTaskActivity` |
| **2** | **Thành viên 2** | **Auth, Profile & Storage** | • Hệ thống Đăng nhập & Đăng ký (`Firebase Auth`)<br>• Chế độ Sáng/Tối (Light/Dark Mode)<br>• Hồ sơ cá nhân (`ProfileActivity`) & Cloudinary Upload | `LoginActivity`<br>`RegisterActivity`<br>`ProfileActivity` |
| **3** | **Thành viên 3** | **Category & Analytics** | • Quản lý Danh mục công việc & Bộ chọn màu (Color Picker)<br>• Tự động khởi tạo danh mục mặc định cho user mới<br>• Hệ thống Thống kê & Biểu đồ 6 tháng (`MPAndroidChart`) | `CategoryActivity`<br>`StatisticsActivity` |
| **4** | **Thành viên 4** | **Alarms, Notifications & Widget** | • Đặt lịch báo thức nhắc nhở chính xác (`AlarmManager`)<br>• Bắn thông báo hệ thống & Khôi phục lịch sau khi reboot<br>• Phát triển App Widget màn hình chính tương tác 3 task | `TaskWidgetProvider`<br>`System Notifications` |

---

## 🚀 Tính năng nổi bật

| Module | Tính năng |
|--------|-----------|
| 🔐 Auth | Đăng ký, Đăng nhập, Tự động lưu phiên đăng nhập (Auto-login) |
| 📋 Task | CRUD đầy đủ, Upload ảnh (Camera/Gallery), Độ ưu tiên, Hạn chót & Nhắc nhở |
| 📁 Category | CRUD Category với bộ chọn màu sắc (Color Picker) |
| 🔔 Reminder | AlarmManager + BroadcastReceiver, tự động đặt lại lịch báo thức sau khi reboot |
| 📊 Statistics | Pie Chart (theo category), Bar Chart (phân tích 6 tháng gần nhất), MPAndroidChart |
| 🖼️ Widget | Home screen widget chuẩn Slate style, hiển thị 3 task gần hạn & tích hoàn thành tại chỗ |
| 🌙 Theme | Chuyển đổi giao diện Sáng / Tối (Light Mode / Dark Mode) linh hoạt |
| 👤 Profile | Xem thông tin tài khoản, đổi tên hiển thị, chuyển đổi giao diện |
| 🔄 Multi-device | Firestore real-time sync tự động trên nhiều thiết bị |

---

## ⚙️ Cấu hình Firebase & Cloudinary

### Bước 1: Tạo Project Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** → Đặt tên `SmartTodo`
3. Hoàn thành các bước setup

### Bước 2: Thêm Android App

1. Trong Firebase Console → **"Add app"** → Chọn Android
2. **Package name**: `com.smarttodo`
3. **App nickname**: `Smart TODO`
4. Click **"Register app"**

### Bước 3: Lấy SHA-1

Mở terminal trong thư mục project và chạy:

```powershell
# Windows
cd C:\Users\OS\StudioProjects\todolist
.\gradlew signingReport
```

Tìm dòng `SHA1:` trong output và copy giá trị đó.

Thêm SHA-1 vào Firebase Console:
- Project Settings → Your apps → Add fingerprint

### Bước 4: Download google-services.json

1. Trong Firebase Console → Project Settings → Download `google-services.json`
2. Copy file vào thư mục: `app/google-services.json`

```text
todolist/
├── app/
│   ├── google-services.json  ← Đặt file ở đây
│   └── src/
```

### Bước 5: Kích hoạt Firebase Services

#### Authentication
- Firebase Console → **Authentication** → **Sign-in method**
- Enable **Email/Password**

#### Firestore Database
- Firebase Console → **Firestore Database** → **Create database**
- Chọn mode: **Start in test mode** (cho development)
- Chọn location gần nhất

**Firestore Security Rules (cho production):**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users: chỉ đọc/ghi data của chính mình
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    // Tasks
    match /tasks/{taskId} {
      allow read, write: if request.auth != null
        && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null
        && request.auth.uid == request.resource.data.userId;
    }
    // Categories
    match /categories/{categoryId} {
      allow read, write: if request.auth != null
        && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null
        && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

### Bước 6: Cấu hình Cloudinary (Lưu trữ ảnh đám mây)

1. Đăng ký tài khoản miễn phí tại [cloudinary.com](https://cloudinary.com/)
2. Lấy **Cloud Name** trên Dashboard.
3. Vào **Settings (⚙️) → Upload → Add upload preset**.
4. Đổi **Signing Mode** thành **Unsigned** và lưu lại.
5. Cập nhật Cloud Name và Upload Preset vào code:
   - Thay Cloud Name trong `app/src/main/java/com/smarttodo/SmartTodoApplication.java`
   - Thay Upload Preset trong `app/src/main/java/com/smarttodo/repository/StorageRepository.java`

---

## 🏃‍♂️ Chạy Project

### Yêu cầu
- Android Studio Hedgehog (2023.1.1) hoặc mới hơn
- JDK 17+
- Android SDK 26+
- Kết nối Internet

### Các bước

```bash
# 1. Clone/mở project trong Android Studio
# File → Open → Chọn thư mục todolist

# 2. Đảm bảo đã có google-services.json trong app/

# 3. Sync Gradle

# 4. Chạy app
# Run → Run 'app' hoặc Shift+F10
```

---

## 📦 Build APK

```bash
# Debug APK
.\gradlew assembleDebug

# Release APK (cần keystore)
.\gradlew assembleRelease
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📁 Cấu trúc Project

```text
app/src/main/java/com/smarttodo/
├── activity/
│   ├── SplashActivity.java        # Màn hình splash
│   ├── LoginActivity.java         # Đăng nhập
│   ├── RegisterActivity.java      # Đăng ký
│   ├── MainActivity.java          # Danh sách task chính
│   ├── AddTaskActivity.java       # Thêm task
│   ├── EditTaskActivity.java      # Sửa task
│   ├── TaskDetailActivity.java    # Chi tiết task
│   ├── CategoryActivity.java      # Quản lý category
│   ├── ProfileActivity.java       # Hồ sơ user & Đổi chế độ Sáng/Tối
│   └── StatisticsActivity.java    # Thống kê & Biểu đồ 6 tháng
│
├── adapter/
│   ├── TaskAdapter.java           # RecyclerView adapter cho task
│   └── CategoryAdapter.java       # RecyclerView adapter cho category
│
├── model/
│   ├── User.java                  # Model user
│   ├── Task.java                  # Model task
│   └── Category.java              # Model category
│
├── repository/
│   ├── AuthRepository.java        # Firebase Auth operations
│   ├── TaskRepository.java        # Firestore task CRUD
│   ├── CategoryRepository.java    # Firestore category CRUD
│   ├── StorageRepository.java     # Cloudinary upload
│   └── UserRepository.java        # User profile operations
│
├── viewmodel/
│   ├── AuthViewModel.java         # Auth state & operations
│   ├── TaskViewModel.java         # Task list, filter, CRUD
│   ├── CategoryViewModel.java     # Category management
│   ├── ProfileViewModel.java      # User profile
│   └── StatisticsViewModel.java   # Statistics computation
│
├── firebase/
│   └── FirebaseManager.java       # Singleton Firebase access
│
├── notification/
│   └── NotificationHelper.java    # Alarm & notification management
│
├── receiver/
│   ├── AlarmReceiver.java         # BroadcastReceiver cho alarm
│   └── BootReceiver.java          # Re-schedule alarms after boot
│
├── widget/
│   └── TaskWidgetProvider.java    # Home screen widget tương tác 3 task
│
├── listener/
│   ├── OnCompleteListener.java    # Generic callback
│   ├── OnTaskListener.java        # Task callbacks
│   └── OnCategoryListener.java    # Category callbacks
│
└── utils/
    ├── Constants.java             # App constants
    ├── DateUtils.java             # Date formatting & comparison
    ├── ThemeUtils.java            # Light/Dark mode manager
    ├── ValidationUtils.java       # Input validation
    └── PreferenceManager.java     # SharedPreferences wrapper
```

---

## 📚 Dependencies

```kotlin
// Firebase
firebase-bom:33.7.0
firebase-auth
firebase-firestore
firebase-analytics

// Cloudinary
cloudinary-android:2.5.0

// Navigation
navigation-fragment:2.7.7
navigation-ui:2.7.7

// Lifecycle
lifecycle-viewmodel:2.8.7
lifecycle-livedata:2.8.7

// UI
material:1.12.x
recyclerview:1.3.2
cardview:1.0.0
swiperefreshlayout:1.1.0
circleimageview:3.1.0

// Image
glide:4.16.0

// Charts
MPAndroidChart:v3.1.0

// Background
work-runtime:2.9.1
```

---

## 🔥 Firestore Collections

```text
users/
  {uid}/
    name: String
    email: String
    avatar: String (URL)
    createdAt: Timestamp

categories/
  {categoryId}/
    userId: String
    name: String
    color: String (#hex)
    createdAt: Timestamp

tasks/
  {taskId}/
    userId: String
    title: String
    description: String
    categoryId: String
    priority: Int (1=High, 2=Medium, 3=Low)
    imageUrl: String (nullable)
    deadline: Timestamp (nullable)
    reminderTime: Timestamp (nullable)
    completed: Boolean
    createdAt: Timestamp
    updatedAt: Timestamp
```

---

## ⚡ Kiến trúc MVVM

```text
View (Activity/XML)
    ↕ observe LiveData
ViewModel
    ↕ calls
Repository
    ↕ reads/writes
Firebase / Cloudinary / SharedPreferences
```

---

## 📝 Ghi chú quan trọng

> [!IMPORTANT]
> 1. **google-services.json**: Phải có file này trong `app/` trước khi build.
> 2. **Internet**: App yêu cầu kết nối internet để sử dụng Firebase & Cloudinary.
> 3. **Test mode**: Firestore Rules mặc định là test mode - hãy cập nhật trước khi production.

---

## 🎨 Design System

- **Primary Color**: `#005BBF` / `#4CAF50`
- **Background**: Light Mode (`#FAF9FD`), Dark Mode (`#121212`)
- **Widget Style**: Slate 800 (`#1E293B`) với viền `#334155` và bo góc 20dp
- **Priority**: Đỏ (High), Cam (Medium), Xanh lá (Low)

---

*Được xây dựng bởi Smart TODO Team - 2026*
