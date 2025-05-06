-- 고객 테이블
CREATE TABLE IF NOT EXISTS customer (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          name VARCHAR(100) NOT NULL,
                          phone VARCHAR(20) NOT NULL,
                          email VARCHAR(100) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL,
                          activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                          UNIQUE KEY uk_customer_email (email),
                          UNIQUE KEY uk_customer_phone (phone)
);

-- 배달 주소 테이블
CREATE TABLE IF NOT EXISTS delivery_address (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  customer_id BIGINT NOT NULL,
                                  name VARCHAR(100) NOT NULL,
                                  address VARCHAR(255) NOT NULL,
                                  detail_address VARCHAR(255) NOT NULL,
                                  latitude DECIMAL(10, 7) NOT NULL,
                                  longitude DECIMAL(10, 7) NOT NULL,
                                  is_default BOOLEAN NOT NULL DEFAULT FALSE,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  deleted_at TIMESTAMP NULL,
                                  activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                                  FOREIGN KEY (customer_id) REFERENCES customer(id)
);

-- 매장 테이블
CREATE TABLE IF NOT EXISTS store (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       store_category ENUM('CAFE', 'KOREAN', 'WESTERN', 'JAPANESE', 'CHINESE', 'ASIAN', 'FASTFOOD', 'DESSERT', 'BAKERY', 'ITALIAN', 'MEXICAN', 'SEAFOOD', 'BBQ', 'VEGAN', 'FUSION') NOT NULL,
                       business_number VARCHAR(20) NOT NULL,
                       owner_name VARCHAR(100) NOT NULL,
                       address VARCHAR(255) NOT NULL,
                       detail_address VARCHAR(255) NOT NULL,
                       latitude DECIMAL(10, 7) NOT NULL,
                       longitude DECIMAL(10, 7) NOT NULL,
                       contact_number VARCHAR(20) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       store_status ENUM('OPEN', 'CLOSE', 'BREAK') NOT NULL DEFAULT 'OPEN',
                       min_order_amount DECIMAL(10, 2) NOT NULL,
                       default_delivery_fee DECIMAL(10, 2) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP NULL,
                       activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                       UNIQUE KEY uk_store_business_number (business_number)
);

-- 메뉴 카테고리 테이블
CREATE TABLE IF NOT EXISTS menu_category (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               store_id BIGINT NOT NULL,
                               display_order INT NOT NULL,
                               name VARCHAR(100) NOT NULL,
                               description TEXT,
                               activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               deleted_at TIMESTAMP NULL,
                               FOREIGN KEY (store_id) REFERENCES store(id)
);

