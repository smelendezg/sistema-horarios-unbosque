import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const workspaceDir = "/Users/sarahmelendez/Documents/Codex/2026-10-15/yo";
const skillDir = "/Users/sarahmelendez/.codex/plugins/cache/openai-primary-runtime/presentations/26.1026.10807/skills/presentations";
const buildDir = path.join(workspaceDir, ".build_test_presentation");
const previewDir = path.join(buildDir, "previews");
const finalPath = path.join(workspaceDir, "output", "Presentacion_Pruebas_Riesgos_UnBosque.pptx");
const pythonExecutable = "/Users/sarahmelendez/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3";

const C = {
  forest: "#176B4B", dark: "#173E2F", mint: "#E5F5EB", mint2: "#D6ECDF",
  green: "#4BA477", blush: "#F7E8ED", pink: "#D98EA2", lavender: "#EEE9FB",
  purple: "#8870BD", peach: "#FFF0DF", orange: "#D99A56", blue: "#E5F1FB",
  blueInk: "#397AA8", cream: "#F8FBF8", white: "#FFFFFF", line: "#DCE7DF",
  muted: "#6E8179", pale: "#F1F8F4", red: "#A94A57", yellow: "#E3A33E",
  riskLow: "#DDF2E6", riskMedium: "#FFF0BE", riskHigh: "#F8D5A2", riskCritical: "#E9909C"
};
const FONT_HEAD = "Arial Rounded MT Bold";
const FONT_BODY = "Arial";
const presentation = Presentation.create({ slideSize: { width: 1280, height: 720 } });

function shape(slide, geometry, left, top, width, height, fill = C.white, line = C.line) {
  return slide.shapes.add({ geometry, position: { left, top, width, height }, fill, line: { style: "solid", fill: line, width: 1 } });
}

function text(slide, value, left, top, width, height, size = 20, color = C.dark, opts = {}) {
  const box = slide.shapes.add({ geometry: "textbox", position: { left, top, width, height }, fill: "none", line: { style: "solid", fill: "none", width: 0 } });
  box.text = value;
  box.text.style = { fontFamily: opts.fontFamily ?? FONT_BODY, fontSize: size, color, bold: opts.bold ?? false, italic: opts.italic ?? false, ...(opts.letterSpacing ? { letterSpacing: opts.letterSpacing } : {}) };
  box.text.paragraphFormat = { alignment: opts.align ?? "left" };
  return box;
}

function line(slide, x1, y1, x2, y2, color = C.line, width = 2, dashed = false) {
  return slide.shapes.add({ geometry: "connector", kind: "straight", position: { left: Math.min(x1,x2), top: Math.min(y1,y2), width: Math.abs(x2-x1), height: Math.abs(y2-y1), ...(x2<x1?{horizontalFlip:true}:{}), ...(y2<y1?{verticalFlip:true}:{}) }, line: { style: dashed ? "dashed" : "solid", fill: color, width } });
}

function header(slide, index, titleValue, eyebrow) {
  slide.background.fill = C.cream;
  text(slide, eyebrow.toUpperCase(), 84, 34, 500, 22, 12, C.green, { bold: true, letterSpacing: 1.4 });
  text(slide, titleValue, 84, 62, 1075, 54, 33, C.dark, { bold: true, fontFamily: FONT_HEAD });
  shape(slide, "ellipse", 1186, 38, 40, 40, index % 2 ? C.blush : C.mint, "none");
  text(slide, String(index).padStart(2,"0"), 1186, 50, 40, 16, 10, C.dark, { bold: true, align: "center" });
}

function footer(slide, index) {
  line(slide, 64, 684, 1216, 684, C.line, 1);
  text(slide, "Universidad El Bosque · Plan de pruebas y riesgos", 64, 690, 430, 15, 9, C.muted);
  text(slide, String(index), 1180, 690, 36, 15, 9, C.muted, { bold: true, align: "right" });
}

