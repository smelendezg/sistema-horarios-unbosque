UPDATE usuario
SET correo = replace(correo, '@campus.edu', '@unbosque.edu.co')
WHERE correo LIKE '%@campus.edu';

UPDATE docente
SET correo = replace(correo, '@campus.edu', '@unbosque.edu.co')
WHERE correo LIKE '%@campus.edu';
