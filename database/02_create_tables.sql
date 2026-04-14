-- ==============================================================================
-- 1. XÓA CÁC BẢNG CŨ (NẾU ĐÃ TỒN TẠI) ĐỂ TRÁNH LỖI TRÙNG LẶP
-- Sử dụng CASCADE để tự động xóa các ràng buộc khóa ngoại liên quan
-- ==============================================================================

DROP TABLE IF EXISTS public.review_likes CASCADE;
DROP TABLE IF EXISTS public.reviews CASCADE;
DROP TABLE IF EXISTS public.order_items CASCADE;
DROP TABLE IF EXISTS public.cart_items CASCADE;
DROP TABLE IF EXISTS public.orders CASCADE;
DROP TABLE IF EXISTS public.product_images CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.addresses CASCADE;
DROP TABLE IF EXISTS public.carts CASCADE;
DROP TABLE IF EXISTS public.categories CASCADE;
DROP TABLE IF EXISTS public.vouchers CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.brands CASCADE;

-- ==============================================================================
-- 2. TẠO BẢNG MỚI
-- ==============================================================================


-- public.brands definition

-- Drop table

-- DROP TABLE public.brands;

CREATE TABLE public.brands (
                               id bigserial NOT NULL,
                               created_at timestamp(6) NULL,
                               description text NULL,
                               logo_url varchar(500) NULL,
                               "name" varchar(100) NOT NULL,
                               updated_at timestamp(6) NULL,
                               CONSTRAINT brands_pkey PRIMARY KEY (id)
);


-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
                              id bigserial NOT NULL,
                              created_at timestamp(6) NULL,
                              email varchar(100) NOT NULL,
                              full_name varchar(100) NOT NULL,
                              password_hash varchar(255) NULL,
                              phone_number varchar(20) NULL,
                              "role" varchar(255) NOT NULL,
                              status varchar(255) NOT NULL,
                              updated_at timestamp(6) NULL,
                              username varchar(50) NOT NULL,
                              avatar varchar(255) NULL,
                              CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
                              CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username),
                              CONSTRAINT users_pkey PRIMARY KEY (id),
                              CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['customer'::character varying, 'admin'::character varying])::text[]))),
	CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['active'::character varying, 'locked'::character varying, 'pending'::character varying])::text[])))
);


-- public.vouchers definition

-- Drop table

-- DROP TABLE public.vouchers;

CREATE TABLE public.vouchers (
                                 id bigserial NOT NULL,
                                 code varchar(50) NOT NULL,
                                 created_at timestamp(6) NULL,
                                 discount_type varchar(255) NOT NULL,
                                 discount_value numeric(12, 2) NOT NULL,
                                 expiry_date timestamp(6) NULL,
                                 max_usage int4 NULL,
                                 min_order_value numeric(12, 2) NULL,
                                 status varchar(255) NOT NULL,
                                 updated_at timestamp(6) NULL,
                                 CONSTRAINT uk_30ftp2biebbvpik8e49wlmady UNIQUE (code),
                                 CONSTRAINT vouchers_discount_type_check CHECK (((discount_type)::text = ANY ((ARRAY['PERCENTAGE'::character varying, 'FIXED_AMOUNT'::character varying])::text[]))),
	CONSTRAINT vouchers_pkey PRIMARY KEY (id),
	CONSTRAINT vouchers_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'EXPIRED'::character varying])::text[])))
);


-- public.addresses definition

-- Drop table

-- DROP TABLE public.addresses;

