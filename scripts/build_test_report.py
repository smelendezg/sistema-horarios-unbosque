from pathlib import Path
import csv
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.section import WD_SECTION
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'output'
OUT.mkdir(exist_ok=True)
doc=Document()
sec=doc.sections[0]
sec.page_width=Inches(8.5);sec.page_height=Inches(11)
sec.top_margin=Inches(.72);sec.bottom_margin=Inches(.68);sec.left_margin=Inches(.72);sec.right_margin=Inches(.72)

GREEN='176B4B'; DARK='18352B'; PALE='EAF7EF'; LINE='D9D9D9'; MUTED='63776F'; WHITE='FFFFFF'

def font(run,name='Arial',size=10.5,bold=False,color='000000'):
    run.font.name=name;run.font.size=Pt(size);run.font.bold=bold;run.font.color.rgb=RGBColor.from_string(color)
    run._element.get_or_add_rPr().rFonts.set(qn('w:ascii'),name);run._element.get_or_add_rPr().rFonts.set(qn('w:hAnsi'),name)

styles=doc.styles
for style_name,size,bold in [('Normal',10.5,False),('Title',24,True),('Heading 1',15,True),('Heading 2',12,True)]:
    s=styles[style_name];s.font.name='Arial';s.font.size=Pt(size);s.font.bold=bold;s.font.color.rgb=RGBColor(0,0,0)
    s._element.get_or_add_rPr().rFonts.set(qn('w:ascii'),'Arial');s._element.get_or_add_rPr().rFonts.set(qn('w:hAnsi'),'Arial')
styles['Normal'].paragraph_format.space_after=Pt(5);styles['Normal'].paragraph_format.line_spacing=1.08
styles['Heading 1'].paragraph_format.space_before=Pt(12);styles['Heading 1'].paragraph_format.space_after=Pt(6)
styles['Heading 2'].paragraph_format.space_before=Pt(8);styles['Heading 2'].paragraph_format.space_after=Pt(4)
# Word's built-in Title style can carry a theme border. Remove it for a clean cover.
title_ppr=styles['Title']._element.get_or_add_pPr();title_border=title_ppr.find(qn('w:pBdr'))
if title_border is not None:title_ppr.remove(title_border)

def shade(cell,color):
    tcPr=cell._tc.get_or_add_tcPr();shd=tcPr.find(qn('w:shd'))
    if shd is None: shd=OxmlElement('w:shd');tcPr.append(shd)
    shd.set(qn('w:fill'),color)

def margins(cell,top=90,start=100,bottom=90,end=100):
    tc=cell._tc;tcPr=tc.get_or_add_tcPr();tcMar=tcPr.first_child_found_in('w:tcMar')
    if tcMar is None: tcMar=OxmlElement('w:tcMar');tcPr.append(tcMar)
    for m,v in [('top',top),('start',start),('bottom',bottom),('end',end)]:
        node=tcMar.find(qn('w:'+m))
        if node is None: node=OxmlElement('w:'+m);tcMar.append(node)
        node.set(qn('w:w'),str(v));node.set(qn('w:type'),'dxa')

def borders(table):
    tblPr=table._tbl.tblPr;tb=OxmlElement('w:tblBorders')
    for edge in ('top','left','bottom','right','insideH','insideV'):
        el=OxmlElement('w:'+edge);el.set(qn('w:val'),'single');el.set(qn('w:sz'),'4');el.set(qn('w:color'),LINE);tb.append(el)
    tblPr.append(tb)

def table(headers,rows,widths=None,font_size=8.5):
    t=doc.add_table(rows=1,cols=len(headers));t.alignment=WD_TABLE_ALIGNMENT.CENTER;t.autofit=False;borders(t)
    for i,h in enumerate(headers):
        c=t.rows[0].cells[i];shade(c,GREEN);margins(c);c.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
        p=c.paragraphs[0];p.alignment=WD_ALIGN_PARAGRAPH.CENTER;r=p.add_run(h);font(r,size=9,bold=True,color=WHITE)
    trPr=t.rows[0]._tr.get_or_add_trPr();repeat=OxmlElement('w:tblHeader');repeat.set(qn('w:val'),'true');trPr.append(repeat)
    for ri,row in enumerate(rows):
        cells=t.add_row().cells
        for i,value in enumerate(row):
            c=cells[i];margins(c);c.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
            if ri%2: shade(c,'F6FAF7')
            p=c.paragraphs[0];p.alignment=WD_ALIGN_PARAGRAPH.CENTER if len(str(value))<18 else WD_ALIGN_PARAGRAPH.LEFT
            r=p.add_run(str(value));font(r,size=font_size,color=DARK)
    if widths:
        for row in t.rows:
            for i,w in enumerate(widths):row.cells[i].width=Inches(w)
    doc.add_paragraph().paragraph_format.space_after=Pt(1)
    return t

