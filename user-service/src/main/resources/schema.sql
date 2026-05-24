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

-- Ensure the schema exists before creating the table
CREATE SCHEMA IF NOT EXISTS gurukul;

-- Create the users table matching your JPA Entity definitions
CREATE TABLE IF NOT EXISTS gurukul.t_users (
    user_id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    role VARCHAR(50) NOT NULL,

    -- Embedded Address columns (Flattened automatically by JPA)
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),

    -- Foreign reference back to your company table
    company_id VARCHAR(36) NOT NULL
);