function pill(slide, label, left, top, width, fill = C.mint, color = C.forest) {
  const p = shape(slide, "roundRect", left, top, width, 30, fill, "none");
  p.text = label; p.text.style = { fontFamily: FONT_BODY, fontSize: 11, bold: true, color }; p.text.paragraphFormat = { alignment: "center" };
  return p;
}

function card(slide, titleValue, body, left, top, width, height, accent = C.green, fill = C.white) {
  shape(slide, "roundRect", left, top, width, height, fill, C.line);
  shape(slide, "roundRect", left + 16, top + 18, 8, 40, accent, "none");
  text(slide, titleValue, left + 38, top + 15, width - 54, 26, 16, C.dark, { bold: true, fontFamily: FONT_HEAD });
  text(slide, body, left + 38, top + 50, width - 54, height - 62, 12, C.muted);
}

function notes(slide, cue) {
  slide.speakerNotes.text = `Guion sugerido: ${cue}\nFuente: Plan de pruebas y gestión de riesgos del proyecto y resultados automatizados del repositorio.`;
}

// 1. Portada
{
  const slide = presentation.slides.add(); slide.background.fill = C.cream;
  shape(slide, "ellipse", -80, -100, 320, 320, C.mint, "none");
  shape(slide, "ellipse", 1110, 550, 250, 250, C.blush, "none");
  shape(slide, "roundRect", 64, 60, 54, 54, C.forest, "none");
  text(slide, "UEB", 70, 78, 42, 18, 12, C.white, { bold: true, align: "center" });
  text(slide, "Universidad El Bosque", 132, 69, 350, 28, 20, C.dark, { bold: true, fontFamily: FONT_HEAD });
  text(slide, "Sistema de Programación de Horarios Académicos", 132, 98, 480, 22, 11, C.muted);
  pill(slide, "ENTREGA · PRUEBAS Y RIESGOS", 64, 170, 230);
  text(slide, "Calidad comprobada,\nriesgos bajo control", 64, 220, 650, 130, 48, C.dark, { bold: true, fontFamily: FONT_HEAD });
  text(slide, "Plan, trazabilidad, evidencia y demostración en vivo", 67, 378, 610, 32, 20, C.forest, { bold: true });
  text(slide, "Grupo 5 · Ingeniería de Sistemas · Noviembre de 2026", 67, 425, 570, 26, 15, C.muted);
  shape(slide, "roundRect", 760, 110, 420, 450, C.white, C.line);
  text(slide, "RESULTADO DE REGRESIÓN", 792, 146, 320, 20, 11, C.muted, { bold: true, letterSpacing: 1.2 });
  text(slide, "57", 790, 186, 155, 78, 58, C.forest, { bold: true, fontFamily: FONT_HEAD });
  text(slide, "pruebas automáticas\naprobadas", 936, 201, 210, 58, 17, C.dark, { bold: true });
  const rows = [["45", "Backend Java"], ["12", "Interfaz Playwright"], ["40", "Casos trazables"], ["2", "Épicas funcionales"]];
  rows.forEach((r,i) => { shape(slide,"roundRect",792,294+i*54,350,42,i%2?C.pale:C.mint,"none"); text(slide,r[0],810,304+i*54,50,22,16,C.forest,{bold:true}); text(slide,r[1],870,307+i*54,240,20,12,C.dark,{bold:true}); });
  notes(slide, "Abrir con el resultado: el prototipo funciona de extremo a extremo y la regresión automática está en verde.");
}

