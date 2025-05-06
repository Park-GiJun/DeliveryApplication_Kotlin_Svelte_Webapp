-- 테스트 고객 데이터 (10명)
INSERT INTO customer (name, phone, email, password, activated_status)
VALUES
    ('김민준', '010-1111-1111', 'kim.minjun@example.com', 'password1', 'ACTIVE'),
    ('이서연', '010-2222-2222', 'lee.seoyeon@example.com', 'password2', 'ACTIVE'),
    ('박지훈', '010-3333-3333', 'park.jihoon@example.com', 'password3', 'ACTIVE'),
    ('최수아', '010-4444-4444', 'choi.sua@example.com', 'password4', 'ACTIVE'),
    ('정우진', '010-5555-5555', 'jung.woojin@example.com', 'password5', 'ACTIVE'),
    ('강하은', '010-6666-6666', 'kang.haeun@example.com', 'password6', 'ACTIVE'),
    ('조현우', '010-7777-7777', 'jo.hyunwoo@example.com', 'password7', 'ACTIVE'),
    ('윤지민', '010-8888-8888', 'yoon.jimin@example.com', 'password8', 'ACTIVE'),
    ('임도윤', '010-9999-9999', 'lim.doyoon@example.com', 'password9', 'ACTIVE'),
    ('한소율', '010-0000-0000', 'han.soyul@example.com', 'password10', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 배달 주소 데이터 (각 고객당 1개, 총 10개)
INSERT INTO delivery_address (customer_id, name, address, detail_address, latitude, longitude, is_default, activated_status)
VALUES
    (1, '집', '서울시 강남구 테헤란로 123', '아파트 101동 1001호', 37.5038, 127.0426, TRUE, 'ACTIVE'),
    (2, '집', '서울시 서초구 서초대로 456', '그린아파트 202동 502호', 37.4923, 127.0292, TRUE, 'ACTIVE'),
    (3, '회사', '서울시 송파구 올림픽로 789', '롯데타워 35층', 37.5116, 127.0997, TRUE, 'ACTIVE'),
    (4, '집', '서울시 마포구 월드컵북로 101', '상암아파트 304동 1201호', 37.5665, 126.9780, TRUE, 'ACTIVE'),
    (5, '학교', '서울시 용산구 이태원로 202', '한남동 빌라 B동 202호', 37.5296, 127.0050, TRUE, 'ACTIVE'),
    (6, '회사', '서울시 중구 을지로 303', '을지로타워 12층', 37.5662, 126.9828, TRUE, 'ACTIVE'),
    (7, '집', '서울시 동작구 사당로 404', '사당 푸르지오 505동 1505호', 37.4784, 126.9718, TRUE, 'ACTIVE'),
    (8, '집', '서울시 성동구 왕십리로 505', '왕십리 더샵 101동 2301호', 37.5472, 127.0366, TRUE, 'ACTIVE'),
    (9, '회사', '서울시 강동구 천호대로 606', '천호 현대 타워 8층', 37.5385, 127.1247, TRUE, 'ACTIVE'),
    (10, '집', '서울시 송파구 잠실로 707', '잠실 리센츠 303동 1803호', 37.5113, 127.0980, TRUE, 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 테스트 라이더 데이터 (10명)
INSERT INTO rider (name, phone, email, password, rider_status, current_latitude, current_longitude, activated_status)
VALUES
    ('박배달', '010-1010-1010', 'rider1@example.com', 'password1', 'ACTIVE', 37.5038, 127.0226, 'ACTIVE'),
    ('김라이더', '010-2020-2020', 'rider2@example.com', 'password2', 'ACTIVE', 37.4923, 127.0192, 'ACTIVE'),
    ('이배송', '010-3030-3030', 'rider3@example.com', 'password3', 'ACTIVE', 37.5216, 127.0297, 'ACTIVE'),
    ('최퀵맨', '010-4040-4040', 'rider4@example.com', 'password4', 'ACTIVE', 37.5565, 126.9680, 'ACTIVE'),
    ('정배달', '010-5050-5050', 'rider5@example.com', 'password5', 'ACTIVE', 37.5196, 127.0150, 'ACTIVE'),
    ('강라이더', '010-6060-6060', 'rider6@example.com', 'password6', 'ACTIVE', 37.5562, 126.9928, 'ACTIVE'),
    ('송배송', '010-7070-7070', 'rider7@example.com', 'password7', 'ACTIVE', 37.4884, 126.9818, 'ACTIVE'),
    ('황쾌속', '010-8080-8080', 'rider8@example.com', 'password8', 'ACTIVE', 37.5372, 127.0266, 'ACTIVE'),
    ('조배달맨', '010-9090-9090', 'rider9@example.com', 'password9', 'ACTIVE', 37.5485, 127.1147, 'ACTIVE'),
    ('윤퀵배송', '010-0101-0101', 'rider10@example.com', 'password10', 'ACTIVE', 37.5213, 127.1080, 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 테스트 매장 데이터 (10개)
INSERT INTO store (name, store_category, business_number, owner_name, address, detail_address,
                   latitude, longitude, contact_number, email, store_status,
                   min_order_amount, default_delivery_fee, activated_status)
VALUES
    ('맛있는 치킨', 'KOREAN', '123-45-67890', '김사장', '서울시 강남구 역삼동 123-45', '1층', 37.5012, 127.0365, '02-123-4567', 'chicken@example.com', 'OPEN', 15000, 3000, 'ACTIVE'),
    ('피자파티', 'ITALIAN', '234-56-78901', '이사장', '서울시 서초구 서초동 234-56', '2층', 37.4945, 127.0276, '02-234-5678', 'pizza@example.com', 'OPEN', 18000, 3500, 'ACTIVE'),
    ('신선한 초밥', 'JAPANESE', '345-67-89012', '박사장', '서울시 송파구 잠실동 345-67', '상가 101호', 37.5125, 127.0910, '02-345-6789', 'sushi@example.com', 'OPEN', 20000, 2500, 'ACTIVE'),
    ('건강 샐러드', 'WESTERN', '456-78-90123', '최사장', '서울시 마포구 홍대입구 456-78', '지하 1층', 37.5565, 126.9245, '02-456-7890', 'salad@example.com', 'OPEN', 12000, 2000, 'ACTIVE'),
    ('중화명가', 'CHINESE', '567-89-01234', '정사장', '서울시 중구 명동 567-89', '3층', 37.5632, 126.9830, '02-567-8901', 'chinese@example.com', 'OPEN', 16000, 3000, 'ACTIVE'),
    ('베트남 쌀국수', 'ASIAN', '678-90-12345', '강사장', '서울시 용산구 이태원동 678-90', '2층', 37.5340, 127.0003, '02-678-9012', 'pho@example.com', 'OPEN', 11000, 3000, 'ACTIVE'),
    ('햄버거 팩토리', 'FASTFOOD', '789-01-23456', '조사장', '서울시 동작구 사당동 789-01', '1층', 37.4765, 126.9718, '02-789-0123', 'burger@example.com', 'OPEN', 10000, 2500, 'ACTIVE'),
    ('디저트 카페', 'DESSERT', '890-12-34567', '윤사장', '서울시 성동구 성수동 890-12', '카페 1층', 37.5445, 127.0550, '02-890-1234', 'dessert@example.com', 'OPEN', 5000, 2000, 'ACTIVE'),
    ('파스타 하우스', 'ITALIAN', '901-23-45678', '임사장', '서울시 강동구 천호동 901-23', '상가 202호', 37.5385, 127.1260, '02-901-2345', 'pasta@example.com', 'OPEN', 17000, 3000, 'ACTIVE'),
    ('고기 철판구이', 'BBQ', '012-34-56789', '한사장', '서울시 강남구 청담동 012-34', '지하 1층', 37.5215, 127.0480, '02-012-3456', 'bbq@example.com', 'OPEN', 25000, 3000, 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 메뉴 카테고리 데이터 (각 매장당 1-2개, 총 15개)
INSERT INTO menu_category (store_id, display_order, name, description, activated_status)
VALUES
    (1, 1, '인기 메뉴', '가장 잘 나가는 메뉴들입니다', 'ACTIVE'),
    (1, 2, '신메뉴', '새롭게 출시된 메뉴입니다', 'ACTIVE'),
    (2, 1, '클래식 피자', '기본 피자 메뉴입니다', 'ACTIVE'),
    (2, 2, '스페셜 피자', '특별한 토핑이 올라간 피자입니다', 'ACTIVE'),
    (3, 1, '모듬 초밥', '다양한 초밥을 한번에', 'ACTIVE'),
    (3, 2, '단품 초밥', '개별 초밥 메뉴입니다', 'ACTIVE'),
    (4, 1, '샐러드', '신선한 채소 샐러드', 'ACTIVE'),
    (5, 1, '중식 요리', '대표 중식 요리', 'ACTIVE'),
    (6, 1, '쌀국수', '베트남 전통 쌀국수', 'ACTIVE'),
    (7, 1, '버거 세트', '햄버거와 사이드 세트', 'ACTIVE'),
    (8, 1, '커피', '다양한 커피 메뉴', 'ACTIVE'),
    (8, 2, '디저트', '달콤한 디저트', 'ACTIVE'),
    (9, 1, '파스타', '수제 파스타', 'ACTIVE'),
    (10, 1, '고기 구이', '숙성된 고기 구이', 'ACTIVE'),
    (10, 2, '사이드 메뉴', '고기와 함께하는 사이드 메뉴', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 메뉴 아이템 데이터 (각 카테고리당 3-4개, 총 40개)
INSERT INTO menu_item (category_id, name, description, price, image_url, item_status, activated_status)
VALUES
-- 치킨집 인기 메뉴
(1, '후라이드 치킨', '바삭한 후라이드 치킨', 18000, '/images/fried_chicken.jpg', 'AVAILABLE', 'ACTIVE'),
(1, '양념 치킨', '달콤 매콤한 양념 치킨', 19000, '/images/spicy_chicken.jpg', 'AVAILABLE', 'ACTIVE'),
(1, '반반 치킨', '후라이드와 양념을 한번에', 19000, '/images/half_chicken.jpg', 'AVAILABLE', 'ACTIVE'),
-- 치킨집 신메뉴
(2, '허니 갈릭 치킨', '달콤한 꿀과 마늘 소스의 치킨', 20000, '/images/honey_garlic.jpg', 'AVAILABLE', 'ACTIVE'),
(2, '칠리 치킨', '매콤한 칠리소스 치킨', 20000, '/images/chili_chicken.jpg', 'AVAILABLE', 'ACTIVE'),
-- 피자집 클래식 피자
(3, '페퍼로니 피자', '클래식한 페퍼로니 피자', 18000, '/images/pepperoni.jpg', 'AVAILABLE', 'ACTIVE'),
(3, '치즈 피자', '풍부한 치즈 피자', 16000, '/images/cheese_pizza.jpg', 'AVAILABLE', 'ACTIVE'),
(3, '포테이토 피자', '감자가 올라간 피자', 18000, '/images/potato_pizza.jpg', 'AVAILABLE', 'ACTIVE'),
-- 피자집 스페셜 피자
(4, '고구마 피자', '달콤한 고구마 무스가 들어간 피자', 19000, '/images/sweet_potato.jpg', 'AVAILABLE', 'ACTIVE'),
(4, '불고기 피자', '한국적인 불고기 피자', 21000, '/images/bulgogi_pizza.jpg', 'AVAILABLE', 'ACTIVE'),
-- 초밥집 모듬 초밥
(5, '모듬 초밥 10pcs', '쉐프가 엄선한 10가지 초밥', 25000, '/images/sushi_set_10.jpg', 'AVAILABLE', 'ACTIVE'),
(5, '모듬 초밥 15pcs', '쉐프가 엄선한 15가지 초밥', 35000, '/images/sushi_set_15.jpg', 'AVAILABLE', 'ACTIVE'),
-- 초밥집 단품 초밥
(6, '연어 초밥', '신선한 연어 초밥 2pcs', 6000, '/images/salmon_sushi.jpg', 'AVAILABLE', 'ACTIVE'),
(6, '참치 초밥', '고급 참치 초밥 2pcs', 7000, '/images/tuna_sushi.jpg', 'AVAILABLE', 'ACTIVE'),
(6, '새우 초밥', '탱글탱글한 새우 초밥 2pcs', 5000, '/images/shrimp_sushi.jpg', 'AVAILABLE', 'ACTIVE'),
-- 샐러드 가게
(7, '시저 샐러드', '클래식 시저 샐러드', 12000, '/images/caesar_salad.jpg', 'AVAILABLE', 'ACTIVE'),
(7, '닭가슴살 샐러드', '단백질이 풍부한 닭가슴살 샐러드', 14000, '/images/chicken_salad.jpg', 'AVAILABLE', 'ACTIVE'),
(7, '그린 샐러드', '신선한 채소로 가득한 그린 샐러드', 10000, '/images/green_salad.jpg', 'AVAILABLE', 'ACTIVE'),
-- 중식당
(8, '짜장면', '춘장 소스의 짜장면', 8000, '/images/jajangmyeon.jpg', 'AVAILABLE', 'ACTIVE'),
(8, '짬뽕', '매콤한 해물 짬뽕', 9000, '/images/jjamppong.jpg', 'AVAILABLE', 'ACTIVE'),
(8, '탕수육', '바삭한 탕수육', 18000, '/images/sweet_sour_pork.jpg', 'AVAILABLE', 'ACTIVE'),
-- 베트남 쌀국수
(9, '소고기 쌀국수', '얇게 썬 소고기가 들어간 쌀국수', 11000, '/images/beef_pho.jpg', 'AVAILABLE', 'ACTIVE'),
(9, '해물 쌀국수', '다양한 해물이 들어간 쌀국수', 12000, '/images/seafood_pho.jpg', 'AVAILABLE', 'ACTIVE'),
(9, '분짜', '구운 돼지고기와 쌀국수', 13000, '/images/bun_cha.jpg', 'AVAILABLE', 'ACTIVE'),
-- 햄버거 가게
(10, '치즈버거 세트', '치즈버거 + 감자튀김 + 콜라', 10000, '/images/cheeseburger_set.jpg', 'AVAILABLE', 'ACTIVE'),
(10, '더블 패티 세트', '더블 패티 버거 + 감자튀김 + 콜라', 12000, '/images/double_patty_set.jpg', 'AVAILABLE', 'ACTIVE'),
(10, '치킨 버거 세트', '치킨 버거 + 감자튀김 + 콜라', 11000, '/images/chicken_burger_set.jpg', 'AVAILABLE', 'ACTIVE'),
-- 디저트 카페 - 커피
(11, '아메리카노', '깊고 진한 아메리카노', 4500, '/images/americano.jpg', 'AVAILABLE', 'ACTIVE'),
(11, '카페라떼', '부드러운 우유 거품의 카페라떼', 5000, '/images/cafe_latte.jpg', 'AVAILABLE', 'ACTIVE'),
(11, '바닐라 라떼', '달콤한 바닐라 향의 라떼', 5500, '/images/vanilla_latte.jpg', 'AVAILABLE', 'ACTIVE'),
-- 디저트 카페 - 디저트
(12, '티라미수', '이탈리안 클래식 티라미수', 6000, '/images/tiramisu.jpg', 'AVAILABLE', 'ACTIVE'),
(12, '초코 브라우니', '진한 초코 브라우니', 5500, '/images/chocolate_brownie.jpg', 'AVAILABLE', 'ACTIVE'),
(12, '치즈케이크', '부드러운 뉴욕 치즈케이크', 6500, '/images/cheesecake.jpg', 'AVAILABLE', 'ACTIVE'),
-- 파스타 가게
(13, '까르보나라', '크리미한 까르보나라', 14000, '/images/carbonara.jpg', 'AVAILABLE', 'ACTIVE'),
(13, '토마토 파스타', '새콤달콤한 토마토 파스타', 13000, '/images/tomato_pasta.jpg', 'AVAILABLE', 'ACTIVE'),
(13, '해물 파스타', '신선한 해물이 가득한 파스타', 16000, '/images/seafood_pasta.jpg', 'AVAILABLE', 'ACTIVE'),
-- 고기 구이집
(14, '삼겹살 (200g)', '두툼한 삼겹살 구이', 18000, '/images/samgyeopsal.jpg', 'AVAILABLE', 'ACTIVE'),
(14, '목살 (200g)', '부드러운 목살 구이', 18000, '/images/moksal.jpg', 'AVAILABLE', 'ACTIVE'),
(14, '갈비 (300g)', '양념 갈비 구이', 25000, '/images/galbi.jpg', 'AVAILABLE', 'ACTIVE'),
-- 고기 구이집 사이드
(15, '된장찌개', '구수한 된장찌개', 7000, '/images/doenjang_jjigae.jpg', 'AVAILABLE', 'ACTIVE'),
(15, '냉면', '시원한 물냉면', 8000, '/images/naengmyeon.jpg', 'AVAILABLE', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 메뉴 옵션 데이터 (10개)
INSERT INTO menu_option (menu_id, name, is_required, display_order, activated_status)
VALUES
    (1, '소스 선택', TRUE, 1, 'ACTIVE'),  -- 치킨 소스
    (3, '맵기 조절', TRUE, 1, 'ACTIVE'),  -- 반반 치킨 맵기
    (6, '도우 선택', TRUE, 1, 'ACTIVE'),  -- 피자 도우
    (6, '추가 토핑', FALSE, 2, 'ACTIVE'), -- 피자 추가 토핑
    (11, '와사비 선택', FALSE, 1, 'ACTIVE'), -- 초밥 와사비
    (16, '드레싱 선택', TRUE, 1, 'ACTIVE'), -- 샐러드 드레싱
    (21, '면 선택', TRUE, 1, 'ACTIVE'),  -- 쌀국수 면
    (24, '사이즈 선택', TRUE, 1, 'ACTIVE'), -- 햄버거 사이즈
    (31, '온도 선택', TRUE, 1, 'ACTIVE'), -- 커피 온도
    (31, '시럽 추가', FALSE, 2, 'ACTIVE')  -- 커피 시럽
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 시스템 상태 데이터 (10개)
INSERT INTO system_status (log_date, log_time, web_flux_status, database_status, redis_status, kafka_status)
VALUES
    ('2025-05-06', '2025-05-06 08:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 09:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 10:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 11:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 12:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 13:00:00', TRUE, TRUE, FALSE, TRUE), -- Redis 일시적 장애
    ('2025-05-06', '2025-05-06 13:15:00', TRUE, TRUE, TRUE, TRUE), -- Redis 복구
    ('2025-05-06', '2025-05-06 14:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 15:00:00', TRUE, TRUE, TRUE, TRUE),
    ('2025-05-06', '2025-05-06 16:00:00', TRUE, TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 시뮬레이션 설정 데이터
INSERT INTO simulation_config (order_generation_interval, rider_count, acceptance_delay,
                               min_delivery_time, max_delivery_time, scenario, is_active)
VALUES
    (5, 10, 1, 3, 8, 'NORMAL', TRUE),
    (3, 10, 1, 3, 8, 'PEAK_TIME', FALSE),
    (5, 5, 1, 3, 8, 'RIDER_SHORTAGE', FALSE),
    (5, 10, 1, 5, 15, 'BAD_WEATHER', FALSE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;