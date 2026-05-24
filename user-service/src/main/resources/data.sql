TRUNCATE TABLE gurukul.t_company RESTART IDENTITY CASCADE;
TRUNCATE TABLE gurukul.t_users RESTART IDENTITY CASCADE;
INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('2c778f13-4c15-4be7-9f2a-2275f0e7fca6', 'Alpha Edutech', 'info@alphaedutech.com', 'Leading provider of K-12 STEM education curriculum and interactive learning software.', 'https://www.alphaedutech.com', 'https://assets.gurukul.com/logos/alpha.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('ca44fd8b-99a6-43d6-bb6f-934669ecb123', 'Zenith Academy', 'contact@zenithacademy.org', 'Online tutoring platform focusing on advanced placement and competitive exam preparations.', 'https://www.zenithacademy.org', 'https://assets.gurukul.com/logos/zenith.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('d6e1dd03-71f4-42a1-ae69-1309980551bf', 'ByteSize Coding', 'hello@bytesizecoding.io', 'Interactive coding bootcamps and computing courses designed for kids and teens.', 'https://bytesizecoding.io', 'https://assets.gurukul.com/logos/bytesize.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('40e4c4c2-4637-4b96-94e5-24031e20022d', 'Nova Learning Labs', 'support@novalearning.com', 'Virtual reality based science labs and immersive learning experiences for higher ed.', 'https://www.novalearninglabs.com', 'https://assets.gurukul.com/logos/nova.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('e0bb619e-f485-417f-8162-4fa2e67cab39', 'Apex Language School', 'admissions@apexlanguages.com', 'Global language certification platform offering live 1-on-1 native speaker sessions.', 'https://www.apexlanguages.com', 'https://assets.gurukul.com/logos/apex.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('45ae0e4b-2b41-4d6b-9053-6ec189553ab0', 'Scholastic Prime', 'corporate@scholasticprime.in', 'Comprehensive test preparation systems and mock trial engines for law and medical school entry.', 'https://scholasticprime.in', 'https://assets.gurukul.com/logos/sprime.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('89609375-e24e-413a-aeb1-c6be3d88170f', 'SkillStream Media', 'creators@skillstream.tv', 'On-demand professional upskilling video courses with real-world case study projects.', 'https://skillstream.tv', 'https://assets.gurukul.com/logos/skillstream.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('82a04c2b-89be-4372-9d3c-0eef06cf1094', 'Curious Minds Co', 'reachus@curiousmindsco.com', 'Montessori-inspired digital teaching aids and early childhood development tracking apps.', 'https://curiousmindsco.com', 'https://assets.gurukul.com/logos/curious.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('af9c8d35-de30-4485-aae9-c5defdff417e', 'Nexus University Systems', 'admin@nexusunisys.edu', 'Cloud-based learning management system (LMS) tailoring custom paths via machine learning.', 'https://nexusunisys.edu', 'https://assets.gurukul.com/logos/nexus.png');

INSERT INTO gurukul.t_company (company_id, company_name, email, description, website_url, logo_url)
VALUES ('66166d7b-1c70-4a4d-b35d-1eacb0e260f6', 'Summit Tutoring Group', 'help@summittutoring.net', 'Hyper-local marketplace connecting credentialed regional tutors with local students.', 'https://www.summittutoring.net', 'https://assets.gurukul.com/logos/summit.png');
-- 2. Insert 10 mock users mapped to valid company IDs
INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Aarav', 'Sharma', 'aarav.sharma@example.com',
    '$2a$12$eImiTXuWVxfM37uY4b6tFOc9JtJEuEie2gLAKa1w8Fh7WfV9f1g1q', '+919876543210', 'ADMIN',
    '123 M.G. Road', 'Bengaluru', 'Karnataka', '560001', 'India', '2c778f13-4c15-4be7-9f2a-2275f0e7fca6'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    'f5e4d3c2-b1a0-9f8e-7d6c-5b4a3f2e1d0c', 'John', 'Doe', 'john.doe@zenith.org',
    '$2a$12$8vM8Fh0Y4b6tFOc9JtJEuEie2gLAKa1w8Fh7WfV9f1g1qeImiTXuW', '+15550198234', 'TUTOR',
    '456 Oak Avenue', 'Austin', 'Texas', '73301', 'USA', 'ca44fd8b-99a6-43d6-bb6f-934669ecb123'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    '3c3d3e3f-4a4b-4c4d-4e4f-5a5b5c5d5e5f', 'Priya', 'Patel', 'priya.patel@bytesize.io',
    '$2a$12$K1w8Fh7WfV9f1g1qeImiTXuWVxfM37uY4b6tFOc9JtJEuEie2gLA', '+919988776655', 'STUDENT',
    '789 Link Road', 'Mumbai', 'Maharashtra', '400001', 'India', 'd6e1dd03-71f4-42a1-ae69-1309980551bf'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    '7a8b9c0d-1e2f-3a4b-5c6d-a1b2c3d4e5f6', 'Sarah', 'Jenkins', 'sarah.j@nova.com',
    '$2a$12$c9JtJEuEie2gLAKa1w8Fh7WfV9f1g1qeImiTXuWVxfM37uY4b6tF', '+15550147721', 'TUTOR',
    '12 Pine Street', 'Seattle', 'Washington', '98101', 'USA', '4004c4c2-4637-4b96-94e5-24031e20022d'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    'bcde1234-5678-90ab-cdef-1234567890ab', 'Carlos', 'Ruiz', 'carlos.r@apex.com',
    '$2a$12$WfV9f1g1qeImiTXuWVxfM37uY4b6tFOc9JtJEuEie2gLAKa1w8Fh7', '+34913334455', 'ADMIN',
    'Gran Via 45', 'Madrid', 'Madrid', '28013', 'Spain', 'e0bb619e-f485-417f-8162-4fa2e67cab39'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    'deadbeef-cafe-babe-feed-face12345678', 'Ananya', 'Nair', 'ananya.n@scholastic.in',
    '$2a$12$uWVxfM37uY4b6tFOc9JtJEuEie2gLAKa1w8Fh7WfV9f1g1qeImiTX', '+919123456789', 'STUDENT',
    'Sector 15, Vashi', 'Navi Mumbai', 'Maharashtra', '400703', 'India', '45ae0e4b-2b41-4d6b-9053-6ec189553ab0'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    '88888888-4444-4444-4444-121212121212', 'David', 'Kim', 'david.kim@skillstream.tv',
    '$2a$12$FOc9JtJEuEie2gLAKa1w8Fh7WfV9f1g1qeImiTXuWVxfM37uY4b6t', '+15550123399', 'TUTOR',
    '88 Broadway', 'New York', 'New York', '10003', 'USA', '89609375-e24e-413a-aeb1-c6be3d88170f'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    '99999999-9999-9999-9999-999999999999', 'Emma', 'Watson', 'emma@curiousminds.com',
    '$2a$12$gLAKa1w8Fh7WfV9f1g1qeImiTXuWVxfM37uY4b6tFOc9JtJEuEie2', '+442079460192', 'STUDENT',
    '221B Baker St', 'London', 'England', 'NW16XE', 'UK', '82a04c2b-89be-4372-9d3c-0eef06cf1094'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    '11111111-2222-3333-4444-555555555555', 'Rohan', 'Das', 'rohan.das@nexus.edu',
    '$2a$12$eImiTXuWVxfM37uY4b6tFOc9JtJEuEie2gLAKa1w8Fh7WfV9f1g1q', '+919830012345', 'ADMIN',
    'Salt Lake Sec V', 'Kolkata', 'West Bengal', '700091', 'India', 'af9c8d35-de30-4485-aae9-c5defdff417e'
);

INSERT INTO gurukul.t_users (user_id, first_name, last_name, email, password, phone_number, role, street, city, state, zip_code, country, company_id)
VALUES (
    'abcdef12-3456-7890-abcd-ef1234567890', 'Liam', 'Smith', 'liam@summit.net',
    '$2a$12$vM8Fh0Y4b6tFOc9JtJEuEie2gLAKa1w8Fh7WfV9f1g1qeImiTXuW', '+15550184451', 'STUDENT',
    '55 Elm Road', 'Denver', 'Colorado', '80202', 'USA', '66166d7b-1c70-4a4d-b35d-1eacb0e260f6'
);
