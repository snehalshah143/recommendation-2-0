import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { cn } from '../lib/utils';

const MarketIndicesDisplay = ({ apiBaseUrl = '' }) => {
  const [indicesData, setIndicesData] = useState({
    nifty: 0,
    banknifty: 0,
    marketOpen: false,
    lastUpdated: null,
    niftyChange: 0,
    niftyChangePercent: 0,
    bankniftyChange: 0,
    bankniftyChangePercent: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch real market indices data from API
  useEffect(() => {
    const fetchIndicesData = async () => {
      setLoading(true);
      setError(null);
      
      if (!apiBaseUrl) {
        // Fallback data when no backend
        setIndicesData({
          nifty: 22050.75,
          banknifty: 48725.30,
          marketOpen: true,
          lastUpdated: new Date().toISOString()
        });
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
        
        setIndicesData({
          nifty: data.nifty || 24500.0,
          banknifty: data.banknifty || 52000.0,
          marketOpen: data.marketOpen || false,
          lastUpdated: data.lastUpdated || new Date().toISOString(),
          niftyChange: data.niftyChange || 0,
          niftyChangePercent: data.niftyChangePercent || 0,
          bankniftyChange: data.bankniftyChange || 0,
          bankniftyChangePercent: data.bankniftyChangePercent || 0
        });
      } catch (err) {
        console.error('Failed to fetch indices data:', err);
        setError(err.message);
        // Use fallback data on error
        setIndicesData({
          nifty: 24500.0,
          banknifty: 52000.0,
          marketOpen: false,
          lastUpdated: new Date().toISOString(),
          niftyChange: 0,
          niftyChangePercent: 0,
          bankniftyChange: 0,
          bankniftyChangePercent: 0
        });
      } finally {
        setLoading(false);
      }
    };

    fetchIndicesData();
    
    // Update every 30 seconds
    const interval = setInterval(fetchIndicesData, 30000);
    return () => clearInterval(interval);
  }, [apiBaseUrl]);

  const formatPrice = (price) => {
    return price.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return '--:--';
    return new Date(timestamp).toLocaleTimeString('en-IN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="flex gap-4">
        <Card className="w-48">
          <CardContent className="p-3">
            <div className="animate-pulse">
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div className="h-6 bg-gray-200 rounded w-1/2"></div>
            </div>
          </CardContent>
        </Card>
        <Card className="w-48">
          <CardContent className="p-3">
            <div className="animate-pulse">
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div className="h-6 bg-gray-200 rounded w-1/2"></div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex gap-4">
      {/* Nifty 50 Card */}
      <Card className="w-48 hover:shadow-md transition-shadow">
        <CardContent className="p-3">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-xs text-gray-600 font-medium">NIFTY50</div>
              <div className="text-lg font-bold text-gray-900">
                {formatPrice(indicesData.nifty)}
              </div>
            </div>
            <div className="text-right">
              <div className={cn(
                "text-sm font-semibold",
                indicesData.niftyChange >= 0 ? "text-green-600" : "text-red-600"
              )}>
                {indicesData.niftyChange >= 0 ? '+' : ''}{indicesData.niftyChange.toFixed(2)}
              </div>
              <div className={cn(
                "text-xs font-medium",
                indicesData.niftyChange >= 0 ? "text-green-600" : "text-red-600"
              )}>
                {indicesData.niftyChange >= 0 ? '+' : ''}{indicesData.niftyChangePercent.toFixed(2)}%
              </div>
              <div className="text-xs text-gray-500">
                {formatTime(indicesData.lastUpdated)}
              </div>
            </div>
          </div>
          
          {/* Market status indicator */}
          <div className="mt-2 flex items-center gap-1">
            <div className={cn(
              "w-2 h-2 rounded-full",
              indicesData.marketOpen ? "bg-green-500" : "bg-red-500"
            )}></div>
            <div className="text-xs text-gray-500">
              {indicesData.marketOpen ? 'Live' : 'Closed'}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Bank Nifty Card */}
      <Card className="w-48 hover:shadow-md transition-shadow">
        <CardContent className="p-3">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-xs text-gray-600 font-medium">BANKNIFTY</div>
              <div className="text-lg font-bold text-gray-900">
                {formatPrice(indicesData.banknifty)}
              </div>
            </div>
            <div className="text-right">
              <div className={cn(
                "text-sm font-semibold",
                indicesData.bankniftyChange >= 0 ? "text-green-600" : "text-red-600"
              )}>
                {indicesData.bankniftyChange >= 0 ? '+' : ''}{indicesData.bankniftyChange.toFixed(2)}
              </div>
              <div className={cn(
                "text-xs font-medium",
                indicesData.bankniftyChange >= 0 ? "text-green-600" : "text-red-600"
              )}>
                {indicesData.bankniftyChange >= 0 ? '+' : ''}{indicesData.bankniftyChangePercent.toFixed(2)}%
              </div>
              <div className="text-xs text-gray-500">
                {formatTime(indicesData.lastUpdated)}
              </div>
            </div>
          </div>
          
          {/* Market status indicator */}
          <div className="mt-2 flex items-center gap-1">
            <div className={cn(
              "w-2 h-2 rounded-full",
              indicesData.marketOpen ? "bg-green-500" : "bg-red-500"
            )}></div>
            <div className="text-xs text-gray-500">
              {indicesData.marketOpen ? 'Live' : 'Closed'}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Error indicator */}
      {error && (
        <div className="text-xs text-red-500 bg-red-50 px-2 py-1 rounded">
          Offline
        </div>
      )}
    </div>
  );
};

export default MarketIndicesDisplay;
