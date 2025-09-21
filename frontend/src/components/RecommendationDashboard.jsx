import React, { useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";

// RecommendationDashboard.jsx
// Tailwind + Framer Motion based React component.
// Props:
//  - apiBaseUrl: string (optional) base URL for REST APIs
//  - wsUrl: string (optional) WebSocket URL for live alerts
//  - authToken: string (optional) bearer token
// This component is intentionally self-contained and uses a small mock if no API is provided.

const BASKETS = [
  "NIFTY",
  "BANKNIFTY",
  "NIFTY200",
  "NIFTY500",
  "MULTICAP",
  "MULTICAPPLUS",
  "FNO",
  "CUSTOM",
];

const DURATIONS = ["INTRADAY", "SHORTTERM", "POSITIONAL", "LONGTERM"];

function timeAgo(ts) {
  if (!ts) return "-";
  const diff = Math.floor((Date.now() - new Date(ts).getTime()) / 1000);
  if (diff < 60) return `${diff}s`;
  if (diff < 3600) return `${Math.floor(diff / 60)}m`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h`;
  return `${Math.floor(diff / 86400)}d`;
}

function useFetchJson(url, options = {}, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let alive = true;
    async function run() {
      try {
        setLoading(true);
        const res = await fetch(url, options);
        if (!alive) return;
        if (!res.ok) throw new Error(`${res.status}`);
        const json = await res.json();
        setData(json);
      } catch (e) {
        setError(e.message || String(e));
      } finally {
        setLoading(false);
      }
    }
    if (url) run();
    return () => {
      alive = false;
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, loading, error, setData };
}

export default function RecommendationDashboard({ apiBaseUrl = "", wsUrl = "", authToken = "" }) {
  const [selectedBaskets, setSelectedBaskets] = useState([]);
  const [selectedDurations, setSelectedDurations] = useState([]);
  const [stocksByType, setStocksByType] = useState({ BUY: [], SELL: [], SIDEWAYS: [] });
  const [alerts, setAlerts] = useState([]);
  const [search, setSearch] = useState("");
  const [isConnected, setIsConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);

  const apiHeaders = useMemo(() => {
    const headers = { "Content-Type": "application/json" };
    if (authToken) headers["Authorization"] = `Bearer ${authToken}`;
    return headers;
  }, [authToken]);

  // Build query param helper
  const qs = (obj) =>
    Object.entries(obj)
      .filter(([, v]) => v != null && !(Array.isArray(v) && v.length === 0))
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(Array.isArray(v) ? v.join(",") : v)}`)
      .join("&");

  // Fetch stocks
  useEffect(() => {
    let alive = true;
    async function load() {
      try {
        const params = {
          baskets: selectedBaskets,
          durations: selectedDurations,
          search,
        };
        if (!apiBaseUrl) {
          // Use inline mock data when no backend provided
          const mock = getMockStocks();
          if (!alive) return;
          setStocksByType(mock);
          return;
        }
        const url = `${apiBaseUrl.replace(/\/$/, "")}/stocks?${qs(params)}`;
        const res = await fetch(url, { headers: apiHeaders });
        if (!alive) return;
        if (!res.ok) {
          console.error("Failed to load stocks", res.status);
          return;
        }
        const json = await res.json();
        // Expecting { BUY: [...], SELL: [...], SIDEWAYS: [...] }
        setStocksByType(json);
      } catch (e) {
        console.error(e);
      }
    }
    load();
    return () => (alive = false);
  }, [apiBaseUrl, apiHeaders, selectedBaskets, selectedDurations, search]);

  // Fetch alerts
  useEffect(() => {
    let alive = true;
    async function loadAlerts() {
      try {
        if (!apiBaseUrl) {
          setAlerts(getMockAlerts());
          return;
        }
        const url = `${apiBaseUrl.replace(/\/$/, "")}/alerts?limit=200`;
        const res = await fetch(url, { headers: apiHeaders });
        if (!alive) return;
        if (!res.ok) return;
        const json = await res.json();
        // Expecting array ordered most recent first
        setAlerts(json.sort((a, b) => new Date(b.time) - new Date(a.time)));
      } catch (e) {
        console.error(e);
      }
    }
    loadAlerts();
    return () => (alive = false);
  }, [apiBaseUrl, apiHeaders]);

  // SSE for live alerts (using backend SSE endpoint)
  useEffect(() => {
    if (!apiBaseUrl) return;
    
    const eventSource = new EventSource(`${apiBaseUrl.replace(/\/$/, "")}/alerts/stream`);
    
    eventSource.onopen = () => {
      console.info("SSE connection opened");
      setIsConnected(true);
    };
    
    eventSource.addEventListener('alert', (event) => {
      try {
        const alertData = JSON.parse(event.data);
        setLastUpdate(new Date());
        
        // Convert backend AlertDto to frontend format
        const frontendAlert = {
          id: `${alertData.stockCode}_${alertData.alertDate}`,
          type: alertData.buySell || 'BUY',
          stock: alertData.stockCode,
          symbol: alertData.stockCode,
          price: parseFloat(alertData.price),
          scan: alertData.scanName,
          scanName: alertData.scanName,
          time: alertData.alertDate,
          timestamp: alertData.alertDate
        };
        
        setAlerts((prev) => [frontendAlert, ...prev].slice(0, 500));
      } catch (e) {
        console.warn("SSE parse error", e);
      }
    });
    
    eventSource.onerror = (error) => {
      console.warn("SSE error", error);
      setIsConnected(false);
    };
    
    return () => {
      eventSource.close();
      setIsConnected(false);
    };
  }, [apiBaseUrl]);

  // Mock real-time updates for testing (when no API URL provided)
  useEffect(() => {
    if (apiBaseUrl) return; // Only run mock updates when no real API
    
    const interval = setInterval(() => {
      setStocksByType((prev) => {
        const updated = { ...prev };
        
        // Randomly update some stocks with new prices and gains
        Object.keys(updated).forEach(category => {
          updated[category] = updated[category].map(stock => {
            if (Math.random() < 0.3) { // 30% chance to update each stock
              const priceChange = (Math.random() - 0.5) * 10; // Random price change
              const newPrice = Math.max(0, (stock.price || 0) + priceChange);
              const newGain = (Math.random() - 0.5) * 5; // Random gain change
              
              return {
                ...stock,
                price: Math.round(newPrice * 100) / 100,
                gain: Math.round(newGain * 100) / 100
              };
            }
            return stock;
          });
        });
        
        return updated;
      });
      setLastUpdate(new Date());
    }, 3000); // Update every 3 seconds

    // Also generate mock alerts occasionally
    const alertInterval = setInterval(() => {
      if (Math.random() < 0.2) { // 20% chance every 3 seconds
        const mockStocks = ['RELIANCE', 'TCS', 'HDFC', 'INFY', 'MARUTI', 'ASIANPAINT'];
        const mockScans = ['Momentum Scanner', 'Volume Breakout', 'RSI Oversold', 'Support Bounce'];
        const randomStock = mockStocks[Math.floor(Math.random() * mockStocks.length)];
        const randomScan = mockScans[Math.floor(Math.random() * mockScans.length)];
        const randomPrice = (Math.random() * 1000 + 100).toFixed(2);
        const randomType = Math.random() > 0.5 ? 'BUY' : 'SELL';
        
        const mockAlert = {
          id: `${randomStock}_${Date.now()}`,
          type: randomType,
          stock: randomStock,
          symbol: randomStock,
          price: parseFloat(randomPrice),
          scan: randomScan,
          scanName: randomScan,
          time: new Date().toISOString(),
          timestamp: new Date().toISOString()
        };
        
        setAlerts((prev) => [mockAlert, ...prev].slice(0, 50));
        setLastUpdate(new Date());
      }
    }, 3000);

    return () => {
      clearInterval(interval);
      clearInterval(alertInterval);
    };
  }, [apiBaseUrl]);

  function toggleBasket(value) {
    setSelectedBaskets((s) => (s.includes(value) ? s.filter((x) => x !== value) : [...s, value]));
  }

  function toggleDuration(d) {
    setSelectedDurations((s) => (s.includes(d) ? s.filter((x) => x !== d) : [...s, d]));
  }

  return (
    <div className="w-full min-h-[480px] p-4 bg-slate-50 rounded-lg shadow-md flex gap-4">
      <div className="flex-1 flex flex-col gap-4">
        {/* Real-time status indicator */}
        <div className="flex justify-between items-center text-xs text-slate-500">
          <div className="flex items-center gap-2">
            <div className={`w-2 h-2 rounded-full ${apiBaseUrl ? (isConnected ? 'bg-green-500' : 'bg-red-500') : 'bg-blue-500'}`}></div>
            <span>
              {apiBaseUrl ? (isConnected ? 'SSE Connected' : 'SSE Disconnected') : 'Mock Mode'}
            </span>
          </div>
          {lastUpdate && (
            <span>Last update: {lastUpdate.toLocaleTimeString()}</span>
          )}
        </div>

        {/* Top controls */}
        <div className="flex flex-wrap gap-3 items-center">
          {BASKETS.map((b) => (
            <button
              key={b}
              onClick={() => toggleBasket(b)}
              className={`px-3 py-1 rounded-full border text-sm ${
                selectedBaskets.includes(b) 
                  ? "bg-indigo-600 text-white border-indigo-600" 
                  : "bg-white text-slate-700"
              }`}>
              {b}
            </button>
          ))}

          <div className="ml-auto flex items-center gap-2">
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search stock"
              className="px-3 py-1 rounded-md border text-sm"
            />
          </div>
        </div>

        <div className="flex gap-4 items-center">
          {DURATIONS.map((d) => (
            <button 
              key={d} 
              onClick={() => toggleDuration(d)} 
              className={`px-3 py-1 rounded-md border text-sm ${
                selectedDurations.includes(d) 
                  ? "bg-amber-500 text-white" 
                  : "bg-white"
              }`}>
              {d}
            </button>
          ))}
        </div>

        {/* Main Panels */}
        <div className="flex gap-4 h-[62vh]">
          {/* Left - buy/sell/sideways columns */}
          <div className="flex-1 grid grid-cols-3 gap-4">
            {[
              ["BUY", stocksByType.BUY || []],
              ["SELL", stocksByType.SELL || []],
              ["SIDEWAYS", stocksByType.SIDEWAYS || []],
            ].map(([title, items]) => (
              <motion.div 
                key={title} 
                initial={{ opacity: 0, y: 6 }} 
                animate={{ opacity: 1, y: 0 }} 
                className="bg-white rounded-lg p-3 shadow-sm flex flex-col"
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="text-sm font-semibold">{title}</div>
                  <div className="text-xs text-slate-400">{items.length}</div>
                </div>
                <div className="flex-1 overflow-y-auto divide-y">
                  {items.length === 0 && <div className="text-xs text-slate-400 p-3">No items</div>}
                  {items.map((s) => (
                    <StockCard key={s.symbol || s.id} stock={s} onClick={() => console.info("open", s)} />
                  ))}
                </div>
              </motion.div>
            ))}
          </div>

          {/* Right - Alerts panel */}
          <div className="w-[380px] flex flex-col bg-white rounded-lg p-3 shadow-sm">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-slate-600 rounded-full flex items-center justify-center">
                  <div className="w-2 h-2 bg-white rounded-full"></div>
                </div>
                <div className="text-sm font-semibold">Alerts</div>
              </div>
              <div className="text-xs text-slate-400">Latest on top</div>
            </div>
            <div className="flex-1 overflow-y-auto h-full divide-y">
              {alerts.length === 0 && <div className="text-xs text-slate-400 p-3">No alerts yet</div>}
              {alerts.map((a) => (
                <AlertRow key={a.id || `${a.stock}_${a.time}`} alert={a} />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function StockCard({ stock = {}, onClick }) {
  const gain = stock.gain ?? stock.changePercent ?? 0;
  const [isUpdating, setIsUpdating] = React.useState(false);
  
  // Show update animation when price changes
  React.useEffect(() => {
    if (stock.price !== undefined) {
      setIsUpdating(true);
      const timer = setTimeout(() => setIsUpdating(false), 1000);
      return () => clearTimeout(timer);
    }
  }, [stock.price, stock.gain]);
  
  return (
    <div onClick={onClick} className={`cursor-pointer p-2 hover:bg-slate-50 flex items-center justify-between gap-3 transition-colors ${
      isUpdating ? 'bg-blue-50' : ''
    }`}>
      <div>
        <div className="font-medium">{stock.symbol || stock.name}</div>
        <div className="text-xs text-slate-400">{stock.tag || stock.sector || ""}</div>
      </div>
      <div className="text-right">
        <div className={`text-sm font-semibold transition-colors ${
          isUpdating ? 'text-blue-600' : ''
        }`}>
          {stock.price != null ? stock.price : "-"}
        </div>
        <div className={`text-xs transition-colors ${
          gain >= 0 ? "text-green-600" : "text-red-600"
        } ${isUpdating ? 'font-semibold' : ''}`}>
          {gain >= 0 ? `+${gain}` : gain}
        </div>
      </div>
    </div>
  );
}

function AlertRow({ alert = {} }) {
  // Expecting alert: { type: 'BUY'|'SELL', stock: 'TATasteel', price: 123.4, scan: 'Snehal_Buy', time: '2025-09-10T12:34:00Z' }
  const title = `${alert.type || "BUY"} :: ${alert.stock || alert.symbol} @ ${alert.price ?? "-"}`;
  const sub = `${alert.scan || alert.scanName || "scan"} - ${new Date(alert.time || alert.timestamp || Date.now()).toLocaleString()}`;
  return (
    <div className="p-3 hover:bg-slate-50">
      <div className="text-sm font-semibold">{title}</div>
      <div className="text-xs text-slate-400">{sub}</div>
    </div>
  );
}

// -------------------- Mock helpers --------------------
function getMockStocks() {
  return {
    BUY: [
      { symbol: "RELIANCE", price: 2375.5, gain: 1.2, tag: "Energy" },
      { symbol: "TCS", price: 3210.0, gain: 0.6 },
      { symbol: "SDBL", price: 175.2, gain: 3.2 },
    ],
    SELL: [
      { symbol: "INFY", price: 1350.5, gain: -0.6 },
      { symbol: "MARUTI", price: 950.3, gain: -1.2 },
    ],
    SIDEWAYS: [
      { symbol: "SBJ", price: 285, gain: 0.01 },
    ],
  };
}

function getMockAlerts() {
  return [
    { id: 1, type: "BUY", stock: "GMBREW", price: 699, scan: "Snehal-BUY-Tradingview", time: new Date().toISOString() },
    { id: 2, type: "BUY", stock: "POLICYBZR", price: 1839.6, scan: "Snehal_BUY_Daily_PriceGreaterThanPreviousDaysHigh", time: new Date(Date.now() - 60000).toISOString() },
    { id: 3, type: "BUY", stock: "GAIL", price: 174.45, scan: "Snehal_BUY_DAILY_Reversal", time: new Date(Date.now() - 120000).toISOString() },
  ];
}
