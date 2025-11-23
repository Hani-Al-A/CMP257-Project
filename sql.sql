CREATE DATABASE resortdb;

USE resortdb;

CREATE TABLE Admin (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50)
);

INSERT INTO Admin VALUES ('admin','1234');

CREATE TABLE Facility (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    image VARCHAR(255)
);

-- Optional sample data
INSERT INTO Facility(name, description, image) VALUES
('Main Pool','25-meter pool with lounge chairs','images/main_pool.jpeg'),
('Kids Pool','Shallow pool for children','images/kids_pool.jpeg');
