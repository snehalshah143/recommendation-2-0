import React, { useState, useEffect } from 'react';
import { cn } from '../lib/utils';

const DashboardHeader = ({ apiBaseUrl = '' }) => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [indicesData, setIndicesData] = useState({
    nifty: 0,
    banknifty: 0,
    marketOpen: false
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Update clock every second
  useEffect(() => {
    const timeInterval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timeInterval);
  }, []);

  // Fetch indices data every 10 seconds
  const fetchIndicesData = async () => {
    if (!apiBaseUrl) {
      // Fallback data when no backend
      setIndicesData({
        nifty: 22050.75,
        banknifty: 48725.30,
        marketOpen: true
      });
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
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
      setIndicesData(data);
    } catch (err) {
      console.error('Failed to fetch indices data:', err);
      setError(err.message);
      // Use fallback data on error
      setIndicesData({
        nifty: 22050.75,
        banknifty: 48725.30,
        marketOpen: true
      });
    } finally {
      setLoading(false);
    }
  };

  // Initial fetch and set up interval
  useEffect(() => {
    fetchIndicesData();
    
    const indicesInterval = setInterval(fetchIndicesData, 10000); // 10 seconds
    
    return () => clearInterval(indicesInterval);
  }, [apiBaseUrl]);

  const formatTime = (date) => {
    return date.toLocaleString('en-IN', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
  };

  const formatPrice = (price) => {
    return price.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  };

  return (
    <header className="sticky top-0 z-50 bg-gradient-to-r from-gray-50 to-gray-100 shadow-lg border-b border-gray-200 p-4">
      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          {/* Left side - Logo and Title */}
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-3">
              <div className="flex flex-col space-y-1 group">
                <h1 className="text-3xl font-black tracking-wide drop-shadow-sm transition-all duration-300 group-hover:scale-105">
                  <span className="bg-gradient-to-r from-blue-600 via-blue-700 to-green-600 bg-clip-text text-transparent">
                    Ideas To Invest
                  </span>
                </h1>
                <h2 className="text-xl font-bold tracking-wide drop-shadow-sm transition-all duration-300 group-hover:scale-105">
                  <span className="bg-gradient-to-r from-red-600 via-red-700 to-orange-600 bg-clip-text text-transparent">
                    Ideas To Trade
                  </span>
                </h2>
              </div>
            </div>
          </div>

          {/* Right side - Market Info */}
          <div className="flex flex-col sm:flex-row items-center gap-4 sm:gap-6">
            {/* Indices */}
            <div className="flex flex-col sm:flex-row items-center gap-2 sm:gap-4">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-600">NIFTY:</span>
                <span className="font-semibold text-blue-600">
                  {loading ? (
                    <span className="animate-pulse">---</span>
                  ) : (
                    formatPrice(indicesData.nifty)
                  )}
                </span>
              </div>
              
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-600">BANKNIFTY:</span>
                <span className="font-semibold text-red-600">
                  {loading ? (
                    <span className="animate-pulse">---</span>
                  ) : (
                    formatPrice(indicesData.banknifty)
                  )}
                </span>
              </div>
            </div>

            {/* Market Status */}
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-gray-600">Market:</span>
              <span
                className={cn(
                  "px-3 py-1 rounded-full text-xs font-semibold",
                  indicesData.marketOpen
                    ? "bg-green-100 text-green-800"
                    : "bg-red-100 text-red-800"
                )}
              >
                {indicesData.marketOpen ? "OPEN" : "CLOSED"}
              </span>
            </div>

            {/* Clock */}
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-gray-600">Time:</span>
              <span className="font-semibold text-gray-800 text-sm">
                {formatTime(currentTime)}
              </span>
            </div>

            {/* Error indicator (if any) */}
            {error && (
              <div className="text-xs text-red-500 bg-red-50 px-2 py-1 rounded">
                Offline
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default DashboardHeader;
