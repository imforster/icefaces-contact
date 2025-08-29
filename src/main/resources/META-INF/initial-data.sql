-- Initial data for ICEfaces Phonebook Application
-- This script will be executed on first deployment to populate sample data

-- Insert sample contacts (only if table is empty)
INSERT INTO contacts (name, phone_number, email) 
SELECT 'John Doe', '+1-555-0123', 'john.doe@example.com'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE name = 'John Doe');

INSERT INTO contacts (name, phone_number, email) 
SELECT 'Jane Smith', '+1-555-0456', 'jane.smith@example.com'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE name = 'Jane Smith');

INSERT INTO contacts (name, phone_number, email) 
SELECT 'Bob Johnson', '+1-555-0789', 'bob.johnson@example.com'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE name = 'Bob Johnson');

INSERT INTO contacts (name, phone_number, email) 
SELECT 'Alice Brown', '+1-555-0321', 'alice.brown@example.com'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE name = 'Alice Brown');

INSERT INTO contacts (name, phone_number, email) 
SELECT 'Charlie Wilson', '+1-555-0654', 'charlie.wilson@example.com'
WHERE NOT EXISTS (SELECT 1 FROM contacts WHERE name = 'Charlie Wilson');