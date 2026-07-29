# 50 CÂU HỎI BẢO VỆ DỰ ÁN (PHẦN NOTIFICATION & WIDGET)

Dưới đây là danh sách 50 câu hỏi mà giảng viên có thể hỏi để kiểm tra hiểu biết của bạn về cách triển khai tính năng **Thông báo (Notification), Hẹn giờ (AlarmManager)** và **Tiện ích màn hình chính (App Widget)** trong dự án Smart TODO.

---

### Phần 1: Kiến thức chung về Notification (15 câu)
1. **NotificationChannel là gì?** Tại sao từ Android 8.0 (Oreo) trở lên lại bắt buộc phải khai báo NotificationChannel thì thông báo mới hiển thị được?
2. **Âm thanh tùy chỉnh:** Bằng cách nào ứng dụng phát âm thanh tùy chỉnh (custom sound) khi có thông báo? Nếu người dùng chuyển điện thoại sang chế độ rung, thông báo của em có kêu không?
3. **PendingIntent:** `PendingIntent` trong Notification dùng để làm gì? Sự khác biệt giữa cờ `FLAG_UPDATE_CURRENT` và `FLAG_IMMUTABLE` là gì?
4. **Tự động đóng thông báo:** Làm thế nào để thông báo tự động biến mất khỏi thanh trạng thái sau khi người dùng bấm vào? (`setAutoCancel(true)`).
5. **Khả năng tương thích:** Tại sao em dùng `NotificationCompat.Builder` thay vì `Notification.Builder` thuần?
6. **Quyền (Permissions):** Quyền `POST_NOTIFICATIONS` trên Android 13 (API 33) hoạt động như thế nào? Nếu người dùng từ chối cấp quyền, app của em xử lý ra sao?
7. **Rung (Vibration):** Trong `NotificationHelper`, em thiết lập chế độ rung `setVibrate(new long[]{0, 500, 250, 500})`. Ý nghĩa của các con số trong mảng này là gì?
8. **Định danh thông báo:** Tham số `notificationId` khi gọi `notificationManager.notify()` có tác dụng gì? Điều gì xảy ra nếu tất cả các task đều dùng chung một ID (ví dụ ID = 1)?
9. **Mức độ ưu tiên:** Thuộc tính `setPriority(NotificationCompat.PRIORITY_HIGH)` ảnh hưởng như thế nào đến cách thông báo xuất hiện? (Heads-up notification là gì?).
10. **Màn hình khóa:** `Notification.VISIBILITY_PUBLIC` có ý nghĩa gì đối với việc hiển thị thông báo trên màn hình khóa (Lock screen)?
11. **Giao diện thông báo:** Làm cách nào để hiển thị văn bản mô tả (Description) rất dài trong thông báo mà không bị cắt bớt (cụt chữ)? (Gợi ý: `BigTextStyle`).
12. **Cập nhật thông báo:** Có thể cập nhật nội dung của một thông báo đang hiển thị mà không làm nó reo chuông/rung lại lần nữa không? Bằng cách nào?
13. **Hủy thông báo thủ công:** Nếu muốn lập trình để ẩn/hủy một thông báo đang hiển thị (ví dụ: người dùng đã hoàn thành task trong app), em dùng phương thức nào?
14. **AudioAttributes:** `AudioAttributes` (`USAGE_NOTIFICATION_RINGTONE`) được sử dụng để làm gì khi cấu hình âm thanh thông báo trong Channel?
15. **Vòng đời:** Khi người dùng vuốt đóng ứng dụng hoàn toàn (kill app khỏi màn hình đa nhiệm recents), thông báo em đã đặt giờ có được hiển thị khi đến giờ không? Tại sao?

---

