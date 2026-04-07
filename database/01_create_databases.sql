-- ==============================================================================
-- Tên file: 01_create_databases.sql
-- Mục đích: Làm sạch hoàn toàn database audiogear_db (khi đang kết nối trực tiếp vào nó)
-- ==============================================================================

-- 1. Xóa toàn bộ không gian public (nơi chứa tất cả các bảng, khóa ngoại, dữ liệu...)
-- Lệnh CASCADE sẽ tự động quét sạch mọi thứ bên trong.
DROP SCHEMA IF EXISTS public CASCADE;

-- 2. Tạo lại không gian public mới hoàn toàn trống rỗng
CREATE SCHEMA public;

-- 3. Phục hồi lại quyền truy cập cơ bản cho không gian public (Chuẩn của PostgreSQL)
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;