def bullet(text,level=0):
    p=doc.add_paragraph(style='List Bullet' if level==0 else 'List Bullet 2');font(p.add_run(text),size=10.3,color=DARK);return p

# Cover
p=doc.add_paragraph();p.alignment=WD_ALIGN_PARAGRAPH.CENTER;p.paragraph_format.space_before=Pt(70)
r=p.add_run('UNIVERSIDAD EL BOSQUE');font(r,size=13,bold=True,color=GREEN)
p=doc.add_paragraph(style='Title');p.alignment=WD_ALIGN_PARAGRAPH.CENTER;p.add_run('Plan de pruebas y gestión de riesgos')
p=doc.add_paragraph();p.alignment=WD_ALIGN_PARAGRAPH.CENTER;r=p.add_run('Sistema de Programación de Horarios Académicos');font(r,size=16,bold=True,color=DARK)
p=doc.add_paragraph();p.alignment=WD_ALIGN_PARAGRAPH.CENTER;p.paragraph_format.space_before=Pt(28);font(p.add_run('Grupo 5\nIngeniería de Sistemas\nNoviembre de 2026'),size=11,color=MUTED)
p=doc.add_paragraph();p.alignment=WD_ALIGN_PARAGRAPH.CENTER;p.paragraph_format.space_before=Pt(85);font(p.add_run('Documento de entrega académica con estructura basada en ISO IEC IEEE 29119'),size=9,color=MUTED)
doc.add_page_break()

doc.add_heading('Resumen',level=1)
p=doc.add_paragraph();font(p.add_run('Este documento presenta el plan de pruebas, los cuarenta casos trazables a las historias de usuario, la categorización de defectos y la gestión de riesgos del prototipo. La regresión ejecutada contiene 45 pruebas Java y 12 pruebas de interfaz, todas aprobadas. Treinta y nueve casos cuentan con automatización total o parcial. CP-18 y la comprobación visual de CP-26 requieren evidencia con un tercero.'),size=10.5)
p=doc.add_paragraph();font(p.add_run('Palabras clave: '),bold=True);font(p.add_run('horarios académicos, pruebas de software, riesgos, Spring Boot, React, Neon PostgreSQL.'),color=DARK)

doc.add_heading('1 Introducción',level=1)
doc.add_paragraph('El sistema organiza la construcción, revisión, publicación y consulta de horarios académicos. Los riesgos principales se concentran en la disponibilidad de la base de datos, la integridad de las reglas y la correcta asignación de permisos. El plan combina pruebas automáticas y una validación manual de usabilidad.')

doc.add_heading('2 Alcance y estrategia',level=1)
for text in ['Pruebas unitarias para reglas, entidades y transiciones de estado.','Pruebas de controladores y servicios para seguridad, usuarios, matrícula y exportación.','Pruebas Playwright para recorridos por rol y comportamiento visible.','Prueba manual con un tercero para medir interacciones y claridad de conflictos.']:
    bullet(text)
doc.add_paragraph('Criterio de aceptación: la compilación debe finalizar sin errores, todas las pruebas automáticas deben aprobar y no puede quedar abierto ningún defecto crítico o alto.')

doc.add_heading('3 Épicas e historias de usuario',level=1)
table(['Épica','Historia','Propósito'],[
    ['Construcción inteligente','HU-01','Administrar usuarios, maestros, parámetros y disponibilidad'],
    ['Construcción inteligente','HU-02','Importar matrícula y sugerir secciones'],
    ['Construcción inteligente','HU-03','Asignar y mover sesiones con reglas inmediatas'],
    ['Construcción inteligente','HU-04','Exportar PDF y Excel'],
    ['Revisión y publicación','HU-05 a HU-08','Enviar, decidir, publicar y consultar según el rol'],
],[1.45,1.0,4.4],9)

doc.add_page_break()
doc.add_heading('4 Casos de prueba y trazabilidad',level=1)
with open(ROOT/'docs/kanban-casos-prueba.csv',encoding='utf-8') as f:
    cases=list(csv.DictReader(f))
rows=[[c['ID'],c['HU'],c['Titulo'],c['Tipo'],c['Estado']] for c in cases]
table(['ID','HU','Caso','Tipo','Estado'],rows,[.55,.75,3.25,1.0,1.1],7.7)

doc.add_heading('5 Resultados de ejecución',level=1)
table(['Capa','Herramienta','Pruebas','Resultado'],[
    ['Backend','JUnit 5, Mockito y Maven','45','45 aprobadas'],
    ['Frontend','Playwright y Chromium','12','12 aprobadas'],
    ['Compilación','TypeScript y Vite','1 build','Exitoso'],
    ['Manual','Tercero','CP-18 y visual CP-26','Pendiente de video'],
],[1.25,2.2,.9,2.3],9)