CREATE TABLE public.addresses (
                                  id bigserial NOT NULL,
                                  created_at timestamp(6) NULL,
                                  is_default bool NULL,
                                  phone_number varchar(20) NOT NULL,
                                  province varchar(100) NOT NULL,
                                  province_code varchar(10) NULL,
                                  recipient_name varchar(100) NOT NULL,
                                  street_address text NOT NULL,
                                  updated_at timestamp(6) NULL,
                                  ward varchar(100) NOT NULL,
                                  ward_code varchar(10) NULL,
                                  user_id int8 NOT NULL,
                                  district_code varchar(10) NULL,
                                  district varchar(100) DEFAULT 'Chưa cập nhật'::character varying NOT NULL,
                                  CONSTRAINT addresses_pkey PRIMARY KEY (id),
                                  CONSTRAINT fk1fa36y2oqhao3wgg2rw1pi459 FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.carts definition

-- Drop table

-- DROP TABLE public.carts;

CREATE TABLE public.carts (
                              id bigserial NOT NULL,
                              created_at timestamp(6) NULL,
                              updated_at timestamp(6) NULL,
                              user_id int8 NOT NULL,
                              CONSTRAINT carts_pkey PRIMARY KEY (id),
                              CONSTRAINT uk_64t7ox312pqal3p7fg9o503c2 UNIQUE (user_id),
                              CONSTRAINT fkb5o626f86h46m4s7ms6ginnop FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.categories definition

-- Drop table

-- DROP TABLE public.categories;

CREATE TABLE public.categories (
                                   id bigserial NOT NULL,
                                   created_at timestamp(6) NULL,
                                   description text NULL,
                                   "name" varchar(100) NOT NULL,
                                   updated_at timestamp(6) NULL,
                                   parent_id int8 NULL,
                                   CONSTRAINT categories_pkey PRIMARY KEY (id),
                                   CONSTRAINT fksaok720gsu4u2wrgbk10b5n8d FOREIGN KEY (parent_id) REFERENCES public.categories(id)
);


-- public.orders definition

-- Drop table

-- DROP TABLE public.orders;

CREATE TABLE public.orders (
                               id bigserial NOT NULL,
                               city varchar(100) NOT NULL,
                               created_at timestamp(6) NULL,
                               email varchar(100) NOT NULL,
                               order_code varchar(50) NOT NULL,
                               payment_method varchar(255) NOT NULL,
                               phone_number varchar(20) NOT NULL,
                               recipient_name varchar(100) NOT NULL,
                               status varchar(255) NOT NULL,
                               street_address text NOT NULL,
                               total_amount numeric(12, 2) NOT NULL,
                               updated_at timestamp(6) NULL,
                               user_id int8 NULL,
                               voucher_id int8 NULL,
                               CONSTRAINT orders_pkey PRIMARY KEY (id),
                               CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'SHIPPING'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying])::text[]))),
	CONSTRAINT uk_dhk2umg8ijjkg4njg6891trit UNIQUE (order_code),
	CONSTRAINT fk32ql8ubntj5uh44ph9659tiih FOREIGN KEY (user_id) REFERENCES public.users(id),
	CONSTRAINT fkdimvsocblb17f45ikjr6xn1wj FOREIGN KEY (voucher_id) REFERENCES public.vouchers(id)
);


-- public.products definition

-- Drop table

-- DROP TABLE public.products;

CREATE TABLE public.products (
                                 id bigserial NOT NULL,
                                 created_at timestamp(6) NULL,
                                 description text NULL,
                                 "name" varchar(200) NOT NULL,
                                 price numeric(12, 2) NOT NULL,
                                 specifications jsonb NULL,
                                 status bool NOT NULL,
                                 thumbnail_url varchar(500) NULL,
                                 updated_at timestamp(6) NULL,
                                 brand_id int8 NOT NULL,
                                 category_id int8 NOT NULL,
                                 stock_quantity int4 DEFAULT 0 NOT NULL,
                                 CONSTRAINT products_pkey PRIMARY KEY (id),
                                 CONSTRAINT fka3a4mpsfdf4d2y6r8ra3sc8mv FOREIGN KEY (brand_id) REFERENCES public.brands(id),
                                 CONSTRAINT fkog2rp4qthbtt2lfyhfo32lsw9 FOREIGN KEY (category_id) REFERENCES public.categories(id)
);


-- public.reviews definition

