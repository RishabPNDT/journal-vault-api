import { useEffect, useMemo, useState } from 'react'
import { request } from './api'

const emptyEntry = { title: '', content: '' }

function Auth({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const submit = async (event) => {
    event.preventDefault(); setError(''); setLoading(true)
    try {
      const result = await request(`/api/auth/${mode}`, { method: 'POST', body: JSON.stringify(form) })
      localStorage.setItem('journal_token', result.token); localStorage.setItem('journal_username', result.username)
      onAuthenticated(result.username)
    } catch (e) { setError(e.message) } finally { setLoading(false) }
  }
  return <main className="auth-page"><section className="auth-copy"><span className="eyebrow">YOUR PRIVATE SPACE</span><h1>Make room for<br/><em>what matters.</em></h1><p>A quiet place to collect the thoughts, moments, and ideas you want to keep.</p></section><section className="auth-card"><div className="brand">quiet<span>journal</span></div><h2>{mode === 'login' ? 'Welcome back' : 'Begin your journal'}</h2><p className="muted">{mode === 'login' ? 'Sign in to continue writing.' : 'Create an account to save your first entry.'}</p><form onSubmit={submit}><label>Username<input required minLength="3" maxLength="40" value={form.username} onChange={e => setForm({...form, username:e.target.value})} /></label><label>Password<input required minLength="8" type="password" value={form.password} onChange={e => setForm({...form, password:e.target.value})} /></label>{error && <p className="error">{error}</p>}<button disabled={loading}>{loading ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button></form><p className="switch">{mode === 'login' ? 'New here?' : 'Already have an account?'} <button type="button" onClick={() => {setMode(mode === 'login' ? 'register' : 'login');setError('')}}>{mode === 'login' ? 'Create an account' : 'Sign in'}</button></p></section></main>
}

function App() {
  const [username, setUsername] = useState(() => localStorage.getItem('journal_username'))
  const [entries, setEntries] = useState([]); const [selected, setSelected] = useState(null)
  const [draft, setDraft] = useState(emptyEntry); const [query, setQuery] = useState('')
  const [status, setStatus] = useState(''); const [loading, setLoading] = useState(true)
  const load = async () => { try { setLoading(true); const data = await request('/api/entries'); setEntries(data); if (data.length && !selected) open(data[0]) } catch (e) { if (e.message) logout() } finally { setLoading(false) } }
  useEffect(() => { if (username) load() }, [username])
  const open = (entry) => { setSelected(entry); setDraft({title: entry.title, content: entry.content}); setStatus('') }
  const newEntry = () => { setSelected(null); setDraft(emptyEntry); setStatus('') }
  const save = async () => { if (!draft.title.trim() || !draft.content.trim()) return setStatus('Please give this entry a title and some words.')
    try { const saved = await request(selected ? `/api/entries/${selected.id}` : '/api/entries', {method: selected ? 'PUT' : 'POST', body: JSON.stringify(draft)}); setEntries(old => selected ? old.map(x => x.id === saved.id ? saved : x) : [saved, ...old]); open(saved); setStatus('Saved') } catch (e) { setStatus(e.message) } }
  const remove = async () => { if (!selected || !confirm('Delete this journal entry?')) return; try { await request(`/api/entries/${selected.id}`, {method:'DELETE'}); const next = entries.filter(x => x.id !== selected.id); setEntries(next); next.length ? open(next[0]) : newEntry() } catch(e) { setStatus(e.message) } }
  const logout = () => { localStorage.removeItem('journal_token'); localStorage.removeItem('journal_username'); setUsername(null); setEntries([]); setSelected(null) }
  const filtered = useMemo(() => entries.filter(x => `${x.title} ${x.content}`.toLowerCase().includes(query.toLowerCase())), [entries, query])
  if (!username) return <Auth onAuthenticated={setUsername}/>
  return <main className="app-shell"><aside><div className="brand">quiet<span>journal</span></div><button className="new-entry" onClick={newEntry}>＋ New entry</button><input className="search" placeholder="Search entries" value={query} onChange={e=>setQuery(e.target.value)}/><div className="entry-list">{loading ? <p className="muted">Loading…</p> : filtered.length ? filtered.map(entry => <button className={`entry-preview ${selected?.id===entry.id?'active':''}`} key={entry.id} onClick={()=>open(entry)}><strong>{entry.title}</strong><span>{new Date(entry.date).toLocaleDateString(undefined,{month:'short',day:'numeric',year:'numeric'})}</span><small>{entry.content}</small></button>) : <p className="muted">No entries found.</p>}</div><div className="profile"><span>{username.slice(0,1).toUpperCase()}</span><div><strong>{username}</strong><button onClick={logout}>Sign out</button></div></div></aside><section className="editor"><header><div><span className="eyebrow">{selected ? 'EDITING ENTRY' : 'NEW ENTRY'}</span><p>{selected?.date ? new Date(selected.date).toLocaleString(undefined,{weekday:'long',month:'long',day:'numeric'}) : 'A fresh page awaits.'}</p></div><div className="actions">{selected && <button className="text-button danger" onClick={remove}>Delete</button>}<button className="save" onClick={save}>Save entry</button></div></header><div className="writing"><input className="title" placeholder="Give this moment a title" value={draft.title} onChange={e=>setDraft({...draft,title:e.target.value})}/><textarea placeholder="Start writing…" value={draft.content} onChange={e=>setDraft({...draft,content:e.target.value})}/>{status && <p className={status === 'Saved' ? 'success' : 'error'}>{status}</p>}</div></section></main>
}
export default App