doc.add_heading('6 Categorización de bugs',level=1)
table(['Severidad','Definición','Objetivo'],[
    ['Crítica','Sistema indisponible, pérdida de datos o acceso indebido','Corrección inmediata'],
    ['Alta','Falla de una épica o publicación incorrecta','Antes de 24 horas'],
    ['Media','Función usable mediante alternativa','Antes de entregar'],
    ['Baja','Problema visual o mejora de experiencia','Próxima iteración'],
],[1.1,3.8,1.7],9)
doc.add_paragraph('Los defectos de disponibilidad docente, tolerancia, nueva versión, usuarios y matrícula quedaron cerrados. El registro detallado se conserva en registro-bugs.md.')

doc.add_heading('7 Gestión de riesgos',level=1)
risks=[
['R-01','Neon no disponible',3,5,15,'Verificar conexión y conservar video'],
['R-02','Exposición del .env',2,5,10,'Ignorar archivo y rotar credenciales'],
['R-03','Migración incompatible',2,5,10,'Migraciones nuevas y respaldo'],
['R-04','Conflicto no detectado',2,5,10,'Motor de reglas y regresión'],
['R-05','CSV inválido',3,3,9,'Validación completa y plantilla'],
['R-06','Demora en exposición',3,4,12,'Guion cronometrado'],
['R-07','Puertos ocupados',3,2,6,'Comprobar procesos antes de iniciar'],
['R-08','Arrastre poco claro',2,3,6,'Instrucción visible y prueba humana'],
]
table(['ID','Riesgo','P','I','Nivel','Respuesta'],risks,[.5,2.05,.4,.4,.6,2.6],7.8)
img=ROOT/'.artifacts/heatmap.png'
if img.exists():
    p=doc.add_paragraph();p.alignment=WD_ALIGN_PARAGRAPH.CENTER;p.add_run().add_picture(str(img),width=Inches(6.65))
    p=doc.add_paragraph('Figura 1 Mapa de calor de riesgos');p.alignment=WD_ALIGN_PARAGRAPH.CENTER

doc.add_heading('8 Análisis cuantitativo y costos',level=1)
table(['Concepto','Cálculo','Valor COP'],[
    ['Desarrollo','4 personas x 20 h x $15.000','$1.200.000'],
    ['Pruebas y documentación','4 personas x 8 h x $15.000','$480.000'],
    ['Reserva de riesgos','Suma de valores monetarios esperados','$165.000'],
    ['Total académico estimado','Esfuerzo más reserva','$1.845.000'],
],[2.0,3.15,1.45],9)
doc.add_paragraph('La reserva utiliza VME = probabilidad porcentual por costo del impacto. Neon y las tecnologías principales emplean planes gratuitos o licencias de código abierto.')

doc.add_heading('9 Estándares y arquitectura',level=1)
for text in ['ISO IEC IEEE 29119 organiza el plan, el diseño de casos y las evidencias.','ISO IEC 25010 orienta adecuación funcional, seguridad, usabilidad, rendimiento y mantenibilidad.','OWASP orienta BCrypt, JWT, RBAC y manejo seguro de credenciales.','WCAG 2.1 orienta etiquetas, contraste, teclado y mensajes que no dependen solo del color.','REST, Repository, Service, Strategy, State y DTO separan responsabilidades.','Flyway mantiene la evolución reproducible del esquema de Neon PostgreSQL.']:
    bullet(text)

doc.add_heading('10 Demostración y cierre',level=1)
doc.add_paragraph('La demostración recorre administración, matrícula, construcción, conflicto, revisión, publicación, consulta y exportación. El guion cronometrado se encuentra en guion-demostracion.md. Para cerrar la entrega, un tercero debe completar la evidencia CP-18 y la validación visual CP-26.')

doc.add_heading('Referencias',level=1)
for ref in ['ISO/IEC/IEEE 29119 Software testing.','ISO/IEC 25010 Systems and software quality models.','OWASP Application Security Verification Standard.','Documentación técnica del Sistema de Programación de Horarios Académicos.']:
    p=doc.add_paragraph();font(p.add_run(ref),size=9.5)

# Footer
for section in doc.sections:
    p=section.footer.paragraphs[0];p.alignment=WD_ALIGN_PARAGRAPH.CENTER
    font(p.add_run('Universidad El Bosque  |  Grupo 5  |  Plan de pruebas y riesgos'),size=8,color=MUTED)

path=OUT/'Plan_Pruebas_Riesgos_Horarios_UnBosque.docx'
doc.save(path)
print(path)
