import React, {useEffect, useState} from 'react'

function useSseAlerts() {
  const [alerts, setAlerts] = useState([])
  useEffect(() => {
    const es = new EventSource('/api/alerts/stream')
    es.addEventListener('alert', e => {
      try {
        const data = JSON.parse(e.data)
        setAlerts(prev => [data, ...prev])
      } catch(err) { console.error(err) }
    })
    es.onopen = () => console.log('SSE connected')
    es.onerror = () => { console.warn('SSE error'); es.close() }
    return () => es.close()
  }, [])
  return [alerts, setAlerts]
}

function loadFavorites() {
  const raw = localStorage.getItem('trademon_favs')
  if (!raw) return { stocks: [], baskets: [] }
  try { return JSON.parse(raw) } catch(e){ return {stocks:[], baskets:[]} }
}
function saveFavorites(f) { localStorage.setItem('trademon_favs', JSON.stringify(f)) }

export default function AlertsView() {
  const [alerts] = useSseAlerts()
  const [favs, setFavs] = useState(loadFavorites())
  const [filterSet, setFilterSet] = useState(new Set(favs.stocks || []))
  const [visible, setVisible] = useState([])

  useEffect(()=> setFilterSet(new Set(favs.stocks || [])), [favs])

  useEffect(()=> {
    if (!filterSet || filterSet.size===0) {
      setVisible(alerts.slice(0,100))
      return
    }
    const filtered = alerts.filter(a => filterSet.has(a.stockCode))
    setVisible(filtered.slice(0,200))
  }, [alerts, filterSet])

  const toggleStock = (s) => {
    const nf = {...favs}
    nf.stocks = nf.stocks || []
    if (nf.stocks.includes(s)) nf.stocks = nf.stocks.filter(x=>x!==s)
    else nf.stocks.push(s)
    setFavs(nf); saveFavorites(nf)
  }

  const viewStockHistory = async (stock) => {
    const res = await fetch('/api/alerts/stock/' + encodeURIComponent(stock) + '?days=7')
    const data = await res.json()
    alert('Last week alerts for ' + stock + '\n' + data.map(d=> new Date(d.alertDate).toLocaleString() + ' :: ' + d.scanName).join('\n'))
  }

  return (
    <div className="container">
      <div className="header">
        <h1>Trade Alerts Dashboard</h1>
        <div>
          <button onClick={()=>{ localStorage.removeItem('trademon_favs'); setFavs({stocks:[],baskets:[]}); alert('Favorites cleared') }}>Clear Favorites</button>
        </div>
      </div>

      <div style={{display:'flex', gap:20, marginTop:12}}>
        <div style={{flex:3}}>
          <h2>Latest Alerts</h2>
          <table className="table">
            <thead>
              <tr><th>Stock</th><th>Price</th><th>Time</th><th>Scan</th><th>Side</th><th>Fav</th></tr>
            </thead>
            <tbody>
              {visible.map((a,idx)=>(
                <tr key={idx}>
                  <td><a href="#" onClick={(e)=>{e.preventDefault(); viewStockHistory(a.stockCode)}}>{a.stockCode}</a></td>
                  <td>{a.price}</td>
                  <td>{new Date(a.alertDate).toLocaleString()}</td>
                  <td>{a.scanName}</td>
                  <td><span className={'badge ' + (a.buySell==='BUY' ? 'buy':'sell')}>{a.buySell}</span></td>
                  <td><input type="checkbox" className="fav" checked={(favs.stocks||[]).includes(a.stockCode)} onChange={()=>toggleStock(a.stockCode)} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div style={{flex:1}}>
          <h3>Favorites</h3>
          <div>
            <strong>Stocks:</strong>
            <ul>
              {(favs.stocks||[]).map(s=> <li key={s}>{s}</li>)}
            </ul>
            <input placeholder="Add stock e.g. RELIANCE" id="addstock" />
            <button onClick={()=>{ const v = document.getElementById('addstock').value.trim(); if(v){ const nf = favs; nf.stocks = nf.stocks||[]; nf.stocks.push(v); setFavs(nf); saveFavorites(nf); document.getElementById('addstock').value=''; } }}>Add</button>
          </div>
        </div>
      </div>
    </div>
  )
}
