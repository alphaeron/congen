--liquibase formatted sql

--changeset John Matty:17 labels:prod,test
--comment: Insert initial test protocol configurations for performance tracking.

-- Insert initial test protocol configurations
INSERT INTO test_protocol_config (test_name, display_name, description, unit, icon_name, is_required, display_order, radar_chart_color, radar_chart_enabled) VALUES
('vertical_jump', 'Vertical Jump', 'Measure explosive power using MyJump2 app', 'cm', 'fitness_center', true, 1, '#FF6B6B', true),
('hr_recovery', 'HR Recovery', '1-minute heart rate drop after exercise', 'bpm drop', 'favorite', true, 2, '#4ECDC4', true),
('reflex', 'Reflex Speed', 'Reaction time using Human Benchmark', 'ms', 'psychology', true, 3, '#DDA0DD', true),
('mobility', 'Mobility Assessment', 'Functional Movement Screen or similar mobility test', '%', 'accessibility', true, 4, '#9C27B0', true);

--rollback DELETE FROM test_protocol_config WHERE test_name IN ('vertical_jump', 'hr_recovery', 'reflex', 'mobility');
