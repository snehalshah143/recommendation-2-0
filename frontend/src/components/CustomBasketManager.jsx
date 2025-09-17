import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { cn } from '../lib/utils';

const CustomBasketManager = ({ 
  isOpen, 
  onClose, 
  customStocks, 
  onStocksChange, 
  availableStocks = [],
  apiBaseUrl = '' 
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStocks, setSelectedStocks] = useState(customStocks || []);
  const [loading, setLoading] = useState(false);

  // Fetch available stocks from backend
  useEffect(() => {
    const fetchAvailableStocks = async () => {
      if (!apiBaseUrl) return;
      
      setLoading(true);
      try {
        const response = await fetch(`${apiBaseUrl}/api/stocks/available`);
        if (response.ok) {
          const data = await response.json();
          // Assuming the API returns an array of stock objects with symbol and name
          console.log('Available stocks from backend:', data);
        }
      } catch (error) {
        console.error('Error fetching available stocks:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAvailableStocks();
  }, [apiBaseUrl]);

  // Get unique stocks from alerts and backend
  const allAvailableStocks = useMemo(() => {
    const stockSet = new Set();
    
    // Add stocks from alerts
    availableStocks.forEach(stock => {
      if (stock.symbol) {
        stockSet.add(JSON.stringify({
          symbol: stock.symbol,
          name: stock.name || stock.symbol,
          source: 'alerts'
        }));
      }
    });

    // Add some common NSE stocks for demo
    const commonStocks = [
      { symbol: 'RELIANCE', name: 'Reliance Industries Ltd' },
      { symbol: 'TCS', name: 'Tata Consultancy Services Ltd' },
      { symbol: 'HDFCBANK', name: 'HDFC Bank Ltd' },
      { symbol: 'INFY', name: 'Infosys Ltd' },
      { symbol: 'HINDUNILVR', name: 'Hindustan Unilever Ltd' },
      { symbol: 'ITC', name: 'ITC Ltd' },
      { symbol: 'SBIN', name: 'State Bank of India' },
      { symbol: 'BHARTIARTL', name: 'Bharti Airtel Ltd' },
      { symbol: 'KOTAKBANK', name: 'Kotak Mahindra Bank Ltd' },
      { symbol: 'LT', name: 'Larsen & Toubro Ltd' },
      { symbol: 'ASIANPAINT', name: 'Asian Paints Ltd' },
      { symbol: 'AXISBANK', name: 'Axis Bank Ltd' },
      { symbol: 'MARUTI', name: 'Maruti Suzuki India Ltd' },
      { symbol: 'SUNPHARMA', name: 'Sun Pharmaceutical Industries Ltd' },
      { symbol: 'TITAN', name: 'Titan Company Ltd' },
      { symbol: 'ULTRACEMCO', name: 'UltraTech Cement Ltd' },
      { symbol: 'WIPRO', name: 'Wipro Ltd' },
      { symbol: 'NESTLEIND', name: 'Nestle India Ltd' },
      { symbol: 'POWERGRID', name: 'Power Grid Corporation of India Ltd' },
      { symbol: 'NTPC', name: 'NTPC Ltd' },
      { symbol: 'ONGC', name: 'Oil & Natural Gas Corporation Ltd' },
      { symbol: 'COALINDIA', name: 'Coal India Ltd' },
      { symbol: 'TECHM', name: 'Tech Mahindra Ltd' },
      { symbol: 'TATAMOTORS', name: 'Tata Motors Ltd' },
      { symbol: 'BAJFINANCE', name: 'Bajaj Finance Ltd' },
      { symbol: 'HCLTECH', name: 'HCL Technologies Ltd' },
      { symbol: 'DRREDDY', name: 'Dr. Reddy\'s Laboratories Ltd' },
      { symbol: 'CIPLA', name: 'Cipla Ltd' },
      { symbol: 'EICHERMOT', name: 'Eicher Motors Ltd' },
      { symbol: 'BAJAJFINSV', name: 'Bajaj Finserv Ltd' },
      { symbol: 'GRASIM', name: 'Grasim Industries Ltd' },
      { symbol: 'JSWSTEEL', name: 'JSW Steel Ltd' },
      { symbol: 'TATASTEEL', name: 'Tata Steel Ltd' },
      { symbol: 'APOLLOHOSP', name: 'Apollo Hospitals Enterprise Ltd' },
      { symbol: 'DIVISLAB', name: 'Divi\'s Laboratories Ltd' },
      { symbol: 'HEROMOTOCO', name: 'Hero MotoCorp Ltd' },
      { symbol: 'HINDALCO', name: 'Hindalco Industries Ltd' },
      { symbol: 'INDUSINDBK', name: 'IndusInd Bank Ltd' },
      { symbol: 'M&M', name: 'Mahindra & Mahindra Ltd' },
      { symbol: 'SBILIFE', name: 'SBI Life Insurance Company Ltd' },
      { symbol: 'TATACONSUM', name: 'Tata Consumer Products Ltd' },
      { symbol: 'UPL', name: 'UPL Ltd' },
      { symbol: 'ADANIPORTS', name: 'Adani Ports and Special Economic Zone Ltd' },
      { symbol: 'BAJAJ-AUTO', name: 'Bajaj Auto Ltd' },
      { symbol: 'BRITANNIA', name: 'Britannia Industries Ltd' },
      { symbol: 'CIPLA', name: 'Cipla Ltd' },
      { symbol: 'HDFCLIFE', name: 'HDFC Life Insurance Company Ltd' },
      { symbol: 'ICICIBANK', name: 'ICICI Bank Ltd' },
      { symbol: 'SHREECEM', name: 'Shree Cement Ltd' },
      { symbol: 'TATACONSUM', name: 'Tata Consumer Products Ltd' }
    ];

    commonStocks.forEach(stock => {
      stockSet.add(JSON.stringify({
        symbol: stock.symbol,
        name: stock.name,
        source: 'nse'
      }));
    });

    return Array.from(stockSet).map(stock => JSON.parse(stock));
  }, [availableStocks]);

  // Filter stocks based on search term
  const filteredStocks = useMemo(() => {
    if (!searchTerm.trim()) return allAvailableStocks;
    
    return allAvailableStocks.filter(stock => 
      stock.symbol.toLowerCase().includes(searchTerm.toLowerCase()) ||
      stock.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [allAvailableStocks, searchTerm]);

  const handleStockToggle = (stock) => {
    setSelectedStocks(prev => {
      const isSelected = prev.some(s => s.symbol === stock.symbol);
      if (isSelected) {
        return prev.filter(s => s.symbol !== stock.symbol);
      } else {
        return [...prev, stock];
      }
    });
  };

  const handleSave = () => {
    onStocksChange(selectedStocks);
    onClose();
  };

  const handleClear = () => {
    setSelectedStocks([]);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <Card className="w-full max-w-4xl max-h-[80vh] overflow-hidden">
        <CardHeader className="pb-4">
          <div className="flex items-center justify-between">
            <CardTitle className="text-xl font-bold">Custom Stock Basket</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={onClose}
              className="text-gray-500 hover:text-gray-700"
            >
              ✕
            </Button>
          </div>
        </CardHeader>
        
        <CardContent className="space-y-4">
          {/* Search Input */}
          <div className="relative">
            <Input
              type="text"
              placeholder="Search stocks by symbol or name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10"
            />
            <svg 
              className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" 
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>

          {/* Selected Stocks Count */}
          <div className="flex items-center justify-between">
            <p className="text-sm text-gray-600">
              {selectedStocks.length} stock{selectedStocks.length !== 1 ? 's' : ''} selected
            </p>
            <Button
              variant="outline"
              size="sm"
              onClick={handleClear}
              disabled={selectedStocks.length === 0}
            >
              Clear All
            </Button>
          </div>

          {/* Stock List */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 max-h-96 overflow-y-auto">
            {loading ? (
              <div className="col-span-full text-center py-8 text-gray-500">
                Loading stocks...
              </div>
            ) : filteredStocks.length === 0 ? (
              <div className="col-span-full text-center py-8 text-gray-500">
                No stocks found
              </div>
            ) : (
              filteredStocks.map((stock) => {
                const isSelected = selectedStocks.some(s => s.symbol === stock.symbol);
                return (
                  <button
                    key={stock.symbol}
                    onClick={() => handleStockToggle(stock)}
                    className={cn(
                      "p-3 text-left rounded-lg border transition-colors",
                      isSelected
                        ? "bg-blue-50 border-blue-300 text-blue-900"
                        : "bg-white border-gray-200 hover:bg-gray-50"
                    )}
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="font-medium text-sm">{stock.symbol}</div>
                        <div className="text-xs text-gray-500 truncate">{stock.name}</div>
                      </div>
                      <div className={cn(
                        "w-4 h-4 border-2 rounded flex items-center justify-center",
                        isSelected
                          ? "bg-blue-600 border-blue-600"
                          : "border-gray-300"
                      )}>
                        {isSelected && (
                          <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                          </svg>
                        )}
                      </div>
                    </div>
                  </button>
                );
              })
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-2 pt-4 border-t">
            <Button
              variant="outline"
              onClick={onClose}
            >
              Cancel
            </Button>
            <Button
              onClick={handleSave}
              className="bg-blue-600 hover:bg-blue-700"
            >
              Save Basket ({selectedStocks.length})
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default CustomBasketManager;