### Phần 2: AlarmManager & BroadcastReceivers (15 câu)
16. **Cơ chế hẹn giờ:** `AlarmManager` là gì? Tại sao không dùng một vòng lặp (Thread.sleep) hoặc `TimerTask` để đếm ngược đến giờ nhắc nhở?
17. **Loại Alarm:** Em sử dụng loại alarm nào (`RTC_WAKEUP` hay `RTC`)? Tại sao lại cần cờ `WAKEUP`?
18. **Độ chính xác:** Sự khác biệt giữa `setExact`, `setExactAndAllowWhileIdle`, và `set` thông thường là gì? Tại sao làm ứng dụng báo thức/nhắc nhở lại cần `setExactAndAllowWhileIdle`?
19. **Quyền mới (Android 12+):** Kể từ Android 12, quyền `SCHEDULE_EXACT_ALARM` đóng vai trò gì? Nếu thiếu quyền này trong Manifest thì app có bị crash khi đặt lịch không?
20. **Lặp lại báo thức:** Trong hàm `scheduleReminder`, em dùng vòng lặp để tạo tối đa 3 alarm cách nhau 3 phút. Tại sao không dùng hàm `setRepeating` của AlarmManager?
21. **Định danh PendingIntent:** Để `AlarmManager` hiểu đâu là báo thức cũ cần ghi đè, em sử dụng những yếu tố nào để phân biệt các `PendingIntent`? (Gợi ý: `requestCode` và `Intent.filterEquals`).
22. **Hủy hẹn giờ:** Hàm `cancelReminder` hoạt động như thế nào? Làm sao nó hủy chính xác báo thức của Task A mà không làm mất báo thức của Task B?
23. **Luồng xử lý (Threads):** `AlarmReceiver` chạy trên luồng nào (Main thread hay Background thread)? Nếu gọi API tải dữ liệu nặng trong hàm `onReceive`, ứng dụng sẽ gặp lỗi gì? (ANR).
24. **Kiểm tra trước khi báo:** Tại sao trong `AlarmReceiver`, em lại phải query lên Firestore để kiểm tra trạng thái `completed` và `deletedAt` trước khi hiển thị Notification ra màn hình?
25. **Xử lý bất đồng bộ trong Receiver:** Lệnh gọi Firestore (addOnSuccessListener) là bất đồng bộ. Làm sao đảm bảo tiến trình của `AlarmReceiver` không bị hệ thống kill trước khi Firestore trả về kết quả?
26. **Vai trò của BootReceiver:** Tại sao cần có `BootReceiver`? Nếu xóa `BootReceiver` đi thì tính năng nhắc nhở sẽ gặp lỗi gì sau khi người dùng khởi động lại điện thoại?
27. **Quyền khởi động:** Intent action `android.intent.action.BOOT_COMPLETED` cần quyền gì trong `AndroidManifest.xml` để app có thể nhận được sự kiện bật máy?
28. **Khôi phục dữ liệu:** Trong `BootReceiver`, làm thế nào em lấy được danh sách các task cần đặt lại báo thức?
29. **Thuật toán sinh ID:** Em dùng `Math.abs(taskId.hashCode()) * 10 + i` để sinh `requestCode`. Mã băm (hashCode) này có đảm bảo an toàn tuyệt đối 100% không bị trùng lặp không? Chuyện gì xảy ra nếu 2 task sinh ra cùng 1 requestCode?
30. **Cập nhật giờ nhắc nhở:** Khi người dùng thay đổi giờ nhắc nhở của một task từ 8:00 thành 9:00, em làm thế nào để xóa alarm 8:00 cũ và thay bằng alarm 9:00 mới?

---

