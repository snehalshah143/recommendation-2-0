import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './ui/select';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { cn } from '../lib/utils';

const StockDetailModal = ({ isOpen, onClose, stock, alerts = [], apiBaseUrl = '' }) => {
  const [showOrderForm, setShowOrderForm] = useState(false);
  const [orderForm, setOrderForm] = useState({
    price: '',
    quantity: '',
    orderType: 'Market',
    productType: 'CNC',
    validity: 'Day'
  });
  const [selectedTimeframe, setSelectedTimeframe] = useState('INTRADAY');
  const [ltp, setLtp] = useState(0);

  // Reset form state when modal closes
  useEffect(() => {
    if (!isOpen) {
      setShowOrderForm(false);
      setOrderForm({
        price: '',
        quantity: '',
        orderType: 'Market',
        productType: 'CNC',
        validity: 'Day'
      });
    }
  }, [isOpen]);

  // Pre-fill price with LTP when order form opens
  useEffect(() => {
    if (showOrderForm && ltp > 0) {
      setOrderForm(prev => ({
        ...prev,
        price: ltp.toFixed(2)
      }));
    }
  }, [showOrderForm, ltp]);

  // Set LTP from stock price (no dummy variation)
  useEffect(() => {
    if (stock?.price) {
      const basePrice = parseFloat(stock.price);
      setLtp(basePrice);
    }
  }, [stock]);


  // Get targets and stoplosses based on trade duration and alert price
  const getTargetsAndStoplosses = (timeframe) => {
    const alertPrice = ltp || stock?.price || 0;
    const isSellAlert = stock?.action === 'SELL';
    
    if (alertPrice === 0) {
      return {
        target1: '0.00',
        target2: '0.00', 
        target3: '0.00',
        stoploss1: '0.00',
        stoploss2: '0.00',
        hardStoploss: '0.00'
      };
    }

    let targets, stoplosses;
    
    // For SELL alerts, use inverted logic (targets go down, stoplosses go up)
    if (isSellAlert) {
      switch (timeframe) {
        case 'INTRADAY':
          targets = {
            t1: alertPrice * 0.985,  // -1.5%
            t2: alertPrice * 0.975,  // -2.5%
            t3: alertPrice * 0.96    // -4%
          };
          stoplosses = {
            sl1: alertPrice * 1.01,  // +1%
            sl2: alertPrice * 1.02,  // +2%
            hardSL: alertPrice * 1.03 // +3%
          };
          break;
          
        case 'SHORTTERM':
          targets = {
            t1: alertPrice * 0.98,   // -2%
            t2: alertPrice * 0.95,   // -5%
            t3: alertPrice * 0.92    // -8%
          };
          stoplosses = {
            sl1: alertPrice * 1.02,  // +2%
            sl2: alertPrice * 1.04,  // +4%
            hardSL: alertPrice * 1.05 // +5%
          };
          break;
          
        case 'POSITIONAL':
          targets = {
            t1: alertPrice * 0.95,   // -5%
            t2: alertPrice * 0.92,   // -8%
            t3: alertPrice * 0.88    // -12%
          };
          stoplosses = {
            sl1: alertPrice * 1.025, // +2.5%
            sl2: alertPrice * 1.04,  // +4%
            hardSL: alertPrice * 1.07 // +7%
          };
          break;
          
        case 'LONGTERM':
          targets = {
            t1: alertPrice * 0.90,   // -10%
            t2: alertPrice * 0.80,   // -20%
            t3: alertPrice * 0.70    // -30%
          };
          stoplosses = {
            sl1: alertPrice * 1.05,  // +5%
            sl2: alertPrice * 1.08,  // +8%
            hardSL: alertPrice * 1.10 // +10%
          };
          break;
          
        default:
          // Default to INTRADAY for SELL alerts
          targets = {
            t1: alertPrice * 0.985,  // -1.5%
            t2: alertPrice * 0.975,  // -2.5%
            t3: alertPrice * 0.96    // -4%
          };
          stoplosses = {
            sl1: alertPrice * 1.01,  // +1%
            sl2: alertPrice * 1.02,  // +2%
            hardSL: alertPrice * 1.03 // +3%
          };
      }
    } else {
      // For BUY alerts, use original logic (targets go up, stoplosses go down)
      switch (timeframe) {
        case 'INTRADAY':
          targets = {
            t1: alertPrice * 1.015,  // +1.5%
            t2: alertPrice * 1.025,  // +2.5%
            t3: alertPrice * 1.04    // +4%
          };
          stoplosses = {
            sl1: alertPrice * 0.99,  // -1%
            sl2: alertPrice * 0.98,  // -2%
            hardSL: alertPrice * 0.97 // -3%
          };
          break;
          
        case 'SHORTTERM':
          targets = {
            t1: alertPrice * 1.02,   // +2%
            t2: alertPrice * 1.05,   // +5%
            t3: alertPrice * 1.08    // +8%
          };
          stoplosses = {
            sl1: alertPrice * 0.98,  // -2%
            sl2: alertPrice * 0.96,  // -4%
            hardSL: alertPrice * 0.95 // -5%
          };
          break;
          
        case 'POSITIONAL':
          targets = {
            t1: alertPrice * 1.05,   // +5%
            t2: alertPrice * 1.08,   // +8%
            t3: alertPrice * 1.12    // +12%
          };
          stoplosses = {
            sl1: alertPrice * 0.975, // -2.5%
            sl2: alertPrice * 0.96,  // -4%
            hardSL: alertPrice * 0.93 // -7%
          };
          break;
          
        case 'LONGTERM':
          targets = {
            t1: alertPrice * 1.10,   // +10%
            t2: alertPrice * 1.20,   // +20%
            t3: alertPrice * 1.30    // +30%
          };
          stoplosses = {
            sl1: alertPrice * 0.95,  // -5%
            sl2: alertPrice * 0.92,  // -8%
            hardSL: alertPrice * 0.90 // -10%
          };
          break;
          
        default:
          // Default to INTRADAY for BUY alerts
          targets = {
            t1: alertPrice * 1.015,
            t2: alertPrice * 1.025,
            t3: alertPrice * 1.04
          };
          stoplosses = {
            sl1: alertPrice * 0.99,
            sl2: alertPrice * 0.98,
            hardSL: alertPrice * 0.97
          };
      }
    }
    
    return {
      target1: targets.t1.toFixed(2),
      target2: targets.t2.toFixed(2),
      target3: targets.t3.toFixed(2),
      stoploss1: stoplosses.sl1.toFixed(2),
      stoploss2: stoplosses.sl2.toFixed(2),
      hardStoploss: stoplosses.hardSL.toFixed(2)
    };
  };

  // No dummy data - show NA when no real data available
  const priceActionAlerts = [];

  // Filter alerts for this stock (latest first)
  const stockAlerts = alerts
    .filter(alert => alert.symbol === stock?.symbol)
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

  // Calculate alert duration based on current consecutive streak
  const getAlertDuration = () => {
    if (!stock) return "No stock data";
    
    // Use sinceDays from backend (already calculated)
    const sinceDays = stock.sinceDays || stock.since_days || 0;
    
    if (sinceDays === 0) return "since 0 days";
    if (sinceDays === 1) return "since 1 day";
    return `since ${sinceDays} days`;
  };

  const handleOrderSubmit = (e) => {
    e.preventDefault();
    // Here you would integrate with actual trading API
    console.log('Order placed:', { stock: stock.symbol, ...orderForm });
    alert(`Order placed for ${stock.symbol}: ${orderForm.quantity} shares at ${orderForm.price}`);
    setShowOrderForm(false);
  };

  const targets = getTargetsAndStoplosses(selectedTimeframe);

  if (!stock) return null;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="w-[95vw] max-w-6xl max-h-[95vh] sm:max-h-[90vh] overflow-hidden flex flex-col">

        {/* Mobile Compact Header */}
        <div className="sm:hidden p-2 bg-gray-50 rounded-lg mb-2 flex-shrink-0">
          {/* Stock Info Row */}
          <div className="flex items-center justify-between mb-2">
            <div>
              <h3 className="text-lg font-bold">{stock.symbol}</h3>
              <p className="text-xs text-gray-600">LTP: ₹{ltp.toFixed(2)} • {getAlertDuration()}</p>
            </div>
            {stock.action === 'BUY' ? (
              <Button
                disabled
                className="bg-green-600 text-white px-4 py-2 text-sm font-bold cursor-not-allowed"
              >
                Buy
              </Button>
            ) : (
              <Button
                disabled
                className="bg-red-600 text-white px-4 py-2 text-sm font-bold cursor-not-allowed"
              >
                Sell
              </Button>
            )}
          </div>
          
          {/* Timeframe Filter */}
          <div className="flex items-center gap-2 mb-2">
            <span className="text-xs font-medium">Duration:</span>
            <Select value={selectedTimeframe} onValueChange={setSelectedTimeframe}>
              <SelectTrigger className="w-24 h-7 text-xs px-2 py-1 bg-white border border-gray-300 rounded">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="w-32 bg-white border border-gray-300 rounded shadow-lg">
                <SelectItem value="INTRADAY" className="px-2 py-1 text-xs">Intraday</SelectItem>
                <SelectItem value="SHORTTERM" className="px-2 py-1 text-xs">Shortterm</SelectItem>
                <SelectItem value="POSITIONAL" className="px-2 py-1 text-xs">Positional</SelectItem>
                <SelectItem value="LONGTERM" className="px-2 py-1 text-xs">Longterm</SelectItem>
              </SelectContent>
            </Select>
          </div>
          
          {/* Targets and Stoplosses - Compact Grid */}
          <div className="grid grid-cols-6 gap-1 text-xs">
            <div className="text-center">
              <div className="text-green-600 font-medium">T1</div>
              <div className="font-mono">₹{targets.target1}</div>
            </div>
            <div className="text-center">
              <div className="text-green-600 font-medium">T2</div>
              <div className="font-mono">₹{targets.target2}</div>
            </div>
            <div className="text-center">
              <div className="text-green-600 font-medium">T3</div>
              <div className="font-mono">₹{targets.target3}</div>
            </div>
            <div className="text-center">
              <div className="text-red-600 font-medium">SL1</div>
              <div className="font-mono">₹{targets.stoploss1}</div>
            </div>
            <div className="text-center">
              <div className="text-red-600 font-medium">SL2</div>
              <div className="font-mono">₹{targets.stoploss2}</div>
            </div>
            <div className="text-center">
              <div className="text-red-600 font-medium">Hard</div>
              <div className="font-mono">₹{targets.hardStoploss}</div>
            </div>
          </div>
        </div>

        {/* Desktop Header - Original Design */}
        <div className="hidden sm:block p-4 bg-gray-50 rounded-lg mb-4 flex-shrink-0">
          <div className="flex items-center justify-between gap-4">
            {/* Stock Info and Order Button */}
            <div className="flex items-center gap-6">
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="text-sm text-gray-600">LTP: ₹{ltp.toFixed(2)}</p>
                  <h3 className="text-xl font-bold">{stock.symbol}</h3>
                  <p className="text-xs text-gray-500 mt-1">
                    {getAlertDuration()}
                  </p>
                </div>
                {stock.action === 'BUY' ? (
                  <div className="flex flex-col items-center">
                    <Button
                      disabled
                      onClick={() => {}} // Prevent form opening
                      className="bg-green-600 text-white px-8 py-3 text-lg font-bold cursor-not-allowed"
                    >
                      Buy
                    </Button>
                    <span className="text-xs text-gray-500 mt-1">Coming Soon</span>
                  </div>
                ) : (
                  <div className="flex flex-col items-center">
                    <Button
                      disabled
                      onClick={() => {}} // Prevent form opening
                      className="bg-red-600 text-white px-8 py-3 text-lg font-bold cursor-not-allowed"
                    >
                      Sell
                    </Button>
                    <span className="text-xs text-gray-500 mt-1">Coming Soon</span>
                  </div>
                )}
              </div>

              {/* Targets & Stoplosses with Timeframe Filter */}
              <div className="flex flex-col gap-2">
                {/* Timeframe Filter */}
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">Trade Duration:</span>
                  <Select value={selectedTimeframe} onValueChange={setSelectedTimeframe}>
                    <SelectTrigger className="w-32 h-10 text-sm px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-green-500">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="w-48 bg-white border border-gray-300 rounded-lg shadow-lg">
                      <SelectItem value="INTRADAY" className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors text-gray-700">Intraday</SelectItem>
                      <SelectItem value="SHORTTERM" className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors text-gray-700">Shortterm</SelectItem>
                      <SelectItem value="POSITIONAL" className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors text-gray-700">Positional</SelectItem>
                      <SelectItem value="LONGTERM" className="w-full flex items-center gap-3 px-4 py-2 text-sm hover:bg-gray-50 transition-colors text-gray-700">Longterm</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                {/* Targets and Stoplosses Grid */}
                <div className="grid grid-cols-3 gap-4 text-xs">
                  {/* Column 1: T1 and SL1 */}
                  <div className="space-y-1">
                    <div className="flex items-center gap-1">
                      <span className="text-green-600 font-medium">T1:</span>
                      <span className="font-mono">₹{targets.target1}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <span className="text-red-600 font-medium">SL1:</span>
                      <span className="font-mono">₹{targets.stoploss1}</span>
                    </div>
                  </div>
                  
                  {/* Column 2: T2 and SL2 */}
                  <div className="space-y-1">
                    <div className="flex items-center gap-1">
                      <span className="text-green-600 font-medium">T2:</span>
                      <span className="font-mono">₹{targets.target2}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <span className="text-red-600 font-medium">SL2:</span>
                      <span className="font-mono">₹{targets.stoploss2}</span>
                    </div>
                  </div>
                  
                  {/* Column 3: T3 and Hard SL */}
                  <div className="space-y-1">
                    <div className="flex items-center gap-1">
                      <span className="text-green-600 font-medium">T3:</span>
                      <span className="font-mono">₹{targets.target3}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <span className="text-red-600 font-medium">Hard SL:</span>
                      <span className="font-mono">₹{targets.hardStoploss}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Trend Analysis */}
            <div className="flex gap-4">
              {[
                { name: 'Intraday', value: 'NA' },
                { name: 'Short Term', value: 'NA' },
                { name: 'Positional', value: 'NA' },
                { name: 'Long Term', value: 'NA' }
              ].map((trend) => (
                <div key={trend.name} className="text-center p-2 bg-white rounded border">
                  <div className="text-xs text-gray-600 mb-1">{trend.name}</div>
                  <div className="text-sm font-semibold text-gray-500">
                    {trend.value}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Order Form Modal */}
        {showOrderForm && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
            onClick={(e) => {
              if (e.target === e.currentTarget) {
                setShowOrderForm(false);
              }
            }}
          >
            <Card className="w-full max-w-md mx-auto">
              <CardHeader className="pb-4">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-lg">
                      Place {stock.action} Order - {stock.symbol}
                    </CardTitle>
                    <p className="text-sm text-gray-600">
                      LTP: ₹{ltp.toFixed(2)}
                    </p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowOrderForm(false)}
                    className="h-8 w-8 p-0"
                  >
                    <span className="sr-only">Close</span>
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleOrderSubmit} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-1">Price</label>
                      <Input
                        type="number"
                        step="0.01"
                        value={orderForm.price}
                        onChange={(e) => setOrderForm({...orderForm, price: e.target.value})}
                        placeholder="Enter price"
                        required
                        className="w-full"
                      />
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium mb-1">Quantity</label>
                      <Input
                        type="number"
                        value={orderForm.quantity}
                        onChange={(e) => setOrderForm({...orderForm, quantity: e.target.value})}
                        placeholder="Enter quantity"
                        required
                        className="w-full"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-1">Order Type</label>
                      <Select
                        value={orderForm.orderType}
                        onValueChange={(value) => setOrderForm({...orderForm, orderType: value})}
                      >
                        <SelectTrigger className="w-full">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="Market">Market</SelectItem>
                          <SelectItem value="Limit">Limit</SelectItem>
                          <SelectItem value="SL">SL</SelectItem>
                          <SelectItem value="SL-M">SL-M</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium mb-1">Product Type</label>
                      <Select
                        value={orderForm.productType}
                        onValueChange={(value) => setOrderForm({...orderForm, productType: value})}
                      >
                        <SelectTrigger className="w-full">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="CNC">CNC</SelectItem>
                          <SelectItem value="MIS">MIS</SelectItem>
                          <SelectItem value="NRML">NRML</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-1">Validity</label>
                    <Select
                      value={orderForm.validity}
                      onValueChange={(value) => setOrderForm({...orderForm, validity: value})}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="Day">Day</SelectItem>
                        <SelectItem value="IOC">IOC</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="flex gap-3 pt-4">
                    <Button 
                      type="submit" 
                      className={cn(
                        "flex-1",
                        stock.action === 'BUY' 
                          ? "bg-green-600 hover:bg-green-700" 
                          : "bg-red-600 hover:bg-red-700"
                      )}
                    >
                      Place {stock.action} Order
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => setShowOrderForm(false)}
                      className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 border-gray-300"
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Middle and Bottom Frame - Combined Layout */}
        <div className="flex-1 overflow-y-auto min-h-0">
          {/* Mobile Layout - Stacked Cards */}
          <div className="sm:hidden space-y-2">
            {/* Alert History */}
            <Card>
              <CardHeader className="pb-1">
                <CardTitle className="text-sm">Alert History</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="max-h-24 overflow-y-auto space-y-1">
                  {stockAlerts.length === 0 ? (
                    <p className="text-gray-500 text-xs">No alerts found</p>
                  ) : (
                    stockAlerts.slice(0, 3).map((alert, index) => (
                      <div
                        key={index}
                        className={cn(
                          "p-1.5 rounded text-xs border-l-2",
                          alert.action === 'BUY' 
                            ? "border-green-500 bg-green-50" 
                            : "border-red-500 bg-red-50"
                        )}
                      >
                        <div className="font-medium">
                          {alert.action} @ ₹{alert.price?.toFixed(2) || '0.00'}
                        </div>
                        <div className="text-gray-600 text-xs">
                          {new Date(alert.timestamp).toLocaleString('en-IN', {
                            day: '2-digit',
                            month: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Price Action */}
            <Card>
              <CardHeader className="pb-1">
                <CardTitle className="text-sm">Price Action</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="max-h-16 overflow-y-auto space-y-1">
                  {priceActionAlerts.length > 0 ? (
                    priceActionAlerts.slice(0, 3).map((alert, index) => (
                      <div
                        key={index}
                        className="p-1 bg-blue-50 border-l-2 border-blue-500 rounded text-xs"
                      >
                        {alert}
                      </div>
                    ))
                  ) : (
                    <div className="p-1 text-gray-500 text-xs text-center">
                      NA
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Fundamentals and Technical - Side by Side */}
            <div className="grid grid-cols-2 gap-2">
              <Card>
                <CardHeader className="pb-1">
                  <CardTitle className="text-sm">Fundamentals</CardTitle>
                </CardHeader>
                <CardContent className="space-y-1">
                  {[
                    { label: 'P/E', value: 'NA' },
                    { label: 'ROE', value: 'NA' },
                    { label: 'Market Cap', value: 'NA' }
                  ].map((item) => (
                    <div key={item.label} className="flex justify-between items-center py-0.5 text-xs">
                      <span className="text-gray-600">{item.label}</span>
                      <span className="font-semibold text-gray-500">{item.value}</span>
                    </div>
                  ))}
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-1">
                  <CardTitle className="text-sm">Technical</CardTitle>
                </CardHeader>
                <CardContent className="space-y-1">
                  {[
                    { label: 'RSI', value: 'NA' },
                    { label: 'MACD', value: 'NA' },
                    { label: 'EMA 21', value: 'NA' }
                  ].map((item) => (
                    <div key={item.label} className="flex justify-between items-center py-0.5 text-xs">
                      <span className="text-gray-600">{item.label}</span>
                      <span className="font-semibold text-gray-500">{item.value}</span>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </div>
          </div>

          {/* Desktop Layout - Original Grid */}
          <div className="hidden sm:grid grid-cols-2 lg:grid-cols-5 gap-4">
            {/* Left Column - Alert History */}
            <Card className="lg:col-span-2">
              <CardHeader className="pb-2">
                <CardTitle className="text-base">Alert History</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="max-h-48 overflow-y-auto space-y-2">
                  {stockAlerts.length === 0 ? (
                    <p className="text-gray-500 text-xs">No alerts found</p>
                  ) : (
                    stockAlerts.map((alert, index) => (
                      <div
                        key={index}
                        className={cn(
                          "p-2 rounded text-xs border-l-2",
                          alert.action === 'BUY' 
                            ? "border-green-500 bg-green-50" 
                            : "border-red-500 bg-red-50"
                        )}
                      >
                        <div className="font-medium">
                          {alert.action} @ ₹{alert.price?.toFixed(2) || '0.00'}
                        </div>
                        <div className="text-gray-600 text-xs">
                          {alert.source} – {new Date(alert.timestamp).toLocaleString('en-IN', {
                            day: '2-digit',
                            month: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Middle Column - Price Action Alerts */}
            <Card className="lg:col-span-1">
              <CardHeader className="pb-2">
                <CardTitle className="text-base">Price Action</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="max-h-48 overflow-y-auto space-y-1">
                  {priceActionAlerts.length > 0 ? (
                    priceActionAlerts.slice(0, 6).map((alert, index) => (
                      <div
                        key={index}
                        className="p-1.5 bg-blue-50 border-l-2 border-blue-500 rounded text-xs"
                      >
                        {alert}
                      </div>
                    ))
                  ) : (
                    <div className="p-1.5 text-gray-500 text-xs text-center">
                      NA
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Right Column - Fundamental Data */}
            <Card className="lg:col-span-1">
              <CardHeader className="pb-2">
                <CardTitle className="text-base">Fundamentals</CardTitle>
              </CardHeader>
              <CardContent className="space-y-1">
                {[
                  { label: 'P/E Ratio', value: 'NA' },
                  { label: 'ROE', value: 'NA' },
                  { label: 'ROC', value: 'NA' },
                  { label: 'Book Value', value: 'NA' },
                  { label: 'Market Cap', value: 'NA' },
                  { label: 'Sales (Qtr)', value: 'NA' }
                ].map((item) => (
                  <div key={item.label} className="flex justify-between items-center py-1 text-xs">
                    <span className="text-gray-600">{item.label}</span>
                    <span className="font-semibold text-gray-500">{item.value}</span>
                  </div>
                ))}
              </CardContent>
            </Card>

            {/* Far Right Column - Technical Indicators */}
            <Card className="lg:col-span-1">
              <CardHeader className="pb-2">
                <CardTitle className="text-base">Technical</CardTitle>
              </CardHeader>
              <CardContent className="space-y-1">
                {[
                  { label: 'RSI', value: 'NA' },
                  { label: 'MACD', value: 'NA' },
                  { label: 'EMA 21', value: 'NA' },
                  { label: 'Supertrend', value: 'NA' },
                  { label: 'Support', value: 'NA' },
                  { label: 'Resistance', value: 'NA' }
                ].map((item) => (
                  <div key={item.label} className="flex justify-between items-center py-1 text-xs">
                    <span className="text-gray-600">{item.label}</span>
                    <span className="font-semibold text-gray-500">
                      {item.value}
                    </span>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

        </div>
      </DialogContent>
    </Dialog>
  );
};

export default StockDetailModal;