-- Drop table

-- DROP TABLE public.reviews;

CREATE TABLE public.reviews (
                                id bigserial NOT NULL,
                                "comment" text NULL,
                                created_at timestamp(6) NULL,
                                rating int4 NOT NULL,
                                updated_at timestamp(6) NULL,
                                product_id int8 NOT NULL,
                                user_id int8 NOT NULL,
                                CONSTRAINT reviews_pkey PRIMARY KEY (id),
                                CONSTRAINT reviews_rating_check CHECK (((rating >= 1) AND (rating <= 5))),
                                CONSTRAINT uq_reviews_user_product UNIQUE (user_id, product_id),
                                CONSTRAINT fkcgy7qjc1r99dp117y9en6lxye FOREIGN KEY (user_id) REFERENCES public.users(id),
                                CONSTRAINT fkpl51cejpw4gy5swfar8br9ngi FOREIGN KEY (product_id) REFERENCES public.products(id)
);


-- public.cart_items definition

-- Drop table

-- DROP TABLE public.cart_items;

CREATE TABLE public.cart_items (
                                   id bigserial NOT NULL,
                                   created_at timestamp(6) NULL,
                                   quantity int4 NOT NULL,
                                   updated_at timestamp(6) NULL,
                                   cart_id int8 NOT NULL,
                                   product_id int8 NOT NULL,
                                   CONSTRAINT cart_items_pkey PRIMARY KEY (id),
                                   CONSTRAINT fk1re40cjegsfvw58xrkdp6bac6 FOREIGN KEY (product_id) REFERENCES public.products(id),
                                   CONSTRAINT fkpcttvuq4mxppo8sxggjtn5i2c FOREIGN KEY (cart_id) REFERENCES public.carts(id)
);


-- public.order_items definition

-- Drop table

-- DROP TABLE public.order_items;

CREATE TABLE public.order_items (
                                    id bigserial NOT NULL,
                                    created_at timestamp(6) NULL,
                                    price numeric(12, 2) NOT NULL,
                                    quantity int4 NOT NULL,
                                    order_id int8 NOT NULL,
                                    product_id int8 NOT NULL,
                                    CONSTRAINT order_items_pkey PRIMARY KEY (id),
                                    CONSTRAINT fkbioxgbv59vetrxe0ejfubep1w FOREIGN KEY (order_id) REFERENCES public.orders(id),
                                    CONSTRAINT fkocimc7dtr037rh4ls4l95nlfi FOREIGN KEY (product_id) REFERENCES public.products(id)
);


-- public.product_images definition

-- Drop table

-- DROP TABLE public.product_images;

CREATE TABLE public.product_images (
                                       id bigserial NOT NULL,
                                       alt_text varchar(200) NULL,
                                       created_at timestamp(6) NULL,
                                       image_url varchar(500) NOT NULL,
                                       is_primary bool NULL,
                                       product_id int8 NOT NULL,
                                       CONSTRAINT product_images_pkey PRIMARY KEY (id),
                                       CONSTRAINT fkqnq71xsohugpqwf3c9gxmsuy FOREIGN KEY (product_id) REFERENCES public.products(id)
);


-- public.review_likes definition

-- Drop table

-- DROP TABLE public.review_likes;

CREATE TABLE public.review_likes (
                                     id bigserial NOT NULL,
                                     created_at timestamp(6) NULL,
                                     review_id int8 NOT NULL,
                                     user_id int8 NOT NULL,
                                     CONSTRAINT review_likes_pkey PRIMARY KEY (id),
                                     CONSTRAINT uq_review_likes_user_review UNIQUE (user_id, review_id),
                                     CONSTRAINT fkm2uonfg8ky6jwtu6iugkilox8 FOREIGN KEY (review_id) REFERENCES public.reviews(id),
                                     CONSTRAINT fknual15vv88tiqnwmi60tb2l8d FOREIGN KEY (user_id) REFERENCES public.users(id)
);