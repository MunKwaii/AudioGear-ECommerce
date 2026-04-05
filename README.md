# HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ THIẾT BỊ ÂM THANH (AudioGear E-Commerce)
## Jakarta EE (Servlet/Thymeleaf) + Hibernate/JPA + PostgreSQL + Redis

## 1. Giới thiệu đề tài
Đây là dự án xây dựng website thương mại điện tử chuyên cung cấp thiết bị âm thanh (tai nghe, loa, v.v.) chạy trên nền tảng Web, được phát triển bằng Java (Jakarta EE), sử dụng mô hình MVC với Thymeleaf làm View Engine, Hibernate/JPA để kết nối cơ sở dữ liệu PostgreSQL và Redis để tối ưu hóa hiệu suất/quản lý cache.
Hệ thống hỗ trợ các chức năng chính:
- Xác thực và phân quyền (Local, Google OAuth, OTP Email, JWT).
- Quản lý sản phẩm, danh mục, thương hiệu.
- Giỏ hàng (hỗ trợ cả Guest và User) và thanh toán đa phương thức (COD, Momo, SePay, Chuyển khoản).
- Quản lý trạng thái đơn hàng (Order State Machine).
- Quản lý Voucher giảm giá và Đánh giá sản phẩm (Review/Like).
- Dashboard tổng quan và thống kê báo cáo cho Admin.

## 2. Công nghệ sử dụng
- Java 25
- Jakarta EE (Servlet API 6.1.0, JSP, JSTL)
- Maven
- Hibernate ORM 6.4.1.Final / HikariCP
- CSDL quan hệ: PostgreSQL (42.7.1)
- CSDL NoSQL: Redis (Jedis 5.1.0)
- View Engine: Thymeleaf 3.1.2
- Security & Utilities: JWT (jjwt), Google OAuth API, JavaMail, jBCrypt, Gson, Log4j2
- Server: Apache Tomcat 10+

## 3. Kiến trúc hệ thống
Dự án được tổ chức theo kiến trúc:
- **View**: Giao diện HTML/CSS/JS render bằng Thymeleaf.
- **Controller**: Các lớp Servlet/Controller tiếp nhận thao tác từ giao diện và điều hướng.
- **Service**: Lớp bọc và xử lý logic nghiệp vụ cốt lõi.
- **Repository/DAO**: Thao tác dữ liệu qua JPA/Hibernate.
- **Entity**: Ánh xạ đối tượng với bảng trong cơ sở dữ liệu.
- **Design Patterns**: Áp dụng mạnh mẽ các mẫu thiết kế phần mềm thực tế như Strategy (Thanh toán, Discount), State (Trạng thái đơn hàng), Observer (Gửi email/Thông báo đơn hàng), Command (Xử lý tác vụ người dùng), Builder (Tạo sản phẩm).

Luồng chính:
`View -> Controller -> Service -> DAO/Repository -> PostgreSQL/Redis`

## 4. Chức năng chính
### 4.1. Đăng nhập và Xác thực
- Đăng nhập/Đăng ký tài khoản (mã hóa mật khẩu bằng BCrypt).
- Đăng nhập qua Google (Google OAuth).
- Xác thực OTP qua Email (Forgot password, Verify account).
- Quản lý phiên đăng nhập bằng JWT.

### 4.2. Quản lý sản phẩm (Admin & User)
- Thêm/Sửa/Xóa sản phẩm, Upload hình ảnh (Storage Strategy).
- Phân loại theo Danh mục (Category) và Thương hiệu (Brand).
- Tìm kiếm, lọc và sắp xếp sản phẩm nâng cao (CategorySortStrategy, SortByDate/Name/Price).

### 4.3. Giỏ hàng & Thanh toán
- Giỏ hàng linh hoạt (Lưu trên session/Redis cho Guest và DB cho User).
- Tích hợp Voucher giảm giá (Giảm % hoặc số tiền cố định).
- Hỗ trợ thanh toán đa dạng: COD, Chuyển khoản ngân hàng, Momo, SePay.

### 4.4. Quản lý Đơn hàng
- Theo dõi đơn hàng (Order Tracking).
- Chuyển đổi trạng thái đơn hàng khép kín bằng State Pattern (Pending, Processing, Shipped, Delivered, Cancelled).
- Tự động trừ/cộng tồn kho sản phẩm khi đặt/hủy hàng.

### 4.5. Đánh giá và Hồ sơ người dùng
- Viết đánh giá (Review) sản phẩm, thích đánh giá (Review Like).
- Quản lý hồ sơ cá nhân, sổ địa chỉ (Address).
- Xem lịch sử đơn hàng cá nhân.

### 4.6. Trang quản trị (Dashboard)
- Chỉ ADMIN được truy cập.
- Quản lý toàn bộ hệ thống: Users, Products, Orders, Categories, Brands, Vouchers.
- Thống kê doanh thu, báo cáo hoạt động kinh doanh (TimeRangeStrategy).

## 5. Cấu trúc thư mục dự án
- `src/main/java/vn/edu/ute`: Mã nguồn chính phân chia theo domain (auth, cart, order, product, user, v.v.) và các layer (controller, service, dao, entity, dto).
- `src/main/resources`: Chứa các file cấu hình `META-INF/persistence.xml`.
- `src/main/webapp/WEB-INF/templates`: Các file giao diện Thymeleaf (.html).
- `src/main/webapp/static`: Các tài nguyên tĩnh (css, js, images).
- `uml`: các file file PlantUML.
- `database`: các script PostgreSQL.
- `pom.xml`: Cấu hình dependencies và build dự án.

