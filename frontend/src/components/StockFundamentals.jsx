import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { getStockFundamentals } from '../lib/api';

const StockFundamentals = ({ symbol, baseUrl = '' }) => {
  const [fundamentals, setFundamentals] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchFundamentals = async () => {
      if (!symbol) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);
        
        const data = await getStockFundamentals({ 
          symbol: symbol.toUpperCase(), 
          baseUrl 
        });
        
        setFundamentals(data);
      } catch (err) {
        console.error('Error fetching stock fundamentals:', err);
        setError(err.message);
        // Set mock data on error to prevent UI breaking
        setFundamentals({
          symbol: symbol.toUpperCase(),
          peRatio: null,
          roe: null,
          roc: null,
          bookValue: null,
          marketCap: null,
          sales: null,
          ltp: null
        });
      } finally {
        setLoading(false);
      }
    };

    fetchFundamentals();
  }, [symbol, baseUrl]);

  // Format number with proper currency/percentage formatting
  const formatValue = (value, type = 'number') => {
    if (value === null || value === undefined) return '--';
    
    switch (type) {
      case 'percentage':
        return `${value}%`;
      case 'currency':
        return `₹${value.toLocaleString('en-IN')}`;
      case 'marketCap':
        if (value >= 10000000) { // 1 crore
          return `₹${(value / 10000000).toFixed(1)}L Cr`;
        }
        return `₹${value.toLocaleString('en-IN')}`;
      case 'sales':
        if (value >= 1000) {
          return `₹${(value / 1000).toFixed(1)}K Cr`;
        }
        return `₹${value.toLocaleString('en-IN')}`;
      default:
        return value.toString();
    }
  };

  // Fundamentals data array matching the existing layout
  const fundamentalsData = [
    { label: 'P/E Ratio', value: fundamentals?.peRatio, type: 'number' },
    { label: 'ROE', value: fundamentals?.roe, type: 'percentage' },
    { label: 'ROC', value: fundamentals?.roc, type: 'percentage' },
    { label: 'Book Value', value: fundamentals?.bookValue, type: 'currency' },
    { label: 'Market Cap', value: fundamentals?.marketCap, type: 'marketCap' },
    { label: 'Sales (Qtr)', value: fundamentals?.sales, type: 'sales' }
  ];

  if (loading) {
    return (
      <Card className="lg:col-span-1">
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Fundamentals</CardTitle>
        </CardHeader>
        <CardContent className="space-y-1">
          <div className="flex justify-center items-center py-4">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="lg:col-span-1">
      <CardHeader className="pb-2">
        <CardTitle className="text-base">
          Fundamentals
          {fundamentals?.symbol && (
            <span className="text-sm text-gray-500 ml-2">({fundamentals.symbol})</span>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-1">
        {fundamentalsData.map((item) => (
          <div key={item.label} className="flex justify-between items-center py-1 text-xs">
            <span className="text-gray-600">{item.label}</span>
            <span className="font-semibold text-gray-800">
              {formatValue(item.value, item.type)}
            </span>
          </div>
        ))}
        
        {/* LTP (Live Price) - separate section */}
        {fundamentals?.ltp && (
          <div className="flex justify-between items-center py-1 text-xs border-t border-gray-200 mt-2 pt-2">
            <span className="text-gray-600 font-medium">LTP</span>
            <span className="font-bold text-green-600">
              {formatValue(fundamentals.ltp, 'currency')}
            </span>
          </div>
        )}
        
        {/* Error message */}
        {error && (
          <div className="text-xs text-red-500 mt-2 text-center">
            API Error: {error}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default StockFundamentals;
