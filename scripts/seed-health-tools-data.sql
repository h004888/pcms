-- =====================================================
-- PCMS - Seed data for health-tools-service (quizzes)
-- Run: mysql -u pcms_user -ppcms_pass pcms_health_tools < seed-health-tools-data.sql
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE health_quiz_results;
TRUNCATE TABLE health_quizzes;
SET FOREIGN_KEY_CHECKS=1;

INSERT INTO health_quizzes (id, slug, name, description, scoring_logic, questions_json, status, created_at, updated_at) VALUES
(UUID_TO_BIN('caaaaaaa-0001-0001-0001-000000000001'),
 'tri-nho',
 'Trí nhớ & Tập trung',
 'Đánh giá trí nhớ ngắn hạn và khả năng tập trung. Phù hợp với dược sĩ tư vấn người cao tuổi.',
 'SUM',
 JSON_ARRAY(
   JSON_OBJECT('id', 'q1', 'text', 'Bạn có thường xuyên quên lịch hẹn không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không bao giờ', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Thỉnh thoảng', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 3))),
   JSON_OBJECT('id', 'q2', 'text', 'Bạn có khó tập trung khi đọc sách lâu không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Đôi khi', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 3))),
   JSON_OBJECT('id', 'q3', 'text', 'Bạn có hay quên tên người quen không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Thỉnh thoảng', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 2)))
 ),
 'PUBLISHED', NOW(), NOW()),

(UUID_TO_BIN('caaaaaaa-0002-0002-0002-000000000002'),
 'tim-mach',
 'Nguy cơ tim mạch',
 'Sàng lọc nguy cơ nhồi máu cơ tim, đột quỵ trong 10 năm tới.',
 'SUM',
 JSON_ARRAY(
   JSON_OBJECT('id', 'q1', 'text', 'Bạn có hút thuốc lá không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Thỉnh thoảng', 'score', 2), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 5))),
   JSON_OBJECT('id', 'q2', 'text', 'Huyết áp của bạn thường là bao nhiêu?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Bình thường (<120/80)', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Cao nhẹ (120-140)', 'score', 2), JSON_OBJECT('id', 'c', 'label', 'Cao (>140/90)', 'score', 4))),
   JSON_OBJECT('id', 'q3', 'text', 'Trong gia đình có ai bị bệnh tim mạch không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Có 1 người', 'score', 2), JSON_OBJECT('id', 'c', 'label', 'Nhiều người', 'score', 4))),
   JSON_OBJECT('id', 'q4', 'text', 'Bạn có tập thể dục thường xuyên không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', '3+ lần/tuần', 'score', 0), JSON_OBJECT('id', 'b', 'label', '1-2 lần/tuần', 'score', 2), JSON_OBJECT('id', 'c', 'label', 'Ít/không tập', 'score', 3)))
 ),
 'PUBLISHED', NOW(), NOW()),

(UUID_TO_BIN('caaaaaaa-0003-0003-0003-000000000003'),
 'tien-dai-thao-duong',
 'Tiền đái tháo đường',
 'Phát hiện sớm nguy cơ tiểu đường type 2.',
 'SUM',
 JSON_ARRAY(
   JSON_OBJECT('id', 'q1', 'text', 'Bạn có hay khát nước và đi tiểu nhiều không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Thỉnh thoảng', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 3))),
   JSON_OBJECT('id', 'q2', 'text', 'BMI của bạn là bao nhiêu?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', '<23', 'score', 0), JSON_OBJECT('id', 'b', 'label', '23-27', 'score', 2), JSON_OBJECT('id', 'c', 'label', '>27', 'score', 3))),
   JSON_OBJECT('id', 'q3', 'text', 'Gia đình có ai bị tiểu đường không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Có', 'score', 3)))
 ),
 'PUBLISHED', NOW(), NOW()),

(UUID_TO_BIN('caaaaaaa-0004-0004-0004-000000000004'),
 'hen',
 'Kiểm soát hen (ACT)',
 'Đánh giá mức độ kiểm soát cơn hen trong 4 tuần qua.',
 'SUM',
 JSON_ARRAY(
   JSON_OBJECT('id', 'q1', 'text', 'Trong 4 tuần qua, bạn bị khò khè bao nhiêu lần?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 5), JSON_OBJECT('id', 'b', 'label', '1-2 lần/tuần', 'score', 3), JSON_OBJECT('id', 'c', 'label', '>2 lần/tuần', 'score', 1))),
   JSON_OBJECT('id', 'q2', 'text', 'Bạn có thức giấc vì hen không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 5), JSON_OBJECT('id', 'b', 'label', '1-2 đêm/tuần', 'score', 3), JSON_OBJECT('id', 'c', 'label', '>2 đêm/tuần', 'score', 1)))
 ),
 'PUBLISHED', NOW(), NOW()),

(UUID_TO_BIN('caaaaaaa-0005-0005-0005-000000000005'),
 'suy-giap',
 'Suy giáp',
 'Sàng lọc triệu chứng suy giáp thường gặp.',
 'SUM',
 JSON_ARRAY(
   JSON_OBJECT('id', 'q1', 'text', 'Bạn có hay mệt mỏi, uể oải không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Thỉnh thoảng', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 3))),
   JSON_OBJECT('id', 'q2', 'text', 'Bạn có tăng cân bất thường không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Tăng nhẹ', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Tăng nhiều', 'score', 3))),
   JSON_OBJECT('id', 'q3', 'text', 'Bạn có bị táo bón thường xuyên không?', 'type', 'SINGLE', 'options', JSON_ARRAY(JSON_OBJECT('id', 'a', 'label', 'Không', 'score', 0), JSON_OBJECT('id', 'b', 'label', 'Thỉnh thoảng', 'score', 1), JSON_OBJECT('id', 'c', 'label', 'Thường xuyên', 'score', 2)))
 ),
 'PUBLISHED', NOW(), NOW());

SELECT '✅ Health-tools seeded' AS status;
SELECT COUNT(*) AS quizzes FROM health_quizzes;