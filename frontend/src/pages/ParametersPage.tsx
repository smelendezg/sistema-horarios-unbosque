import {useMutation,useQuery,useQueryClient} from '@tanstack/react-query';
import {Clock3,Save,Settings2,SlidersHorizontal} from 'lucide-react';
import {useEffect,useState} from 'react';
import Shell from '../components/Shell';
import {api} from '../lib/api';
import type {User} from '../types';

export default function ParametersPage({user,onLogout}:{user:User;onLogout:()=>void}){
  const qc=useQueryClient();const {data,isLoading,error}=useQuery({queryKey:['parameters'],queryFn:api.parameters});const [values,setValues]=useState<Record<string,string>>({});const [message,setMessage]=useState('');const writable=['DIRECCION','ADMINISTRADOR'].includes(user.role);
  useEffect(()=>{if(data)setValues(Object.fromEntries(data.map(x=>[x.clave,x.valor])))},[data]);
  const save=useMutation({mutationFn:async()=>{for(const [key,value] of Object.entries(values))await api.updateParameter(key,value)},onSuccess:()=>{setMessage('Parámetros guardados. Los nuevos valores ya están disponibles.');qc.invalidateQueries({queryKey:['parameters']})},onError:e=>setMessage(e instanceof Error?e.message:'No pudimos guardar.')});
  return <Shell user={user} onLogout={onLogout}><section className="content page-section"><div className="page-title"><div><p className="eyebrow">Configuración normativa</p><h1>Parámetros</h1><p>Ajusta las reglas institucionales sin modificar ni recompilar el código.</p></div><span className="soft-pill"><Settings2 size={16}/>Configuración activa</span></div>{message&&<div className={message.startsWith('Parámetros')?'notice success':'notice error'}>{message}</div>}{error&&<div className="notice error">No pudimos cargar los parámetros.</div>}{isLoading?<div className="loading">Cargando configuración…</div>:<div className="parameter-grid">{data?.map((item,i)=><article className="panel parameter-card" key={item.id}><span className={`parameter-icon ${i%2?'pink':''}`}>{item.clave.includes('hora')?<Clock3/>:<SlidersHorizontal/>}</span><div><b>{pretty(item.clave)}</b><small>{item.descripcion}</small></div><label>Valor<div className="parameter-input"><input value={values[item.clave]??''} onChange={e=>setValues({...values,[item.clave]:e.target.value})} disabled={!writable}/><span>{item.unidad}</span></div></label></article>)}</div>} {writable&&<div className="sticky-actions"><span>Los cambios se almacenan en Neon DB.</span><button className="primary" onClick={()=>save.mutate()} disabled={save.isPending}><Save size={17}/>{save.isPending?'Guardando…':'Guardar parámetros'}</button></div>}</section></Shell>;
}
const pretty=(s:string)=>s.replaceAll('_',' ').replace(/^./,c=>c.toUpperCase());
