import {ArrowRight,BookOpen,CalendarDays,CheckCircle2,Clock3,DoorOpen,Users} from 'lucide-react';
import {useQuery} from '@tanstack/react-query';
import {Link} from 'react-router-dom';
import Shell from '../components/Shell';
import {api} from '../lib/api';
import type {User} from '../types';

export default function Dashboard({user,onLogout}:{user:User;onLogout:()=>void}){
  const publicOnly=['DOCENTE','ESTUDIANTE'].includes(user.role);
  const canReadMasters=['DIRECCION','ADMINISTRADOR','COMITE_AREA','COMITE_TACTICO'].includes(user.role);
  const {data:schedule}=useQuery({queryKey:['schedule',publicOnly],queryFn:()=>api.schedule(publicOnly),retry:false});
  const {data:masters}=useQuery({queryKey:['master-data-summary'],queryFn:api.catalogs,enabled:canReadMasters,retry:false});
  const remaining=schedule?.unassigned.reduce((total,section)=>total+Math.max(0,section.weeklyFrequency-section.assignedSessions),0)??0;
  const assigned=schedule?.sessions.length??0;
  const total=assigned+remaining;
  const completion=total?Math.round(assigned*100/total):0;
  const actionLabel=publicOnly?'Consultar horario':'Abrir constructor';

  return <Shell user={user} onLogout={onLogout}><section className="content">
    <article className="hero">
      <div><span className="status-dot">Sistema académico conectado</span><h1>{publicOnly?'Tu semana académica, clara y organizada.':'Construyamos un horario que funcione para todos.'}</h1><p>{publicOnly?'Consulta la versión oficial publicada por la Universidad El Bosque, con aulas, docentes y horas en un solo lugar.':'Organiza las sesiones, valida conflictos y acompaña la propuesta hasta su aprobación y publicación.'}</p><Link className="primary" to="/horario"><CalendarDays size={17}/>{actionLabel}<ArrowRight size={16}/></Link></div>
      <div className="hero-art" aria-hidden="true"><i className="hero-bubble bubble-one"/><div className="calendar-art"><span>Horario</span><b>{completion}%</b><small>{schedule?.status?.toLowerCase().replaceAll('_',' ')??'en preparación'}</small></div><i className="hero-bubble bubble-two"/></div>
    </article>

    <div className="section-heading"><h2>Vista general</h2><span>Datos actualizados desde Neon</span></div>
    <div className="stats">
      <article className="stat"><span className="stat-icon mint"><BookOpen/></span><div><strong>{canReadMasters?masters?.courses.length??'—':assigned}</strong><small>{canReadMasters?'Asignaturas registradas':'Sesiones publicadas'}</small></div></article>
      <article className="stat"><span className="stat-icon lavender"><Users/></span><div><strong>{canReadMasters?masters?.teachers.length??'—':'—'}</strong><small>Docentes registrados</small></div></article>
      <article className="stat"><span className="stat-icon peach"><DoorOpen/></span><div><strong>{canReadMasters?masters?.classrooms.length??'—':'—'}</strong><small>Aulas disponibles</small></div></article>
      <article className="stat"><span className="stat-icon blue"><CheckCircle2/></span><div><strong>{completion}%</strong><small>Programación completada</small></div></article>
    </div>

    <div className="dashboard-grid">
      <article className="panel"><div className="panel-title"><div><p className="eyebrow">Progreso del periodo</p><h3>{schedule?.period??'Periodo académico'}</h3></div><span className="badge green">{pretty(schedule?.status??'SIN PUBLICAR')}</span></div><div className="big-progress"><div style={{width:`${completion}%`}}/></div><div className="progress-meta"><span><b>{assigned}</b> sesiones asignadas</span><span><b>{remaining}</b> sesiones pendientes</span><span><b>v{schedule?.version??1}</b> versión</span></div><Link className="text-link" to="/horario">{actionLabel}<ArrowRight size={15}/></Link></article>
      <article className="panel"><div className="panel-title"><div><p className="eyebrow">Próximos pasos</p><h3>Ruta del horario</h3></div><Clock3 size={18}/></div><div className="task"><span className="task-icon"><CalendarDays/></span><div><b>Completar la programación</b><small>Ubica todas las sesiones requeridas sin conflictos.</small></div></div><div className="task"><span className="task-icon purple"><CheckCircle2/></span><div><b>Revisar y publicar</b><small>El comité valida la propuesta antes de hacerla oficial.</small></div></div></article>
    </div>
  </section></Shell>;
}

const pretty=(value:string)=>value.toLowerCase().replaceAll('_',' ').replace(/^./,letter=>letter.toUpperCase());