-- 메뉴 아이템 테이블
CREATE TABLE IF NOT EXISTS menu_item (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           category_id BIGINT NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           price DECIMAL(10, 2) NOT NULL,
                           image_url VARCHAR(255),
                           item_status ENUM('AVAILABLE', 'SOLD_OUT', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP NULL,
                           activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                           FOREIGN KEY (category_id) REFERENCES menu_category(id)
);

-- 메뉴 옵션 그룹 테이블
CREATE TABLE IF NOT EXISTS menu_option (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             menu_id BIGINT NOT NULL,
                             name VARCHAR(100) NOT NULL,
                             is_required BOOLEAN NOT NULL DEFAULT FALSE,
                             display_order INT NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMP NULL,
                             activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                             FOREIGN KEY (menu_id) REFERENCES menu_item(id)
);

-- 라이더 테이블
CREATE TABLE IF NOT EXISTS rider (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       phone VARCHAR(20) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       rider_status ENUM('ACTIVE', 'SUSPENDED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                       current_latitude DECIMAL(10, 7),
                       current_longitude DECIMAL(10, 7),
                       current_order_id BIGINT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP NULL,
                       activated_status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                       UNIQUE KEY uk_rider_email (email),
                       UNIQUE KEY uk_rider_phone (phone)
);

-- 주문 테이블
CREATE TABLE IF NOT EXISTS orders (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         customer_id BIGINT NOT NULL,
                         store_id BIGINT NOT NULL,
                         address_id BIGINT NOT NULL,
                         order_number VARCHAR(50) NOT NULL,
                         order_status ENUM('CREATED', 'ACCEPTED', 'REJECTED', 'COOKING', 'READY', 'ASSIGNED', 'PICKED_UP', 'DELIVERING', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'CREATED',
                         order_time TIMESTAMP NOT NULL,
                         accepted_time TIMESTAMP NULL,
                         ready_time TIMESTAMP NULL,
                         delivered_time TIMESTAMP NULL,
                         cancelled_time TIMESTAMP NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         total_amount DECIMAL(10, 2) NOT NULL,
                         discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
                         delivery_fee DECIMAL(10, 2) NOT NULL,
                         payed_amount DECIMAL(10, 2) NOT NULL,
                         request_store TEXT,
                         request_rider TEXT,
                         version INTEGER NOT NULL DEFAULT 0,
                         FOREIGN KEY (customer_id) REFERENCES customer(id),
                         FOREIGN KEY (store_id) REFERENCES store(id),
                         FOREIGN KEY (address_id) REFERENCES delivery_address(id),
                         UNIQUE KEY uk_order_number (order_number),
                         INDEX idx_order_status (order_status),
                         INDEX idx_order_customer_id (customer_id),
                         INDEX idx_order_store_id (store_id)
);

-- 주문 아이템 테이블
CREATE TABLE IF NOT EXISTS order_item (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            order_id BIGINT NOT NULL,
                            menu_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            unit_price DECIMAL(10, 2) NOT NULL,
                            total_price DECIMAL(10, 2) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP NULL,
                            FOREIGN KEY (order_id) REFERENCES orders(id),
                            FOREIGN KEY (menu_id) REFERENCES menu_item(id)
);

-- 주문 아이템 옵션 테이블
CREATE TABLE IF NOT EXISTS order_item_option (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   order_id BIGINT NOT NULL,
                                   order_item_id BIGINT NOT NULL,
                                   option_item_id BIGINT NOT NULL,
                                   name VARCHAR(100) NOT NULL,
                                   price DECIMAL(10, 2) NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   deleted_at TIMESTAMP NULL,
                                   FOREIGN KEY (order_id) REFERENCES orders(id),
                                   FOREIGN KEY (order_item_id) REFERENCES order_item(id)
);

-- 배달 테이블
CREATE TABLE IF NOT EXISTS delivery (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          order_id BIGINT NOT NULL,
                          store_id BIGINT NOT NULL,
                          rider_id BIGINT NOT NULL,
                          delivery_status ENUM('WAITING', 'ASSIGNED', 'PICKED_UP', 'DELIVERING', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          assigned_at TIMESTAMP NULL,
                          pick_up_at TIMESTAMP NULL,
                          delivering_at TIMESTAMP NULL,
                          delivered_at TIMESTAMP NULL,
                          cancelled_at TIMESTAMP NULL,
                          destination_longitude DECIMAL(10, 7) NOT NULL,
                          destination_latitude DECIMAL(10, 7) NOT NULL,
                          destination_address VARCHAR(255) NOT NULL,
                          estimated_delivery_time TIMESTAMP NULL,
                          actual_delivery_time TIMESTAMP NULL,
                          FOREIGN KEY (order_id) REFERENCES orders(id),
                          FOREIGN KEY (store_id) REFERENCES store(id),
                          FOREIGN KEY (rider_id) REFERENCES rider(id),
                          UNIQUE KEY uk_delivery_order_id (order_id),
                          INDEX idx_delivery_status (delivery_status),
                          INDEX idx_delivery_rider_id (rider_id)
);


-- 시스템 상태 테이블
CREATE TABLE IF NOT EXISTS system_status (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               log_date DATE NOT NULL,
                               log_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               web_flux_status BOOLEAN NOT NULL DEFAULT TRUE,
                               database_status BOOLEAN NOT NULL DEFAULT TRUE,
                               redis_status BOOLEAN NOT NULL DEFAULT TRUE,
                               kafka_status BOOLEAN NOT NULL DEFAULT TRUE,
                               INDEX idx_system_status_log_date (log_date)
);

-- 시뮬레이션 설정 테이블 (새로 추가)
CREATE TABLE IF NOT EXISTS simulation_config (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   order_generation_interval INT NOT NULL DEFAULT 5,  -- 주문 생성 간격(초)
                                   rider_count INT NOT NULL DEFAULT 10,              -- 시뮬레이션 라이더 수
                                   acceptance_delay INT NOT NULL DEFAULT 1,          -- 주문 수락 지연 시간(초)
                                   min_delivery_time INT NOT NULL DEFAULT 3,         -- 최소 배달 시간(분)
                                   max_delivery_time INT NOT NULL DEFAULT 8,         -- 최대 배달 시간(분)
                                   scenario ENUM('NORMAL', 'PEAK_TIME', 'BAD_WEATHER', 'RIDER_SHORTAGE') NOT NULL DEFAULT 'NORMAL',
                                   is_active BOOLEAN NOT NULL DEFAULT FALSE,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);