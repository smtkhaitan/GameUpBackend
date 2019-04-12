INSERT INTO users_donate_it (name, email,password, gender, phone, age, blood_group, weight,address)
VALUES
  (
    '@{name}', '@{email}' ,'@{password}' , '@{gender}' ,'@{phone}' , '@{age}' , '@{blood_group}' , '@{weight}',
    '@{address}'
  );