### Phần 3: App Widget (15 câu)
31. **Bản chất của Widget:** Lớp `AppWidgetProvider` thực chất kế thừa từ lớp (component) nào trong Android? (Activity, Service, ContentProvider hay BroadcastReceiver?).
32. **Vòng đời Widget:** Các phương thức callback chính của `AppWidgetProvider` gồm những gì? (onUpdate, onEnabled, onDisabled, onDeleted). Em dùng hàm nào để render giao diện?
33. **RemoteViews:** `RemoteViews` là gì? Tại sao trong Widget không thể dùng trực tiếp lệnh `findViewById` hoặc gán sự kiện `setOnClickListener` như trong Activity?
34. **Sự kiện click:** Làm thế nào để bắt sự kiện người dùng bấm vào nút Refresh hoặc tiêu đề trên Widget? (`setOnClickPendingIntent`).
35. **Widget cuộn (Scrollable):** `WidgetService` (kế thừa RemoteViewsService) đóng vai trò gì trong kiến trúc của một Widget có danh sách cuộn ListView?
36. **Adapter cho Widget:** Lớp triển khai `RemoteViewsFactory` (`TaskWidgetFactory`) có chức năng tương đương với thành phần nào khi lập trình ListView/RecyclerView thông thường?
37. **Tải dữ liệu:** Làm thế nào em truy vấn và truyền danh sách Task từ Firebase vào trong `RemoteViewsFactory` để hiển thị?
38. **Đồng bộ hóa luồng (Concurrency):** Tại sao trong hàm `onDataSetChanged()` của `TaskWidgetFactory`, em lại phải sử dụng `CountDownLatch` (chờ tối đa 3 giây)? Nếu không dùng cơ chế chặn luồng này thì danh sách trên widget sẽ ra sao?
39. **Luồng của Factory:** Hàm `onDataSetChanged()` của `RemoteViewsFactory` được hệ điều hành gọi trên luồng (thread) nào? Main thread hay Worker thread?
40. **Click vào phần tử trong List:** Làm thế nào để xử lý sự kiện click vào nút Check (hoàn thành) của một task cụ thể bên trong danh sách cuộn của Widget? (Sự kết hợp giữa `setPendingIntentTemplate` và `setOnClickFillInIntent`).
41. **Luồng cập nhật trạng thái:** Khi người dùng bấm check task trên Widget, quá trình cập nhật dữ liệu (Widget -> Firestore -> Widget refresh) diễn ra chi tiết qua những bước nào?
42. **Thông báo thay đổi dữ liệu:** Hàm `AppWidgetManager.notifyAppWidgetViewDataChanged` dùng để làm gì? Khi nào (ở sự kiện nào) bắt buộc phải gọi nó?
43. **Đa Widget:** Nếu người dùng kéo 3 widget của app em ra màn hình chính, mảng `appWidgetIds` trong hàm `onUpdate` sẽ chứa bao nhiêu phần tử? Các widget này dùng chung hay dùng riêng dữ liệu?
44. **Cấu hình XML:** File `widget_info.xml` quy định những gì? Thuộc tính `resizeMode` hoạt động ra sao?
45. **Độ trễ khởi tạo:** Tại sao đôi khi Widget mất vài giây mới hiển thị danh sách task sau khi bật máy hoặc sau khi mở app, thay vì hiển thị ngay lập tức?

---

### Phần 4: Tích hợp Firebase và Kiến trúc (5 câu)
46. **Tối ưu truy vấn (Quota):** Trong `WidgetService`, việc đọc toàn bộ dữ liệu từ Firestore mỗi lần Widget tự động refresh (ví dụ mỗi 30 phút) có gây tốn hạn mức (Quota) đọc của Firebase không? Nếu có, giải pháp lưu cache tại local (như SQLite/Room) giúp giải quyết vấn đề này thế nào?
47. **Chế độ tiết kiệm pin (Doze mode):** Khi thiết bị Android rơi vào Doze mode (chế độ ngủ sâu), AlarmManager của em có chắc chắn được kích hoạt đúng từng phút không?
48. **Giới hạn hệ điều hành:** Một số hãng điện thoại (như Xiaomi, Oppo, Vivo) có trình quản lý pin cực gắt, thường xuyên kill các Background Receiver và Alarm. Ứng dụng của em bị ảnh hưởng thế nào và cách xử lý (xin quyền auto-start) ra sao?
49. **Đồng bộ đa thiết bị:** Nếu người dùng đăng nhập tài khoản trên 2 thiết bị khác nhau. Khi task đến giờ, thông báo sẽ hiển thị ở máy nào? Nếu họ đánh dấu hoàn thành task ở Widget máy 1, Widget ở máy 2 có tự động cập nhật ngay không?
50. **Rủi ro dữ liệu:** Việc truyền thẳng chuỗi `taskId` và `taskTitle` thông qua `Intent` (Extra) giữa các Receiver và Service có gặp hạn chế gì về kích thước dữ liệu (TransactionTooLargeException) không? Kích thước tối đa cho phép truyền qua Intent là bao nhiêu?
