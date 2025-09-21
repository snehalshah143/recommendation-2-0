/**
 * API utility functions for fetching stocks and alerts data
 */

// Base API configuration
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '';

/**
 * Creates headers object with optional authorization token
 * @param {string} token - Optional authorization token
 * @returns {object} Headers object
 */
const createHeaders = (token) => {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return headers;
};

/**
 * Makes an HTTP request with error handling
 * @param {string} url - Request URL
 * @param {object} options - Fetch options
 * @returns {Promise<any>} Parsed JSON response
 * @throws {Error} If request fails or response is not ok
 */
const makeRequest = async (url, options = {}) => {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...createHeaders(options.token),
        ...options.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status} - ${response.statusText}`);
    }

    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      throw new Error('Response is not JSON');
    }

    return await response.json();
  } catch (error) {
    console.error('API request failed:', error);
    throw error;
  }
};

/**
 * Fetches stocks data based on filters
 * @param {object} params - Query parameters
 * @param {string|string[]} params.baskets - Basket(s) to filter by (e.g., 'NIFTY', ['NIFTY', 'BANKNIFTY'])
 * @param {string|string[]} params.durations - Duration(s) to filter by (e.g., 'INTRADAY', ['INTRADAY', 'SHORTTERM'])
 * @param {string} params.search - Search query for stock symbols
 * @param {string} params.token - Optional authorization token
 * @param {string} params.baseUrl - Optional base URL override
 * @returns {Promise<Array>} Array of stock objects
 * @throws {Error} If request fails
 * 
 * @example
 * // Get NIFTY intraday stocks
 * const stocks = await getStocks({ baskets: 'NIFTY', durations: 'INTRADAY' });
 * 
 * // Get stocks with search query
 * const stocks = await getStocks({ 
 *   baskets: ['NIFTY', 'BANKNIFTY'], 
 *   durations: 'SHORTTERM',
 *   search: 'RELIANCE',
 *   token: 'your-auth-token'
 * });
 */
export const getStocks = async ({ 
  baskets, 
  durations, 
  search, 
  token, 
  baseUrl = API_BASE_URL 
} = {}) => {
  if (!baseUrl) {
    throw new Error('API base URL is required');
  }

  // Build query parameters
  const params = new URLSearchParams();
  
  if (baskets) {
    const basketArray = Array.isArray(baskets) ? baskets : [baskets];
    basketArray.forEach(basket => {
      if (basket) params.append('basket', basket);
    });
  }
  
  if (durations) {
    const durationArray = Array.isArray(durations) ? durations : [durations];
    durationArray.forEach(duration => {
      if (duration) params.append('duration', duration);
    });
  }
  
  if (search && search.trim()) {
    params.append('search', search.trim());
  }

  const url = `${baseUrl}/stocks?${params.toString()}`;
  
  return makeRequest(url, { token });
};

/**
 * Fetches alerts data
 * @param {object} params - Query parameters
 * @param {number} params.limit - Maximum number of alerts to return
 * @param {string} params.token - Optional authorization token
 * @param {string} params.baseUrl - Optional base URL override
 * @returns {Promise<Array>} Array of alert objects
 * @throws {Error} If request fails
 * 
 * @example
 * // Get latest 50 alerts
 * const alerts = await getAlerts({ limit: 50 });
 * 
 * // Get all alerts with authentication
 * const alerts = await getAlerts({ 
 *   limit: 100,
 *   token: 'your-auth-token'
 * });
 */
export const getAlerts = async ({ 
  limit, 
  token, 
  baseUrl = API_BASE_URL 
} = {}) => {
  if (!baseUrl) {
    throw new Error('API base URL is required');
  }

  // Build query parameters
  const params = new URLSearchParams();
  
  if (limit && limit > 0) {
    params.append('limit', limit.toString());
  }

  const url = `${baseUrl}/alerts?${params.toString()}`;
  
  return makeRequest(url, { token });
};

/**
 * Fetches stock fundamentals data
 * @param {object} params - Query parameters
 * @param {string} params.symbol - Stock symbol (e.g., 'RELIANCE', 'TCS')
 * @param {string} params.token - Optional authorization token
 * @param {string} params.baseUrl - Optional base URL override
 * @returns {Promise<object>} Stock fundamentals object
 * @throws {Error} If request fails
 * 
 * @example
 * // Get RELIANCE fundamentals
 * const fundamentals = await getStockFundamentals({ symbol: 'RELIANCE' });
 * 
 * // Get TCS fundamentals with authentication
 * const fundamentals = await getStockFundamentals({ 
 *   symbol: 'TCS',
 *   token: 'your-auth-token'
 * });
 */
export const getStockFundamentals = async ({ 
  symbol, 
  token, 
  baseUrl = API_BASE_URL 
} = {}) => {
  if (!baseUrl) {
    throw new Error('API base URL is required');
  }

  if (!symbol) {
    throw new Error('Stock symbol is required');
  }

  const url = `${baseUrl}/api/stocks/fundamentals/${encodeURIComponent(symbol)}`;
  
  return makeRequest(url, { token });
};

/**
 * Fetches active targets and stoploss for a stock
 * @param {object} params - Query parameters
 * @param {string} params.stockCode - Stock code (e.g., 'RELIANCE', 'TCS')
 * @param {string} params.token - Optional authorization token
 * @param {string} params.baseUrl - Optional base URL override
 * @returns {Promise<Array>} Array of target/stoploss objects
 * @throws {Error} If request fails
 * 
 * @example
 * // Get active targets/stoploss for RELIANCE
 * const targets = await getActiveTargetStoploss({ stockCode: 'RELIANCE' });
 */
export const getActiveTargetStoploss = async ({ 
  stockCode, 
  token, 
  baseUrl = API_BASE_URL 
} = {}) => {
  if (!baseUrl) {
    throw new Error('API base URL is required');
  }

  if (!stockCode) {
    throw new Error('Stock code is required');
  }

  const url = `${baseUrl}/api/targets-stoploss/stock/${encodeURIComponent(stockCode)}`;
  
  return makeRequest(url, { token });
};

/**
 * Fetches active targets and stoploss for a stock and timeframe
 * @param {object} params - Query parameters
 * @param {string} params.stockCode - Stock code (e.g., 'RELIANCE', 'TCS')
 * @param {string} params.timeframe - Timeframe (e.g., 'INTRADAY', 'POSITIONAL')
 * @param {string} params.token - Optional authorization token
 * @param {string} params.baseUrl - Optional base URL override
 * @returns {Promise<object>} Target/stoploss object
 * @throws {Error} If request fails
 * 
 * @example
 * // Get INTRADAY targets/stoploss for RELIANCE
 * const targets = await getActiveTargetStoplossByTimeframe({ 
 *   stockCode: 'RELIANCE', 
 *   timeframe: 'INTRADAY' 
 * });
 */
export const getActiveTargetStoplossByTimeframe = async ({ 
  stockCode, 
  timeframe,
  token, 
  baseUrl = API_BASE_URL 
} = {}) => {
  if (!baseUrl) {
    throw new Error('API base URL is required');
  }

  if (!stockCode) {
    throw new Error('Stock code is required');
  }

  if (!timeframe) {
    throw new Error('Timeframe is required');
  }

  const url = `${baseUrl}/api/targets-stoploss/stock/${encodeURIComponent(stockCode)}/timeframe/${encodeURIComponent(timeframe)}`;
  
  return makeRequest(url, { token });
};

/**
 * Fetches market indices data (Nifty, Bank Nifty, market status)
 * @param {object} params - Query parameters
 * @param {string} params.token - Optional authorization token
 * @param {string} params.baseUrl - Optional base URL override
 * @returns {Promise<object>} Market indices object
 * @throws {Error} If request fails
 * 
 * @example
 * // Get market indices
 * const indices = await getMarketIndices();
 * 
 * // Get market indices with authentication
 * const indices = await getMarketIndices({ 
 *   token: 'your-auth-token'
 * });
 */
export const getMarketIndices = async ({ 
  token, 
  baseUrl = API_BASE_URL 
} = {}) => {
  if (!baseUrl) {
    throw new Error('API base URL is required');
  }

  const url = `${baseUrl}/api/indices`;
  
  return makeRequest(url, { token });
};

/**
 * Utility function to check if API is available
 * @param {string} baseUrl - Base URL to check
 * @returns {Promise<boolean>} True if API is reachable
 */
export const checkApiHealth = async (baseUrl = API_BASE_URL) => {
  if (!baseUrl) {
    return false;
  }

  try {
    const response = await fetch(`${baseUrl}/health`, {
      method: 'GET',
      headers: createHeaders(),
    });
    return response.ok;
  } catch (error) {
    console.error('API health check failed:', error);
    return false;
  }
};

/**
 * Default export with all API functions
 */
export default {
  getStocks,
  getAlerts,
  getStockFundamentals,
  getActiveTargetStoploss,
  getActiveTargetStoplossByTimeframe,
  getMarketIndices,
  checkApiHealth,
};