## 6. Hướng dẫn tạo cơ sở dữ liệu
### Bước 1: Tạo database
1. Cài đặt PostgreSQL và Redis server.
2. Tạo một database mới trên PostgreSQL (ví dụ: `audiogear_ecommerce`).
3. Import các file script `.sql` (nếu có) hoặc để Hibernate tự động `update`/`validate` schema.

### Bước 2: Cấu hình kết nối
Mở file `src/main/resources/META-INF/persistence.xml` (hoặc cấu hình DB tương ứng trong code `DatabaseConfig.java`):
Cập nhật lại các thông số:
- JDBC URL: `jdbc:postgresql://localhost:5432/audiogear_ecommerce`
- Username và Password của PostgreSQL.
- Đảm bảo Redis đang chạy ở port mặc định `6379`.

## 7. Hướng dẫn chạy project
**Cách 1: Chạy bằng IntelliJ IDEA (Khuyến nghị)**
1. Mở project trong IntelliJ IDEA.
2. Chờ Maven tải toàn bộ dependencies.
3. Add configuration -> Chọn **Tomcat Server** (Local).
4. Cấu hình Deployment trỏ tới Artifact `AudioGear-ECommerce:war exploded`.
5. Ấn Run/Debug để khởi động server (truy cập `http://localhost:8080`).

**Cách 2: Build file WAR**
Mở terminal tại thư mục project và chạy:
```bash
mvn clean package
```

Sau đó copy file target/AudioGear-ECommerce.war bỏ vào thư mục webapps của Apache Tomcat 10+ và khởi động Tomcat.

## 8. Tài khoản mẫu

| Vai trò | Username / Email | Password |
| :--- | :--- | :--- |
| **ADMIN** | `admin@example.com` | `123456` |
| **USER** | `user@example.com` | `123456` |

## 9. Phân quyền

| Đối tượng | Quyền truy cập & Chức năng |
|:----------| :--- |
| **ADMIN** | - Truy cập trang Dashboard quản trị.<br>- Quản lý toàn bộ danh mục, thương hiệu, sản phẩm, mã giảm giá.<br>- Quản lý tài khoản người dùng, đơn hàng.<br>- Xem báo cáo, thống kê doanh thu. |
| **USER**  | - Xem, tìm kiếm, lọc sản phẩm.<br>- Thêm vào giỏ hàng, áp dụng Voucher, tiến hành thanh toán.<br>- Quản lý sổ địa chỉ, lịch sử đơn hàng, cập nhật avatar.<br>- Đánh giá sản phẩm. |
| **GUEST** | - Xem tìm kiếm sản phẩm.<br>- Sử dụng giỏ hàng (lưu tạm thời). |

## 10. UML
Các sơ đồ UML được lưu trong thư mục `uml/`, bao gồm:
- **Use Case Diagram**
- **Activity Diagram**
- **Sequence Diagram**
- **Class Diagram** (Thể hiện rõ các Design Patterns như State, Strategy, Command)

## 11. Thành viên nhóm đề xuất

| Thành viên                      | Nhiệm vụ chính                                                                                     |
|:--------------------------------|:---------------------------------------------------------------------------------------------------|
| **23110219_Võ Trí Hiệu**        | Thiết kế Database, Entities, DAO/Repository, Tích hợp Hibernate, Redis, Thanh toán.                |
| **23110239_Nguyễn Quốc Khánh**  | Giao diện Thymeleaf, Controllers, chức năng Authentication (JWT, Google), Profile, Giỏ hàng.       |
| **23110315_Lê Ngô Nhựt Tân**    | Nghiệp vụ Order, Quản trị Admin, Dashboard thống kê, Báo cáo UML.                                  |

## 12. Các điểm nổi bật của đồ án
- Áp dụng kiến trúc phần mềm chuyên nghiệp, tách biệt rõ ràng các tầng (Controller, Service, DAO, view).
- Thể hiện kỹ năng hướng đối tượng nâng cao bằng việc áp dụng hàng loạt Design Patterns cốt lõi: State, Strategy, Observer, Command, Builder, Factory.
- Sử dụng công nghệ hiện đại: Redis để tăng tốc độ và quản lý dữ liệu tạm, JWT để bảo mật API/Session, API thanh toán.
- Tích hợp đăng nhập bên thứ 3 (Google OAuth) và gửi email tự động (JavaMail OTP).
- Xử lý mượt mà giỏ hàng cho cả đối tượng chưa đăng nhập và đã đăng nhập.

## 13. Hướng phát triển thêm
- Tích hợp thêm AI gợi ý sản phẩm (Recommendation System).
- Mở rộng thanh toán qua các cổng quốc tế (PayPal, Stripe).
- Phát triển thêm app mobile.
- Tích hợp ElasticSearch để tối ưu hóa khả năng tìm kiếm sản phẩm.

## 14. Kết luận
Dự án minh họa cách xây dựng một hệ thống thương mại điện tử thực tế, mạnh mẽ, bảo mật và hiệu suất cao. Sự kết hợp giữa Jakarta EE, Hibernate/PostgreSQL cùng Redis và hệ thống Design Pattern giúp source code dễ dàng bảo trì, mở rộng và hoàn toàn đáp ứng được các tiêu chuẩn của một đồ án môn học nhóm 3 sinh viên.