// 2. Alcance y épicas
{
  const slide = presentation.slides.add(); header(slide,2,"Dos épicas que completan el ciclo","Alcance funcional");
  text(slide,"La solución cubre desde la preparación de datos hasta la consulta del horario publicado, con responsabilidades separadas por rol.",64,126,1120,42,17,C.muted);
  card(slide,"Épica 1 · Construcción inteligente","Administrar usuarios, cursos, aulas y docentes. Importar matrícula proyectada, configurar reglas, asignar sesiones, detectar conflictos y exportar el horario.",64,198,544,230,C.green,C.white);
  card(slide,"Épica 2 · Revisión y publicación","Enviar a revisión, aprobar o devolver con observaciones, crear nuevas versiones, publicar y permitir la consulta por docentes y estudiantes.",672,198,544,230,C.purple,C.white);
  text(slide,"Flujo funcional demostrado",64,472,360,26,19,C.dark,{bold:true,fontFamily:FONT_HEAD});
  const stages=[["1","Datos",C.mint],["2","Construir",C.blue],["3","Revisar",C.lavender],["4","Publicar",C.blush],["5","Consultar",C.peach]];
  stages.forEach((s,i)=>{ const x=64+i*226; shape(slide,"ellipse",x,524,44,44,s[2],"none"); text(slide,s[0],x,536,44,18,13,C.dark,{bold:true,align:"center"}); text(slide,s[1],x+54,535,132,20,13,C.dark,{bold:true}); if(i<4) line(slide,x+182,546,x+216,546,C.line,3); });
  footer(slide,2); notes(slide,"Nombrar las dos épicas y recorrer el ciclo completo sin detenerse aún en detalles técnicos.");
}

// 3. Estrategia
{
  const slide=presentation.slides.add(); header(slide,3,"Estrategia y metodología de pruebas","Plan de pruebas");
  text(slide,"Se combinan pruebas tempranas sobre las reglas, verificación de servicios y recorridos visibles por rol. La evidencia manual completa la validación humana.",64,126,1130,48,17,C.muted);
  const levels=[["01","Unitaria","Reglas, entidades y estados","JUnit 5 + Mockito",C.mint,C.forest],["02","Integración","Controladores, seguridad y datos","Spring Boot + H2",C.blue,C.blueInk],["03","Sistema","Recorridos funcionales por rol","Playwright + Chromium",C.lavender,C.purple],["04","Aceptación","Claridad, arrastre y conflictos","Tercero + video",C.blush,C.red]];
  levels.forEach((l,i)=>{ const x=64+i*288; shape(slide,"roundRect",x,220,264,292,C.white,C.line); pill(slide,l[0],x+20,240,54,l[4],l[5]); text(slide,l[1],x+20,289,220,26,19,C.dark,{bold:true,fontFamily:FONT_HEAD}); text(slide,l[2],x+20,332,220,62,13,C.muted); line(slide,x+20,414,x+244,414,C.line,1); text(slide,l[3],x+20,435,220,48,12,l[5],{bold:true}); });
  shape(slide,"roundRect",64,550,1152,82,C.pale,C.line); text(slide,"Criterio de salida",88,570,220,22,14,C.forest,{bold:true,fontFamily:FONT_HEAD}); text(slide,"Compilación exitosa · 100 % de pruebas automáticas aprobadas · cero defectos críticos o altos abiertos",306,570,860,24,15,C.dark,{bold:true});
  footer(slide,3); notes(slide,"Explicar que la pirámide de pruebas reduce el costo de detectar errores y que la aceptación humana sigue siendo obligatoria.");
}

