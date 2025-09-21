import React, { useState, useEffect, useRef } from 'react';
import { cn } from '../lib/utils';

const DashboardHeader = React.memo(({ apiBaseUrl = '', onSettingsClick }) => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [indicesData, setIndicesData] = useState({
    nifty: 0,
    banknifty: 0,
    marketOpen: false,
    niftyChange: 0,
    niftyChangePercent: 0,
    bankniftyChange: 0,
    bankniftyChangePercent: 0
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [hasInitialData, setHasInitialData] = useState(false);
  const updateTimeoutRef = useRef(null);

  // Update clock every second
  useEffect(() => {
    const timeInterval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timeInterval);
  }, []);

  // Fetch indices data with stable updates
  const fetchIndicesData = async (isInitialLoad = false) => {
    if (!apiBaseUrl) {
      // Fallback data when no backend
      const fallbackData = {
        nifty: 24500.0,
        banknifty: 52000.0,
        marketOpen: true,
        niftyChange: 0,
        niftyChangePercent: 0,
        bankniftyChange: 0,
        bankniftyChangePercent: 0
      };
      setIndicesData(fallbackData);
      setHasInitialData(true);
      return;
    }

    try {
      // Only show loading on initial load, not on periodic updates
      if (isInitialLoad) {
        setLoading(true);
      } else {
        setIsUpdating(true);
      }
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
      
      // Prepare new data
      const newData = {
        nifty: data.nifty || 24500.0,
        banknifty: data.banknifty || 52000.0,
        marketOpen: data.marketOpen || false,
        niftyChange: data.niftyChange || 0,
        niftyChangePercent: data.niftyChangePercent || 0,
        bankniftyChange: data.bankniftyChange || 0,
        bankniftyChangePercent: data.bankniftyChangePercent || 0
      };

      // Debounced update - only update if data has actually changed and is stable
      if (updateTimeoutRef.current) {
        clearTimeout(updateTimeoutRef.current);
      }

      updateTimeoutRef.current = setTimeout(() => {
        setIndicesData(prevData => {
          // More precise change detection with tolerance for floating point differences
          const tolerance = 0.01;
          const hasChanged = 
            Math.abs(prevData.nifty - newData.nifty) > tolerance ||
            Math.abs(prevData.banknifty - newData.banknifty) > tolerance ||
            prevData.marketOpen !== newData.marketOpen ||
            Math.abs(prevData.niftyChange - newData.niftyChange) > tolerance ||
            Math.abs(prevData.niftyChangePercent - newData.niftyChangePercent) > tolerance ||
            Math.abs(prevData.bankniftyChange - newData.bankniftyChange) > tolerance ||
            Math.abs(prevData.bankniftyChangePercent - newData.bankniftyChangePercent) > tolerance;

          if (hasChanged) {
            console.log('📊 Market data updated:', newData);
            return newData;
          }
          return prevData; // No change, keep existing data
        });
      }, 200); // 200ms debounce to ensure very stable updates

      setHasInitialData(true);
    } catch (err) {
      console.error('Failed to fetch indices data:', err);
      setError(err.message);
      // Use fallback data on error only if we don't have initial data
      if (!hasInitialData) {
        setIndicesData({
          nifty: 22050.75,
          banknifty: 48725.30,
          marketOpen: false,
          niftyChange: 0,
          niftyChangePercent: 0,
          bankniftyChange: 0,
          bankniftyChangePercent: 0
        });
        setHasInitialData(true);
      }
    } finally {
      // Only clear loading on initial load
      if (isInitialLoad) {
        setLoading(false);
      } else {
        setIsUpdating(false);
      }
    }
  };

  // Initial fetch and set up interval
  useEffect(() => {
    fetchIndicesData(true); // Initial load with loading state
    
    const indicesInterval = setInterval(() => {
      fetchIndicesData(false); // Periodic updates without loading state
    }, 60000); // 60 seconds - much less frequent updates to eliminate flickering
    
    return () => {
      clearInterval(indicesInterval);
      if (updateTimeoutRef.current) {
        clearTimeout(updateTimeoutRef.current);
      }
    };
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
                <div className="flex items-center gap-1">
                  <span 
                    key={`nifty-${indicesData.nifty}`}
                    className="font-semibold text-blue-600 transition-all duration-500 ease-in-out"
                  >
                    {loading ? (
                      <span className="animate-pulse">---</span>
                    ) : (
                      formatPrice(indicesData.nifty)
                    )}
                  </span>
                  {!loading && indicesData.niftyChange !== 0 && (
                    <span className={cn(
                      "text-xs font-medium px-1 py-0.5 rounded transition-all duration-300",
                      indicesData.niftyChange >= 0 
                        ? "text-green-600 bg-green-100" 
                        : "text-red-600 bg-red-100"
                    )}>
                      {indicesData.niftyChange >= 0 ? '+' : ''}{indicesData.niftyChange.toFixed(2)} ({indicesData.niftyChangePercent >= 0 ? '+' : ''}{indicesData.niftyChangePercent.toFixed(2)}%)
                    </span>
                  )}
                </div>
              </div>
              
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-600">BANKNIFTY:</span>
                <div className="flex items-center gap-1">
                  <span 
                    key={`banknifty-${indicesData.banknifty}`}
                    className="font-semibold text-red-600 transition-all duration-500 ease-in-out"
                  >
                    {loading ? (
                      <span className="animate-pulse">---</span>
                    ) : (
                      formatPrice(indicesData.banknifty)
                    )}
                  </span>
                  {!loading && indicesData.bankniftyChange !== 0 && (
                    <span className={cn(
                      "text-xs font-medium px-1 py-0.5 rounded transition-all duration-300",
                      indicesData.bankniftyChange >= 0 
                        ? "text-green-600 bg-green-100" 
                        : "text-red-600 bg-red-100"
                    )}>
                      {indicesData.bankniftyChange >= 0 ? '+' : ''}{indicesData.bankniftyChange.toFixed(2)} ({indicesData.bankniftyChangePercent >= 0 ? '+' : ''}{indicesData.bankniftyChangePercent.toFixed(2)}%)
                    </span>
                  )}
                </div>
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
              {isUpdating && (
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" title="Updating..."></div>
              )}
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

            {/* Settings Button */}
            <button
              onClick={onSettingsClick || (() => {})}
              className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-200 rounded-lg transition-colors"
              title="Settings"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
});

export default DashboardHeader;
