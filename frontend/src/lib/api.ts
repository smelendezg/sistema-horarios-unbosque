import type {Schedule,User,Issue,MasterData,Parameter,Review,ManagedUser,Availability,Enrollment,ImportResult} from '../types';

const base=import.meta.env.VITE_API_URL??'/api';
const authHeaders=():Record<string,string>=>{const token=localStorage.getItem('unbosque_token');return token?{Authorization:`Bearer ${token}`}:{}};
const headers=(extra?:HeadersInit)=>{const result=new Headers(extra);result.set('Content-Type','application/json');for(const [key,value] of Object.entries(authHeaders()))result.set(key,value);return result};

async function request<T>(path:string,options:RequestInit={}):Promise<T>{
  let res:Response;
  try{res=await fetch(base+path,{...options,headers:headers(options.headers)})}
  catch{throw new Error('No pudimos conectar con el servidor. Verifica que el backend esté activo en el puerto 8080.');}
  if(!res.ok){
    const body=await res.json().catch(()=>({}));
    if(res.status===401&&path!=='/auth/login'){
      localStorage.removeItem('unbosque_token');localStorage.removeItem('unbosque_user');window.location.assign('/login');
      throw new Error('Tu sesión terminó. Ingresa nuevamente para continuar.');
    }
    if(res.status===403)throw new Error('Tu perfil no tiene permiso para realizar esta acción.');
    throw new Error(body.message??body.detail??'No fue posible completar la solicitud.');
  }
  if(res.status===204)return undefined as T;
  return res.json();
}

async function download(path:string,filename:string){
  const res=await fetch(base+path,{headers:authHeaders()});
  if(!res.ok)throw new Error(res.status===403?'Tu perfil no tiene permiso para exportar.':'No pudimos generar el archivo.');
  const url=URL.createObjectURL(await res.blob());const link=document.createElement('a');link.href=url;link.download=filename;link.click();URL.revokeObjectURL(url);
}

export const api={
  async login(correo:string,clave:string):Promise<User>{const r=await request<{token:string;nombre:string;correo:string;rol:string}>('/auth/login',{method:'POST',body:JSON.stringify({correo,clave})});return{name:r.nombre,email:r.correo,role:r.rol,token:r.token};},
  schedule:(published=false)=>request<Schedule>(published?'/public/schedule':'/schedules/current'),
  createSchedule:()=>request<Schedule>('/schedules',{method:'POST'}),
  assign:(scheduleId:number,seccionId:number,aulaId:number,dia:number,horaInicio:string)=>request<{session:null|Schedule['sessions'][number];issues:Issue[]}>(`/schedules/${scheduleId}/sessions`,{method:'POST',body:JSON.stringify({seccionId,aulaId,dia,horaInicio})}),
  move:(scheduleId:number,sessionId:number,aulaId:number,dia:number,horaInicio:string)=>request<{session:null|Schedule['sessions'][number];issues:Issue[]}>(`/schedules/${scheduleId}/sessions/${sessionId}`,{method:'PUT',body:JSON.stringify({aulaId,dia,horaInicio})}),
  removeSession:(scheduleId:number,sessionId:number)=>request<void>(`/schedules/${scheduleId}/sessions/${sessionId}`,{method:'DELETE'}),
  submit:(id:number)=>request<Schedule>(`/schedules/${id}/submit`,{method:'POST'}),
  review:(id:number,decision:'APROBADO'|'DEVUELTO',observaciones:string)=>request<Schedule>(`/schedules/${id}/review`,{method:'POST',body:JSON.stringify({decision,observaciones})}),
  publish:(id:number)=>request<Schedule>(`/schedules/${id}/publish`,{method:'POST'}),
  reviews:(id:number)=>request<Review[]>(`/schedules/${id}/reviews`),
  catalogs:()=>request<MasterData>('/catalogs'),
  masterData:()=>request<MasterData>('/master-data'),
  saveMaster:(type:string,data:unknown,id?:number)=>request<unknown>(`/master-data/${type}${id?`/${id}`:''}`,{method:id?'PUT':'POST',body:JSON.stringify(data)}),
  deleteMaster:(type:string,id:number)=>request<void>(`/master-data/${type}/${id}`,{method:'DELETE'}),
  parameters:()=>request<Parameter[]>('/parameters'),
  updateParameter:(key:string,valor:string)=>request<Parameter>(`/parameters/${key}`,{method:'PUT',body:JSON.stringify({valor})}),
  users:()=>request<ManagedUser[]>('/users'),
  createUser:(data:{nombre:string;correo:string;clave:string;rol:string})=>request<ManagedUser>('/users',{method:'POST',body:JSON.stringify(data)}),
  updateUserRole:(id:number,rol:string)=>request<ManagedUser>(`/users/${id}/role`,{method:'PUT',body:JSON.stringify({rol})}),
  updateUserStatus:(id:number,activo:boolean)=>request<ManagedUser>(`/users/${id}/status`,{method:'PUT',body:JSON.stringify({activo})}),
  availability:(teacherId:number)=>request<Availability[]>(`/teachers/${teacherId}/availability`),
  saveAvailability:(teacherId:number,ranges:Availability[])=>request<Availability[]>(`/teachers/${teacherId}/availability`,{method:'PUT',body:JSON.stringify(ranges.map(({dia,horaInicio,horaFin})=>({dia,horaInicio,horaFin})))}),
  enrollments:(periodId:number)=>request<Enrollment[]>(`/enrollments?periodId=${periodId}`),
  async importEnrollments(periodId:number,file:File):Promise<ImportResult>{const data=new FormData();data.append('file',file);const res=await fetch(`${base}/enrollments/import?periodId=${periodId}`,{method:'POST',headers:authHeaders(),body:data});const body=await res.json().catch(()=>({}));if(!res.ok)throw new Error(body.message??body.detail??'No pudimos importar la matrícula.');return body;},
  export:(id:number,format:'pdf'|'xlsx')=>download(`/schedules/${id}/export.${format}`,`horario-unbosque.${format}`)
};