// 4. Trazabilidad y resultados
{
  const slide=presentation.slides.add(); header(slide,4,"Trazabilidad y resultados ejecutados","Evidencia");
  text(slide,"Cada caso se relaciona con una historia de usuario y un tipo de evidencia. La regresión actual cumple el criterio técnico de salida.",64,126,1120,42,17,C.muted);
  const metrics=[["40","casos definidos",C.mint,C.forest],["39","automatizados total o parcialmente",C.blue,C.blueInk],["45","pruebas backend aprobadas",C.lavender,C.purple],["12","pruebas UI aprobadas",C.blush,C.red]];
  metrics.forEach((m,i)=>{ const x=64+i*288; shape(slide,"roundRect",x,200,264,120,m[2],"none"); text(slide,m[0],x+20,219,80,48,32,m[3],{bold:true,fontFamily:FONT_HEAD}); text(slide,m[1],x+92,224,150,54,12,C.dark,{bold:true}); });
  text(slide,"Cobertura por historia",64,365,300,26,19,C.dark,{bold:true,fontFamily:FONT_HEAD});
  const hus=[["HU-01","Administración y maestros","11 casos"],["HU-02","Matrícula y sugerencias","7 casos"],["HU-03","Grilla y reglas","11 casos"],["HU-04","Exportación","2 casos"],["HU-05–08","Revisión, publicación y consulta","9 casos"]];
  hus.forEach((h,i)=>{ const y=410+i*43; pill(slide,h[0],64,y,92,i%2?C.lavender:C.mint,i%2?C.purple:C.forest); text(slide,h[1],170,y+5,430,22,13,C.dark,{bold:true}); text(slide,h[2],590,y+5,100,22,12,C.muted,{bold:true,align:"right"}); });
  shape(slide,"roundRect",748,365,468,235,C.white,C.line); text(slide,"Pendiente de evidencia humana",776,390,390,28,18,C.dark,{bold:true,fontFamily:FONT_HEAD});
  card(slide,"CP-18","Interacciones necesarias para completar el arrastre.",776,438,190,118,C.green,C.pale);
  card(slide,"CP-26","Comprobar visualmente conflictos simultáneos.",986,438,190,118,C.pink,C.blush);
  footer(slide,4); notes(slide,"Mostrar que la única brecha no es técnica: faltan dos evidencias visuales dentro del mismo video con un tercero.");
}

// 5. Casos críticos
{
  const slide=presentation.slides.add(); header(slide,5,"Casos límite que protegen el horario","Reglas de negocio");
  text(slide,"La prueba no solo confirma el camino feliz: fuerza valores frontera, transiciones inválidas y restricciones académicas.",64,126,1120,42,17,C.muted);
  const cases=[
    ["Capacidad","0, 40, 44 y 45 estudiantes","Rechazo o aceptación exacta",C.mint,C.forest],
    ["Disponibilidad","Cruces entre franjas docentes","Bloqueo inmediato",C.blue,C.blueInk],
    ["Horario","Almuerzo y sábado después de 13:00","Restricción institucional",C.peach,C.orange],
    ["Estados","Publicar borrador o editar publicado","Transición inválida",C.lavender,C.purple],
    ["CSV","Cabecera, curso y demanda inválidos","Importación atómica",C.blush,C.red],
    ["Seguridad","Rol sin permiso sobre endpoint","Respuesta 403",C.pale,C.forest]
  ];
  cases.forEach((c,i)=>{ const x=64+(i%3)*384,y=210+Math.floor(i/3)*184; shape(slide,"roundRect",x,y,352,154,C.white,C.line); pill(slide,c[0],x+18,y+18,110,c[3],c[4]); text(slide,c[1],x+18,y+62,316,40,15,C.dark,{bold:true,fontFamily:FONT_HEAD}); text(slide,c[2],x+18,y+111,316,24,12,c[4],{bold:true}); });
  shape(slide,"roundRect",64,594,1120,50,C.pale,"none"); text(slide,"Resultado: los errores se muestran antes de guardar y no dejan datos parciales en la base.",84,608,1080,22,15,C.forest,{bold:true,align:"center"});
  footer(slide,5); notes(slide,"Elegir dos ejemplos para la exposición: valor límite de capacidad e intento de publicar un borrador.");
}

