# 📋 PHÂN CHIA LUỒNG CHỨC NĂNG DỰ ÁN SMART TODO (NHÓM 4 NGUỜI)

---

## 📌 TỔNG QUAN DỰ ÁN
* **Tên ứng dụng**: Smart TODO App
* **Nền tảng**: Android Native (Java)
* **Kiến trúc**: MVVM (Model - View - ViewModel) + Repository Pattern
* **Công nghệ tích hợp**:
  - **Cơ sở dữ liệu**: Firebase Cloud Firestore (Real-time Sync & Offline Cache)
  - **Xác thực**: Firebase Authentication
  - **Lưu trữ phương tiện**: Cloudinary API (Unsigned Upload Preset)
  - **Hệ thống nhắc nhở**: AlarmManager, NotificationChannel, BroadcastReceiver, App Widget

---

## 👥 PHÂN CÔNG CHỨC NĂNG CHI TIẾT (4 THÀNH VIÊN)

### 👨‍💻 1. THÀNH VIÊN 1: LEADER / CORE TASK MANAGER
> **Vai trò chính**: Quản lý vòng đời Công việc (Task CRUD), Tìm kiếm, Lọc và Đồng bộ dữ liệu Real-Time.

#### 🎯 Nhiệm vụ chính:
* **Quản lý Task (CRUD)**:
  - Xây dựng màn hình Thêm mới công việc (`AddTaskActivity`).
  - Xây dựng màn hình Chỉnh sửa công việc (`EditTaskActivity`).
  - Xóa công việc và Tích chọn chuyển trạng thái Hoàn thành / Chưa hoàn thành.
* **Tìm kiếm & Bộ lọc**:
  - Tìm kiếm công việc theo từ khóa tiêu đề real-time qua ô Search bar.
  - Bộ lọc công việc theo Độ ưu tiên (Cao / Trung bình / Thấp) dạng nằm ngang cạnh ô tìm kiếm.
  - Bộ lọc công việc theo Danh mục dạng Chip filter.
* **Tối ưu hóa Giao diện & Đồng bộ**:
  - Áp dụng cơ chế **Optimistic UI Updates (0ms độ trễ)** cho thao tác Thêm, Sửa, Xóa, Tích chọn.
  - Tối ưu `TaskAdapter` với `DiffUtil` so sánh toàn diện thuộc tính.
  - Thiết kế bố cục Màn hình chính (`activity_main.xml`) ghim cố định thanh Bottom Navigation.

#### 📂 File mã nguồn phụ trách:
- `app/src/main/java/com/smarttodo/activity/MainActivity.java`
- `app/src/main/java/com/smarttodo/activity/AddTaskActivity.java`
- `app/src/main/java/com/smarttodo/activity/EditTaskActivity.java`
- `app/src/main/java/com/smarttodo/adapter/TaskAdapter.java`
- `app/src/main/java/com/smarttodo/viewmodel/TaskViewModel.java`
- `app/src/main/java/com/smarttodo/repository/TaskRepository.java`
- `app/src/main/java/com/smarttodo/model/Task.java`

---

### 👨‍💻 2. THÀNH VIÊN 2: AUTH, USER PROFILE & CLOUD STORAGE SPECIALIST
> **Vai trò chính**: Quản lý tài khoản, Đăng nhập/Đăng ký, Hồ sơ cá nhân và Upload hình ảnh.

#### 🎯 Nhiệm vụ chính:
* **Hệ thống Xác thực (Auth System)**:
  - Xây dựng luồng Đăng nhập (`LoginActivity`) và Đăng ký (`RegisterActivity`).
  - Đăng xuất và điều hướng màn hình khi hết phiên làm việc.
* **Quản lý Hồ sơ Người dùng (`ProfileActivity`)**:
  - Hiển thị tên người dùng và email chính xác từ Firestore / Auth.
  - Đổi tên hiển thị thông qua Dialog chỉnh sửa.
  - Quản lý trạng thái lưu vết đệm local (`PreferenceManager`).
* **Lưu trữ Ảnh & Media (`Cloudinary & Local Storage`)**:
  - Upload ảnh đính kèm Task và ảnh đại diện Hồ sơ lên máy chủ Cloudinary.
  - Chuyển đổi `content://` Uri sang Tệp tạm (`Temp File`) tránh lỗi Android Permission Denial.
  - Lưu bản sao ảnh đại diện vĩnh viễn trong bộ nhớ riêng của máy (`Internal Storage`) giúp ảnh nạp tức thì 0ms.

#### 📂 File mã nguồn phụ trách:
- `app/src/main/java/com/smarttodo/activity/LoginActivity.java`
- `app/src/main/java/com/smarttodo/activity/RegisterActivity.java`
- `app/src/main/java/com/smarttodo/activity/ProfileActivity.java`
- `app/src/main/java/com/smarttodo/viewmodel/AuthViewModel.java`
- `app/src/main/java/com/smarttodo/viewmodel/ProfileViewModel.java`
- `app/src/main/java/com/smarttodo/repository/AuthRepository.java`
- `app/src/main/java/com/smarttodo/repository/UserRepository.java`
- `app/src/main/java/com/smarttodo/repository/StorageRepository.java`
- `app/src/main/java/com/smarttodo/model/User.java`
- `app/src/main/java/com/smarttodo/utils/PreferenceManager.java`

---

