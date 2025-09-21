import React, { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { cn } from '../lib/utils';
import DashboardHeader from './DashboardHeader';
import StockDetailModal from './StockDetailModal';
import SimpleCustomBasket from './SimpleCustomBasket';
import { isStockInBasket, getBasketCount } from '../config/stockBaskets';

// Basket options
const BASKETS = [
  'ALL',
  'NIFTY50',
  'BANKNIFTY', 
  'NIFTY200',
  'NIFTY500',
  'MULTICAP',
  'MULTICAPPLUS',
  'FNO',
  'CUSTOM'
];

// Timeframe options
const TIMEFRAMES = [
  'INTRADAY',
  'SHORTTERM', 
  'POSITIONAL',
  'LONGTERM'
];

const TIME_FILTER_OPTIONS = [
  'TODAY',
  'YESTERDAY', 
  'THIS_WEEK',
  'ALL'
];

// No mock data - only real alerts from backend

export default function RecommendationDashboard({ apiBaseUrl = '' }) {
  const [selectedBaskets, setSelectedBaskets] = useState(['ALL']);
  const [selectedTimeframes, setSelectedTimeframes] = useState(['INTRADAY']);
  const [selectedPanels, setSelectedPanels] = useState(['BUY', 'SELL']);
  const [selectedTimeFilter, setSelectedTimeFilter] = useState('TODAY');
  const [basketDropdownOpen, setBasketDropdownOpen] = useState(false);
  const [timeframeDropdownOpen, setTimeframeDropdownOpen] = useState(false);
  const [panelDropdownOpen, setPanelDropdownOpen] = useState(false);
  const [timeFilterDropdownOpen, setTimeFilterDropdownOpen] = useState(false);
  const [stocks, setStocks] = useState({ BUY: [], SELL: [], SIDEWAYS: [] });
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);
  const [selectedStock, setSelectedStock] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Custom basket states
  const [customBasketStocks, setCustomBasketStocks] = useState(['RELIANCE', 'TCS', 'HDFC']); // Default stocks for testing
  const [showStockSearch, setShowStockSearch] = useState(false);
  const [stockSearchQuery, setStockSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isCustomBasketMinimized, setIsCustomBasketMinimized] = useState(true);
  const [showSettings, setShowSettings] = useState(false);
  const [defaultBaskets, setDefaultBaskets] = useState(['ALL']);
  const [searchQuery, setSearchQuery] = useState('');
  const [backendStatus, setBackendStatus] = useState('Unknown');

  // Check backend status
  const checkBackendStatus = useCallback(async () => {
    if (!apiBaseUrl) {
      setBackendStatus('Offline');
      return;
    }
    
    try {
      const response = await fetch(`${apiBaseUrl}/api/alerts?limit=1`);
      if (response.ok) {
        setBackendStatus('Active');
      } else {
        setBackendStatus('Inactive');
      }
    } catch (error) {
      setBackendStatus('Inactive');
    }
  }, [apiBaseUrl]);

  // Toggle functions for multi-select
  const toggleBasket = (basket) => {
    if (basket === 'CUSTOM') {
      setSelectedBaskets(prev => 
        prev.includes('CUSTOM') 
          ? prev.filter(b => b !== 'CUSTOM')
          : [...prev, 'CUSTOM']
      );
      return;
    }
    
    setSelectedBaskets(prev => 
      prev.includes(basket) 
        ? prev.filter(b => b !== basket)
        : [...prev, basket]
    );
  };

  // Custom basket handlers - removed manual input handler

  // Stock search functionality
  const searchStocks = (query) => {
    if (query.length < 2) {
      setSearchResults([]);
      return;
    }

    // Get unique stocks from alerts
    const availableStocks = [...new Set(alerts.map(alert => alert.symbol))];
    
    // Add some common NSE stocks
    const commonStocks = [
      'RELIANCE', 'TCS', 'HDFCBANK', 'INFY', 'HINDUNILVR', 'ITC', 'SBIN', 'BHARTIARTL',
      'KOTAKBANK', 'LT', 'ASIANPAINT', 'AXISBANK', 'MARUTI', 'SUNPHARMA', 'TITAN',
      'ULTRACEMCO', 'WIPRO', 'NESTLEIND', 'POWERGRID', 'NTPC', 'ONGC', 'COALINDIA',
      'TECHM', 'TATAMOTORS', 'BAJFINANCE', 'HCLTECH', 'DRREDDY', 'CIPLA', 'EICHERMOT',
      'BAJAJFINSV', 'GRASIM', 'JSWSTEEL', 'TATASTEEL', 'APOLLOHOSP', 'DIVISLAB',
      'HEROMOTOCO', 'HINDALCO', 'INDUSINDBK', 'M&M', 'SBILIFE', 'TATACONSUM', 'UPL',
      'ADANIPORTS', 'BAJAJ-AUTO', 'BRITANNIA', 'HDFCLIFE', 'ICICIBANK', 'SHREECEM'
    ];

    const allStocks = [...new Set([...availableStocks, ...commonStocks])];
    
    const filtered = allStocks
      .filter(stock => stock.toLowerCase().includes(query.toLowerCase()))
      .slice(0, 10); // Show top 10 results
    
    setSearchResults(filtered);
  };

  const handleStockSearch = (e) => {
    const query = e.target.value;
    setStockSearchQuery(query);
    searchStocks(query);
  };

  const addStockToBasket = (stock) => {
    if (!customBasketStocks.includes(stock)) {
      if (customBasketStocks.length >= 20) {
        alert('Maximum 20 stocks allowed in custom basket. Please remove some stocks first.');
        return;
      }
      setCustomBasketStocks([...customBasketStocks, stock]);
    }
    setStockSearchQuery('');
    setSearchResults([]);
    setShowStockSearch(false); // Auto-close search after adding stock
  };

  const handleSearchButtonClick = (e) => {
    e.stopPropagation();
    if (!showStockSearch) {
      // Opening search - auto-expand the panel
      setIsCustomBasketMinimized(false);
    }
    setShowStockSearch(!showStockSearch);
  };

  const handleSearchInputBlur = () => {
    // Auto-close search when input loses focus (after a short delay)
    setTimeout(() => {
      setShowStockSearch(false);
    }, 200);
  };

  const removeStockFromBasket = (stock) => {
    setCustomBasketStocks(customBasketStocks.filter(s => s !== stock));
  };

  // Settings functions
  const toggleDefaultBasket = (basket) => {
    setDefaultBaskets(prev => 
      prev.includes(basket) 
        ? prev.filter(b => b !== basket)
        : [...prev, basket]
    );
  };

  const saveDefaultBaskets = () => {
    setSelectedBaskets([...defaultBaskets]);
    localStorage.setItem('defaultBaskets', JSON.stringify(defaultBaskets));
    setShowSettings(false);
  };

  const resetToDefaults = () => {
    setDefaultBaskets(['ALL']);
    setSelectedBaskets(['ALL']);
    localStorage.removeItem('defaultBaskets');
  };

  // Helper function to extract timeframe from scan name
  const getTimeframeFromScanName = (scanName) => {
    if (!scanName) return 'INTRADAY';
    
    const upperScanName = scanName.toUpperCase();
    if (upperScanName.includes('INTRADAY')) {
      return 'INTRADAY';
    } else if (upperScanName.includes('POSITIONAL')) {
      return 'POSITIONAL';
    } else if (upperScanName.includes('SHORT') || upperScanName.includes('SHORTTERM')) {
      return 'SHORTTERM';
    } else if (upperScanName.includes('LONG') || upperScanName.includes('LONGTERM')) {
      return 'LONGTERM';
    }
    
    return 'INTRADAY'; // Default
  };

  const toggleTimeframe = (timeframe) => {
    setSelectedTimeframes(prev => 
      prev.includes(timeframe)
        ? prev.filter(t => t !== timeframe)
        : [...prev, timeframe]
    );
  };

  const toggleBasketDropdown = (e) => {
    e.stopPropagation();
    setBasketDropdownOpen(!basketDropdownOpen);
    setTimeframeDropdownOpen(false);
    setPanelDropdownOpen(false);
    setTimeFilterDropdownOpen(false);
  };

  const toggleTimeframeDropdown = (e) => {
    e.stopPropagation();
    setBasketDropdownOpen(false);
    setTimeframeDropdownOpen(!timeframeDropdownOpen);
    setPanelDropdownOpen(false);
    setTimeFilterDropdownOpen(false);
  };

  const togglePanelDropdown = (e) => {
    e.stopPropagation();
    setBasketDropdownOpen(false);
    setTimeframeDropdownOpen(false);
    setPanelDropdownOpen(!panelDropdownOpen);
    setTimeFilterDropdownOpen(false);
  };

  const toggleTimeFilterDropdown = (e) => {
    e.stopPropagation();
    setBasketDropdownOpen(false);
    setTimeframeDropdownOpen(false);
    setPanelDropdownOpen(false);
    setTimeFilterDropdownOpen(!timeFilterDropdownOpen);
  };

  const togglePanel = (panel) => {
    setSelectedPanels(prev => {
      const newSelection = prev.includes(panel) 
        ? prev.filter(p => p !== panel)
        : [...prev, panel];
      
      // If no panels are selected, show all panels
      if (newSelection.length === 0) {
        return ['BUY', 'SELL', 'SIDEWAYS'];
      }
      
      return newSelection;
    });
  };

  const setTimeFilter = (filter) => {
    setSelectedTimeFilter(filter);
    setTimeFilterDropdownOpen(false);
  };

  // Modal handlers
  const handleStockClick = (stock) => {
    setSelectedStock(stock);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedStock(null);
  };

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      // Close all dropdowns when clicking anywhere
      setBasketDropdownOpen(false);
      setTimeframeDropdownOpen(false);
      setPanelDropdownOpen(false);
      setTimeFilterDropdownOpen(false);
    };

    // Add event listener to document
    document.addEventListener('click', handleClickOutside);
    
    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, []);

  // No separate stock fetching - stocks come from alerts only

  // Fetch alerts from backend
  const fetchAlerts = useCallback(async () => {
    if (!apiBaseUrl) {
      setAlerts([]);
      return;
    }

    try {
      const response = await fetch(`${apiBaseUrl}/api/alerts?limit=50`);
      if (response.ok) {
        const data = await response.json();
        console.log('🔗 Raw backend alerts data:', data);
        
        // Convert backend AlertDto to frontend format
        const convertedAlerts = data.map(alert => {
          const converted = {
            id: `${alert.stockCode}_${alert.alertDate}`,
            action: alert.buySell || 'BUY',
            symbol: alert.stockCode,
            price: parseFloat(alert.price),
            source: alert.scanName,
            timestamp: alert.alertDate
          };
          console.log(`🔄 Converting alert: ${alert.stockCode} - buySell: ${alert.buySell} -> action: ${converted.action}`);
          return converted;
        });
        
        console.log('✅ Converted alerts:', convertedAlerts);
        
        // Apply filters
        const filteredAlerts = convertedAlerts.filter(alert => {
          // Filter by selected panels (BUY/SELL/SIDEWAYS)
          if (selectedPanels.length > 0 && !selectedPanels.includes(alert.action)) {
            return false;
          }
          
          // Filter by selected timeframes (based on scanName)
          if (selectedTimeframes.length > 0) {
            const alertTimeframe = getTimeframeFromScanName(alert.source);
            if (!selectedTimeframes.includes(alertTimeframe)) {
              return false;
            }
          }
          
          return true;
        });
        
        console.log('🔍 Filtered alerts:', filteredAlerts);
        setAlerts(filteredAlerts);
      }
    } catch (error) {
      console.error('Failed to fetch alerts:', error);
      setAlerts([]);
    }
  }, [apiBaseUrl, selectedPanels, selectedTimeframes]);

  // SSE connection for real-time alerts
  useEffect(() => {
    if (!apiBaseUrl) {
      console.log('❌ No API base URL provided, skipping SSE connection');
      return;
    }

    const sseUrl = `${apiBaseUrl}/api/alerts/stream`;
    console.log('🔗 Attempting to connect to SSE:', sseUrl);
    console.log('🔗 Full URL details:', {
      protocol: window.location.protocol,
      host: window.location.host,
      apiBaseUrl: apiBaseUrl,
      sseUrl: sseUrl
    });
    
    const eventSource = new EventSource(sseUrl);
    
    eventSource.onopen = () => {
      console.log('✅ SSE connection opened successfully');
      console.log('✅ SSE readyState:', eventSource.readyState);
      console.log('✅ SSE URL:', eventSource.url);
      setIsConnected(true);
    };

    eventSource.addEventListener('alert', (event) => {
      console.log('📨 Received SSE alert event:', event);
      console.log('📨 Event data:', event.data);
      console.log('📨 Event type:', event.type);
      console.log('📨 Event lastEventId:', event.lastEventId);
      
      try {
        const alertData = JSON.parse(event.data);
        console.log('📨 Parsed alert data:', alertData);
        
        // Convert backend AlertDto to frontend format
        const newAlert = {
          id: `${alertData.stockCode}_${alertData.alertDate}`,
          action: alertData.buySell || 'BUY',
          symbol: alertData.stockCode,
          price: parseFloat(alertData.price),
          source: alertData.scanName,
          timestamp: alertData.alertDate
        };
        
        console.log('📨 Converted to frontend alert:', newAlert);
        setAlerts(prev => {
          const updated = [newAlert, ...prev].slice(0, 100);
          console.log('📨 Updated alerts array length:', updated.length);
          return updated;
        });
        setLastUpdate(new Date());
      } catch (error) {
        console.error('❌ SSE parse error:', error);
        console.error('❌ Raw event data:', event.data);
      }
    });

    // Listen for any message (for debugging)
    eventSource.onmessage = (event) => {
      console.log('📨 Received SSE message (any type):', event);
      console.log('📨 Message data:', event.data);
      console.log('📨 Message type:', event.type);
    };

    eventSource.onerror = (error) => {
      console.error('❌ SSE connection error:', error);
      console.error('❌ SSE readyState:', eventSource.readyState);
      console.error('❌ SSE URL:', eventSource.url);
      console.error('❌ Error details:', {
        type: error.type,
        target: error.target,
        isTrusted: error.isTrusted
      });
      setIsConnected(false);
    };

    // Test the connection after 2 seconds
    setTimeout(() => {
      console.log('🔍 SSE connection test after 2s:');
      console.log('🔍 ReadyState:', eventSource.readyState);
      console.log('🔍 URL:', eventSource.url);
      console.log('🔍 Is connected:', isConnected);
    }, 2000);

    return () => {
      console.log('🔌 Closing SSE connection');
      eventSource.close();
      setIsConnected(false);
    };
  }, [apiBaseUrl]);

  // No mock updates - only real alerts from backend

  // No separate stock loading - stocks come from alerts

  // Load initial alerts
  useEffect(() => {
    fetchAlerts();
  }, [apiBaseUrl]);

  // Check backend status on mount and periodically
  useEffect(() => {
    checkBackendStatus();
    const statusInterval = setInterval(checkBackendStatus, 30000); // Check every 30 seconds
    return () => clearInterval(statusInterval);
  }, [checkBackendStatus]);

  // Refetch alerts when filters change
  useEffect(() => {
    if (apiBaseUrl) {
      fetchAlerts();
    }
  }, [selectedPanels, selectedTimeframes]);

  // Load saved default baskets on mount
  useEffect(() => {
    const savedDefaults = localStorage.getItem('defaultBaskets');
    if (savedDefaults) {
      try {
        const parsed = JSON.parse(savedDefaults);
        if (Array.isArray(parsed) && parsed.length > 0) {
          setDefaultBaskets(parsed);
          setSelectedBaskets(parsed);
        }
      } catch (e) {
        console.error('Error loading saved default baskets:', e);
      }
    }
  }, []);

  // Filter alerts by time period
  const filterAlertsByTime = useCallback((alerts) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const weekStart = new Date(today);
    weekStart.setDate(weekStart.getDate() - today.getDay()); // Start of this week (Sunday)

    return alerts.filter(alert => {
      const alertDate = new Date(alert.timestamp);
      
      switch (selectedTimeFilter) {
        case 'TODAY':
          return alertDate >= today;
        case 'YESTERDAY':
          return alertDate >= yesterday && alertDate < today;
        case 'THIS_WEEK':
          return alertDate >= weekStart;
        case 'ALL':
        default:
          return true;
      }
    });
  }, [selectedTimeFilter]);

  // Extract stocks from alerts and categorize them
  const stocksFromAlerts = useCallback(() => {
    try {
    const categorized = { BUY: [], SELL: [], SIDEWAYS: [] };

    // Filter alerts by time first
      let filteredAlerts = filterAlertsByTime(alerts);
    
    // Apply basket filtering
    if (selectedBaskets.length > 0 && !selectedBaskets.includes('ALL')) {
      const beforeCount = filteredAlerts.length;
      
      console.log('🔍 DEBUG: Basket filtering:');
      console.log('  - Selected baskets:', selectedBaskets);
      console.log('  - Alerts before filtering:', filteredAlerts.map(a => `${a.symbol}(${a.action})`));
      
      filteredAlerts = filteredAlerts.filter(alert => {
        // Check if stock belongs to any of the selected baskets
        const belongsToAnyBasket = selectedBaskets.some(basket => {
          if (basket === 'CUSTOM') {
            return customBasketStocks && customBasketStocks.includes(alert.symbol);
          }
          return isStockInBasket(alert.symbol, basket);
        });
        
        console.log(`  - ${alert.symbol}: ${belongsToAnyBasket ? 'INCLUDED' : 'FILTERED OUT'}`);
        return belongsToAnyBasket;
      });
      
      console.log(`🔍 Basket filter: ${beforeCount} alerts → ${filteredAlerts.length} alerts`);
      console.log('🔍 Filtered alerts by basket:', filteredAlerts.map(a => `${a.symbol}(${a.action})`));
    }
    
    console.log('🔍 Filtered alerts by time:', filteredAlerts);
    
    // Show all alerts grouped by stock for debugging
    const alertsByStock = {};
    filteredAlerts.forEach(alert => {
      if (!alertsByStock[alert.symbol]) {
        alertsByStock[alert.symbol] = [];
      }
      alertsByStock[alert.symbol].push(alert);
    });
    
    console.log('📋 Alerts grouped by stock:');
    Object.keys(alertsByStock).forEach(stock => {
      const actions = alertsByStock[stock].map(a => a.action);
      console.log(`  ${stock}: [${actions.join(', ')}]`);
    });

    // For each stock, find the LAST alert (by array order) and use that action
    Object.keys(alertsByStock).forEach(stockSymbol => {
      const stockAlerts = alertsByStock[stockSymbol];
      const lastAlert = stockAlerts[stockAlerts.length - 1]; // Get the last alert
      
      console.log(`📋 Stock ${stockSymbol}: Last alert action = ${lastAlert.action}`);

      // Create stock object from last alert
      const stock = {
        symbol: stockSymbol,
        price: lastAlert.price,
        action: lastAlert.action,
        source: lastAlert.source,
        timestamp: lastAlert.timestamp
      };

      // Categorize based on last action
      if (lastAlert.action === 'BUY') {
        categorized.BUY.push(stock);
        console.log(`✅ Added ${stockSymbol} to BUY bucket`);
      } else if (lastAlert.action === 'SELL') {
        categorized.SELL.push(stock);
        console.log(`✅ Added ${stockSymbol} to SELL bucket`);
      } else {
        categorized.SIDEWAYS.push(stock);
        console.log(`✅ Added ${stockSymbol} to SIDEWAYS bucket`);
      }
    });

    console.log('📈 Final categorization:', {
      BUY: categorized.BUY.length,
      SELL: categorized.SELL.length,
      SIDEWAYS: categorized.SIDEWAYS.length
    });

    console.log('📋 BUY stocks:', categorized.BUY.map(s => s.symbol));
    console.log('📋 SELL stocks:', categorized.SELL.map(s => s.symbol));
    console.log('📋 SIDEWAYS stocks:', categorized.SIDEWAYS.map(s => s.symbol));

    return categorized;
    } catch (error) {
      console.error('Error in stocksFromAlerts:', error);
      return { BUY: [], SELL: [], SIDEWAYS: [] };
    }
  }, [alerts, filterAlertsByTime, selectedBaskets, customBasketStocks]);

  const stocksData = stocksFromAlerts();

  // Filter stocks based on search query
  const filterStocksBySearch = (stocks) => {
    if (!searchQuery.trim()) {
      return stocks;
    }
    
    const query = searchQuery.toLowerCase().trim();
    return stocks.filter(stock => 
      stock.symbol.toLowerCase().includes(query) ||
      stock.symbol.toLowerCase().startsWith(query)
    );
  };

  // Apply search filter to all stock categories
  const filteredStocksData = {
    BUY: filterStocksBySearch(stocksData.BUY),
    SELL: filterStocksBySearch(stocksData.SELL),
    SIDEWAYS: filterStocksBySearch(stocksData.SIDEWAYS)
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <DashboardHeader apiBaseUrl={apiBaseUrl} onSettingsClick={() => setShowSettings(true)} />
      <div className="p-2">
      <div className="max-w-7xl mx-auto space-y-3">
        {/* Status Indicator */}
        <div className="flex justify-between items-center text-sm text-gray-500">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <div className={cn(
                "w-2 h-2 rounded-full",
                apiBaseUrl ? (isConnected ? "bg-green-500" : "bg-red-500") : "bg-blue-500"
              )}></div>
              <span>
                {apiBaseUrl ? (isConnected ? 'SSE Connected' : 'SSE Disconnected') : 'No Backend - Connect to see data'}
              </span>
            </div>
            {apiBaseUrl && (
              <button
                onClick={checkBackendStatus}
                className={cn(
                  "px-2 py-1 text-xs rounded transition-colors",
                  backendStatus === 'Active' 
                    ? "bg-green-100 text-green-700 hover:bg-green-200" 
                    : backendStatus === 'Inactive'
                    ? "bg-red-100 text-red-700 hover:bg-red-200"
                    : "bg-yellow-100 text-yellow-700 hover:bg-yellow-200"
                )}
              >
                Backend: {backendStatus}
              </button>
            )}
          </div>
          {lastUpdate && (
            <span>Last update: {lastUpdate.toLocaleTimeString()}</span>
          )}
        </div>


        {/* First Row - All Main Filters */}
        <div className="flex gap-2 items-center">
          {/* Stock Baskets Dropdown */}
          <div className="relative dropdown-container">
            <button
              onClick={toggleBasketDropdown}
              data-dropdown-button
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <span className="text-sm font-medium">
                Stock Baskets ({selectedBaskets.length})
              </span>
              <svg className={`w-4 h-4 transition-transform ${basketDropdownOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {basketDropdownOpen && (
              <div className="absolute top-full left-0 mt-1 w-48 bg-white border border-gray-300 rounded-lg shadow-lg z-10" data-dropdown-content onClick={(e) => e.stopPropagation()}>
                {BASKETS.map((basket) => (
                  <button
                    key={basket}
                    onClick={() => toggleBasket(basket)}
                    className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors"
                  >
                    <div className={cn(
                      "w-4 h-4 border-2 rounded flex items-center justify-center",
                      selectedBaskets.includes(basket) 
                        ? "bg-blue-600 border-blue-600" 
                        : "border-gray-300"
                    )}>
                      {selectedBaskets.includes(basket) && (
                        <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </div>
                    <span className="text-gray-700">{basket === 'ALL' ? 'All' : basket === 'NIFTY50' ? 'Nifty50' : basket === 'BANKNIFTY' ? 'BankNifty' : basket === 'NIFTY200' ? 'Nifty200' : basket === 'NIFTY500' ? 'Nifty500' : basket === 'MULTICAP' ? 'MultiCap' : basket === 'MULTICAPPLUS' ? 'MultiCapPlus' : basket === 'FNO' ? 'Fno' : basket === 'CUSTOM' ? 'Custom' : basket}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Buy/Sell Dropdown */}
          <div className="relative dropdown-container">
            <button
              onClick={togglePanelDropdown}
              data-dropdown-button
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <span className="text-sm font-medium">
                Buy/Sell ({selectedPanels.length})
              </span>
              <svg className={`w-4 h-4 transition-transform ${panelDropdownOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {panelDropdownOpen && (
              <div className="absolute top-full left-0 mt-1 w-48 bg-white border border-gray-300 rounded-lg shadow-lg z-10" data-dropdown-content onClick={(e) => e.stopPropagation()}>
                {['BUY', 'SELL', 'SIDEWAYS'].map((panel) => (
                  <button
                    key={panel}
                    onClick={() => togglePanel(panel)}
                    className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors"
                  >
                    <div className={cn(
                      "w-4 h-4 border-2 rounded flex items-center justify-center",
                      selectedPanels.includes(panel) 
                        ? "bg-purple-600 border-purple-600" 
                        : "border-gray-300"
                    )}>
                      {selectedPanels.includes(panel) && (
                        <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </div>
                    <span className="text-gray-700">{panel === 'BUY' ? 'Buy' : panel === 'SELL' ? 'Sell' : panel === 'SIDEWAYS' ? 'Sideways' : panel}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Timeframes Dropdown */}
          <div className="relative dropdown-container">
            <button
              onClick={toggleTimeframeDropdown}
              data-dropdown-button
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              <span className="text-sm font-medium">
                Trade Duration ({selectedTimeframes.length})
              </span>
              <svg className={`w-4 h-4 transition-transform ${timeframeDropdownOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {timeframeDropdownOpen && (
              <div className="absolute top-full left-0 mt-1 w-48 bg-white border border-gray-300 rounded-lg shadow-lg z-10" data-dropdown-content onClick={(e) => e.stopPropagation()}>
                {TIMEFRAMES.map((timeframe) => (
                  <button
                    key={timeframe}
                    onClick={() => toggleTimeframe(timeframe)}
                    className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors"
                  >
                    <div className={cn(
                      "w-4 h-4 border-2 rounded flex items-center justify-center",
                      selectedTimeframes.includes(timeframe) 
                        ? "bg-green-600 border-green-600" 
                        : "border-gray-300"
                    )}>
                      {selectedTimeframes.includes(timeframe) && (
                        <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </div>
                    <span className="text-gray-700">{timeframe === 'INTRADAY' ? 'Intraday' : timeframe === 'SHORTTERM' ? 'Shortterm' : timeframe === 'POSITIONAL' ? 'Positional' : timeframe === 'LONGTERM' ? 'Longterm' : timeframe}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Alerts For Dropdown */}
          <div className="relative dropdown-container">
            <button
              onClick={toggleTimeFilterDropdown}
              data-dropdown-button
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <span className="text-sm font-medium">
                Alerts For {selectedTimeFilter === 'TODAY' ? 'Today' : selectedTimeFilter === 'YESTERDAY' ? 'Yesterday' : selectedTimeFilter === 'THIS_WEEK' ? 'This Week' : selectedTimeFilter === 'ALL' ? 'All' : selectedTimeFilter}
              </span>
              <svg className={`w-4 h-4 transition-transform ${timeFilterDropdownOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {timeFilterDropdownOpen && (
              <div className="absolute top-full left-0 mt-1 w-32 bg-white border border-gray-300 rounded-lg shadow-lg z-10" data-dropdown-content onClick={(e) => e.stopPropagation()}>
                {TIME_FILTER_OPTIONS.map((filter) => (
                  <button
                    key={filter}
                    onClick={() => setTimeFilter(filter)}
                    className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors"
                  >
                    <div className={cn(
                      "w-4 h-4 border-2 rounded flex items-center justify-center",
                      selectedTimeFilter === filter 
                        ? "bg-blue-600 border-blue-600" 
                        : "border-gray-300"
                    )}>
                      {selectedTimeFilter === filter && (
                        <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </div>
                    <span className="text-gray-700">{filter === 'TODAY' ? 'Today' : filter === 'YESTERDAY' ? 'Yesterday' : filter === 'THIS_WEEK' ? 'This Week' : filter === 'ALL' ? 'All' : filter}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Second Row - Active Tags */}
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-600 font-medium">Active:</span>
          <div className="flex flex-wrap gap-1">
            {/* Selected Baskets */}
            {selectedBaskets.map((basket) => (
              <span
                key={basket}
                className="inline-flex items-center gap-1 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full"
              >
                {basket === 'ALL' ? 'All' : basket === 'NIFTY50' ? 'Nifty50' : basket === 'BANKNIFTY' ? 'BankNifty' : basket === 'NIFTY200' ? 'Nifty200' : basket === 'NIFTY500' ? 'Nifty500' : basket === 'MULTICAP' ? 'MultiCap' : basket === 'MULTICAPPLUS' ? 'MultiCapPlus' : basket === 'FNO' ? 'Fno' : basket === 'CUSTOM' ? 'Custom' : basket}
                {basket === 'CUSTOM' && customBasketStocks.length > 0 && (
                  <span className="text-blue-600">({customBasketStocks.length})</span>
                )}
                {basket !== 'CUSTOM' && (
                  <span className="text-blue-600">({getBasketCount(basket)})</span>
                )}
              </span>
            ))}
            
            {/* Selected Timeframes */}
            {selectedTimeframes.map((timeframe) => (
              <span
                key={timeframe}
                className="inline-flex items-center gap-1 px-2 py-1 text-xs bg-green-100 text-green-800 rounded-full"
              >
                {timeframe === 'INTRADAY' ? 'Intraday' : timeframe === 'SHORTTERM' ? 'Shortterm' : timeframe === 'POSITIONAL' ? 'Positional' : timeframe === 'LONGTERM' ? 'Longterm' : timeframe}
              </span>
            ))}
            
            {/* Selected Panels */}
            {selectedPanels.map((panel) => (
              <span
                key={panel}
                className={cn(
                  "inline-flex items-center gap-1 px-2 py-1 text-xs rounded-full",
                  panel === 'BUY' ? 'bg-green-100 text-green-800' :
                  panel === 'SELL' ? 'bg-red-100 text-red-800' :
                  'bg-yellow-100 text-yellow-800'
                )}
              >
                {panel === 'BUY' ? 'Buy' : panel === 'SELL' ? 'Sell' : panel === 'SIDEWAYS' ? 'Sideways' : panel}
              </span>
            ))}
            
            {/* Time Filter */}
            <span className="inline-flex items-center gap-1 px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded-full">
              {selectedTimeFilter === 'TODAY' ? 'Today' : selectedTimeFilter === 'YESTERDAY' ? 'Yesterday' : selectedTimeFilter === 'THIS_WEEK' ? 'This Week' : selectedTimeFilter === 'ALL' ? 'All' : selectedTimeFilter}
            </span>
          </div>
        </div>

        {/* Custom Basket Panel - Inline with filters */}
        {selectedBaskets.includes('CUSTOM') && (
          <div className="flex-1 bg-blue-50 rounded-lg border border-blue-200">
            {/* Header with minimize/expand */}
            <div 
              className="flex items-center justify-between p-3 cursor-pointer hover:bg-blue-100 transition-colors"
              onClick={() => setIsCustomBasketMinimized(!isCustomBasketMinimized)}
            >
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-blue-800">
                  Custom Basket ({customBasketStocks.length}/20 stocks)
                </span>
                <button
                  onClick={handleSearchButtonClick}
                  disabled={customBasketStocks.length >= 20}
                  className={`px-2 py-1 text-xs rounded ${
                    customBasketStocks.length >= 20
                      ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                      : 'bg-green-600 text-white hover:bg-green-700'
                  }`}
                >
                  {customBasketStocks.length >= 20 ? 'Limit Reached' : 'Search & Add Stocks'}
                </button>
              </div>
              <div className="text-blue-600">
                {isCustomBasketMinimized ? '▼' : '▲'}
              </div>
            </div>
            {/* Expandable Content */}
            {!isCustomBasketMinimized && (
              <div className="px-3 pb-3 space-y-3">
                {/* Stock Search */}
                {showStockSearch && (
                  <div className="space-y-2">
                    <input
                      type="text"
                      placeholder="Search stocks (e.g., RELIANCE, TCS, HDFC)"
                      value={stockSearchQuery}
                      onChange={handleStockSearch}
                      onBlur={handleSearchInputBlur}
                      className="w-full px-3 py-2 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      autoFocus
                    />
                    {searchResults.length > 0 && (
                      <div className="max-h-32 overflow-y-auto border rounded-lg bg-white">
                        {searchResults.map((stock) => (
                          <button
                            key={stock}
                            onClick={() => addStockToBasket(stock)}
                            className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            {stock}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                )}
                {/* Custom Basket Stocks List */}
                <SimpleCustomBasket 
                  customBasketStocks={customBasketStocks} 
                  removeStockFromBasket={removeStockFromBasket} 
                />
              </div>
            )}
          </div>
        )}

        {/* Search Bar - Compact 25% width */}
        <div className="bg-white rounded-lg border border-gray-200 p-1.5 shadow-sm w-1/4">
          <div className="flex items-center gap-1.5">
            <div className="flex-1 relative">
              <div className="absolute inset-y-0 left-0 pl-1.5 flex items-center pointer-events-none">
                <svg className="h-3.5 w-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <input
                type="text"
                placeholder="Search stocks..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-6 pr-2 py-1 text-xs border border-gray-300 rounded focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none"
              />
            </div>
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="px-1.5 py-0.5 text-xs text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded transition-colors"
              >
                ×
              </button>
            )}
          </div>
          {searchQuery && (
            <div className="mt-1 text-xs text-gray-600">
              {filteredStocksData.BUY.length + filteredStocksData.SELL.length + filteredStocksData.SIDEWAYS.length} stocks matching "{searchQuery}"
              {selectedBaskets.length > 0 && !selectedBaskets.includes('ALL') && (
                <span className="ml-2 text-gray-500">
                  (filtered by {selectedBaskets.join(', ')})
                </span>
              )}
            </div>
          )}
        </div>

        {/* Main Content Layout */}
        <div className="relative">
          {/* Stock Panels Container */}
          <div className="flex gap-3 mr-[42%] w-3/5">
            {/* BUY Section */}
            {selectedPanels.includes('BUY') && (
              <Card className="h-[500px] flex-1">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg text-green-700 flex justify-between items-center">
                BUY
                <span className="text-sm font-normal text-gray-500">
                  {filteredStocksData.BUY.length}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="h-[400px] overflow-y-auto px-4 pb-4">
                {loading ? (
                  <div className="flex items-center justify-center h-32">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
                  </div>
                ) : filteredStocksData.BUY.length === 0 ? (
                  <div className="text-center text-gray-500 py-8">
                    {searchQuery ? `No BUY stocks matching "${searchQuery}"` : 'No BUY recommendations'}
                  </div>
                ) : (
                  <div className={`grid gap-2 ${selectedPanels.length === 1 ? 'grid-cols-4' : selectedPanels.length === 2 ? 'grid-cols-3' : 'grid-cols-2'}`}>
                    {filteredStocksData.BUY.map((stock) => (
                      <StockCard key={stock.symbol} stock={stock} onClick={handleStockClick} allAlerts={alerts} />
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
              </Card>
            )}

            {/* SELL Section */}
            {selectedPanels.includes('SELL') && (
              <Card className="h-[500px] flex-1">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg text-red-700 flex justify-between items-center">
                SELL
                <span className="text-sm font-normal text-gray-500">
                  {filteredStocksData.SELL.length}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="h-[400px] overflow-y-auto px-4 pb-4">
                {loading ? (
                  <div className="flex items-center justify-center h-32">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-red-600"></div>
                  </div>
                ) : filteredStocksData.SELL.length === 0 ? (
                  <div className="text-center text-gray-500 py-8">
                    {searchQuery ? `No SELL stocks matching "${searchQuery}"` : 'No SELL recommendations'}
                  </div>
                ) : (
                  <div className={`grid gap-2 ${selectedPanels.length === 1 ? 'grid-cols-4' : selectedPanels.length === 2 ? 'grid-cols-3' : 'grid-cols-2'}`}>
                    {filteredStocksData.SELL.map((stock) => (
                      <StockCard key={stock.symbol} stock={stock} onClick={handleStockClick} allAlerts={alerts} />
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
              </Card>
            )}

            {/* SIDEWAYS Section */}
            {selectedPanels.includes('SIDEWAYS') && (
              <Card className="h-[500px] flex-1">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg text-yellow-700 flex justify-between items-center">
                SIDEWAYS
                <span className="text-sm font-normal text-gray-500">
                  {filteredStocksData.SIDEWAYS.length}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="h-[400px] overflow-y-auto px-4 pb-4">
                {loading ? (
                  <div className="flex items-center justify-center h-32">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-yellow-600"></div>
                  </div>
                ) : filteredStocksData.SIDEWAYS.length === 0 ? (
                  <div className="text-center text-gray-500 py-8">
                    {searchQuery ? `No SIDEWAYS stocks matching "${searchQuery}"` : 'No SIDEWAYS recommendations'}
                  </div>
                ) : (
                  <div className={`grid gap-2 ${selectedPanels.length === 1 ? 'grid-cols-4' : selectedPanels.length === 2 ? 'grid-cols-3' : 'grid-cols-2'}`}>
                    {filteredStocksData.SIDEWAYS.map((stock) => (
                      <StockCard key={stock.symbol} stock={stock} onClick={handleStockClick} allAlerts={alerts} />
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
              </Card>
            )}
          </div>

          {/* Alerts Section - Fixed on Right */}
          <Card className="h-[500px] absolute top-0 right-0 w-2/5">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg text-blue-700 flex justify-between items-center">
                ALERTS
                <span className="text-xs text-gray-500 font-normal">Latest on top</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="h-[400px] overflow-y-auto px-2 pb-4">
                {(() => {
                  const filteredAlerts = filterAlertsByTime(alerts);
                  return filteredAlerts.length === 0 ? (
                    <div className="text-center text-gray-500 py-8">
                      No alerts available for {selectedTimeFilter === 'TODAY' ? 'today' : selectedTimeFilter === 'YESTERDAY' ? 'yesterday' : selectedTimeFilter === 'THIS_WEEK' ? 'this week' : selectedTimeFilter === 'ALL' ? 'all' : selectedTimeFilter.toLowerCase()}
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {filteredAlerts.map((alert) => (
                        <AlertCard key={alert.id} alert={alert} allAlerts={alerts} />
                      ))}
                    </div>
                  );
                })()}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
      </div>


      {/* Stock Detail Modal */}
      <StockDetailModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        stock={selectedStock}
        alerts={alerts}
        apiBaseUrl={apiBaseUrl}
      />

      {/* Settings Modal */}
      {showSettings && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
          onClick={() => setShowSettings(false)}
        >
          <div 
            className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold">Default Basket Settings</h3>
                <button
                  onClick={() => setShowSettings(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              
              <p className="text-sm text-gray-600 mb-4">
                Choose which baskets should be selected by default when you load the dashboard.
              </p>
              
              <div className="grid grid-cols-2 gap-2 mb-6">
                {BASKETS.map((basket) => (
                  <label key={basket} className="flex items-center space-x-2 cursor-pointer p-2 hover:bg-gray-50 rounded">
                    <input
                      type="checkbox"
                      checked={defaultBaskets.includes(basket)}
                      onChange={() => toggleDefaultBasket(basket)}
                      className="rounded border-gray-300"
                    />
                    <span className="text-sm">{basket}</span>
                  </label>
                ))}
              </div>
              
              <div className="flex gap-2">
                <button
                  onClick={saveDefaultBaskets}
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Save & Apply
                </button>
                <button
                  onClick={resetToDefaults}
                  className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                >
                  Reset to ALL
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// Stock Card Component
function StockCard({ stock, onClick, allAlerts }) {
  // Use the same getAlertDuration function from StockDetailModal
  const getAlertDuration = (stock, allAlerts) => {
    if (!allAlerts || allAlerts.length === 0) {
      console.log(`🔍 ${stock.symbol}: No alerts available`);
      return null;
    }
    
    // Filter alerts for this specific stock
    const stockAlerts = allAlerts.filter(alert => alert.symbol === stock.symbol);
    
    if (stockAlerts.length === 0) {
      console.log(`🔍 ${stock.symbol}: No alerts found for this stock`);
      return null;
    }
    
    console.log(`🔍 ${stock.symbol}: Found ${stockAlerts.length} alerts:`, stockAlerts);
    
    // Sort by timestamp (newest first)
    const sortedAlerts = stockAlerts.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    
    // Get the action type of the most recent alert
    const actionType = sortedAlerts[0].action;
    console.log(`🔍 ${stock.symbol}: Most recent action type: ${actionType}`);
    
    // Count consecutive alerts of the same type from the most recent
    let consecutiveCount = 0;
    const today = new Date();
    
    for (let i = 0; i < sortedAlerts.length; i++) {
      const alert = sortedAlerts[i];
      const alertDate = new Date(alert.timestamp);
      const daysDiff = Math.floor((today - alertDate) / (1000 * 60 * 60 * 24));
      
      console.log(`🔍 ${stock.symbol}: Alert ${i + 1} - Action: ${alert.action}, Date: ${alert.timestamp}, Days ago: ${daysDiff}`);
      
      // If this is the first alert or same action type, count it
      if (i === 0 || alert.action === actionType) {
        consecutiveCount++;
        console.log(`🔍 ${stock.symbol}: Counted alert ${i + 1}, consecutive count: ${consecutiveCount}`);
      } else {
        // Different action type, stop counting
        console.log(`🔍 ${stock.symbol}: Different action type (${alert.action} vs ${actionType}), stopping count`);
        break;
      }
    }
    
    console.log(`🔍 ${stock.symbol}: Final consecutive count: ${consecutiveCount}`);
    
    if (consecutiveCount <= 1) {
      console.log(`🔍 ${stock.symbol}: Only ${consecutiveCount} consecutive alerts, showing as since 0 day`);
      return {
        action: actionType,
        days: 0
      };
    }
    
    const result = {
      action: actionType,
      days: consecutiveCount
    };
    
    console.log(`🔍 ${stock.symbol}: Returning duration:`, result);
    return result;
  };

  const alertDuration = getAlertDuration(stock, allAlerts);

  return (
    <button
      onClick={() => onClick(stock)}
      className="w-full p-2 rounded-full bg-blue-50 hover:bg-blue-100 border border-blue-200 hover:border-blue-300 transition-all duration-200 cursor-pointer text-center"
    >
      <div className="font-bold text-gray-900 uppercase" style={{ fontSize: '10px' }}>
        {stock.symbol}
      </div>
      {alertDuration && (
        <div className="text-xs text-gray-600 mt-1" style={{ fontSize: '8px' }}>
          since {alertDuration.days} day{alertDuration.days > 1 ? 's' : ''}
        </div>
      )}
    </button>
  );
}

// Alert Card Component
function AlertCard({ alert, allAlerts }) {
  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleString('en-IN', {
      day: '2-digit',
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
  };

  // Calculate alert duration for this specific alert
  const getAlertDuration = (alert, allAlerts) => {
    if (!allAlerts || allAlerts.length === 0) return null;
    
    // Filter alerts for this specific stock
    const stockAlerts = allAlerts.filter(a => a.symbol === alert.symbol);
    
    if (stockAlerts.length === 0) return null;
    
    // Sort by timestamp (newest first)
    const sortedAlerts = stockAlerts.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    
    // Get the action type of the most recent alert
    const actionType = sortedAlerts[0].action;
    
    // Count consecutive alerts of the same type from the most recent
    let consecutiveCount = 0;
    const today = new Date();
    
    for (let i = 0; i < sortedAlerts.length; i++) {
      const a = sortedAlerts[i];
      const alertDate = new Date(a.timestamp);
      const daysDiff = Math.floor((today - alertDate) / (1000 * 60 * 60 * 24));
      
      // If this is the first alert or same action type, count it
      if (i === 0 || a.action === actionType) {
        consecutiveCount++;
      } else {
        // Different action type, stop counting
        break;
      }
    }
    
    if (consecutiveCount <= 1) {
      return {
        action: actionType,
        days: 0
      };
    }
    
    return {
      action: actionType,
      days: consecutiveCount
    };
  };

  const alertDuration = getAlertDuration(alert, allAlerts);

  return (
    <div className="p-3 rounded-lg hover:bg-gray-50 transition-colors duration-200">
      <div className="space-y-1">
        <div className={cn(
          "font-bold text-sm",
          alert.action === 'BUY' ? 'text-green-600' : 'text-red-600'
        )}>
          {alert.action} - {alert.symbol} 
          <span className="ml-2">@ ₹{alert.price?.toFixed(2) || '0.00'}</span>
          {alertDuration && (
            <span className="text-xs text-gray-500 ml-2" style={{ fontSize: '10px' }}>
              (since {alertDuration.days} day{alertDuration.days > 1 ? 's' : ''})
            </span>
          )}
        </div>
        <div className="text-xs text-gray-500">
          {alert.source} - {formatTimestamp(alert.timestamp)}
        </div>
      </div>
    </div>
  );
}