// 6. Bugs
{
  const slide=presentation.slides.add(); header(slide,6,"Categorización y cierre de defectos","Gestión de bugs");
  text(slide,"La severidad depende del impacto sobre disponibilidad, datos, seguridad y continuidad del ciclo funcional.",64,126,1120,42,17,C.muted);
  const sev=[["Crítica","Sistema caído, pérdida de datos o acceso indebido","Inmediata",C.red,C.blush],["Alta","Falla una épica o se publica incorrectamente","< 24 horas",C.orange,C.peach],["Media","La función tiene una alternativa temporal","Antes de entregar",C.purple,C.lavender],["Baja","Detalle visual o mejora de experiencia","Próxima iteración",C.green,C.mint]];
  sev.forEach((s,i)=>{ const y=204+i*74; pill(slide,s[0],64,y,100,s[4],s[3]); text(slide,s[1],184,y+5,620,24,13,C.dark,{bold:true}); pill(slide,s[2],878,y,190,C.pale,C.muted); });
  shape(slide,"roundRect",64,522,1152,112,C.white,C.line); text(slide,"Defectos corregidos durante la implementación",88,544,450,25,18,C.dark,{bold:true,fontFamily:FONT_HEAD});
  const fixes=["disponibilidad docente","tolerancia de matrícula","nueva versión de horario","usuarios y roles","importación CSV"];
  fixes.forEach((f,i)=>pill(slide,f,88+i*210,585,190,i%2?C.lavender:C.mint,i%2?C.purple:C.forest));
  footer(slide,6); notes(slide,"Aclarar que el registro de bugs conserva causa, severidad, corrección y prueba de regresión.");
}

// 7. Heatmap
{
  const slide=presentation.slides.add(); header(slide,7,"Mapa de calor y respuestas planificadas","Gestión de riesgos");
  text(slide,"La valoración combina probabilidad e impacto. El riesgo principal es la indisponibilidad de Neon durante la demostración.",84,126,1100,42,17,C.muted);
  const left=92,top=210,cell=72;
  for(let p=5;p>=1;p--) for(let impact=1;impact<=5;impact++) { const level=p*impact; const fill=level>=16?C.riskCritical:level>=10?C.riskHigh:level>=5?C.riskMedium:C.riskLow; shape(slide,"rect",left+(impact-1)*cell,top+(5-p)*cell,cell,cell,fill,C.white); }
  for(let i=1;i<=5;i++){ text(slide,String(i),left+(i-1)*cell,top+366,cell,20,11,C.dark,{bold:true,align:"center"}); text(slide,String(6-i),left-28,top+(i-1)*cell+24,24,20,11,C.dark,{bold:true,align:"center"}); }
  text(slide,"IMPACTO →",left+104,top+394,180,20,12,C.forest,{bold:true,align:"center"}); text(slide,"PROBABILIDAD ↑",left,top-28,180,20,10,C.forest,{bold:true});
  const risks=[["R-01",5,3],["R-02/03/04",5,2],["R-05",3,3],["R-06",4,3],["R-07",2,3],["R-08",3,2]];
  risks.forEach(r=>text(slide,r[0],left+(r[1]-1)*cell+4,top+(5-r[2])*cell+27,cell-8,18,9,C.dark,{bold:true,align:"center"}));
  text(slide,"Respuestas prioritarias",540,202,480,28,20,C.dark,{bold:true,fontFamily:FONT_HEAD});
  const responses=[["R-01 · Neon","Verificar conexión, conservar video y datos de respaldo.",C.red,C.blush],["R-02 · .env","Ignorar el archivo, rotar credenciales expuestas.",C.orange,C.peach],["R-03/04 · Datos","Flyway, copia y regresión del motor de reglas.",C.purple,C.lavender],["R-05 · CSV","Validación completa y plantilla descargable.",C.blueInk,C.blue],["R-06 · Tiempo","Guion cronometrado para diez minutos.",C.green,C.mint]];
  responses.forEach((r,i)=>card(slide,r[0],r[1],540,244+i*76,676,62,r[2],r[3]));
  footer(slide,7); notes(slide,"Señalar R-01 y R-06. La respuesta no elimina todo el riesgo, pero reduce la probabilidad o el impacto.");
}