### 👨‍💻 3. THÀNH VIÊN 3: CATEGORY & ANALYTICS ENGINEER
> **Vai trò chính**: Quản lý hệ thống Danh mục công việc và Xây dựng Báo cáo Thống kê.

#### 🎯 Nhiệm vụ chính:
* **Quản lý Danh mục (`CategoryActivity`)**:
  - Thêm mới, Chỉnh sửa và Xóa danh mục công việc.
  - Chọn bảng màu tùy chỉnh cho từng danh mục.
  - Tự động tạo bộ Danh mục mặc định (Công việc, Học tập, Cá nhân...) cho tài khoản mới đăng ký.
* **Hệ thống Thống kê & Phân tích (`StatisticsActivity`)**:
  - Tính toán Tỷ lệ hoàn thành công việc (Completion Rate %).
  - Tổng hợp số lượng Task: Tổng số, Đã hoàn thành, Đang chờ làm.
  - Thống kê công việc theo Danh mục và công việc theo Tháng (`DateUtils`).
  - Cập nhật số liệu thống kê Real-Time ngay khi Task có sự thay đổi.

#### 📂 File mã nguồn phụ trách:
- `app/src/main/java/com/smarttodo/activity/CategoryActivity.java`
- `app/src/main/java/com/smarttodo/activity/StatisticsActivity.java`
- `app/src/main/java/com/smarttodo/adapter/CategoryAdapter.java`
- `app/src/main/java/com/smarttodo/viewmodel/CategoryViewModel.java`
- `app/src/main/java/com/smarttodo/viewmodel/StatisticsViewModel.java`
- `app/src/main/java/com/smarttodo/repository/CategoryRepository.java`
- `app/src/main/java/com/smarttodo/model/Category.java`

---

### 👨‍💻 4. THÀNH VIÊN 4: SYSTEM INTEGRATION, ALARMS & APP WIDGET
> **Vai trò chính**: Lập lịch báo thức nhắc nhở, nổ thông báo hệ thống và Widget màn hình chính.

#### 🎯 Nhiệm vụ chính:
* **Cơ chế Hẹn giờ & Báo thức (`AlarmManager`)**:
  - Đặt lịch báo thức nhắc nhở khi tạo/sửa Task có chọn thời gian nhắc nhở.
  - Tương thích quyền Báo thức chính xác trên Android 12+ (`SCHEDULE_EXACT_ALARM`).
  - Khôi phục lại lịch báo thức sau khi thiết bị khởi động lại (`RECEIVE_BOOT_COMPLETED`).
* **Quản lý Thông báo (`NotificationManager`)**:
  - Khởi tạo Notification Channel mức ưu tiên cao (`IMPORTANCE_HIGH`).
  - Bắn thông báo nhắc nhở kèm rung/âm thanh khi đến giờ hẹn (`AlarmReceiver`).
  - Xin quyền Thông báo thời gian thực trên Android 13+ (`POST_NOTIFICATIONS`).
* **App Widget màn hình chính (`TaskWidgetProvider`)**:
  - Thiết kế và phát triển Widget ngoài màn hình chính Android.
  - Tự động đồng bộ và hiển thị danh sách task cần làm ra Widget ngoài Home Screen.

#### 📂 File mã nguồn phụ trách:
- `app/src/main/java/com/smarttodo/notification/NotificationHelper.java`
- `app/src/main/java/com/smarttodo/receiver/AlarmReceiver.java`
- `app/src/main/java/com/smarttodo/widget/TaskWidgetProvider.java`
- `app/src/main/java/com/smarttodo/utils/Constants.java`
- `app/src/main/java/com/smarttodo/utils/DateUtils.java`
- `app/src/main/AndroidManifest.xml`

---

## 📊 BẢNG TỔNG HỢP MA TRẬN PHÂN CÔNG

| Thành viên | Vị trí / Vai trò | Module chính phụ trách | Màn hình Giao diện (UI) |
| :---: | :--- | :--- | :--- |
| **TV 1** | **Leader / Core Task Manager** | Task CRUD, Filter, Search, Real-Time Sync | `MainActivity`, `AddTaskActivity`, `EditTaskActivity` |
| **TV 2** | **Auth, Profile & Storage** | Firebase Auth, Profile Management, Cloudinary | `LoginActivity`, `RegisterActivity`, `ProfileActivity` |
| **TV 3** | **Category & Analytics** | Category CRUD, Statistics Engine, Color Picker | `CategoryActivity`, `StatisticsActivity` |
| **TV 4** | **Alarms, Notifications & Widget** | AlarmManager, Notification Channel, Home Widget | `TaskWidgetProvider`, System Notifications |

---

## 🔀 QUY TRÌNH QUẢN LÝ MÃ NGUỒN (GIT WORKFLOW)

1. **`main`**: Nhánh chính chứa mã nguồn sản phẩm hoàn chỉnh và ổn định.
2. **Các nhánh tính năng (Feature Branches)**:
   - `feature/task-management` *(Dành cho Thành viên 1)*
   - `feature/auth-profile-storage` *(Dành cho Thành viên 2)*
   - `feature/category-statistics` *(Dành cho Thành viên 3)*
   - `feature/notifications-widget` *(Dành cho Thành viên 4)*
3. **Quy tắc Merge**: Kiểm thử độc lập từng nhánh tính năng trước khi mở Pull Request (PR) hợp nhất vào nhánh `main`.
