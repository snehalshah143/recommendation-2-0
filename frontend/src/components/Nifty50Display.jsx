import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { cn } from '../lib/utils';

const Nifty50Display = ({ apiBaseUrl = '' }) => {
  const [niftyData, setNiftyData] = useState({
    price: 0,
    change: 0,
    changePercent: 0,
    isPositive: true
  });
  const [loading, setLoading] = useState(true);

  // Fetch real Nifty 50 data from API
  useEffect(() => {
    const fetchNiftyData = async () => {
      setLoading(true);
      
      if (!apiBaseUrl) {
        // Fallback data when no backend
        const mockData = {
          price: 24567.35,
          change: Math.random() > 0.5 ? 125.50 : -87.25,
          changePercent: Math.random() > 0.5 ? 0.51 : -0.35,
          isPositive: Math.random() > 0.5
        };
        setNiftyData(mockData);
        setLoading(false);
        return;
      }
      
      try {
        const response = await fetch(`${apiBaseUrl}/api/indices`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        
        // Use actual change values from API
        const change = data.niftyChange || 0;
        const changePercent = data.niftyChangePercent || 0;
        
        setNiftyData({
          price: data.nifty || 24567.35,
          change: change,
          changePercent: changePercent,
          isPositive: change >= 0
        });
      } catch (err) {
        console.error('Failed to fetch Nifty data:', err);
        // Use fallback data on error
        const fallbackData = {
          price: 24567.35,
          change: 0,
          changePercent: 0,
          isPositive: true
        };
        setNiftyData(fallbackData);
      } finally {
        setLoading(false);
      }
    };

    fetchNiftyData();
    
    // Update every 30 seconds
    const interval = setInterval(fetchNiftyData, 30000);
    return () => clearInterval(interval);
  }, [apiBaseUrl]);

  if (loading) {
    return (
      <Card className="w-48">
        <CardContent className="p-3">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
            <div className="h-6 bg-gray-200 rounded w-1/2"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-48 hover:shadow-md transition-shadow">
      <CardContent className="p-3">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-xs text-gray-600 font-medium">NIFTY50</div>
            <div className="text-lg font-bold text-gray-900">
              {niftyData.price.toLocaleString('en-IN', { 
                minimumFractionDigits: 2, 
                maximumFractionDigits: 2 
              })}
            </div>
          </div>
          <div className="text-right">
            <div className={cn(
              "text-sm font-semibold",
              niftyData.isPositive ? "text-green-600" : "text-red-600"
            )}>
              {niftyData.isPositive ? '+' : ''}{niftyData.change.toFixed(2)}
            </div>
            <div className={cn(
              "text-xs font-medium",
              niftyData.isPositive ? "text-green-600" : "text-red-600"
            )}>
              {niftyData.isPositive ? '+' : ''}{niftyData.changePercent.toFixed(2)}%
            </div>
          </div>
        </div>
        
        {/* Mini chart indicator */}
        <div className="mt-2 flex items-center gap-1">
          <div className={cn(
            "w-2 h-2 rounded-full",
            niftyData.isPositive ? "bg-green-500" : "bg-red-500"
          )}></div>
          <div className="text-xs text-gray-500">
            {niftyData.isPositive ? '↗' : '↘'} Live
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default Nifty50Display;
