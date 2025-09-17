import { useEffect, useRef, useCallback } from 'react';

/**
 * Custom hook for managing live alerts via WebSocket connection
 * @param {string} wsUrl - WebSocket URL to connect to
 * @param {function} onAlert - Callback function called when alert is received
 * @returns {object} - Connection status and control functions
 */
const useLiveAlerts = (wsUrl, onAlert) => {
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const reconnectAttemptsRef = useRef(0);
  const isConnectingRef = useRef(false);
  const shouldReconnectRef = useRef(true);
  const maxReconnectAttempts = 10;
  const baseReconnectDelay = 1000; // 1 second

  // Exponential backoff calculation
  const getReconnectDelay = useCallback((attempt) => {
    return Math.min(baseReconnectDelay * Math.pow(2, attempt), 30000); // Max 30 seconds
  }, []);

  // Clean up WebSocket connection
  const cleanup = useCallback(() => {
    shouldReconnectRef.current = false;
    
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    
    if (wsRef.current) {
      wsRef.current.onopen = null;
      wsRef.current.onmessage = null;
      wsRef.current.onclose = null;
      wsRef.current.onerror = null;
      
      if (wsRef.current.readyState === WebSocket.OPEN || 
          wsRef.current.readyState === WebSocket.CONNECTING) {
        wsRef.current.close();
      }
      
      wsRef.current = null;
    }
    
    isConnectingRef.current = false;
  }, []);

  // Connect to WebSocket
  const connect = useCallback(() => {
    if (!wsUrl || isConnectingRef.current || !shouldReconnectRef.current) {
      return;
    }

    try {
      isConnectingRef.current = true;
      console.log(`Connecting to WebSocket: ${wsUrl} (attempt ${reconnectAttemptsRef.current + 1})`);
      
      const ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        console.log('WebSocket connected successfully');
        isConnectingRef.current = false;
        reconnectAttemptsRef.current = 0; // Reset attempts on successful connection
      };

      ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          
          // Check if message is an alert
          if (message.type === 'ALERT' && message.data) {
            console.log('Alert received:', message.data);
            onAlert(message.data);
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      ws.onclose = (event) => {
        console.log('WebSocket connection closed:', event.code, event.reason);
        isConnectingRef.current = false;
        
        // Only attempt to reconnect if we should and haven't exceeded max attempts
        if (shouldReconnectRef.current && reconnectAttemptsRef.current < maxReconnectAttempts) {
          const delay = getReconnectDelay(reconnectAttemptsRef.current);
          console.log(`Attempting to reconnect in ${delay}ms...`);
          
          reconnectTimeoutRef.current = setTimeout(() => {
            reconnectAttemptsRef.current++;
            connect();
          }, delay);
        } else if (reconnectAttemptsRef.current >= maxReconnectAttempts) {
          console.error('Max reconnection attempts reached. Stopping reconnection attempts.');
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        isConnectingRef.current = false;
      };

    } catch (error) {
      console.error('Error creating WebSocket connection:', error);
      isConnectingRef.current = false;
      
      // Attempt to reconnect on error
      if (shouldReconnectRef.current && reconnectAttemptsRef.current < maxReconnectAttempts) {
        const delay = getReconnectDelay(reconnectAttemptsRef.current);
        reconnectTimeoutRef.current = setTimeout(() => {
          reconnectAttemptsRef.current++;
          connect();
        }, delay);
      }
    }
  }, [wsUrl, onAlert, getReconnectDelay]);

  // Manual reconnect function
  const reconnect = useCallback(() => {
    if (isConnectingRef.current) {
      return;
    }
    
    cleanup();
    reconnectAttemptsRef.current = 0;
    shouldReconnectRef.current = true;
    connect();
  }, [cleanup, connect]);

  // Disconnect function
  const disconnect = useCallback(() => {
    shouldReconnectRef.current = false;
    cleanup();
  }, [cleanup]);

  // Get connection status
  const getConnectionStatus = useCallback(() => {
    if (!wsRef.current) {
      return 'disconnected';
    }
    
    switch (wsRef.current.readyState) {
      case WebSocket.CONNECTING:
        return 'connecting';
      case WebSocket.OPEN:
        return 'connected';
      case WebSocket.CLOSING:
        return 'closing';
      case WebSocket.CLOSED:
        return 'disconnected';
      default:
        return 'unknown';
    }
  }, []);

  // Initialize connection when wsUrl or onAlert changes
  useEffect(() => {
    if (wsUrl && onAlert) {
      shouldReconnectRef.current = true;
      connect();
    } else {
      cleanup();
    }

    // Cleanup on unmount or when dependencies change
    return cleanup;
  }, [wsUrl, onAlert, connect, cleanup]);

  // Cleanup on unmount
  useEffect(() => {
    return cleanup;
  }, [cleanup]);

  return {
    connectionStatus: getConnectionStatus(),
    reconnectAttempts: reconnectAttemptsRef.current,
    isConnecting: isConnectingRef.current,
    reconnect,
    disconnect,
    connect
  };
};

export default useLiveAlerts;