// 8. Costos y estándares
{
  const slide=presentation.slides.add(); header(slide,8,"Costos sustentados y estándares aplicados","Calidad y viabilidad");
  text(slide,"La estimación utiliza horas académicas y una reserva calculada con valor monetario esperado. Las tecnologías principales tienen planes gratuitos.",64,126,1120,42,17,C.muted);
  const costs=[["Desarrollo","4 personas × 20 h × $15.000","$1.200.000"],["Pruebas y documentación","4 personas × 8 h × $15.000","$480.000"],["Reserva de riesgos","Suma del VME","$165.000"],["Total estimado","Esfuerzo + reserva","$1.845.000"]];
  shape(slide,"roundRect",64,200,650,306,C.white,C.line); text(slide,"Estimación académica",88,224,300,25,18,C.dark,{bold:true,fontFamily:FONT_HEAD});
  costs.forEach((c,i)=>{ const y=274+i*54; if(i%2===0) shape(slide,"rect",82,y-6,614,44,C.pale,"none"); text(slide,c[0],94,y,210,22,12,C.dark,{bold:true}); text(slide,c[1],306,y,245,22,11,C.muted); text(slide,c[2],558,y,120,22,12,i===3?C.forest:C.dark,{bold:true,align:"right"}); });
  text(slide,"Estándares y propósito",770,200,400,28,20,C.dark,{bold:true,fontFamily:FONT_HEAD});
  const std=[["ISO/IEC/IEEE 29119","Plan, casos, ejecución y evidencias"],["ISO/IEC 25010","Calidad funcional, seguridad y usabilidad"],["OWASP","BCrypt, JWT, RBAC y secretos"],["WCAG 2.1","Contraste, etiquetas y navegación"],["REST + patrones","Separación y mantenibilidad"]];
  std.forEach((s,i)=>{ pill(slide,s[0],770,246+i*65,175,i%2?C.lavender:C.mint,i%2?C.purple:C.forest); text(slide,s[1],960,248+i*65,242,38,11,C.dark,{bold:true}); });
  footer(slide,8); notes(slide,"Explicar VME: probabilidad porcentual por costo del impacto. Los valores representan esfuerzo académico, no una cotización comercial.");
}

// 9. Arquitectura y demo
{
  const slide=presentation.slides.add(); header(slide,9,"Arquitectura preparada para demostrar","Solución en vivo");
  text(slide,"El monolito modular mantiene un despliegue sencillo y separa interfaz, seguridad, servicios, reglas y persistencia.",64,126,1120,42,17,C.muted);
  const nodes=[["React + TypeScript","Interfaz por rol",64,C.mint,C.forest],["Spring Boot 3","REST + JWT",294,C.blue,C.blueInk],["Servicios + reglas","Strategy + State",524,C.lavender,C.purple],["JPA + Flyway","Persistencia",754,C.peach,C.orange],["Neon PostgreSQL","Base administrada",984,C.blush,C.red]];
  nodes.forEach((n,i)=>{ shape(slide,"roundRect",n[2],210,196,100,n[4],n[5]); text(slide,n[0],n[2]+12,230,172,24,14,C.dark,{bold:true,fontFamily:FONT_HEAD,align:"center"}); text(slide,n[1],n[2]+12,265,172,20,11,C.muted,{bold:true,align:"center"}); if(i<4) line(slide,n[2]+196,260,n[2]+230,260,C.line,3); });
  text(slide,"Ruta de demostración · 10 minutos",64,370,440,28,20,C.dark,{bold:true,fontFamily:FONT_HEAD});
  const demo=[["1","Administrador","Usuarios, matrícula y disponibilidad"],["2","Comité de Área","Construcción, regla y exportación"],["3","Comité Táctico","Aprobación o devolución"],["4","Dirección","Publicación y nueva versión"],["5","Docente / estudiante","Consulta del horario publicado"]];
  demo.forEach((d,i)=>{ const x=64+(i%3)*384,y=418+Math.floor(i/3)*82; shape(slide,"ellipse",x,y,34,34,i%2?C.blush:C.mint,"none"); text(slide,d[0],x,y+9,34,15,11,C.dark,{bold:true,align:"center"}); text(slide,d[1],x+48,y-2,150,20,12,C.dark,{bold:true}); text(slide,d[2],x+48,y+22,276,30,10,C.muted); });
  footer(slide,9); notes(slide,"La demo debe mostrar un conflicto real, su corrección, el envío, la aprobación, la publicación y la consulta final.");
}

