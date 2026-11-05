import {Navigate,Route,Routes} from 'react-router-dom';
import {useState} from 'react';
import type {User} from './types';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import SchedulePage from './pages/SchedulePage';
import MasterDataPage from './pages/MasterDataPage';
import ReviewPage from './pages/ReviewPage';
import ParametersPage from './pages/ParametersPage';
import UsersPage from './pages/UsersPage';
import EnrollmentPage from './pages/EnrollmentPage';

const personalize=(user:User):User=>user.email==='area@unbosque.edu.co'?{...user,name:'Pepita Área'}:user;
export default function App(){
  const [user,setUser]=useState<User|null>(()=>{const raw=localStorage.getItem('unbosque_user');if(!raw)return null;return personalize(JSON.parse(raw) as User)});
  const signIn=(received:User)=>{const user=personalize(received);localStorage.setItem('unbosque_token',user.token);localStorage.setItem('unbosque_user',JSON.stringify(user));setUser(user)};
  const signOut=()=>{localStorage.removeItem('unbosque_token');localStorage.removeItem('unbosque_user');setUser(null)};
  const guarded=(page:React.ReactNode)=>user?page:<Navigate to="/login"/>;
  return <Routes>
    <Route path="/login" element={user?<Navigate to="/"/>:<Login onLogin={signIn}/>}/>
    <Route path="/" element={guarded(<Dashboard user={user!} onLogout={signOut}/>)}/>
    <Route path="/horario" element={guarded(<SchedulePage user={user!} onLogout={signOut}/>)}/>
    <Route path="/datos-maestros" element={guarded(<MasterDataPage user={user!} onLogout={signOut}/>)}/>
    <Route path="/revision" element={guarded(<ReviewPage user={user!} onLogout={signOut}/>)}/>
    <Route path="/parametros" element={guarded(<ParametersPage user={user!} onLogout={signOut}/>)}/>
    <Route path="/usuarios" element={guarded(<UsersPage user={user!} onLogout={signOut}/>)}/>
    <Route path="/matricula" element={guarded(<EnrollmentPage user={user!} onLogout={signOut}/>)}/>
    <Route path="*" element={<Navigate to="/"/>}/>
  </Routes>;
}
