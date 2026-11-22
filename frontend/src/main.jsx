import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000';
const demo = { customerId:'11111111-1111-1111-1111-111111111111', restaurantId:'22222222-2222-2222-2222-222222222222', menuItemId:'33333333-3333-3333-3333-333333333333' };

async function request(path, options = {}, token) {
  const headers = { 'Content-Type':'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${API}${path}`, { ...options, headers });
  const text = await response.text();
  let body = text; try { body = text ? JSON.parse(text) : null; } catch {}
  if (!response.ok) throw new Error(`${response.status}: ${typeof body === 'string' ? body : JSON.stringify(body)}`);
  return body;
}
const id = (value) => value?.id || value?.orderId || value?.restaurantId || value?.menuItemId || value;
const terminalOrderStatuses = new Set(['REJECTED', 'CONFIRMED', 'CANCELLED']);

function App() {
  const [token, setToken] = useState(localStorage.getItem('mealMateToken') || '');
  const [login, setLogin] = useState({ username:'customer', password:'password' });
  const [restaurants, setRestaurants] = useState([]), [restaurantId, setRestaurantId] = useState(demo.restaurantId);
  const [menu, setMenu] = useState([]), [selectedItem, setSelectedItem] = useState(demo.menuItemId);
  const [orderId, setOrderId] = useState(''), [order, setOrder] = useState(null), [payment, setPayment] = useState(null), [delivery, setDelivery] = useState(null), [notifications, setNotifications] = useState([]);
  const [flags, setFlags] = useState({ failPayment:false, failDeliveryAssignment:false });
  const [message, setMessage] = useState('');
  const authed = Boolean(token);
  const loadRestaurants = async () => { try { const data = await request('/restaurants', {}, token); setRestaurants(Array.isArray(data) ? data : data.content || []); } catch(e) { setMessage(e.message); } };
  const loadMenu = async (rid = restaurantId) => { try { const data = await request(`/restaurants/${rid}/menu-items`, {}, token); setMenu(Array.isArray(data) ? data : data.content || []); } catch(e) { setMessage(e.message); } };
  const refreshOrder = async (oid = orderId) => { if (!oid) return; try { const [o,p,d,n] = await Promise.all([request(`/food-orders/${oid}`,{},token), request(`/payments/${oid}`,{},token).catch(()=>null), request(`/deliveries/${oid}`,{},token).catch(()=>null), request(`/notifications/${oid}`,{},token).catch(()=>[]) ]); setOrder(o); setPayment(p); setDelivery(d); setNotifications(Array.isArray(n) ? n : n?.content || []); } catch(e) { setMessage(e.message); } };
  useEffect(() => { if (authed) loadRestaurants(); }, [authed]);
  useEffect(() => {
    if (!authed || !orderId || terminalOrderStatuses.has(order?.status)) return;
    let active = true;
    const poll = async () => { if (active) await refreshOrder(); };
    poll();
    const timer = setInterval(poll, 4000);
    return () => { active = false; clearInterval(timer); };
  }, [authed, orderId, order?.status]);
  const doLogin = async (e) => { e.preventDefault(); try { const result = await request('/auth/login', { method:'POST', body:JSON.stringify(login) }); const value = result?.token || result?.accessToken || result; setToken(value); localStorage.setItem('mealMateToken', value); setMessage('Authenticated through the API gateway.'); } catch(e) { setMessage(e.message); } };
  const createOrder = async () => { try { const result = await request('/food-orders', { method:'POST', headers:{'Idempotency-Key':crypto.randomUUID()}, body:JSON.stringify({ ...demo, restaurantId, items:[{ menuItemId:selectedItem, quantity:1 }], ...flags }) }, token); const oid = id(result); setOrderId(oid); setOrder(result); setMessage(result.status === 'REJECTED' ? `Order ${oid} was rejected; polling stopped.` : `Order ${oid} created; polling status and related views.`); } catch(e) { setMessage(e.message); } };
  const cancel = async () => { try { await request(`/food-orders/${orderId}/cancel`, { method:'POST' }, token); await refreshOrder(); setMessage('Cancellation requested.'); } catch(e) { setMessage(e.message); } };
  if (!authed) return <main className="login"><section className="card"><span className="eyebrow">MEAL MATE</span><h1>Secure food delivery demo</h1><p>Sign in through the gateway to browse restaurants and run the Saga flow.</p><form onSubmit={doLogin}><label>Username<input value={login.username} onChange={e=>setLogin({...login,username:e.target.value})}/></label><label>Password<input type="password" value={login.password} onChange={e=>setLogin({...login,password:e.target.value})}/></label><button>Sign in</button></form>{message && <div className="notice">{message}</div>}</section></main>;
  return <main><header><div><span className="eyebrow">MEAL MATE</span><h1>Operations console</h1></div><button className="secondary" onClick={()=>{localStorage.removeItem('mealMateToken');setToken('')}}>Sign out</button></header><div className="grid"><section className="card"><h2>1. Restaurants & menu</h2><button onClick={loadRestaurants}>Refresh restaurants</button><select value={restaurantId} onChange={e=>{setRestaurantId(e.target.value);loadMenu(e.target.value)}}><option value={demo.restaurantId}>Demo restaurant</option>{restaurants.map(r=><option key={id(r)} value={id(r)}>{r.name || id(r)}</option>)}</select><button className="secondary" onClick={()=>loadMenu()}>Load menu</button><div className="items">{menu.map(m=><button className={selectedItem===id(m)?'item selected':'item'} key={id(m)} onClick={()=>setSelectedItem(id(m))}>{m.name || m.title || id(m)}<small>{m.price ? `€${m.price}` : 'menu item'}</small></button>)}</div><p className="muted">Selected item: <code>{selectedItem}</code></p></section><section className="card"><h2>2. Create Saga order</h2><label>Customer ID<input value={demo.customerId} readOnly/></label><label>Restaurant ID<input value={restaurantId} onChange={e=>setRestaurantId(e.target.value)}/></label><label>Menu item ID<input value={selectedItem} onChange={e=>setSelectedItem(e.target.value)}/></label><label className="check"><input type="checkbox" checked={flags.failPayment} onChange={e=>setFlags({...flags,failPayment:e.target.checked})}/> Simulate payment failure</label><label className="check"><input type="checkbox" checked={flags.failDeliveryAssignment} onChange={e=>setFlags({...flags,failDeliveryAssignment:e.target.checked})}/> Simulate delivery assignment failure</label><button onClick={createOrder}>Create order</button>{orderId && <button className="danger" onClick={cancel}>Cancel order</button>}</section><section className="card wide"><div className="row"><h2>3. Live order status</h2><button className="secondary" onClick={()=>refreshOrder()}>Refresh now</button></div>{order ? <div className="status"><strong>{order.status || 'CREATED'}</strong><code>{id(order)}</code><pre>{JSON.stringify(order,null,2)}</pre></div> : <p className="muted">Create an order to start four-second polling.</p>}</section><Info title="Payment result" data={payment}/><Info title="Delivery assignment" data={delivery}/><Info title="Notifications" data={notifications}/></div>{message && <div className="toast">{message}</div>}</main>;
}
function Info({title,data}) { return <section className="card"><h2>{title}</h2>{data ? <pre>{JSON.stringify(data,null,2)}</pre> : <p className="muted">No data yet.</p>}</section>; }
createRoot(document.getElementById('root')).render(<App/>);