// 10. Entrega final
{
  const slide=presentation.slides.add(); header(slide,10,"Lo único que queda en manos del equipo","Cierre");
  text(slide,"El código, los documentos, el Kanban, las pruebas y esta presentación quedan preparados. Solo faltan actividades que requieren personas o la plataforma académica.",64,126,1120,48,17,C.muted);
  card(slide,"1 · Validación con un tercero","Pídele que complete el flujo sin ayuda. Graba la pantalla y registra sus comentarios en la plantilla de evidencia.",64,220,350,190,C.green,C.white);
  card(slide,"2 · Evidencias CP-18 y CP-26","Cuenta las interacciones del arrastre y muestra visualmente los conflictos simultáneos dentro del mismo video.",465,220,350,190,C.pink,C.white);
  card(slide,"3 · Presentar y entregar","Sube el PDF y la presentación, asocia el CSV de casos al gestor del proyecto y realiza la exposición cronometrada.",866,220,350,190,C.purple,C.white);
  shape(slide,"roundRect",64,474,1152,118,C.mint,"none");
  text(slide,"Checklist final",88,496,220,24,17,C.forest,{bold:true,fontFamily:FONT_HEAD});
  const checks=["Video con tercero","Capturas o enlace","PDF formal","PPTX","Kanban actualizado","Demo lista"];
  checks.forEach((c,i)=>{ shape(slide,"ellipse",88+i*182,542,18,18,C.white,C.green); text(slide,c,114+i*182,541,146,22,11,C.dark,{bold:true}); });
  text(slide,"El sistema está listo para ejecutarse y defenderse.",190,628,900,30,22,C.dark,{bold:true,fontFamily:FONT_HEAD,align:"center"});
  footer(slide,10); notes(slide,"Cerrar indicando claramente que el trabajo restante exige participación humana: video, carga en la plataforma y exposición.");
}

async function writeBlob(filePath, blob) { await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer())); }

async function main() {
  await fs.mkdir(previewDir, { recursive: true }); await fs.mkdir(path.dirname(finalPath), { recursive: true });
  for (const [i,slide] of presentation.slides.items.entries()) {
    const png = await presentation.export({ slide, format: "png", scale: 1 });
    await writeBlob(path.join(previewDir, `slide-${String(i+1).padStart(2,"0")}.png`), png);
  }
  const montage = await presentation.export({ format: "webp", montage: true }); await writeBlob(path.join(buildDir,"montage.webp"), montage);
  const { finalizePresentation } = await import(pathToFileURL(path.join(skillDir,"container_tools/artifact_tool_utils.mjs")).href);
  const candidatePath = path.join(buildDir,"candidate.pptx"); await (await PresentationFile.exportPptx(presentation)).save(candidatePath);
  const result = await finalizePresentation({
    workspaceDir, candidatePath, finalPath, explicitTotalSlideCount: 10,
    requiredNativeTableOwnerSlides: [], requiredNativeChartOwnerSlides: [], pythonExecutable,
    integrityValidatorPath: path.join(skillDir,"container_tools/inspect_presentation_package_integrity.py"),
    layoutValidatorPath: path.join(skillDir,"container_tools/inspect_presentation_layout_geometry.py"),
    layoutArgs: ["--expected-slide-size-emu","12192000,6858000","--validate-heading-fit"],
    fontPolicy: { basis: "design", families: [FONT_HEAD,FONT_BODY,"Aptos"] },
    verifyArtifactToolImport: true, receiptPath: path.join(buildDir,"validation.json")
  });
  console.log(JSON.stringify({ finalPath, previewDir, result }, null, 2));
}

main().catch(error => { console.error(error); process.exitCode = 1; });
