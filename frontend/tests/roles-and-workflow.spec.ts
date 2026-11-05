import {test,expect,Page} from '@playwright/test';

const emptySchedule=(status:string)=>({id:1,period:'2026-2',version:1,status,sessions:[],unassigned:[]});

async function loginAs(page:Page,role:string,name='Usuario de prueba'){
  await page.addInitScript(({role,name})=>{
    localStorage.setItem('unbosque_token','test-token');
    localStorage.setItem('unbosque_user',JSON.stringify({token:'test-token',email:'prueba@unbosque.edu.co',role,name}));
  },{role,name});
}

async function mockSchedule(page:Page,status:string,published=false){
  await page.route(`**/api/${published?'public/schedule':'schedules/current'}`,
      route=>route.fulfill({json:emptySchedule(status)}));
  await page.route('**/api/catalogs',route=>route.fulfill({json:{courses:[],teachers:[],classrooms:[],periods:[],sections:[]}}));
  await page.route('**/api/schedules/1/reviews',route=>route.fulfill({json:[]}));
}

test('CP-03 oculta administración y parámetros al Comité de Área',async({page})=>{
  await loginAs(page,'COMITE_AREA','Pepita Área');
  await mockSchedule(page,'BORRADOR');
  await page.goto('/');
  await expect(page.getByRole('link',{name:'Constructor',exact:true})).toBeVisible();
  await expect(page.getByRole('link',{name:'Revisión'})).toBeVisible();
  await expect(page.getByRole('link',{name:'Datos maestros'})).toHaveCount(0);
  await expect(page.getByRole('link',{name:'Parámetros'})).toHaveCount(0);
});

test('CP-30 muestra Aprobar y Devolver al Comité Táctico cuando está en revisión',async({page})=>{
  await loginAs(page,'COMITE_TACTICO','Tomás Táctico');
  await mockSchedule(page,'EN_REVISION');
  await page.goto('/revision');
  await expect(page.getByRole('button',{name:'Aprobar'})).toBeVisible();
  await expect(page.getByRole('button',{name:'Devolver'})).toBeVisible();
});

test('CP-32 muestra Publicar horario a Dirección cuando está aprobado',async({page})=>{
  await loginAs(page,'DIRECCION','Directora de Programa');
  await mockSchedule(page,'APROBADO');
  await page.goto('/revision');
  await expect(page.getByRole('button',{name:'Publicar horario'})).toBeVisible();
});

test('CP-34 abre un horario publicado solo en modo de consulta',async({page})=>{
  await loginAs(page,'COMITE_AREA','Pepita Área');
  await mockSchedule(page,'PUBLICADO');
  await page.goto('/horario');
  await expect(page.getByText('El horario está en modo de consulta para tu rol o estado actual.')).toBeVisible();
  await expect(page.getByRole('button',{name:'Enviar a revisión'})).toHaveCount(0);
  await expect(page.getByRole('button',{name:'Nuevo horario'})).toBeVisible();
});

test('CP-35 permite al estudiante consultar únicamente el horario publicado',async({page})=>{
  await loginAs(page,'ESTUDIANTE','Estudiante');
  await mockSchedule(page,'PUBLICADO',true);
  await page.goto('/horario');
  await expect(page.getByRole('heading',{name:'Horario 2026-2'})).toBeVisible();
  await expect(page.getByText('Publicado',{exact:true})).toBeVisible();
  await expect(page.getByRole('button',{name:'Exportar'})).toHaveCount(0);
});

test('CP-39 y CP-40 ofrecen exportación PDF y Excel al Comité de Área',async({page})=>{
  await loginAs(page,'COMITE_AREA','Pepita Área');
  await mockSchedule(page,'BORRADOR');
  await page.goto('/horario');
  await page.getByRole('button',{name:'Exportar'}).click();
  await expect(page.getByRole('button',{name:'Documento PDF'})).toBeVisible();
  await expect(page.getByRole('button',{name:'Hoja de cálculo'})).toBeVisible();
});

test('CP-04 y CP-05 permiten al administrador gestionar usuarios y roles',async({page})=>{
  await loginAs(page,'ADMINISTRADOR','Alex Administrador');
  await page.route('**/api/users',async route=>{
    if(route.request().method()==='POST')return route.fulfill({status:201,json:{id:2,nombre:'Nueva Persona',correo:'nueva@unbosque.edu.co',rol:'ESTUDIANTE',activo:true}});
    return route.fulfill({json:[{id:1,nombre:'Sofi Estudiante',correo:'estudiante@unbosque.edu.co',rol:'ESTUDIANTE',activo:true}]});
  });
  await page.goto('/usuarios');
  await expect(page.getByRole('heading',{name:'Usuarios'})).toBeVisible();
  await page.getByLabel('Nombre completo').fill('Nueva Persona');
  await page.getByLabel('Correo institucional').fill('nueva@unbosque.edu.co');
  await page.getByLabel('Contraseña inicial').fill('Segura2026!');
  await page.getByRole('button',{name:'Crear usuario'}).click();
  await expect(page.getByText('Usuario creado correctamente.')).toBeVisible();
  await expect(page.getByLabel('Rol de Sofi Estudiante')).toHaveValue('ESTUDIANTE');
});

test('CP-16 y CP-37 importan matrícula y muestran la sugerencia',async({page})=>{
  await loginAs(page,'COMITE_AREA','Pepita Área');
  await page.route('**/api/master-data',route=>route.fulfill({json:{courses:[],teachers:[],classrooms:[],sections:[],periods:[{id:1,codigo:'2026-2'}]}}));
  await page.route('**/api/enrollments?periodId=1',route=>route.fulfill({json:[{id:1,periodoId:1,periodo:'2026-2',cursoId:1,codigoCurso:'SIS-301',curso:'Bases de Datos',demanda:45,minimo:2,recomendado:2,fechaImportacion:'2026-11-04'}]}));
  await page.route('**/api/enrollments/import?periodId=1',route=>route.fulfill({json:{procesados:1,correctos:1,errores:[]}}));
  await page.goto('/matricula');
  await page.getByLabel('Periodo académico').selectOption('1');
  await expect(page.getByText('SIS-301')).toBeVisible();
  await page.locator('input[type=file]').setInputFiles({name:'matricula.csv',mimeType:'text/csv',buffer:Buffer.from('codigo_curso,demanda\nSIS-301,45')});
  await page.getByRole('button',{name:'Importar matrícula'}).click();
  await expect(page.getByText('1 registros importados correctamente.')).toBeVisible();
});

test('CP-06 permite editar la disponibilidad personalizada del docente',async({page})=>{
  await loginAs(page,'DIRECCION','Ana Dirección');
  const teacher={id:1,idExterno:'D-1',nombre:'Laura Méndez',correo:'laura@unbosque.edu.co',vinculacion:'TIEMPO_COMPLETO',escalafon:'ASISTENTE'};
  await page.route('**/api/master-data',route=>route.fulfill({json:{courses:[],teachers:[teacher],classrooms:[],sections:[],periods:[]}}));
  await page.route('**/api/teachers/1/availability',route=>route.fulfill({json:[{id:1,dia:1,horaInicio:'16:00',horaFin:'20:00'}]}));
  await page.goto('/datos-maestros');
  await page.getByRole('button',{name:'Docentes'}).click();
  await page.getByRole('button',{name:/Laura Méndez/}).click();
  await expect(page.getByRole('heading',{name:'Franjas del docente'})).toBeVisible();
  await expect(page.getByLabel('Inicio franja 1')).toHaveValue('16:00');
  await expect(page.getByLabel('Fin franja 1')).toHaveValue('20:00');
});
