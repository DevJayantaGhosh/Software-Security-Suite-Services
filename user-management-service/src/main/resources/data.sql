-- =====================================================
-- User Management Service - Initial Data Insertion
-- =====================================================

-- 1. ROLES: Safe idempotent insert (runs every restart → skips if exists)
INSERT INTO roles (name) VALUES
                             ('Admin'),
                             ('ProjectDirector'),
                             ('SecurityHead'),
                             ('ReleaseEngineer'),
                             ('User')
    ON CONFLICT (name) DO NOTHING;

-- 2. ADMIN USER: Safe idempotent insert 
-- Email: admin@gmail.com | Password: admin123 | Role: Admin | Internal: true
INSERT INTO users (id, name, email, password, role_id, is_internal, created_at)
SELECT
    gen_random_uuid()::text,
    'System Administrator',
    'admin@gmail.com',
    '$2a$10$fNdTC0ac/klMOfuVxq4.OuqKzP52.8VZryLz.DbToi/jdC9Nzvupi', -- BCrypt("admin123")
    r.id,
    true,
    NOW()
FROM roles r
WHERE r.name = 'Admin' AND NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@gmail.com'
)
    ON CONFLICT (email) DO NOTHING;
