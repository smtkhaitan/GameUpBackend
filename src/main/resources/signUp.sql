INSERT INTO dbo.users_donate_it (name, email,password, gender, phone, age, blood_group, weight,address)
VALUES
  (
    '@{name}', '@{email}' ,'@{password}' , '@{gender}' ,'@{phone}' , '@{age}' , '@{blood_group}' , '@{weight}',
    '@{address}'
  );


--CREATE TABLE users_donate_it (
--user_id INT NOT NULL IDENTITY(1,1),
--name VARCHAR(100),
--email VARCHAR(100),
--password VARCHAR(20),
--gender CHAR(1),
--phone VARCHAR(10),
--age VARCHAR(10),
--blood_group VARCHAR(10),
--weight VARCHAR(10),
--address VARCHAR(1000)
--)


--CREATE TABLE users_donate_it (
--user_id INT NOT NULL AUTO_INCREMENT,
--name VARCHAR(100),
--email VARCHAR(100),
--password VARCHAR(20),
--gender CHAR(1),
--phone VARCHAR(10),
--age VARCHAR(10),
--blood_group VARCHAR(10),
--weight VARCHAR(10),
--address VARCHAR(1000),
--PRIMARY KEY (user_id)
--)