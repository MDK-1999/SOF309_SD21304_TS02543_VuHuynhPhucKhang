# SOF309 – Kiểm thử GearAstral cùng AI

**Vũ Huỳnh Phúc Khang · TS02543 · SD21304**

GearAstral là web bán linh kiện máy tính dùng Java Servlet/JSP, JDBC và Maven. Khách xem linh kiện, tạo tài khoản, đặt hàng COD và xem đơn của mình. Nhân viên quản lý sản phẩm, tồn kho, đơn hàng và hóa đơn. Quản trị viên quản lý thêm tài khoản, danh mục và báo cáo.

## 1. Phạm vi và kết quả

| Hạng mục | Sản phẩm trong repo | Kết quả có bằng chứng |
| --- | --- | --- |
| Y1 – Tích hợp web | `src/main/java/com/pcshop/`, `src/main/resources/gearastral/demo.sql`, `src/main/webapp/` | Đã mở GearAstral trên Jetty và Tomcat ở localhost. |
| Y2 – Thiết kế kiểm thử | `SPECIFICATIONS.md`, `TEST_PLAN.md`, `TEST_CASES.md` | Có URL, vai trò, validation, phân vùng tương đương và giá trị biên theo mã GearAstral. |
| Y3 – Unit Test Controller | `AuthControllerTest`, `ProductControllerTest` | 40 lượt chạy, 0 failure, 0 error, 0 skipped trong bộ Controller GearAstral. |
| Y4 – Selenium Page Object Model | `ui/pages/LoginPage.java`, `ui/tests/LoginTest.java` | Khung mẫu cần tích hợp với locator GearAstral. Ca mẫu đang có `@Disabled`. Kế hoạch tại nhật ký 03. |
| Y5 – CI | `.github/workflows/test.yml` | GitHub Actions chạy Maven trên JDK 17 và lưu báo cáo Surefire. |
| Y6 – Minh chứng AI | `docs/ai-logs/01` đến `05` | Ghi ngữ cảnh, đầu ra, đối chiếu mã nguồn, lỗi thật và kết quả kiểm tra theo từng giai đoạn. |

Bộ test mẫu `SampleControllerTest` của template còn trong repo. Báo cáo tổng của `mvn test` gồm cả mẫu đó và ca Selenium bị bỏ qua. 40 ca ở bảng trên là hai class Controller dành cho GearAstral.

## 2. Chạy web tại localhost:8080

Cần JDK 17 trở lên và Maven. Mở Terminal trong thư mục có `pom.xml`, chạy bằng PowerShell.

```powershell
$env:PCSHOP_DEMO = 'true'
$env:PCSHOP_DEMO_STAFF_PASSWORD = 'GearDemo123!'
$env:LOCAL_AI_ENABLED = 'false'
mvn jetty:run
```

Mở [GearAstral](http://localhost:8080/shop), dùng [đăng ký](http://localhost:8080/register) để tạo tài khoản khách hàng. Nhân viên demo là `demostaff`, mật khẩu lấy từ `PCSHOP_DEMO_STAFF_PASSWORD` đã đặt ở trên. H2 demo dùng dữ liệu trong bộ nhớ và khởi tạo lại khi khởi động.

Bấm `Ctrl+C` để dừng Jetty. Khi chạy qua Eclipse/Tomcat, dùng context của project đã cấu hình, chẳng hạn [GearAstral SOF309](http://localhost:8080/GearAstral-SOF309/shop). Mỗi lần chạy chọn một server sử dụng cổng 8080.

Để dùng SQL Server đã có schema GearAstral, đặt `PCSHOP_DEMO=false` và cấu hình `PCSHOP_DB_URL`, `PCSHOP_DB_USER`, `PCSHOP_DB_PASSWORD` ngoài repo.

## 3. Chạy Unit Test và build

```powershell
mvn test "-Dtest=AuthControllerTest,ProductControllerTest"
mvn test "-Dtest=*ControllerTest"
mvn clean package
```

Lệnh đầu chạy 40 ca GearAstral. Lệnh thứ hai gồm thêm test Controller mẫu. Maven tạo WAR tại `target/GearAstral.war` và báo cáo tại `target/surefire-reports/`.

- Dữ liệu biên tên, mật khẩu, giá và tồn kho dùng `@ParameterizedTest` với `@CsvSource`.
- Tài khoản trùng dùng `assertThrows(DuplicateAccountException.class, ...)`.
- Cập nhật sản phẩm có `@Timeout(value = 2, unit = TimeUnit.SECONDS)`.
- Mockito giả lập DAO, request, response, session. Test gọi `service()` của Servlet thật.

## 4. Cây thư mục hiện tại

```text
SOF309_SD21304_TS02543_VuHuynhPhucKhang/
├── pom.xml
├── .github/workflows/test.yml
├── docs/
│   ├── HUONG_DAN_CHI_TIET_6_TUAN.md
│   ├── SPECIFICATIONS.md
│   ├── TEST_PLAN.md
│   ├── TEST_CASES.md
│   └── ai-logs/
│       ├── README.md
│       ├── 01_test_design_ai.md
│       ├── 02_junit_script_ai.md
│       ├── 03_selenium_pom_ai.md
│       ├── 04_debug_logs_ai.md
│       └── 05_github_actions_ai.md
├── src/
│   ├── main/
│   │   ├── java/com/pcshop/
│   │   ├── resources/gearastral/demo.sql
│   │   └── webapp/
│   └── test/
│       ├── java/com/poly/sof309/
│       │   ├── controllers/
│       │   │   ├── AuthControllerTest.java
│       │   │   ├── ProductControllerTest.java
│       │   │   └── SampleControllerTest.java
│       │   └── ui/
│       │       ├── pages/LoginPage.java
│       │       └── tests/LoginTest.java
│       └── resources/mockito-extensions/
├── .gitignore
└── README.md
```

## 5. Tài liệu và bằng chứng

| Tài liệu | Nội dung |
| --- | --- |
| [Đặc tả](docs/SPECIFICATIONS.md) | Vai trò, URL và quy tắc dữ liệu GearAstral. |
| [Kế hoạch](docs/TEST_PLAN.md) | Phạm vi, môi trường, dữ liệu chuẩn, ma trận quyền, tiêu chí nghiệm thu. |
| [Test Case](docs/TEST_CASES.md) | Ca Controller/HTTP/UI, trạng thái và liên kết tới bộ JUnit thực tế. |
| [Nhật ký AI](docs/ai-logs/README.md) | Mục lục minh chứng 01–05 và tiến độ. |
| [Hướng dẫn môn học](docs/HUONG_DAN_CHI_TIET_6_TUAN.md) | Lộ trình và tiêu chí từ template giảng viên. |
| [Commit Unit Test Tuần 2](https://github.com/MDK-1999/SOF309_SD21304_TS02543_VuHuynhPhucKhang/commit/214fed9474b62e9d639aa0e51c78f55e219f1624) | Code đã đăng lên main. |
| [CI của commit Tuần 2](https://github.com/MDK-1999/SOF309_SD21304_TS02543_VuHuynhPhucKhang/actions/runs/43726837327) | Workflow đã thành công. |

Ngày cập nhật 18/11/2026.
