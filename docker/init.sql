-- 1. Create the schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS gurukul;

-- 2. Create the company table matching your String UUID strategy
CREATE TABLE IF NOT EXISTS gurukul.t_company (
    company_id VARCHAR(36) PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    website_url VARCHAR(255),
    logo_url VARCHAR(512)
    );