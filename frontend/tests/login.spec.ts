import {test,expect} from '@playwright/test';
test('muestra el acceso académico',async({page})=>{await page.goto('/login');await expect(page.getByRole('heading',{name:'Ingresa a tu espacio'})).toBeVisible();await expect(page.getByText('Universidad El Bosque').first()).toBeVisible();});

test('CP-01 permite iniciar sesión con credenciales válidas',async({page})=>{
  await page.route('**/api/auth/login',route=>route.fulfill({status:200,contentType:'application/json',body:JSON.stringify({token:'test-token',nombre:'Pepita Área',correo:'area@unbosque.edu.co',rol:'COMITE_AREA'})}));
  await page.route('**/api/schedules/current',route=>route.fulfill({json:{id:1,period:'2026-2',version:1,status:'BORRADOR',sessions:[],unassigned:[]}}));
  await page.route('**/api/catalogs',route=>route.fulfill({json:{courses:[],teachers:[],classrooms:[],periods:[],sections:[]}}));
  await page.goto('/login');
  await page.getByRole('button',{name:'Ingresar'}).click();
  await expect(page).toHaveURL('/');
  await expect(page.getByText('Hola, Pepita')).toBeVisible();
});

test('CP-02 muestra un mensaje genérico con credenciales inválidas',async({page})=>{
  await page.route('**/api/auth/login',route=>route.fulfill({status:401,contentType:'application/json',body:JSON.stringify({message:'Correo o contraseña incorrectos'})}));
  await page.goto('/login');
  await page.getByLabel('Contraseña').fill('incorrecta');
  await page.getByRole('button',{name:'Ingresar'}).click();
  await expect(page.getByText('Correo o contraseña incorrectos')).toBeVisible();
});
